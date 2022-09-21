package io.github.uharaqo.k8s.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import io.github.uharaqo.k8s.discovery.internal.DefaultServiceDiscoveryHttpHandlerFactory;
import io.github.uharaqo.k8s.discovery.internal.DefaultServiceDiscoveryHttpRequestFactory;
import io.github.uharaqo.k8s.discovery.internal.DefaultServiceDiscoveryJsonDeserializer;
import io.github.uharaqo.k8s.discovery.testutil.MockKubeApiServer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class IntegrationTest {

  private static final ServiceDiscoveryRequest request = new ServiceDiscoveryRequest("ns1", "ep1");

  private static final DefaultServiceDiscoveryHttpRequestFactory requestFactory =
      new DefaultServiceDiscoveryHttpRequestFactory(
          "http",
          "127.0.0.1",
          "1080",
          IntegrationTest.class.getClassLoader().getResource("token").getPath().toString(),
          5,
          60);
  private static KubernetesServiceDiscovery client;

  private static MockKubeApiServer mockServer;

  private static String mockGetResponse =
      "{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"ep1\",\"namespace\":\"ns1\",\"uid\":\"161b9670-4f11-412d-b8f8-3150c4827a99\",\"resourceVersion\":\"1022092107\",\"creationTimestamp\":\"2021-06-28T21:10:41Z\",\"labels\":{\"app\":\"app1\"},\"annotations\":{\"endpoints.kubernetes.io/last-change-trigger-time\":\"2022-09-14T20:00:54Z\"},\"managedFields\":[{\"manager\":\"kube-controller-manager\",\"operation\":\"Update\",\"apiVersion\":\"v1\",\"time\":\"2022-09-14T20:00:21Z\",\"fieldsType\":\"FieldsV1\",\"fieldsV1\":{\"f:metadata\":{\"f:annotations\":{\".\":{},\"f:endpoints.kubernetes.io/last-change-trigger-time\":{}},\"f:labels\":{\".\":{},\"f:app\":{},\"f:app.kubernetes.io/managed-by\":{},\"f:service.kubernetes.io/headless\":{}}},\"f:subsets\":{}}}]},\"subsets\":[{\"addresses\":[{\"ip\":\"10.0.0.101\",\"nodeName\":\"node1\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-47dtx\",\"uid\":\"db58349a-0ccc-4a3c-8782-5cea25163ef7\",\"resourceVersion\":\"1006548709\"}},{\"ip\":\"10.0.0.201\",\"nodeName\":\"node2\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-prscr\",\"uid\":\"4e65ed4e-0e36-421d-ae10-8481d12a4c88\",\"resourceVersion\":\"1022092100\"}}],\"ports\":[{\"name\":\"grpc\",\"port\":50051,\"protocol\":\"TCP\"},{\"name\":\"http\",\"port\":8080,\"protocol\":\"TCP\"}]}]}";

  private String firstWatchEvent =
      "{\"type\":\"ADDED\",\"object\":{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"ep1\",\"namespace\":\"ns1\",\"uid\":\"161b9670-4f11-412d-b8f8-3150c4827a99\",\"resourceVersion\":\"1022075955\",\"creationTimestamp\":\"2021-06-28T21:10:41Z\",\"labels\":{\"app\":\"app1\"},\"annotations\":{\"endpoints.kubernetes.io/last-change-trigger-time\":\"2022-09-14T19:52:18Z\"},\"managedFields\":[{\"manager\":\"kube-controller-manager\",\"operation\":\"Update\",\"apiVersion\":\"v1\",\"time\":\"2022-09-14T19:51:45Z\",\"fieldsType\":\"FieldsV1\",\"fieldsV1\":{\"f:metadata\":{\"f:annotations\":{\".\":{},\"f:endpoints.kubernetes.io/last-change-trigger-time\":{}},\"f:labels\":{\".\":{},\"f:app\":{},\"f:app.kubernetes.io/managed-by\":{},\"f:service.kubernetes.io/headless\":{}}},\"f:subsets\":{}}}]},\"subsets\":[{\"addresses\":[{\"ip\":\"10.0.0.101\",\"nodeName\":\"node1\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-47dtx\",\"uid\":\"db58349a-0ccc-4a3c-8782-5cea25163ef7\",\"resourceVersion\":\"1006548709\"}},{\"ip\":\"10.0.0.57\",\"nodeName\":\"node2\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-lqjbz\",\"uid\":\"afbcb1f1-5e4b-4096-bf04-659d30fdf385\",\"resourceVersion\":\"1022075951\"}}],\"ports\":[{\"name\":\"grpc\",\"port\":50051,\"protocol\":\"TCP\"},{\"name\":\"http\",\"port\":8080,\"protocol\":\"TCP\"}]}]}}";
  private List<String> mockWatchResponses =
      List.of(
          "{\"type\":\"MODIFIED\",\"object\":{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"ep1\",\"namespace\":\"ns1\",\"uid\":\"161b9670-4f11-412d-b8f8-3150c4827a99\",\"resourceVersion\":\"1022090971\",\"creationTimestamp\":\"2021-06-28T21:10:41Z\",\"labels\":{\"app\":\"app1\"},\"managedFields\":[{\"manager\":\"kube-controller-manager\",\"operation\":\"Update\",\"apiVersion\":\"v1\",\"time\":\"2022-09-14T20:00:20Z\",\"fieldsType\":\"FieldsV1\",\"fieldsV1\":{\"f:metadata\":{\"f:labels\":{\".\":{},\"f:app\":{},\"f:app.kubernetes.io/managed-by\":{},\"f:service.kubernetes.io/headless\":{}}},\"f:subsets\":{}}}]},\"subsets\":[{\"addresses\":[{\"ip\":\"10.0.0.101\",\"nodeName\":\"node1\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-47dtx\",\"uid\":\"db58349a-0ccc-4a3c-8782-5cea25163ef7\",\"resourceVersion\":\"1006548709\"}}],\"ports\":[{\"name\":\"grpc\",\"port\":50051,\"protocol\":\"TCP\"},{\"name\":\"http\",\"port\":8080,\"protocol\":\"TCP\"}]}]}}",
          "{\"type\":\"MODIFIED\",\"object\":{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"ep1\",\"namespace\":\"ns1\",\"uid\":\"161b9670-4f11-412d-b8f8-3150c4827a99\",\"resourceVersion\":\"1022091052\",\"creationTimestamp\":\"2021-06-28T21:10:41Z\",\"labels\":{\"app\":\"app1\"},\"annotations\":{\"endpoints.kubernetes.io/last-change-trigger-time\":\"2022-09-14T20:00:20Z\"},\"managedFields\":[{\"manager\":\"kube-controller-manager\",\"operation\":\"Update\",\"apiVersion\":\"v1\",\"time\":\"2022-09-14T20:00:21Z\",\"fieldsType\":\"FieldsV1\",\"fieldsV1\":{\"f:metadata\":{\"f:annotations\":{\".\":{},\"f:endpoints.kubernetes.io/last-change-trigger-time\":{}},\"f:labels\":{\".\":{},\"f:app\":{},\"f:app.kubernetes.io/managed-by\":{},\"f:service.kubernetes.io/headless\":{}}},\"f:subsets\":{}}}]},\"subsets\":[{\"addresses\":[{\"ip\":\"10.0.0.101\",\"nodeName\":\"node1\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-47dtx\",\"uid\":\"db58349a-0ccc-4a3c-8782-5cea25163ef7\",\"resourceVersion\":\"1006548709\"}}],\"notReadyAddresses\":[{\"ip\":\"10.0.0.201\",\"nodeName\":\"node2\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-prscr\",\"uid\":\"4e65ed4e-0e36-421d-ae10-8481d12a4c88\",\"resourceVersion\":\"1022091046\"}}],\"ports\":[{\"name\":\"grpc\",\"port\":50051,\"protocol\":\"TCP\"},{\"name\":\"http\",\"port\":8080,\"protocol\":\"TCP\"}]}]}}",
          "{\"type\":\"MODIFIED\",\"object\":{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"ep1\",\"namespace\":\"ns1\",\"uid\":\"161b9670-4f11-412d-b8f8-3150c4827a99\",\"resourceVersion\":\"1022092107\",\"creationTimestamp\":\"2021-06-28T21:10:41Z\",\"labels\":{\"app\":\"app1\"},\"annotations\":{\"endpoints.kubernetes.io/last-change-trigger-time\":\"2022-09-14T20:00:54Z\"},\"managedFields\":[{\"manager\":\"kube-controller-manager\",\"operation\":\"Update\",\"apiVersion\":\"v1\",\"time\":\"2022-09-14T20:00:21Z\",\"fieldsType\":\"FieldsV1\",\"fieldsV1\":{\"f:metadata\":{\"f:annotations\":{\".\":{},\"f:endpoints.kubernetes.io/last-change-trigger-time\":{}},\"f:labels\":{\".\":{},\"f:app\":{},\"f:app.kubernetes.io/managed-by\":{},\"f:service.kubernetes.io/headless\":{}}},\"f:subsets\":{}}}]},\"subsets\":[{\"addresses\":[{\"ip\":\"10.0.0.101\",\"nodeName\":\"node1\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-47dtx\",\"uid\":\"db58349a-0ccc-4a3c-8782-5cea25163ef7\",\"resourceVersion\":\"1006548709\"}},{\"ip\":\"10.0.0.201\",\"nodeName\":\"node2\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-prscr\",\"uid\":\"4e65ed4e-0e36-421d-ae10-8481d12a4c88\",\"resourceVersion\":\"1022092100\"}}],\"ports\":[{\"name\":\"grpc\",\"port\":50051,\"protocol\":\"TCP\"},{\"name\":\"http\",\"port\":8080,\"protocol\":\"TCP\"}]}]}}");

  @BeforeAll
  static void init() throws Exception {
    mockServer = new MockKubeApiServer();
    mockServer.setMockGetResponse(mockGetResponse);
    mockServer.start(requestFactory, request);
    client =
        KubernetesServiceDiscovery.builder()
            .withHttpRequestFactory(requestFactory)
            .withSslContextProvider(SslContextProvider.PLAINTEXT)
            .withJsonDeserializer(new DefaultServiceDiscoveryJsonDeserializer())
            .withHttpHandlerFactory(new DefaultServiceDiscoveryHttpHandlerFactory())
            .withRetryConfig(new ServiceDiscoveryRetryConfig(1, 60_000L))
            .build();
  }

  @AfterAll
  static void shutdown() throws Exception {
    mockServer.close();
    client.close();
  }

  @Test
  void get_request() throws Exception {
    val endpoint = client.getEndpoints(request).get();

    assertEquals(
        "Endpoints(apiVersion=v1, kind=Endpoints, metadata=ObjectMeta(clusterName=null,"
            + " creationTimestamp=2021-06-28T21:10:41Z, deletionTimestamp=null, labels={app=app1},"
            + " name=ep1, resourceVersion=1022092107, selfLink=null,"
            + " uid=161b9670-4f11-412d-b8f8-3150c4827a99),"
            + " subsets=[EndpointSubset(addresses=[EndpointAddress(hostname=null, ip=10.0.0.101,"
            + " nodeName=node1, targetRef=ObjectReference(apiVersion=null, fieldPath=null,"
            + " kind=Pod, name=ns1-v20220908233031-app1-7945fdf498-47dtx, namespace=ns1,"
            + " resourceVersion=1006548709, uid=db58349a-0ccc-4a3c-8782-5cea25163ef7)),"
            + " EndpointAddress(hostname=null, ip=10.0.0.201, nodeName=node2,"
            + " targetRef=ObjectReference(apiVersion=null, fieldPath=null, kind=Pod,"
            + " name=ns1-v20220908233031-app1-7945fdf498-prscr, namespace=ns1,"
            + " resourceVersion=1022092100, uid=4e65ed4e-0e36-421d-ae10-8481d12a4c88))],"
            + " notReadyAddresses=null, ports=[EndpointPort(appProtocol=null, name=grpc,"
            + " port=50051, protocol=TCP), EndpointPort(appProtocol=null, name=http, port=8080,"
            + " protocol=TCP)])])",
        endpoint.toString());
  }

  @Test
  void watch_request() throws InterruptedException {
    mockServer.addWatchResponse(firstWatchEvent);
    addWatchEvents();

    LinkedBlockingQueue<EndpointWatchEvent> q = new LinkedBlockingQueue<>();
    CountDownLatch latch = new CountDownLatch(1);

    client
        .watchChanges(request)
        .subscribe(
            new SimpleSubscriber<>(
                s -> System.out.println("started"),
                q::offer,
                t -> {
                  System.out.println("error");
                  t.printStackTrace();
                  latch.countDown();
                },
                () -> {
                  System.out.println("closed");
                  latch.countDown();
                }));

    List<EndpointWatchEvent> l1 = waitForEvents(q, 4);
    assertEquals(4, l1.size(), "Couldn't receive all expected events");
    assertEquals(1L, latch.getCount());

    addWatchEvents();
    List<EndpointWatchEvent> l2 = waitForEvents(q, 3);
    assertEquals(3, l2.size(), "Couldn't receive all expected events");

    mockServer.closeSession();
    assertEquals(1L, latch.getCount());

    // the client should make another request
    mockServer.addWatchResponse(firstWatchEvent);
    addWatchEvents();
    List<EndpointWatchEvent> l3 = waitForEvents(q, 4);
    assertEquals(4, l3.size(), "Couldn't receive all expected events");
    assertEquals(1L, latch.getCount());

    // the client will retry once
    mockServer.addWatchResponse("Dummy Invalid Response 1");
    mockServer.closeSession();
    mockServer.addWatchResponse("Dummy Invalid Response 2");
    mockServer.closeSession();
    latch.await(5000, TimeUnit.MILLISECONDS);
    assertEquals(0L, latch.getCount());
  }

  private void addWatchEvents() {
    mockWatchResponses.forEach(r -> mockServer.addWatchResponse(r));
  }

  private static List<EndpointWatchEvent> waitForEvents(
      LinkedBlockingQueue<EndpointWatchEvent> q, int count) throws InterruptedException {
    for (int i = 0; i < 50; i++) {
      if (count <= q.stream().count()) {
        break;
      }
      Thread.sleep(100);
    }
    List<EndpointWatchEvent> l = new ArrayList<>();
    q.drainTo(l);
    return l;
  }
}
