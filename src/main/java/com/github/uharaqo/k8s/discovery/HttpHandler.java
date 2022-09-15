package com.github.uharaqo.k8s.discovery;

import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import javax.annotation.Nonnull;

/**
 * @see <a href="https://kubernetes.io/docs/reference/kubernetes-api/service-resources/endpoints-v1/#get-read-the-specified-endpoints">the official document:</a>
 */
public interface HttpHandler {

  @Nonnull
  CompletableFuture<Endpoints> getEndpoints(HttpRequest request);

  @Nonnull
  Publisher<EndpointWatchEvent> watchEndpoints(HttpRequest request);
}
