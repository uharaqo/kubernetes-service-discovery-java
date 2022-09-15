package com.github.uharaqo.k8s.discovery;

import javax.annotation.Nullable;

public final class KubernetesDiscoveryException extends RuntimeException {
  private final ErrorCause errorCause;

  public KubernetesDiscoveryException(
      ErrorCause errorCause, String message, @Nullable Throwable cause) {
    super(message, cause);
    this.errorCause = errorCause;
  }

  public ErrorCause getErrorCause() {
    return errorCause;
  }

  public enum ErrorCause {
    HTTP,
    HTTP_REQUEST_FACTORY,
    SSL_CONTEXT_PROVIDER,
  }
}
