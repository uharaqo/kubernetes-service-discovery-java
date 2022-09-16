package com.github.uharaqo.k8s.discovery.internal;

import com.github.uharaqo.k8s.discovery.Config;
import com.github.uharaqo.k8s.discovery.HttpHandler;
import com.github.uharaqo.k8s.discovery.HttpHandlerFactory;
import com.github.uharaqo.k8s.discovery.JsonDeserializer;
import com.github.uharaqo.k8s.discovery.SslContextProvider;

public final class DefaultHttpHandlerFactory implements HttpHandlerFactory {

  @Override
  public HttpHandler create(
      Config config, SslContextProvider sslContextProvider, JsonDeserializer deserializer) {
    return new DefaultHttpHandler(
        config.protocol, deserializer, sslContextProvider, config.connectTimeout);
  }
}
