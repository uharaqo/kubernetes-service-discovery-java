package io.github.uharaqo.k8s.discovery;

import java.net.http.HttpRequest;
import javax.annotation.Nonnull;

public interface ServiceDiscoveryHttpRequestFactory {

  @Nonnull
  HttpRequest forGet(ServiceDiscoveryRequest request);

  @Nonnull
  HttpRequest forWatch(ServiceDiscoveryRequest request);
}
