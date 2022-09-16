package com.github.uharaqo.k8s.discovery.internal;

import static com.github.uharaqo.k8s.discovery.ServiceDiscoveryException.ErrorCause.SSL_CONTEXT_PROVIDER;

import com.github.uharaqo.k8s.discovery.ServiceDiscoveryException;
import com.github.uharaqo.k8s.discovery.SslContextProvider;
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

  private final Path caCertFilePath;

  public ServiceAccountSslContextProvider() {
    this("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt");
  }

  /**
   * Constructor
   *
   * @param caCertFilePath CA certificate file for the Kubernetes API. Default:
   *     "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt"
   */
  public ServiceAccountSslContextProvider(String caCertFilePath) {
    this.caCertFilePath = Utils.toPath(caCertFilePath);
  }

  @Override
  public SSLContext create() {
    try {
      byte[] caCert = Files.readAllBytes(caCertFilePath);
      return newSslContext(caCert);

    } catch (IOException e) {
      throw new ServiceDiscoveryException(
          SSL_CONTEXT_PROVIDER, "Failed to read ca certificate file: " + caCertFilePath, e);
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
      throw new ServiceDiscoveryException(SSL_CONTEXT_PROVIDER, "Failed to create SSLContext", e);
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
