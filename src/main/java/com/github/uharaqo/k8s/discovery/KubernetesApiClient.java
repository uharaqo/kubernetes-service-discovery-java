package com.github.uharaqo.k8s.discovery;

import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import com.github.uharaqo.k8s.discovery.internal.DefaultKubernetesApiClientFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import javax.annotation.Nonnull;

/**
 * Main interface to send a request and handle asynchronous responses.
 */
public interface KubernetesApiClient {

  /**
   * Calls <a
   * href="https://kubernetes.io/docs/reference/kubernetes-api/service-resources/endpoints-v1/#get-read-the-specified-endpoints">the
   * endpoint API</a>
   *
   * @param request request
   * @return response
   * @throws KubernetesDiscoveryException exception
   */
  CompletableFuture<Endpoints> getEndpoints(
      KubernetesApiClientRequest request) throws KubernetesDiscoveryException;

  /**
   * Calls <a
   * href="https://kubernetes.io/docs/reference/kubernetes-api/service-resources/endpoints-v1/#list-list-or-watch-objects-of-kind-endpoints">the
   * list endpoint API</a> with the 'watch' option.
   *
   * @param request request
   * @return response
   * @throws KubernetesDiscoveryException exception
   */
  Publisher<EndpointWatchEvent> watch(
      KubernetesApiClientRequest request) throws KubernetesDiscoveryException;

  // TODO: improve

  @Nonnull
  static KubernetesApiClient createDefault(
      JsonDeserializer jsonDeserializer) throws KubernetesDiscoveryException {
    return create(Config.builder().build(), jsonDeserializer);
  }

  @Nonnull
  static KubernetesApiClient create(
      Config config, JsonDeserializer jsonDeserializer) throws KubernetesDiscoveryException {
    ServiceAccountSslContextProvider sslContextProvider =
        new ServiceAccountSslContextProvider(Config.isPathReadable(config.caCertFilePath));
    return DefaultKubernetesApiClientFactory.createDefault(
        config, jsonDeserializer, sslContextProvider);
  }

//  @Nonnull
//  static KubernetesApiClient create(
//      Config config, HttpHandler httpHandler) throws KubernetesDiscoveryException {
//
//    return DefaultKubernetesApiClientFactory.create(config, httpHandler);
//  }
}
