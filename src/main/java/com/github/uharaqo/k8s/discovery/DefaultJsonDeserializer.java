package com.github.uharaqo.k8s.discovery;

import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import javax.annotation.Nonnull;

/**
 * JSON-B implementation of the {@link JsonDeserializer}. To use this class, include a library that
 * implements JSON-B such as <a
 * href="https://projects.eclipse.org/projects/ee4j.yasson">yaason</a>.
 */
public class DefaultJsonDeserializer implements JsonDeserializer {

  private final Jsonb jsonb = JsonbBuilder.create();

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
