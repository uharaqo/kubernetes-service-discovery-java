package com.github.uharaqo.k8s.discovery.internal;

import static com.github.uharaqo.k8s.discovery.ServiceDiscoveryException.ErrorCause.SETUP;
import static java.lang.String.format;

import com.github.uharaqo.k8s.discovery.ServiceDiscoveryException;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpRequestFactory;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryRequest;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Supplier;

public final class DefaultServiceDiscoveryHttpRequestFactory
    implements ServiceDiscoveryHttpRequestFactory {

  private final URI nameSpacesBaseUri;
  private final Path tokenFilePath;
  private final int getTimeoutSec;
  private final int watchTimeoutSec;

  public DefaultServiceDiscoveryHttpRequestFactory() {
    this(
        "https",
        System.getenv("KUBERNETES_SERVICE_HOST"),
        System.getenv("KUBERNETES_SERVICE_PORT"),
        "/var/run/secrets/kubernetes.io/serviceaccount/token",
        3,
        60);
  }

  /**
   * @param protocol Protocol of the API. "http" or "https". Default: "https"
   * @param host Hostname of the Api. default: "KUBERNETES_SERVICE_HOST" environment variable
   * @param port Port of the API. Default: "KUBERNETES_SERVICE_PORT" environment variable
   * @param tokenFilePath * Access Token file for the Kubernetes API. Default:
   *     "/var/run/secrets/kubernetes.io/serviceaccount/token"
   * @param getTimeoutSec Timeout [seconds] for the GET endpoint API. Default: 5
   * @param watchTimeoutSec Timeout [seconds] for the GET endpoints API with watch query. Default:
   *     60
   */
  public DefaultServiceDiscoveryHttpRequestFactory(
      String protocol,
      String host,
      String port,
      String tokenFilePath,
      int getTimeoutSec,
      int watchTimeoutSec) {
    nameSpacesBaseUri = getNamespacesUri(protocol, host, port);
    this.tokenFilePath = Utils.toPath(tokenFilePath);
    this.getTimeoutSec = getTimeoutSec;
    this.watchTimeoutSec = watchTimeoutSec;
  }

  @Override
  public HttpRequest forGet(ServiceDiscoveryRequest request) {
    return newRequest(
        () ->
            resolveUrl(
                nameSpacesBaseUri,
                "%s/endpoints/%s?pretty=false&timeoutSeconds=%s",
                request.namespace,
                request.endpoint,
                String.valueOf(getTimeoutSec)),
        getTimeoutSec);
  }

  @Override
  public HttpRequest forWatch(ServiceDiscoveryRequest request) {
    return newRequest(
        () ->
            resolveUrl(
                nameSpacesBaseUri,
                "%s/endpoints?watch=true&fieldSelector=metadata.name=%s&timeoutSeconds=%s",
                request.namespace,
                request.endpoint,
                String.valueOf(watchTimeoutSec)),
        watchTimeoutSec);
  }

  private HttpRequest newRequest(Supplier<URI> uri, int timeoutSec) {
    return HttpRequest.newBuilder()
        .GET()
        .uri(uri.get())
        .headers(getHeaders(getAuthHeader(tokenFilePath)))
        .timeout(Duration.ofSeconds(timeoutSec * 2))
        .build();
  }

  private static URI getNamespacesUri(String protocol, String host, String portText) {
    try {
      int port = Integer.parseInt(portText);
      return new URI(protocol, null, host, port, "/api/v1/namespaces/", null, null);
    } catch (Exception e) {
      throw new ServiceDiscoveryException(SETUP, "Invalid API URI", e);
    }
  }

  private static URI resolveUrl(URI baseUri, String format, String... params) {
    String uri = format(format, (Object[]) params);
    try {
      return baseUri.resolve(uri);
    } catch (Exception e) {
      throw new ServiceDiscoveryException(SETUP, "Invalid Resource Names: " + uri, e);
    }
  }

  private static String[] getHeaders(String authHeader) {
    return new String[] {
      "Authorization", authHeader, "User-Agent", "KubernetesServiceDiscovery",
    };
  }

  private static String getAuthHeader(Path tokenFilePath) {
    try {
      return "Bearer " + Files.readString(tokenFilePath);
    } catch (Exception e) {
      throw new ServiceDiscoveryException(
          SETUP, "Failed to read access token: " + tokenFilePath, e);
    }
  }
}
