package com.github.uharaqo.k8s.discovery.internal;

import static com.github.uharaqo.k8s.discovery.KubernetesDiscoveryException.ErrorCause.HTTP_REQUEST_FACTORY;
import static com.github.uharaqo.k8s.discovery.KubernetesDiscoveryException.ErrorCause.SSL_CONTEXT_PROVIDER;
import static java.lang.String.format;

import com.github.uharaqo.k8s.discovery.Config;
import com.github.uharaqo.k8s.discovery.KubernetesApiClientRequest;
import com.github.uharaqo.k8s.discovery.KubernetesDiscoveryException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Supplier;

/** Generate a request based on the given {@link Config} and {@link KubernetesApiClientRequest}. */
public class HttpRequestFactory {

  private final URI nameSpacesBaseUri;
  private final Path tokenFilePath;
  private final int getTimeoutSec;
  private final int watchTimeoutSec;

  public HttpRequestFactory(Config config) {
    nameSpacesBaseUri = getNamespacesUri(config.protocol, config.host, config.port);
    tokenFilePath = Paths.get(config.tokenFilePath); // TODO: check
    getTimeoutSec = config.getTimeoutSec;
    watchTimeoutSec = config.watchTimeoutSec;
  }

  public HttpRequest forGet(KubernetesApiClientRequest request) {
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

  public HttpRequest forWatch(KubernetesApiClientRequest request) {
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
    try {
      return HttpRequest.newBuilder()
          .GET()
          .uri(uri.get())
          .headers(getHeaders(tokenFilePath))
          .timeout(Duration.ofSeconds(timeoutSec * 2))
          .build();
    } catch (KubernetesDiscoveryException e) {
      throw e;
    } catch (Exception e) {
      throw new KubernetesDiscoveryException(SSL_CONTEXT_PROVIDER, "Unknown Exception", e);
    }
  }

  private static URI getNamespacesUri(String protocol, String host, String portText) {
    try {
      int port = Integer.parseInt(portText);
      return new URI(protocol, null, host, port, "/api/v1/namespaces", null, null);
    } catch (Exception e) {
      throw new KubernetesDiscoveryException(HTTP_REQUEST_FACTORY, "Invalid API URI", e);
    }
  }

  private static URI resolveUrl(URI baseUri, String format, String... params) {
    String uri = format(format, (Object[]) params);
    try {
      return baseUri.resolve(uri);
    } catch (Exception e) {
      throw new KubernetesDiscoveryException(
          HTTP_REQUEST_FACTORY, "Invalid Resource Names: " + uri, e);
    }
  }

  private static String[] getHeaders(Path tokenFilePath) {
    try {
      return new String[] {
        "Authorization",
        "Bearer " + Files.readString(tokenFilePath),
        "User-Agent",
        "KubernetesServiceDiscovery",
      };

    } catch (IOException e) {
      throw new KubernetesDiscoveryException(
          HTTP_REQUEST_FACTORY, "Failed to read access token: " + tokenFilePath, e);
    }
  }
}
