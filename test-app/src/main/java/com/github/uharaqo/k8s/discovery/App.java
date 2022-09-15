package com.github.uharaqo.k8s.discovery;

import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyOrderStrategy;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class App {

  public static void main(String[] args) {
    System.out.println("HELLO");
    KubernetesApiClient ksd = KubernetesApiClient.createDefault();

    KubernetesApiClientRequest request =
        new KubernetesApiClientRequest(
            System.getProperty("namespace"), System.getProperty("endpoint"));

    Jsonb jsonb = JsonbBuilder.create(
        new JsonbConfig()
            .withNullValues(false)
            .withFormatting(true)
            .withPropertyOrderStrategy(PropertyOrderStrategy.LEXICOGRAPHICAL));

    if ("get".equals(System.getProperty("method"))) {
      ksd.getEndpoints(request)
          .whenComplete((eps, t) -> {
            if (t != null) {
              throw new RuntimeException("Failed", t);
            }
            jsonb.toJson(eps);
            System.out.println(eps.toString());
          });

    } else if ("get".equals(System.getProperty("method"))) {
      ksd.watch(request).subscribe(new Subscriber<>() {
        @Override
        public void onSubscribe(Subscription subscription) {
          System.out.println("------------------------------------------------------------");
          System.out.println("SUBSCRIBING");
        }

        @Override
        public void onNext(EndpointWatchEvent item) {
          System.out.println();
          System.out.println(jsonb.toJson(item));
          System.out.println();
        }

        @Override
        public void onError(Throwable throwable) {
          System.out.println();
          throwable.printStackTrace();
          System.out.println();
        }

        @Override
        public void onComplete() {
          System.out.println("COMPLETE");
          System.out.println("------------------------------------------------------------");
        }
      });
    }
  }
}