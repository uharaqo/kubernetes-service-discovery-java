package com.github.uharaqo.k8s.discovery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.Getter;

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

  @Getter
  public enum ErrorCause {
    HTTP(true),
    SETUP(false),
    ;

    private boolean recoverable;

    ErrorCause(boolean recoverable) {
      this.recoverable = recoverable;
    }
  }
}
