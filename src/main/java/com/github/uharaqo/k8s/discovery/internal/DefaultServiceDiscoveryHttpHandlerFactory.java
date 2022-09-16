package com.github.uharaqo.k8s.discovery.internal;

import com.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpHandler;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpHandlerFactory;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryJsonDeserializer;
import com.github.uharaqo.k8s.discovery.SslContextProvider;
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
