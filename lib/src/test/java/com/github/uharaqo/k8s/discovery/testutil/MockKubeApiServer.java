package com.github.uharaqo.k8s.discovery.testutil;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpRequestFactory;
import com.github.uharaqo.k8s.discovery.ServiceDiscoveryRequest;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MockKubeApiServer {

  private final ExecutorService es = Executors.newCachedThreadPool();
  private final AtomicReference<String> mockGetResponse = new AtomicReference<>("CHANGEME_GET");
  private final LinkedBlockingQueue<String> mockWatchResponses = new LinkedBlockingQueue<>();
  private final AtomicBoolean sessionActive = new AtomicBoolean(true);

  private HttpServer server;

  public MockKubeApiServer start(
      ServiceDiscoveryHttpRequestFactory requestFactory, ServiceDiscoveryRequest request) {

    try {
      server = HttpServer.create(new InetSocketAddress("127.0.0.1", 1080), 0);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    server.createContext(
        requestFactory.forGet(request).uri().getPath(),
        exchange -> {
          OutputStream out = exchange.getResponseBody();
          byte[] response = mockGetResponse.get().getBytes(UTF_8);
          exchange.sendResponseHeaders(200, response.length);
          out.write(response);
          out.flush();
          out.close();
        });

    server.createContext(
        requestFactory.forWatch(request).uri().getPath(),
        exchange ->
            es.submit(
                () -> {
                  try {
                    OutputStream out = exchange.getResponseBody();

                    exchange.sendResponseHeaders(200, 0);
                    while (sessionActive.get()) {
                      String ev = mockWatchResponses.poll(100, TimeUnit.MILLISECONDS);
                      if (ev != null) {
                        out.write(ev.getBytes(UTF_8));
                        out.write("\r\n".getBytes(UTF_8));
                        out.flush();
                      }
                    }
                    out.close();
                    sessionActive.set(true);
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                }));

    server.start();
    return this;
  }

  public void setMockGetResponse(String mockGetResponse) {
    this.mockGetResponse.set(mockGetResponse);
  }

  public void addWatchResponse(String watchResponse) {
    this.mockWatchResponses.offer(watchResponse);
  }

  public void closeSession() {
    this.sessionActive.set(false);
  }

  public void close() {
    closeSession();
    es.shutdownNow();
    if (server != null) {
      server.stop(0);
    }
  }
}
