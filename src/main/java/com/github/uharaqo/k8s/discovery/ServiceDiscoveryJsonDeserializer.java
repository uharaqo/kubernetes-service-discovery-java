package com.github.uharaqo.k8s.discovery;

import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import java.io.IOException;
import javax.annotation.Nonnull;

/** Deserialize JSON responses from the API. */
public interface ServiceDiscoveryJsonDeserializer {

  @Nonnull
  Endpoints deserializeEndpoints(String body) throws IOException;

  @Nonnull
  EndpointWatchEvent deserializeEndpointEvent(String body) throws IOException;
}
