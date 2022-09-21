package io.github.uharaqo.k8s.discovery;

import io.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import io.github.uharaqo.k8s.discovery.data.Endpoints;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import javax.annotation.Nonnull;

/**
 * Makes asynchronous calls to Kubernetes API server
 *
 * @see <a
 *     href="https://kubernetes.io/docs/reference/kubernetes-api/service-resources/endpoints-v1/#get-read-the-specified-endpoints">the
 *     official document:</a>
 */
public interface ServiceDiscoveryHttpHandler extends AutoCloseable {

  @Nonnull
  CompletableFuture<Endpoints> getEndpoints(HttpRequest request);

  @Nonnull
  Publisher<EndpointWatchEvent> watchEndpoints(HttpRequest request);
}
