package adarko22.reacted.quickstart.mongodb.common.messages;

import adarko22.reacted.quickstart.mongodb.common.model.ExampleDBDocument;
import java.io.Serializable;
import java.util.Collection;
import lombok.Value;
import org.bson.conversions.Bson;

public class MongoDBServiceMessages {

  @Value
  public static class QueryReply implements Serializable {

    Collection<ExampleDBDocument.Pojo> exampleDBDocumentPojos;
  }

  @Value
  public static class QueryRequest implements Serializable {

    Bson filter;
  }
  @Value
  public static class QueryError implements Serializable {
  }


  @Value
  public static class StoreReply implements Serializable {
  }

  @Value
  public static class StoreRequest implements Serializable {

    ExampleDBDocument.Pojo exampleDBDocumentPojo;
  }
}
