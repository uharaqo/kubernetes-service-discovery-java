package com.github.uharaqo.k8s.discovery.internal;

import static com.github.uharaqo.k8s.discovery.KubernetesDiscoveryException.ErrorCause.HTTP;
import static com.github.uharaqo.k8s.discovery.KubernetesDiscoveryException.ErrorCause.SSL_CONTEXT_PROVIDER;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.uharaqo.k8s.discovery.HttpHandler;
import com.github.uharaqo.k8s.discovery.JsonDeserializer;
import com.github.uharaqo.k8s.discovery.KubernetesDiscoveryException;
import com.github.uharaqo.k8s.discovery.SslContextProvider;
import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
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

public final class DefaultHttpHandler implements HttpHandler {

  private final JsonDeserializer deserializer;
  private final HttpClient httpClient;

  public DefaultHttpHandler(
      String protocol,
      JsonDeserializer deserializer,
      SslContextProvider sslContextProvider,
      Duration connectTimeout) {

    this.deserializer = deserializer;
    Builder builder = HttpClient.newBuilder();
    if ("https".equals(protocol)) {
      try {
        builder.sslContext(sslContextProvider.create());
      } catch (KubernetesDiscoveryException e) {
        throw e;
      } catch (Exception e) {
        throw new KubernetesDiscoveryException(
            SSL_CONTEXT_PROVIDER, "Failed to create SSLContext", e);
      }
    }
    httpClient = builder.connectTimeout(connectTimeout).build();
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

                throw new KubernetesDiscoveryException(
                    HTTP,
                    format("HTTP Error Response. status: %d, body: %s", statusCode, body),
                    null);
              } catch (IOException e) {
                throw new KubernetesDiscoveryException(HTTP, "HTTP call failed", e);
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
                  String body = r.body().collect(Collectors.joining(System.lineSeparator()));
                  publisher.closeExceptionally(
                      new KubernetesDiscoveryException(HTTP, "HTTP call failed: " + body, t));

                  //                } else if (r.statusCode() == 403) {
                  // TODO can't continue

                } else if (r.statusCode() != 200) {
                  String body = r.body().collect(Collectors.joining(System.lineSeparator()));
                  publisher.closeExceptionally(
                      new KubernetesDiscoveryException(HTTP, "HTTP error response: " + body, null));

                } else {
                  Consumer<String> f =
                      body -> {
                        try {
                          EndpointWatchEvent event = deserializer.deserializeEndpointEvent(body);
                          publisher.offer(event, 1000L, TimeUnit.MILLISECONDS, (s, ep) -> false);

                        } catch (IOException e) {
                          publisher.closeExceptionally(
                              new KubernetesDiscoveryException(
                                  HTTP, "Failed to parse the response: " + body, e));
                        }
                      };

                  r.body().forEach(f);
                  publisher.close();
                }
              } catch (Exception e) {
                publisher.closeExceptionally(
                    new KubernetesDiscoveryException(HTTP, "Unexpected Exception", e));
              }
            });

    return publisher;
  }
}
