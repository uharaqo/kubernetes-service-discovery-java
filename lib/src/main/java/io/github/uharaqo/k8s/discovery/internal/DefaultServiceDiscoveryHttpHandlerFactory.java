package io.github.uharaqo.k8s.discovery.internal;

import io.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpHandler;
import io.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpHandlerFactory;
import io.github.uharaqo.k8s.discovery.ServiceDiscoveryJsonDeserializer;
import io.github.uharaqo.k8s.discovery.SslContextProvider;
import java.time.Duration;

public final class DefaultServiceDiscoveryHttpHandlerFactory
    implements ServiceDiscoveryHttpHandlerFactory {

  @Override
  public ServiceDiscoveryHttpHandler create(
      SslContextProvider sslContextProvider, ServiceDiscoveryJsonDeserializer deserializer) {
    return new DefaultServiceDiscoveryHttpHandler(
        sslContextProvider, deserializer, Duration.ofSeconds(5));
  }
}
