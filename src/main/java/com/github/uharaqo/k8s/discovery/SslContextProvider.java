package com.github.uharaqo.k8s.discovery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;

public interface SslContextProvider {

  /**
   * Return a SSLContext
   *
   * @return SSLContext or null if secure protocol is not used
   * @throws Exception exception on creating an SSLContext
   */
  @Nullable
  SSLContext create() throws Exception;

  static SslContextProvider noOp() {
    return new SslContextProvider() {
      @Nonnull
      @Override
      public SSLContext create() {
        return null;
      }
    };
  }
}
