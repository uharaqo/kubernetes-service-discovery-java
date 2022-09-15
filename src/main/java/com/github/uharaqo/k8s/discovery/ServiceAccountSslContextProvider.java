package com.github.uharaqo.k8s.discovery;

import static com.github.uharaqo.k8s.discovery.KubernetesDiscoveryException.ErrorCause.SSL_CONTEXT_PROVIDER;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public final class ServiceAccountSslContextProvider implements SslContextProvider {

  private final Path caCertFile;

  public ServiceAccountSslContextProvider(Path caCertFile) {
    this.caCertFile = caCertFile;
  }

  @Override
  public SSLContext create() {
    try {
      byte[] caCert = Files.readAllBytes(caCertFile);
      return newSslContext(caCert);

    } catch (IOException e) {
      throw new KubernetesDiscoveryException(
          SSL_CONTEXT_PROVIDER, "Failed to read ca certificate file: " + caCertFile, e);
    }
  }

  private static SSLContext newSslContext(byte[] caCert) {
    try {
      KeyStore keyStore = newKeyStore(caCert);
      TrustManagerFactory trustFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustFactory.init(keyStore);

      final SSLContext ctx = SSLContext.getInstance("TLS");
      ctx.init(null, trustFactory.getTrustManagers(), new SecureRandom());

      return ctx;
    } catch (Exception e) {
      throw new KubernetesDiscoveryException(
          SSL_CONTEXT_PROVIDER, "Failed to create SSLContext", e);
    }
  }

  private static KeyStore newKeyStore(byte[] caCert)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    ByteArrayInputStream trustStream = new ByteArrayInputStream(caCert);

    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    trustStore.load(null, null);

    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    Collection<? extends Certificate> certificates = certFactory.generateCertificates(trustStream);
    if (certificates.isEmpty()) {
      throw new IllegalArgumentException("expected non-empty set of trusted certificates");
    }
    int index = 0;
    for (Certificate certificate : certificates) {
      trustStore.setCertificateEntry("ca" + index++, certificate);
    }
    return trustStore;
  }
}
