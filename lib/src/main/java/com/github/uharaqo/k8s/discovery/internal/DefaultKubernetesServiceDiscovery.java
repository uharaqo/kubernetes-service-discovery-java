package com.github.uharaqo.k8s.discovery.internal;

import static com.github.uharaqo.k8s.discovery.ServiceDiscoveryException.ErrorCause.SETUP;

import com.github.uharaqo.k8s.discovery.KubernetesServiceDiscovery;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryException;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpHandler;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpHandlerFactory;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpRequestFactory;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryJsonDeserializer;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryRequest;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryRetryConfig;
import com.github.uharaqo.k8s.discovery.SimpleSubscriber;
import com.github.uharaqo.k8s.discovery.SslContextProvider;
import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/** The main class that manages the discovery process. */
public final class DefaultKubernetesServiceDiscovery implements KubernetesServiceDiscovery {

  private final ServiceDiscoveryHttpRequestFactory requestFactory;
  private final SslContextProvider sslContextProvider;
  private final ServiceDiscoveryJsonDeserializer deserializer;
  private final ServiceDiscoveryHttpHandlerFactory httpHandlerFactory;
  private final AtomicReference<ServiceDiscoveryHttpHandler> httpHandlerCache =
      new AtomicReference<>();
  private final ServiceDiscoveryRetryConfig retryConfig;

  public DefaultKubernetesServiceDiscovery(
      SslContextProvider sslContextProvider,
      ServiceDiscoveryJsonDeserializer deserializer,
      ServiceDiscoveryHttpHandlerFactory httpHandlerFactory,
      ServiceDiscoveryHttpRequestFactory requestFactory,
      ServiceDiscoveryRetryConfig retryConfig) {
    this.sslContextProvider = sslContextProvider;
    this.deserializer = deserializer;
    this.httpHandlerFactory = httpHandlerFactory;
    this.requestFactory = requestFactory;
    this.retryConfig = retryConfig;

    refreshHttpHandler();
  }

  @Override
  public CompletableFuture<Endpoints> getEndpoints(ServiceDiscoveryRequest request) {
    return httpHandlerCache.get().getEndpoints(requestFactory.forGet(request));
  }

  @Override
  public Publisher<EndpointWatchEvent> watchChanges(ServiceDiscoveryRequest request) {
    SubmissionPublisher<EndpointWatchEvent> publisher = new SubmissionPublisher<>();

    ServiceDiscoveryHttpHandler httpHandler = httpHandlerCache.get();

    watchChanges(
        publisher, request, httpHandler, new DefaultServiceDiscoveryRetryStrategy(retryConfig));

    return publisher;
  }

  private void watchChanges(
      SubmissionPublisher<EndpointWatchEvent> publisher,
      ServiceDiscoveryRequest request,
      ServiceDiscoveryHttpHandler httpHandler,
      ServiceDiscoveryRetryStrategy errorHandlingStrategy) {

    HttpRequest httpRequest = requestFactory.forWatch(request);

    SimpleSubscriber<EndpointWatchEvent> subscriber =
        new SimpleSubscriber<>(
            s -> {},
            publisher::submit,
            t ->
                errorHandlingStrategy
                    .next(t)
                    .ifPresentOrElse(
                        strategy -> {
                          // cancel the current subscription and retry with a new handler because
                          // it might be due to expired SSL / token
                          Logger.getLogger(DefaultServiceDiscoveryHttpHandler.class.getName())
                              .log(Level.WARNING, "HTTP error. Retrying the watch call", t);
                          watchChanges(publisher, request, refreshHttpHandler(), strategy);
                        },
                        () -> publisher.closeExceptionally(t)),
            () -> watchChanges(publisher, request, httpHandler, errorHandlingStrategy));

    httpHandler.watchEndpoints(httpRequest).subscribe(subscriber);
  }

  private ServiceDiscoveryHttpHandler refreshHttpHandler() {
    ServiceDiscoveryHttpHandler newHandler =
        httpHandlerFactory.create(sslContextProvider, deserializer);
    ServiceDiscoveryHttpHandler prev = httpHandlerCache.getAndSet(newHandler);
    try {
      if (prev != null) {
        prev.close();
      }
    } catch (Exception e) {
      throw new ServiceDiscoveryException(SETUP, "Failed to close HttpHandler", e);
    }
    return newHandler;
  }

  @Override
  public void close() throws Exception {
    httpHandlerCache.get().close();
  }
}
