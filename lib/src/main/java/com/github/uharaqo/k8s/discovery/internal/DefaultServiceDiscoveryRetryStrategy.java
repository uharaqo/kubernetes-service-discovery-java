package com.github.uharaqo.k8s.discovery.internal;

import com.github.uharaqo.k8s.discovery.ServiceDiscoveryException;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryRetryConfig;
import java.time.Instant;
import java.util.Optional;

public final class DefaultServiceDiscoveryRetryStrategy implements ServiceDiscoveryRetryStrategy {
  // TODO: backoff

  private final int remainingRetry;
  private final Instant deadline;

  public DefaultServiceDiscoveryRetryStrategy(ServiceDiscoveryRetryConfig retryConfig) {
    remainingRetry = retryConfig.maxRetry;
    deadline = Instant.now().plusMillis(retryConfig.maxDurationMs);
  }

  public DefaultServiceDiscoveryRetryStrategy(int remainingRetry, Instant deadline) {
    this.remainingRetry = remainingRetry;
    this.deadline = deadline;
  }

  @Override
  public Optional<ServiceDiscoveryRetryStrategy> next(Throwable t) {
    // TODO
    if (t instanceof ServiceDiscoveryException
        && ((ServiceDiscoveryException) t).getErrorCause().isRecoverable()) {
      if (0 < remainingRetry && Instant.now().isBefore(deadline)) {
        return Optional.of(new DefaultServiceDiscoveryRetryStrategy(remainingRetry - 1, deadline));
      }
    }
    return Optional.empty();
  }
}
