package com.github.uharaqo.k8s.discovery.internal;

import com.github.uharaqo.k8s.discovery.Config;
import com.github.uharaqo.k8s.discovery.HttpHandler;
import com.github.uharaqo.k8s.discovery.HttpHandlerFactory;
import com.github.uharaqo.k8s.discovery.HttpRequestFactory;
import com.github.uharaqo.k8s.discovery.JsonDeserializer;
import com.github.uharaqo.k8s.discovery.KubernetesApiClient;
import com.github.uharaqo.k8s.discovery.SslContextProvider;

public final class DefaultKubernetesApiClientFactory {

  public static KubernetesApiClient createDefault(
      Config config, JsonDeserializer deserializer, SslContextProvider sslContextProvider) {
    DefaultHttpHandlerFactory handlerFactory = new DefaultHttpHandlerFactory();
    HttpRequestFactory requestFactory = new HttpRequestFactory(config);
    return create(requestFactory, handlerFactory, config, deserializer, sslContextProvider);
  }

  public static KubernetesApiClient create(
      HttpRequestFactory requestFactory,
      HttpHandlerFactory factory,
      Config config,
      JsonDeserializer deserializer,
      SslContextProvider sslContextProvider) {
    HttpHandler http = factory.create(config, sslContextProvider, deserializer);
    return new DefaultKubernetesApiClient(http, requestFactory);
  }
}
