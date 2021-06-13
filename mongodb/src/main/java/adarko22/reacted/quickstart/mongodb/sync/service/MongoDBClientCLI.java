package adarko22.reacted.quickstart.mongodb.sync.service;

import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBClientCLIMessages.DiscoveryError;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBClientCLIMessages.WaitForCLIInput;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.QueryError;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.QueryReply;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.QueryRequest;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.StoreError;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.StoreReply;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.StoreRequest;
import adarko22.reacted.quickstart.mongodb.common.model.ExampleDBDocument;
import adarko22.reacted.quickstart.mongodb.sync.ReactedMongoDBSyncApp;
import com.mongodb.client.model.Filters;
import io.reacted.core.config.reactors.ReActorConfig;
import io.reacted.core.mailboxes.BasicMbox;
import io.reacted.core.messages.reactors.ReActorInit;
import io.reacted.core.messages.services.BasicServiceDiscoverySearchFilter;
import io.reacted.core.messages.services.ServiceDiscoveryReply;
import io.reacted.core.reactors.ReActions;
import io.reacted.core.reactors.ReActor;
import io.reacted.core.reactorsystem.ReActorContext;
import io.reacted.core.reactorsystem.ReActorRef;
import io.reacted.patterns.Try;
import java.util.Objects;
import java.util.Scanner;
import org.jetbrains.annotations.NotNull;

public class MongoDBClientCLI implements ReActor {

  private final Scanner in = new Scanner(System.in);

  private ReActorRef mongoDBSyncServiceRef;

  @NotNull
  @Override
  public ReActorConfig getConfig() {
    return ReActorConfig.newBuilder()
               .setReActorName(MongoDBClientCLI.class.getSimpleName())
               .setMailBoxProvider(reActorContext -> new BasicMbox())
               .build();
  }

  @NotNull
  @Override
  public ReActions getReActions() {
    return ReActions.newBuilder()
               .reAct(ReActorInit.class, this::onCLIInit)
               .reAct(WaitForCLIInput.class, this::onWaitForCLIInput)
               .reAct(DiscoveryError.class, this::onDiscoveryError)
               .reAct(QueryReply.class, this::onQueryReply)
               .reAct(QueryError.class, this::onQueryError)
               .reAct(StoreReply.class, this::onStoreReply)
               .reAct(StoreError.class, this::onStoreError)
               .build();
  }

  private void onCLIInit(ReActorContext reActorContext, ReActorInit reActorInit) {
    reActorContext.logInfo("Initializing: running service discovery");
    reActorContext.getReActorSystem()
        .serviceDiscovery(BasicServiceDiscoverySearchFilter.newBuilder()
                              .setServiceName(ReactedMongoDBSyncApp.DB_SERVICE_NAME)
                              .build())
        .thenAccept(serviceDiscoveryReplyTry -> onDBServiceDiscoveryReply(reActorContext, serviceDiscoveryReplyTry));
  }

  private void onDBServiceDiscoveryReply(ReActorContext reActorContext, Try<ServiceDiscoveryReply> serviceDiscoveryReplyTry) {
    reActorContext.logInfo("Initializing: service discovery completed. Selecting a service and starting CLI ...");
    reActorContext.getMbox().request(1);
    serviceDiscoveryReplyTry.filter(services -> !services.getServiceGates().isEmpty())
        .map(services -> services.getServiceGates().iterator().next())
        .mapOrElse(gate -> {
                     mongoDBSyncServiceRef = gate;
                     return reActorContext.selfTell(new WaitForCLIInput());
                   },
                   error -> reActorContext.selfTell(new DiscoveryError(error)));
  }

  private void onDiscoveryError(ReActorContext reActorContext, DiscoveryError discoveryError) {
    reActorContext.logError("Error during discovery: {}", discoveryError.getError());
  }

  private void onWaitForCLIInput(ReActorContext reActorContext, WaitForCLIInput waitForCLIInput) {

    while (true) {
      System.out.println("Type operation: (\"search --topic=<topic>\" or \"store --id=<id> --topic=<topic> --data=<data>\")");
      String operation = in.nextLine();

      try {
        String[] tokens = operation.split("\\s");
        String operationType = tokens[0];

        if (Objects.equals(operationType, "search")) {
          String topic = split(operation, "--topic=");

          mongoDBSyncServiceRef.tell(reActorContext.getSelf(), new QueryRequest(Filters.eq(ExampleDBDocument.TOPIC_FIELD, topic)));
        } else if (Objects.equals(operationType, "store")) {
          String id = split(operation, "--id=");
          String topic = split(operation, "--topic=");
          String data = split(operation, "--data=");

          mongoDBSyncServiceRef.tell(reActorContext.getSelf(), new StoreRequest(new ExampleDBDocument.Pojo(id, topic, data)));
        } else {
          throw new UnsupportedOperationException();
        }
      } catch (Exception e) {
        reActorContext.logError("Error happened processing '{}'", operation, e);
        continue;
      }

      break;
    }
  }

  private void onQueryReply(ReActorContext reActorContext, QueryReply queryReply) {
    reActorContext.logInfo("Completed query: {}", queryReply);
    System.out.println("Query result: " + queryReply.getExampleDBDocumentPojos());
    reActorContext.selfTell(new WaitForCLIInput());
  }

  private void onQueryError(ReActorContext reActorContext, QueryError error) {
    reActorContext.logError("Error during query");
    reActorContext.selfTell(new WaitForCLIInput());
  }

  private void onStoreReply(ReActorContext reActorContext, StoreReply storeReply) {
    reActorContext.logInfo("Completed store");
    reActorContext.selfTell(new WaitForCLIInput());
  }

  private void onStoreError(ReActorContext reActorContext, StoreError storeError) {
    reActorContext.logError("Error during store");
    reActorContext.selfTell(new WaitForCLIInput());
  }

  private static String split(String text, String assignmentKey) {
    String[] tokens = text.split(assignmentKey);
    tokens = tokens[1].split("[(--)\n]");
    return tokens[0].trim();
  }
}
