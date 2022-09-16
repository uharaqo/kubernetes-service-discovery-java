package com.github.uharaqo.k8s.discovery.internal;

import com.github.uharaqo.k8s.discovery.KubernetesServiceDiscovery;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpHandler;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpRequestFactory;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryRequest;
import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import lombok.AllArgsConstructor;

/** The main class that manages the discovery process. */
@AllArgsConstructor
public final class DefaultKubernetesServiceDiscovery implements KubernetesServiceDiscovery {

  private final ServiceDiscoveryHttpHandler http;
  private final ServiceDiscoveryHttpRequestFactory factory;

  @Override
  public CompletableFuture<Endpoints> getEndpoints(ServiceDiscoveryRequest request) {
    return http.getEndpoints(factory.forGet(request));
  }

  @Override
  public Publisher<EndpointWatchEvent> watchChanges(ServiceDiscoveryRequest request) {
    return http.watchEndpoints(factory.forWatch(request));
  }
}
