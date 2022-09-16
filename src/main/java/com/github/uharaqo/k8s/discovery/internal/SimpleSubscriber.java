package com.github.uharaqo.k8s.discovery.internal;

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
    try {
      onSubscribe.accept(subscription);
      subscription.request(1);
    } catch (Throwable t) {
      onError(t);
    }
  }

  @Override
  public void onNext(T item) {
    try {
      onNext.accept(item);
      subscription.request(1);
    } catch (Throwable t) {
      onError(t);
    }
  }

  @Override
  public void onError(Throwable throwable) {
    onError.accept(throwable);
    subscription.cancel();
  }

  @Override
  public void onComplete() {
    onComplete.run();
  }
}
