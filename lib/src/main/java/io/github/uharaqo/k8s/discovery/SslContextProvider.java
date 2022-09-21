package io.github.uharaqo.k8s.discovery;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;

public interface SslContextProvider {

  /**
   * Return a SSLContext
   *
   * @return SSLContext or null if secure protocol is not used
   * @throws Exception exception on creating an SSLContext
   */
  @Nonnull
  Optional<SSLContext> create() throws Exception;

  SslContextProvider PLAINTEXT =
      new SslContextProvider() {
        @Nonnull
        @Override
        public Optional<SSLContext> create() {
          return Optional.empty();
        }
      };
}
