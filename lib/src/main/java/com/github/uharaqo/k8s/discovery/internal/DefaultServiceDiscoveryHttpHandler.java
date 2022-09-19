package com.github.uharaqo.k8s.discovery.internal;

import static com.github.uharaqo.k8s.discovery.ServiceDiscoveryException.ErrorCause.HTTP;
import static com.github.uharaqo.k8s.discovery.ServiceDiscoveryException.ErrorCause.SETUP;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.uharaqo.k8s.discovery.ServiceDiscoveryException;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpHandler;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryJsonDeserializer;
import com.github.uharaqo.k8s.discovery.SslContextProvider;
import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;

public final class DefaultServiceDiscoveryHttpHandler implements ServiceDiscoveryHttpHandler {

  private final ServiceDiscoveryJsonDeserializer deserializer;
  private final HttpClient httpClient;

  public DefaultServiceDiscoveryHttpHandler(
      SslContextProvider sslContextProvider,
      ServiceDiscoveryJsonDeserializer deserializer,
      Duration connectTimeout) {

    HttpClient.Builder builder = HttpClient.newBuilder();
    newSslContext(sslContextProvider).ifPresent(builder::sslContext);
    httpClient = builder.connectTimeout(connectTimeout).build();
    this.deserializer = deserializer;
  }

  private static Optional<SSLContext> newSslContext(SslContextProvider sslContextProvider) {
    try {
      return sslContextProvider.create();
    } catch (ServiceDiscoveryException e) {
      throw e;
    } catch (Exception e) {
      throw new ServiceDiscoveryException(SETUP, "Failed to create SSLContext", e);
    }
  }

  @Nonnull
  @Override
  public CompletableFuture<Endpoints> getEndpoints(HttpRequest request) {
    return httpClient
        .sendAsync(request, BodyHandlers.ofString(UTF_8))
        .thenApply(
            r -> {
              int statusCode = r.statusCode();
              String body = r.body();

              if (statusCode == 200) {
                return parseEndpoints(body);
              }

              throw new ServiceDiscoveryException(
                  HTTP,
                  format("HTTP Error Response. status: %d, body: %s", statusCode, body),
                  null);
            });
  }

  @Nonnull
  @Override
  public Publisher<EndpointWatchEvent> watchEndpoints(HttpRequest request) {
    SubmissionPublisher<EndpointWatchEvent> publisher = new SubmissionPublisher<>();

    httpClient
        .sendAsync(request, BodyHandlers.ofLines())
        .whenComplete(
            (r, t) -> {
              try {
                if (t != null) {
                  throw new ServiceDiscoveryException(HTTP, "HTTP call failed", t);
                }

                int statusCode = r.statusCode();
                if (statusCode != 200) {
                  String body = r.body().collect(Collectors.joining(System.lineSeparator()));
                  throw new ServiceDiscoveryException(
                      HTTP,
                      format("HTTP Error Response. status: %d, body: %s", statusCode, body),
                      null);
                }

                // TODO: can this response be null?
                r.body()
                    .map(this::parseWatchEvents)
                    .forEach(event -> publishEvent(publisher, event));

                publisher.close();

              } catch (ServiceDiscoveryException e) {
                publisher.closeExceptionally(e);
              } catch (Exception e) {
                publisher.closeExceptionally(
                    new ServiceDiscoveryException(HTTP, "Unexpected HTTP Exception", e));
              }
            });

    return publisher;
  }

  private Endpoints parseEndpoints(String body) {
    try {
      return deserializer.deserializeEndpoints(body);
    } catch (IOException e) {
      throw new ServiceDiscoveryException(HTTP, "Failed to parse HTTP response: " + body, e);
    }
  }

  private EndpointWatchEvent parseWatchEvents(String body) {
    try {
      return deserializer.deserializeEndpointEvent(body);
    } catch (Exception e) {
      throw new ServiceDiscoveryException(HTTP, "Failed to parse HTTP response: " + body, e);
    }
  }

  private static void publishEvent(
      SubmissionPublisher<EndpointWatchEvent> publisher, EndpointWatchEvent event) {
    try {
      publisher.offer(event, 100L, TimeUnit.MILLISECONDS, (s, ep) -> false);

    } catch (Exception e) {
      publisher.closeExceptionally(
          new ServiceDiscoveryException(HTTP, "Failed to publish event: " + event, e));
    }
  }

  @Override
  public void close() {
    // no need to shutdown the HttpClient
  }
}
