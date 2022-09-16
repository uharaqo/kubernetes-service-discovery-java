package com.github.uharaqo.k8s.discovery;

import com.github.uharaqo.k8s.discovery.data.EndpointExtractor;
import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import com.github.uharaqo.k8s.discovery.internal.DefaultHttpHandlerFactory;
import com.github.uharaqo.k8s.discovery.internal.DefaultKubernetesApiClientFactory;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyOrderStrategy;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.TimeUnit;

public class App {

  public static void main(String[] args) throws InterruptedException {
    String namespace = System.getProperty("namespace");
    String endpoint = System.getProperty("endpoint");
    String command = System.getProperty("command");
    long watchTimeout = Long.valueOf(System.getProperty("timeout", "5"));
    int watchPort = Integer.valueOf(System.getProperty("port", "-1"));

    System.out.println(namespace);
    System.out.println(endpoint);
    System.out.println(command);
    System.out.println(watchTimeout);
    System.out.println(watchPort);

    Config config = Config.builder().watchTimeoutSec((int) watchTimeout).build();
    ServiceAccountSslContextProvider sslContextProvider =
        new ServiceAccountSslContextProvider(Config.getPath(config.caCertFilePath));
    DefaultJsonDeserializer deserializer = new DefaultJsonDeserializer();
    HttpRequestFactory requestFactory = new HttpRequestFactory(config);
    DefaultHttpHandlerFactory original = new DefaultHttpHandlerFactory();
    HttpHandlerFactory handlerFactory = new HttpHandlerFactory() {
      @Override
      public HttpHandler create(
          Config config, SslContextProvider sslContextProvider, JsonDeserializer deserializer) {
        HttpHandler v = original.create(config, sslContextProvider, deserializer);
        return new HttpHandler() {
          @Override
          public CompletableFuture<Endpoints> getEndpoints(HttpRequest request) {
            System.out.println("Request: " + request);
            return v.getEndpoints(request)
                .whenComplete((r, t) -> System.out.println("done4"));
          }

          @Override
          public Publisher<EndpointWatchEvent> watchEndpoints(HttpRequest request) {
            System.out.println("Request: " + request);
            return v.watchEndpoints(request);
          }
        };
      }
    };

    KubernetesApiClient ksd =
        DefaultKubernetesApiClientFactory.create(
            requestFactory, handlerFactory, config, deserializer, sslContextProvider);

    KubernetesApiClientRequest request = new KubernetesApiClientRequest(namespace, endpoint);

    Jsonb jsonb = JsonbBuilder.create(
        new JsonbConfig()
            .withNullValues(false)
            .withFormatting(true)
            .withPropertyOrderStrategy(PropertyOrderStrategy.LEXICOGRAPHICAL));

    if ("get".equals(command)) {
      get(ksd, request, jsonb)
          .join();

    } else if ("watch".equals(command)) {
      CountDownLatch l = new CountDownLatch(1);

      watch(ksd, request, watchPort, jsonb, l);

      l.await(watchTimeout, TimeUnit.SECONDS);

    } else {
      System.out.println("Invalid command: " + command);
    }
  }

  private static CompletableFuture<Endpoints> get(
      KubernetesApiClient ksd, KubernetesApiClientRequest request, Jsonb jsonb) {
    return ksd.getEndpoints(request)
        .whenComplete((eps, t) -> {
          if (t != null) {
            throw new RuntimeException("Failed", t);
          }
          System.out.println(jsonb.toJson(eps));
        });
  }

  private static void watch(
      KubernetesApiClient ksd, KubernetesApiClientRequest request, int watchPort,
      Jsonb jsonb, CountDownLatch l) {

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
                  jsonb.toJson(EndpointExtractor.getAddressesForPort(
                      e.getObject(), p -> p.getPort() == watchPort
                      )));
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
            }
        );

    ksd.watchEndpoints(request).subscribe(subscriber);
  }
}
