package adarko22.reacted.quickstart.mongodb.common.model;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

@Value
@EqualsAndHashCode(callSuper = true)
public class ExampleDBDocument extends Document {

  public static final String TOPIC_FIELD = "topic";
  public static final String DATA_FIELD = "data";

  //todo fix issue and treat it as a map!

  public static Document fromPojo(Pojo pojo) {
    return new Document()
               .append(TOPIC_FIELD, pojo.getTopic())
               .append(DATA_FIELD, pojo.getData());
  }

  public static @NotNull Pojo toPojo(Document document) {
    String topic = document.getString(TOPIC_FIELD);
    String data = document.getString(DATA_FIELD);
    return new Pojo(topic, data);
  }

  @Value
  public static class Pojo {

    String topic;
    String data;
  }
}
