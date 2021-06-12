package adarko22.reacted.quickstart.mongodb.common.model;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

@Value
@EqualsAndHashCode(callSuper = true)
public class ExampleDBDocument extends Document {

  public static final String ID_FIELD = "_id";
  public static final String TOPIC_FIELD = "topic";
  public static final String DATA_FIELD = "data";

  //todo fix issue and treat it as a map!

  public ExampleDBDocument(String key, String topic, String data) {
    super();
    put(ID_FIELD, key);
    put(TOPIC_FIELD, topic);
    put(DATA_FIELD, data);
  }

  public static ExampleDBDocument fromPojo(Pojo pojo) {
    return new ExampleDBDocument(pojo.getId(), pojo.getTopic(), pojo.getData());
  }

  public static @NotNull Pojo toPojo(Document document) {
    String id = document.getString(ID_FIELD);
    String topic = document.getString(TOPIC_FIELD);
    String data = document.getString(DATA_FIELD);
    return new Pojo(id, topic, data);
  }

  @Value
  public static class Pojo {

    String id;
    String topic;
    String data;
  }
}
