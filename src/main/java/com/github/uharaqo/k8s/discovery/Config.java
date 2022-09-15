package com.github.uharaqo.k8s.discovery;

import static com.github.uharaqo.k8s.discovery.KubernetesDiscoveryException.ErrorCause.HTTP_REQUEST_FACTORY;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Configurations for the {@link KubernetesApiClient}.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
@EqualsAndHashCode
public final class Config {

  /**
   * Protocol of the Kubernetes API. Default: "https"
   */
  @Nonnull
  @Default
  public final String protocol = "https";

  /**
   * Hostname of the Kubernetes API. Default: "KUBERNETES_SERVICE_HOST" environment variable
   */
  @Nonnull
  @Default
  public final String host = System.getenv("KUBERNETES_SERVICE_HOST");

  /**
   * Port of the Kubernetes API. Default: "KUBERNETES_SERVICE_PORT" environment variable
   */
  @Nonnull
  @Default
  public final String port = System.getenv("KUBERNETES_SERVICE_PORT");

  /**
   * HTTP connect timeout. Default: 5 seconds
   */
  @Default
  public final Duration connectTimeout = Duration.ofSeconds(5);

  /**
   * Timeout [seconds] for the GET endpoint API. Default: 5
   */
  @Default
  public final int getTimeoutSec = 3;

  /**
   * Timeout [seconds] for the GET endpoints API with watch query. Default: 60
   */
  @Default
  public final int watchTimeoutSec = 60;

  /**
   * CA certificate file for the Kubernetes API. Default:
   * "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt"
   */
  @Nonnull
  @Default
  public final String caCertFilePath = "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt";

  /**
   * Access Token file for the Kubernetes API. Default:
   * "/var/run/secrets/kubernetes.io/serviceaccount/token"
   */
  @Nonnull
  @Default
  public final String tokenFilePath = "/var/run/secrets/kubernetes.io/serviceaccount/token";

  public static Path isPathReadable(String path) {
    try {
      Path p = Paths.get(path);
      if (!Files.isReadable(p)) {
        throw new IllegalArgumentException("File is not readable");
      }
      return p;
    } catch (Exception e) {
      throw new KubernetesDiscoveryException(
          HTTP_REQUEST_FACTORY, "Failed to open CA cert file: " + path, null);
    }
  }
}
