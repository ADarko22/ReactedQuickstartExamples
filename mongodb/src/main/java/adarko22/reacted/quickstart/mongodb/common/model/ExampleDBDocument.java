package adarko22.reacted.quickstart.mongodb.common.model;

import lombok.Value;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

@Value
public class ExampleDBDocument extends Document {

  public static final String ID_FIELD = "_id";
  public static final String TOPIC_FIELD = "topic";
  public static final String DATA_FIELD = "data";

  public ExampleDBDocument(String key, String topic, String data) {
    super();
    put(ID_FIELD, key);
    put(TOPIC_FIELD, topic);
    put(DATA_FIELD, data);
  }

  public static ExampleDBDocument fromPojo(Pojo pojo) {
    return new ExampleDBDocument(pojo.getId(), pojo.getTopic(), pojo.getData());
  }

  public @NotNull Pojo toPojo() {
    return new Pojo(getString(ID_FIELD), getString(TOPIC_FIELD), getString(DATA_FIELD));
  }

  @Value
  public static class Pojo {

    String id;
    String topic;
    String data;
  }
}
