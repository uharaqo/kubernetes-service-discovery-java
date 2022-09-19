package com.github.uharaqo.k8s.discovery.internal;

import java.util.Optional;

public interface ServiceDiscoveryRetryStrategy {

  /**
   * Provide the next strategy.
   *
   * @param t error
   * @return the next strategy or null on no more retry.
   */
  Optional<ServiceDiscoveryRetryStrategy> next(Throwable t);
}
