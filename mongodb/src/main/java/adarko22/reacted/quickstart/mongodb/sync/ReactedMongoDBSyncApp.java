package adarko22.reacted.quickstart.mongodb.sync;

import static io.reacted.core.config.reactorsystem.ReActorSystemConfig.DEFAULT_LOCAL_DRIVER;

import adarko22.reacted.quickstart.mongodb.sync.service.MongoDBClientCLIService;
import adarko22.reacted.quickstart.mongodb.sync.service.MongoDBSyncService;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.reacted.core.config.reactors.ServiceConfig;
import io.reacted.core.config.reactorsystem.ReActorSystemConfig;
import io.reacted.core.reactorsystem.ReActorSystem;
import io.reacted.core.services.Service;

public class ReactedMongoDBSyncApp {

  private static final String MONGO_DB_URI = "mongodb://127.0.0.1:27017";

  public static void main(String[] args) {

    ReActorSystem backendSystem = new ReActorSystem(ReActorSystemConfig.newBuilder()
                                                        .setLocalDriver(DEFAULT_LOCAL_DRIVER)
                                                        .setReactorSystemName("MongoDBSyncSystem")
                                                        .setRecordExecution(true)
                                                        .build())
                                      .initReActorSystem();

    MongoClient mongoClient = MongoClients.create(MONGO_DB_URI);

    backendSystem.spawnService(ServiceConfig.newBuilder()
                                   .setRouteesNum(1)
                                   .setReActorName(MongoDBSyncService.class.getSimpleName())
                                   .setLoadBalancingPolicy(Service.LoadBalancingPolicy.LOWEST_LOAD)
                                   .setRouteeProvider(() -> new MongoDBSyncService(mongoClient))
                                   .build());

    backendSystem.spawnService(ServiceConfig.newBuilder()
                                   .setReActorName(MongoDBClientCLIService.class.getSimpleName())
                                   .setRouteeProvider(MongoDBClientCLIService::new)
                                   .build());
  }
}
