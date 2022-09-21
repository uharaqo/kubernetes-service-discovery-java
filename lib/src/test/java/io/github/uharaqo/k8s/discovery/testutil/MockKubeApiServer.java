/*
 *    Copyright [2018] [The authors]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.uharaqo.k8s.discovery.testutil;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.sun.net.httpserver.HttpServer;
import io.github.uharaqo.k8s.discovery.ServiceDiscoveryHttpRequestFactory;
import io.github.uharaqo.k8s.discovery.ServiceDiscoveryRequest;
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
