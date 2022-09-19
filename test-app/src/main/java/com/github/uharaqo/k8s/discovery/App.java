package com.github.uharaqo.k8s.discovery;

import com.github.uharaqo.k8s.discovery.data.EndpointExtractor;
import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import com.github.uharaqo.k8s.discovery.internal.DefaultServiceDiscoveryHttpHandler;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyOrderStrategy;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class App {

  public static void main(String[] args) throws InterruptedException {
    String namespace = System.getProperty("namespace", "CHANGEME_NS");
    String endpoint = System.getProperty("endpoint", "CHANGEME_EP");
    String command = System.getProperty("command");
    long watchTimeout = Long.valueOf(System.getProperty("timeout", "5"));
    int watchPort = Integer.valueOf(System.getProperty("port", "-1"));

    System.out.println(namespace);
    System.out.println(endpoint);
    System.out.println(command);
    System.out.println(watchTimeout);
    System.out.println(watchPort);

    System.out.println(JsonbBuilder.create().toJson(Map.of("k", "v")));

    KubernetesServiceDiscovery discovery =
        KubernetesServiceDiscovery.builder()
            .withHttpHandlerFactory(new CustomHttpHandlerFactory(Duration.ofSeconds(watchTimeout)))
            .build();

    ServiceDiscoveryRequest request = new ServiceDiscoveryRequest(namespace, endpoint);

    Jsonb jsonb =
        JsonbBuilder.create(
            new JsonbConfig()
                .withNullValues(false)
                .withFormatting(true)
                .withPropertyOrderStrategy(PropertyOrderStrategy.LEXICOGRAPHICAL));

    if ("get".equals(command)) {
      get(discovery, request, jsonb).join();

    } else if ("watch".equals(command)) {
      CountDownLatch l = new CountDownLatch(1);

      watch(discovery, request, watchPort, jsonb, l);

      l.await(watchTimeout, TimeUnit.SECONDS);

    } else {
      System.out.println("Invalid command: " + command);
    }
  }

  private static CompletableFuture<Endpoints> get(
      KubernetesServiceDiscovery discovery, ServiceDiscoveryRequest request, Jsonb jsonb) {
    return discovery
        .getEndpoints(request)
        .whenComplete(
            (eps, t) -> {
              if (t != null) {
                throw new RuntimeException("Failed", t);
              }
              System.out.println(jsonb.toJson(eps));
            });
  }

  private static void watch(
      KubernetesServiceDiscovery discovery,
      ServiceDiscoveryRequest request,
      int watchPort,
      Jsonb jsonb,
      CountDownLatch l) {

    SimpleSubscriber<EndpointWatchEvent> subscriber =
        new SimpleSubscriber<>(
            s -> {
              System.out.println("------------------------------------------------------------");
              System.out.println("SUBSCRIBING");
            },
            e -> {
              System.out.println();
              System.out.println(jsonb.toJson(e));
              System.out.println();
              System.out.println(
                  jsonb.toJson(
                      EndpointExtractor.getAddressesForPort(
                          e.getObject(), p -> p.getPort() == watchPort)));
              System.out.println();
            },
            t -> {
              System.out.println();
              t.printStackTrace();
              System.out.println();
              l.countDown();
            },
            () -> {
              System.out.println("COMPLETE");

              System.out.println("------------------------------------------------------------");
              l.countDown();
            });

    discovery.watchChanges(request).subscribe(subscriber);
  }

  private static class CustomHttpHandlerFactory implements ServiceDiscoveryHttpHandlerFactory {

    private Duration watchTimeout;

    public CustomHttpHandlerFactory(Duration watchTimeout) {
      this.watchTimeout = watchTimeout;
    }

    @Nonnull
    @Override
    public ServiceDiscoveryHttpHandler create(
        SslContextProvider sslContextProvider, ServiceDiscoveryJsonDeserializer deserializer) {

      ServiceDiscoveryHttpHandler original =
          new DefaultServiceDiscoveryHttpHandler(sslContextProvider, deserializer, watchTimeout);

      return new ServiceDiscoveryHttpHandler() {
        @Nonnull
        @Override
        public CompletableFuture<Endpoints> getEndpoints(HttpRequest request) {
          System.out.println("Request: " + request);
          return original.getEndpoints(request).whenComplete((r, t) -> System.out.println("done4"));
        }

        @Nonnull
        @Override
        public Publisher<EndpointWatchEvent> watchEndpoints(HttpRequest request) {
          System.out.println("Request: " + request);
          return original.watchEndpoints(request);
        }

        @Override
        public void close() throws Exception {
          System.out.println("Closing HTTP Client");
        }
      };
    }
  }
}
