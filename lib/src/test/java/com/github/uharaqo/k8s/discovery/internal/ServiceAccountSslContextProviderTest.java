package com.github.uharaqo.k8s.discovery.internal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Test;

class ServiceAccountSslContextProviderTest {

  @Test
  void ssl_context_successfully_created() throws Exception {
    String testCertPath =
        ServiceAccountSslContextProviderTest.class.getClassLoader().getResource("ca.crt").getPath();

    ServiceAccountSslContextProvider provider = new ServiceAccountSslContextProvider(testCertPath);
    Optional<SSLContext> ctx = provider.create();
    assertTrue(ctx.isPresent());
  }
}
