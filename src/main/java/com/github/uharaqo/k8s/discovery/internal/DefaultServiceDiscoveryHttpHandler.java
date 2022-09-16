package com.github.uharaqo.k8s.discovery.internal;

import static com.github.uharaqo.k8s.discovery.ServiceDiscoveryException.ErrorCause.HTTP;
import static com.github.uharaqo.k8s.discovery.ServiceDiscoveryException.ErrorCause.SSL_CONTEXT_PROVIDER;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
    SSLContext sslContext = newSslContext(sslContextProvider);
    if (sslContext != null) {
      builder.sslContext(sslContext);
    }
    httpClient = builder.connectTimeout(connectTimeout).build();
    this.deserializer = deserializer;
  }

  private static SSLContext newSslContext(SslContextProvider sslContextProvider) {
    try {
      return sslContextProvider.create();
    } catch (ServiceDiscoveryException e) {
      throw e;
    } catch (Exception e) {
      throw new ServiceDiscoveryException(SSL_CONTEXT_PROVIDER, "Failed to create SSLContext", e);
    }
  }

  @Nonnull
  @Override
  public CompletableFuture<Endpoints> getEndpoints(HttpRequest request) {
    return httpClient
        .sendAsync(request, BodyHandlers.ofString(UTF_8))
        .thenApply(
            r -> {
              try {
                int statusCode = r.statusCode();
                String body = r.body();

                if (statusCode == 200) {
                  return deserializer.deserializeEndpoints(body);
                  //                } else if (r.statusCode() == 403) {
                  // TODO can't continue
                }

                throw new ServiceDiscoveryException(
                    HTTP,
                    format("HTTP Error Response. status: %d, body: %s", statusCode, body),
                    null);
              } catch (ServiceDiscoveryException e) {
                throw e;
              } catch (Exception e) {
                throw new ServiceDiscoveryException(HTTP, "HTTP call failed", e);
              }
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
                  publisher.closeExceptionally(
                      new ServiceDiscoveryException(HTTP, "HTTP call failed", t));

                  //                } else if (r.statusCode() == 403) {
                  // TODO can't continue

                } else if (r.statusCode() != 200) {
                  String body =
                      r != null && r.body() != null
                          ? r.body().collect(Collectors.joining(System.lineSeparator())) : "";
                  publisher.closeExceptionally(
                      new ServiceDiscoveryException(HTTP, "HTTP error response: " + body, null));

                } else {
                  Consumer<String> f =
                      body -> {
                        try {
                          EndpointWatchEvent event = deserializer.deserializeEndpointEvent(body);
                          publisher.offer(event, 1000L, TimeUnit.MILLISECONDS, (s, ep) -> false);

                        } catch (Exception e) {
                          publisher.closeExceptionally(
                              new ServiceDiscoveryException(
                                  HTTP, "Failed to parse the response: " + body, e));
                        }
                      };

                  r.body().forEach(f);
                  publisher.close();
                }
              } catch (Exception e) {
                publisher.closeExceptionally(
                    new ServiceDiscoveryException(HTTP, "Unexpected Exception", e));
              }
            });

    return publisher;
  }
}
