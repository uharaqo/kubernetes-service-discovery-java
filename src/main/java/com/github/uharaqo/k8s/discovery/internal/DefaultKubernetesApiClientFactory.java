package com.github.uharaqo.k8s.discovery.internal;

import com.github.uharaqo.k8s.discovery.Config;
import com.github.uharaqo.k8s.discovery.HttpHandler;
import com.github.uharaqo.k8s.discovery.JsonDeserializer;
import com.github.uharaqo.k8s.discovery.KubernetesApiClient;
import com.github.uharaqo.k8s.discovery.SslContextProvider;

public final class DefaultKubernetesApiClientFactory {

  public static KubernetesApiClient createDefault(
      Config config, JsonDeserializer deserializer, SslContextProvider sslContextProvider) {

    DefaultHttpHandlerFactory factory = new DefaultHttpHandlerFactory();
    HttpHandler http = factory.create(config, sslContextProvider, deserializer);
    HttpRequestFactory requestFactory = new HttpRequestFactory(config);

    return new DefaultKubernetesApiClient(http, requestFactory);
  }
}
