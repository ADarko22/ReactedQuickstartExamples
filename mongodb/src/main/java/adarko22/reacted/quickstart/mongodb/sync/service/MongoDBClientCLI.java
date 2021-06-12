package adarko22.reacted.quickstart.mongodb.sync.service;

import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBClientCLIMessages.DiscoveryError;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBClientCLIMessages.WaitForCLIInput;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.QueryReply;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.QueryRequest;
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
import io.reacted.patterns.Try;
import java.util.Objects;
import java.util.Scanner;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBClientCLI implements ReActor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBClientCLI.class);
  private final Scanner in = new Scanner(System.in);

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
               .reAct(StoreReply.class, this::onStoreReply)
               .build();
  }

  private void onCLIInit(ReActorContext reActorContext, ReActorInit reActorInit) {
    LOGGER.info("Initializing: running service discovery");
    reActorContext.getReActorSystem()
        .serviceDiscovery(BasicServiceDiscoverySearchFilter.newBuilder()
                              .setServiceName(ReactedMongoDBSyncApp.DB_SERVICE_NAME)
                              .build())
        .thenAccept(serviceDiscoveryReplyTry -> onDBServiceDiscoveryReply(reActorContext, serviceDiscoveryReplyTry));
  }

  private void onDBServiceDiscoveryReply(ReActorContext reActorContext, Try<ServiceDiscoveryReply> serviceDiscoveryReplyTry) {
    LOGGER.info("Initializing: service discovery completed. Selecting a service and staerting CLI ...");
    reActorContext.getMbox().request(1);
    serviceDiscoveryReplyTry.filter(services -> !services.getServiceGates().isEmpty())
        .map(services -> services.getServiceGates().iterator().next())
        .mapOrElse(gate -> reActorContext.getSelf().tell(gate, new WaitForCLIInput()),
                   error -> reActorContext.selfTell(new DiscoveryError(error)));
  }

  private void onDiscoveryError(ReActorContext reActorContext, DiscoveryError discoveryError) {
    LOGGER.error("Error during discovery: " + discoveryError.getError());
  }

  private void onWaitForCLIInput(ReActorContext reActorContext, WaitForCLIInput waitForCLIInput) {

    while (true) {
      System.out.println("Type operation: (\"search <topic>\" or \"store --id=<id> --topic=<topic> --data=<data>\")");
      String operation = in.nextLine();

      try {
        String[] tokens = operation.split("\\s");
        String operationType = tokens[0];

        if (Objects.equals(operationType, "search")) {
          String topic = split(operation, "topic=");

          reActorContext.getSender().tell(reActorContext.getSelf(), new QueryRequest(Filters.eq(ExampleDBDocument.TOPIC_FIELD, topic)));
        } else if (Objects.equals(operationType, "store")) {
          String id = split(operation, "--id=");
          String topic = split(operation, "--topic=");
          String data = split(operation, "--data=");

          reActorContext.getSender().tell(reActorContext.getSelf(), new StoreRequest(new ExampleDBDocument.Pojo(id, topic, data)));
        } else {
          throw new UnsupportedOperationException();
        }
      } catch (Exception e) {
        LOGGER.warn("Error happened processing '{}'", operation, e);
        continue;
      }

      break;
    }
  }

  private void onQueryReply(ReActorContext reActorContext, QueryReply queryReply) {
    System.out.println("Query result: " + queryReply.getExampleDBDocumentPojos());
  }

  private void onStoreReply(ReActorContext reActorContext, StoreReply storeReply) {
  }

  private static String split(String text, String assignmentKey) {
    String[] tokens = text.split(assignmentKey);
    tokens = tokens[1].split("[(--)\n]");
    return tokens[0];
  }

}
