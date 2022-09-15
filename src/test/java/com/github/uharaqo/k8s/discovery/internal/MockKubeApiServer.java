package com.github.uharaqo.k8s.discovery.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.uharaqo.k8s.discovery.Config;
import com.github.uharaqo.k8s.discovery.KubernetesApiClientRequest;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MockKubeApiServer {

  private final AtomicReference<String> mockGetResponse = new AtomicReference<>("CHANGEME_GET");
  private final LinkedBlockingQueue<String> mockWatchResponses = new LinkedBlockingQueue<>();
  private final AtomicBoolean isActive = new AtomicBoolean(true);

  private HttpServer server;

  public void start(
      Config config, KubernetesApiClientRequest request) throws Exception {

    server =
        HttpServer.create(new InetSocketAddress(config.host, Integer.parseInt(config.port)), 0);

    HttpRequestFactory requestFactory = new HttpRequestFactory(config);

    server.createContext(requestFactory.forGet(request).uri().getPath(), exchange -> {
      OutputStream out = exchange.getResponseBody();
      byte[] response = mockGetResponse.get().getBytes(UTF_8);
      exchange.sendResponseHeaders(200, response.length);
      out.write(response);
      out.flush();
      out.close();
    });

    server.createContext(requestFactory.forWatch(request).uri().getPath(), exchange -> {
      OutputStream out = exchange.getResponseBody();

      exchange.sendResponseHeaders(200, 0);
      while (isActive.get()) {
        try {
          String ev = mockWatchResponses.poll(100, TimeUnit.MILLISECONDS);
          if (ev != null) {
            out.write(ev.getBytes(UTF_8));
            out.write("\r\n".getBytes(UTF_8));
            out.flush();
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      out.close();
    });

    server.start();
  }

  public void setMockGetResponse(String mockGetResponse) {
    this.mockGetResponse.set(mockGetResponse);
  }

  public void addWatchResponse(String watchResponse) {
    this.mockWatchResponses.offer(watchResponse);
  }

  public void stop() {
    this.isActive.set(false);
  }

  public void close() {
    stop();
    if (server != null) {
      server.stop(0);
    }
  }
}
