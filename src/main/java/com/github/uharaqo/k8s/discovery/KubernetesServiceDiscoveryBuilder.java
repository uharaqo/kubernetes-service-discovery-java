package com.github.uharaqo.k8s.discovery;

import com.github.uharaqo.k8s.discovery.internal.DefaultKubernetesServiceDiscovery;
import com.github.uharaqo.k8s.discovery.internal.DefaultServiceDiscoveryHttpHandlerFactory;
import com.github.uharaqo.k8s.discovery.internal.DefaultServiceDiscoveryHttpRequestFactory;
import com.github.uharaqo.k8s.discovery.internal.DefaultServiceDiscoveryJsonDeserializer;
import com.github.uharaqo.k8s.discovery.internal.ServiceAccountSslContextProvider;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;

/** Main interface to send a request and handle asynchronous responses. */
@Getter
public final class KubernetesServiceDiscoveryBuilder {

  @Nullable private ServiceDiscoveryJsonDeserializer deserializer;
  @Nullable private SslContextProvider sslContextProvider;
  @Nullable private ServiceDiscoveryHttpHandlerFactory httpHandlerFactory;
  @Nullable private ServiceDiscoveryHttpRequestFactory requestFactory;

  @Nonnull
  public KubernetesServiceDiscoveryBuilder withJsonDeserializer(
      ServiceDiscoveryJsonDeserializer deserializer) {
    this.deserializer = deserializer;
    return this;
  }

  @Nonnull
  public KubernetesServiceDiscoveryBuilder withSslContextProvider(
      SslContextProvider sslContextProvider) {
    this.sslContextProvider = sslContextProvider;
    return this;
  }

  @Nonnull
  public KubernetesServiceDiscoveryBuilder withServiceDiscoveryHttpHandlerFactory(
      ServiceDiscoveryHttpHandlerFactory httpHandlerFactory) {
    this.httpHandlerFactory = httpHandlerFactory;
    return this;
  }

  @Nonnull
  public KubernetesServiceDiscoveryBuilder withHttpRequestFactory(
      ServiceDiscoveryHttpRequestFactory requestFactory) {
    this.requestFactory = requestFactory;
    return this;
  }

  @Nonnull
  public KubernetesServiceDiscovery build() {
    return build(deserializer, sslContextProvider, httpHandlerFactory, requestFactory);
  }

  public static KubernetesServiceDiscovery build(
      ServiceDiscoveryJsonDeserializer deserializer,
      SslContextProvider sslContextProvider,
      ServiceDiscoveryHttpHandlerFactory httpHandlerFactory,
      ServiceDiscoveryHttpRequestFactory requestFactory) {

    //    Config c = defaultIfNull(config, () -> Config.builder().build());
    ServiceDiscoveryJsonDeserializer json =
        defaultIfNull(deserializer, DefaultServiceDiscoveryJsonDeserializer::new);
    SslContextProvider ssl =
        defaultIfNull(sslContextProvider, ServiceAccountSslContextProvider::new);

    ServiceDiscoveryHttpHandlerFactory httpFactory =
        defaultIfNull(httpHandlerFactory, DefaultServiceDiscoveryHttpHandlerFactory::new);

    ServiceDiscoveryHttpHandler http = httpFactory.create(ssl, json);
    ServiceDiscoveryHttpRequestFactory req =
        defaultIfNull(requestFactory, DefaultServiceDiscoveryHttpRequestFactory::new);
    return new DefaultKubernetesServiceDiscovery(http, req);
  }

  private static <T> T defaultIfNull(T t, Supplier<T> defaultVal) {
    return t != null ? t : defaultVal.get();
  }
}
