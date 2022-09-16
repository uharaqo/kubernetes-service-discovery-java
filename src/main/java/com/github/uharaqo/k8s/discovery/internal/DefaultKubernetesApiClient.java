package com.github.uharaqo.k8s.discovery.internal;

import com.github.uharaqo.k8s.discovery.HttpHandler;
import com.github.uharaqo.k8s.discovery.HttpRequestFactory;
import com.github.uharaqo.k8s.discovery.KubernetesApiClient;
import com.github.uharaqo.k8s.discovery.KubernetesApiClientRequest;
import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import lombok.AllArgsConstructor;

/** The main class that manages the discovery process. */
@AllArgsConstructor
public final class DefaultKubernetesApiClient implements KubernetesApiClient {

  private final HttpHandler http;
  private final HttpRequestFactory factory;

  @Override
  public CompletableFuture<Endpoints> getEndpoints(KubernetesApiClientRequest request) {
    return http.getEndpoints(factory.forGet(request));
  }

  @Override
  public Publisher<EndpointWatchEvent> watchEndpoints(KubernetesApiClientRequest request) {
    return http.watchEndpoints(factory.forWatch(request));
  }
}
