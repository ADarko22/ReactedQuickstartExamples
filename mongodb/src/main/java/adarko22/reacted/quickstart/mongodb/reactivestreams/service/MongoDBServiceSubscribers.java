package adarko22.reacted.quickstart.mongodb.reactivestreams.service;

import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.QueryReply;
import adarko22.reacted.quickstart.mongodb.common.messages.MongoDBServiceMessages.StoreReply;
import adarko22.reacted.quickstart.mongodb.common.model.ExampleDBDocument;
import com.mongodb.client.result.InsertOneResult;
import io.reacted.core.reactorsystem.ReActorRef;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class MongoDBServiceSubscribers {

  @RequiredArgsConstructor
  static class MongoQuerySubscriber implements Subscriber<ExampleDBDocument> {

    private final ReActorRef mongoGate;
    private final ReActorRef requester;

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
      this.subscription = subscription;
    }

    @Override
    public void onNext(ExampleDBDocument exampleDBDocument) {
      requester.tell(mongoGate, new QueryReply(Collections.singletonList(ExampleDBDocument.toPojo(exampleDBDocument))))
          .thenAccept(delivery -> subscription.request(1));
    }

    @Override
    public void onError(Throwable throwable) { }

    @Override
    public void onComplete() { }
  }

  @RequiredArgsConstructor
  static class MongoStoreSubscriber implements Subscriber<InsertOneResult> {

    private final ReActorRef requester;
    private final ReActorRef mongoGate;


    @Override
    public void onSubscribe(Subscription subscription) { }

    @Override
    public void onNext(InsertOneResult item) { }

    @Override
    public void onError(Throwable throwable) { }

    @Override
    public void onComplete() {
      requester.tell(mongoGate, new StoreReply());
    }
  }
}
