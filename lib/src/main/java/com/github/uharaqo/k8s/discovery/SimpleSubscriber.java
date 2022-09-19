package com.github.uharaqo.k8s.discovery;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.Consumer;

public class SimpleSubscriber<T> implements Subscriber<T> {

  private final Consumer<Subscription> onSubscribe;
  private final Consumer<T> onNext;
  private final Consumer<Throwable> onError;
  private final Runnable onComplete;

  private Subscription subscription;

  public SimpleSubscriber(
      Consumer<Subscription> onSubscribe,
      Consumer<T> onNext,
      Consumer<Throwable> onError,
      Runnable onComplete) {
    this.onSubscribe = onSubscribe;
    this.onNext = onNext;
    this.onError = onError;
    this.onComplete = onComplete;
  }

  @Override
  public void onSubscribe(Subscription subscription) {
    this.subscription = subscription;
    subscription.request(1);
    try {
      onSubscribe.accept(subscription);
    } catch (Throwable t) {
      onError(t);
    }
  }

  @Override
  public void onNext(T item) {
    try {
      subscription.request(1);
      onNext.accept(item);
    } catch (Throwable t) {
      onError(t);
    }
  }

  @Override
  public void onError(Throwable throwable) {
    subscription.cancel();
    onError.accept(throwable);
  }

  @Override
  public void onComplete() {
    onComplete.run();
  }
}
