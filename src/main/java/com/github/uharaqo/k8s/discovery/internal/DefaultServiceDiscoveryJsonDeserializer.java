package com.github.uharaqo.k8s.discovery.internal;

import com.github.uharaqo.k8s.discovery.ServiceDiscoveryJsonDeserializer;
import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyOrderStrategy;
import javax.annotation.Nonnull;

/**
 * JSON-B implementation of the {@link ServiceDiscoveryJsonDeserializer}. To use this class, include
 * a library that implements JSON-B such as <a
 * href="https://projects.eclipse.org/projects/ee4j.yasson">yaason</a>.
 */
public final class DefaultServiceDiscoveryJsonDeserializer
    implements ServiceDiscoveryJsonDeserializer {

  private final Jsonb jsonb =
      JsonbBuilder.create(
          new JsonbConfig()
              .withNullValues(false)
              .withPropertyOrderStrategy(PropertyOrderStrategy.LEXICOGRAPHICAL));

  @Nonnull
  @Override
  public Endpoints deserializeEndpoints(String body) {
    return jsonb.fromJson(body, Endpoints.class);
  }

  @Nonnull
  @Override
  public EndpointWatchEvent deserializeEndpointEvent(String body) {
    return jsonb.fromJson(body, EndpointWatchEvent.class);
  }
}
