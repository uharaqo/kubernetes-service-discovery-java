package com.github.uharaqo.k8s.discovery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ServiceDiscoveryException extends RuntimeException {
  private final ErrorCause errorCause;

  public ServiceDiscoveryException(
      ErrorCause errorCause, String message, @Nullable Throwable cause) {
    super(message, cause);
    this.errorCause = errorCause;
  }

  @Nonnull
  public ErrorCause getErrorCause() {
    return errorCause;
  }

  public enum ErrorCause {
    HTTP,
    HTTP_REQUEST_FACTORY,
    SSL_CONTEXT_PROVIDER,
    SETUP,
  }
}
