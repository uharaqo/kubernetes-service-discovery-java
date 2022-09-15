package com.github.uharaqo.k8s.discovery;

public interface HttpHandlerFactory {

  HttpHandler create(
      Config config, SslContextProvider sslContextProvider, JsonDeserializer deserializer);
}
