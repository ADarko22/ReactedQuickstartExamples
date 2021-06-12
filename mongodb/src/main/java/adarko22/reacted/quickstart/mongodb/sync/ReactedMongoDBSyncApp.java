package adarko22.reacted.quickstart.mongodb.sync;

import adarko22.reacted.quickstart.mongodb.sync.service.MongoDBClientCLI;
import adarko22.reacted.quickstart.mongodb.sync.service.MongoDBSyncService;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.reacted.core.config.reactors.ServiceConfig;
import io.reacted.core.config.reactorsystem.ReActorSystemConfig;
import io.reacted.core.reactorsystem.ReActorSystem;

public class ReactedMongoDBSyncApp {

  public static final String DB_SERVICE_NAME = "MongoDBSyncService";

  private static final String SYSTEM_NAME = "MongoDBSyncSystem";
  private static final String MONGO_DB_URI = "mongodb://127.0.0.1:27017";


  public static void main(String[] args) {

    ReActorSystem backendSystem = new ReActorSystem(ReActorSystemConfig.newBuilder()
                                                        .setLocalDriver(ReActorSystemConfig.DEFAULT_LOCAL_DRIVER)
                                                        .setReactorSystemName(SYSTEM_NAME)
                                                        .setRecordExecution(true)
                                                        .build())
                                      .initReActorSystem();

    MongoClient mongoClient = MongoClients.create(MONGO_DB_URI);

    backendSystem.spawnService(ServiceConfig.newBuilder()
                                   .setReActorName(DB_SERVICE_NAME)
                                   .setRouteeProvider(() -> new MongoDBSyncService(mongoClient))
                                   .build());

    backendSystem.spawn(new MongoDBClientCLI());
  }
}
