package com.github.uharaqo.k8s.discovery;

import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import javax.annotation.Nonnull;

/** Main interface to send a request and handle asynchronous responses. */
public interface KubernetesServiceDiscovery {

  /**
   * Calls <a
   * href="https://kubernetes.io/docs/reference/kubernetes-api/service-resources/endpoints-v1/#get-read-the-specified-endpoints">the
   * endpoint API</a>
   *
   * @param request request
   * @return response
   * @throws ServiceDiscoveryException exception
   */
  @Nonnull
  CompletableFuture<Endpoints> getEndpoints(ServiceDiscoveryRequest request)
      throws ServiceDiscoveryException;

  /**
   * Calls <a
   * href="https://kubernetes.io/docs/reference/kubernetes-api/service-resources/endpoints-v1/#list-list-or-watch-objects-of-kind-endpoints">the
   * list endpoint API</a> with the 'watch' option.
   *
   * @param request request
   * @return response
   * @throws ServiceDiscoveryException exception
   */
  @Nonnull
  Publisher<EndpointWatchEvent> watchChanges(ServiceDiscoveryRequest request)
      throws ServiceDiscoveryException;

  /**
   * Create a client with default configs.
   *
   * @return client
   * @throws ServiceDiscoveryException exception during client setup
   */
  @Nonnull
  static KubernetesServiceDiscovery create() throws ServiceDiscoveryException {
    return new KubernetesServiceDiscoveryBuilder().build();
  }

  @Nonnull
  static KubernetesServiceDiscoveryBuilder builder() throws ServiceDiscoveryException {
    return new KubernetesServiceDiscoveryBuilder();
  }
}
