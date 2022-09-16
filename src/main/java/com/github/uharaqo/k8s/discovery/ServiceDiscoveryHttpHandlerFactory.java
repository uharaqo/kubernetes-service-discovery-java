package com.github.uharaqo.k8s.discovery;

import javax.annotation.Nonnull;

public interface ServiceDiscoveryHttpHandlerFactory {

  @Nonnull
  ServiceDiscoveryHttpHandler create(
      SslContextProvider sslContextProvider, ServiceDiscoveryJsonDeserializer deserializer);
}
