package io.github.uharaqo.k8s.discovery;

import io.github.uharaqo.k8s.discovery.internal.DefaultKubernetesServiceDiscovery;
import io.github.uharaqo.k8s.discovery.internal.DefaultServiceDiscoveryHttpHandlerFactory;
import io.github.uharaqo.k8s.discovery.internal.DefaultServiceDiscoveryHttpRequestFactory;
import io.github.uharaqo.k8s.discovery.internal.DefaultServiceDiscoveryJsonDeserializer;
import io.github.uharaqo.k8s.discovery.internal.ServiceAccountSslContextProvider;
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
  @Nullable private ServiceDiscoveryRetryConfig retryConfig;

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
  public KubernetesServiceDiscoveryBuilder withHttpHandlerFactory(
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
  public KubernetesServiceDiscoveryBuilder withRetryConfig(
      ServiceDiscoveryRetryConfig retryConfig) {
    this.retryConfig = retryConfig;
    return this;
  }

  @Nonnull
  public KubernetesServiceDiscovery build() {
    return new DefaultKubernetesServiceDiscovery(
        defaultIfNull(sslContextProvider, ServiceAccountSslContextProvider::new),
        defaultIfNull(deserializer, DefaultServiceDiscoveryJsonDeserializer::new),
        defaultIfNull(httpHandlerFactory, DefaultServiceDiscoveryHttpHandlerFactory::new),
        defaultIfNull(requestFactory, DefaultServiceDiscoveryHttpRequestFactory::new),
        defaultIfNull(retryConfig, ServiceDiscoveryRetryConfig::new));
  }

  private static <T> T defaultIfNull(T t, Supplier<T> defaultVal) {
    return t != null ? t : defaultVal.get();
  }
}
