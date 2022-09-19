package com.github.uharaqo.k8s.discovery;

public final class ServiceDiscoveryRetryConfig {

  public final int maxRetry;
  public final long maxDurationMs;

  public ServiceDiscoveryRetryConfig() {
    this(3, 60_000L);
  }

  public ServiceDiscoveryRetryConfig(int maxRetry, long maxDurationMs) {
    this.maxRetry = maxRetry;
    this.maxDurationMs = maxDurationMs;
  }
}
