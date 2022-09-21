package io.github.uharaqo.k8s.discovery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;

@Data
@AllArgsConstructor
@Generated
public final class ServiceDiscoveryRetryConfig {

  private final int maxRetry;
  private final long maxDurationMillis;

  public ServiceDiscoveryRetryConfig() {
    this(3, 60_000L);
  }
}
