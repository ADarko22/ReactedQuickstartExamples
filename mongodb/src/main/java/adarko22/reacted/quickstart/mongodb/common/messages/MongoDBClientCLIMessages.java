package adarko22.reacted.quickstart.mongodb.common.messages;

import java.io.Serializable;
import lombok.Value;

public class MongoDBClientCLIMessages {

  @Value
  public static class WaitForCLIInput implements Serializable {
  }

  @Value
  public static class DiscoveryError implements Serializable {
    Throwable error;
  }
}
