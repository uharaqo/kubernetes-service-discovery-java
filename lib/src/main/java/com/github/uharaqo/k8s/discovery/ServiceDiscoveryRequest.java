package com.github.uharaqo.k8s.discovery;

import javax.annotation.Nonnull;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
public class ServiceDiscoveryRequest {
  @Nonnull private final String namespace;
  @Nonnull private final String endpoint;
}
