package adarko22.reacted.quickstart.mongodb.sync.service;

import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.QueryError;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.QueryReply;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.QueryRequest;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.StoreReply;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.StoreRequest;
import adarko22.reacted.quickstart.mongodb.common.model.ExampleDBDocument;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import io.reacted.core.config.reactors.ReActorConfig;
import io.reacted.core.mailboxes.BasicMbox;
import io.reacted.core.messages.reactors.ReActorInit;
import io.reacted.core.reactors.ReActions;
import io.reacted.core.reactors.ReActor;
import io.reacted.core.reactorsystem.ReActorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class MongoDBSyncService implements ReActor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBSyncService.class);
  private static final String DB_NAME = "example_db";
  private static final String COLLECTION = "example_data";

  private final MongoClient mongoClient;

  private MongoCollection<Document> mongoCollection;

  @NotNull
  @Override
  public ReActorConfig getConfig() {
    return ReActorConfig.newBuilder()
               .setReActorName(MongoDBSyncService.class.getSimpleName())
               .setMailBoxProvider(reActorContext -> new BasicMbox())
               .build();
  }

  @NotNull
  @Override
  public ReActions getReActions() {
    return ReActions.newBuilder()
               .reAct(ReActorInit.class, this::onMongoInit)
               .reAct(QueryRequest.class, this::onQueryRequest)
               .reAct(StoreRequest.class, this::onStoreRequest)
               .build();
  }

  private void onMongoInit(ReActorContext raCtx, ReActorInit init) {
    LOGGER.info("Initializing: finding mongo collection");
    this.mongoCollection = Objects.requireNonNull(mongoClient)
                               .getDatabase(DB_NAME)
                               .getCollection(COLLECTION, Document.class);
  }

  private void onStoreRequest(ReActorContext raCtx, StoreRequest request) {
    InsertOneResult result = mongoCollection.insertOne(ExampleDBDocument.fromPojo(request.getExampleDBDocumentPojo()));

    LOGGER.info("On StoreRequest of {}: {}", request.getExampleDBDocumentPojo(), result);
    raCtx.getSender().tell(new StoreReply());
  }

  private void onQueryRequest(ReActorContext raCtx, QueryRequest request) {
    List<ExampleDBDocument.Pojo> results = new ArrayList<>();

    try {
      mongoCollection.find(request.getFilter())
          .map(ExampleDBDocument::toPojo)
          .forEach(results::add);

      if (!results.isEmpty()) {
        LOGGER.info("On QueryRequest of {}: {}", request.getFilter(), results);
        raCtx.getSender().tell(new QueryReply(results));
      } else {
        LOGGER.info("On QueryRequest of {}: null result", request.getFilter());
        raCtx.getSender().tell(new QueryError());
      }
    } catch (Exception e) {
      LOGGER.info("Error during on QueryRequest of {}", request.getFilter(), e);
      raCtx.getSender().tell(new QueryError());
    }
  }
}
