package com.github.uharaqo.k8s.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import com.github.uharaqo.k8s.discovery.data.Endpoints;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class JsonDeserializerTest {

  static final String mockGetResponse =
      "{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"ep1\",\"namespace\":\"ns1\",\"uid\":\"161b9670-4f11-412d-b8f8-3150c4827a99\",\"resourceVersion\":\"1022092107\",\"creationTimestamp\":\"2021-06-28T21:10:41Z\",\"labels\":{\"app\":\"app1\"},\"annotations\":{\"endpoints.kubernetes.io/last-change-trigger-time\":\"2022-09-14T20:00:54Z\"},\"managedFields\":[{\"manager\":\"kube-controller-manager\",\"operation\":\"Update\",\"apiVersion\":\"v1\",\"time\":\"2022-09-14T20:00:21Z\",\"fieldsType\":\"FieldsV1\",\"fieldsV1\":{\"f:metadata\":{\"f:annotations\":{\".\":{},\"f:endpoints.kubernetes.io/last-change-trigger-time\":{}},\"f:labels\":{\".\":{},\"f:app\":{},\"f:app.kubernetes.io/managed-by\":{},\"f:service.kubernetes.io/headless\":{}}},\"f:subsets\":{}}}]},\"subsets\":[{\"addresses\":[{\"ip\":\"10.0.0.101\",\"nodeName\":\"node1\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-47dtx\",\"uid\":\"db58349a-0ccc-4a3c-8782-5cea25163ef7\",\"resourceVersion\":\"1006548709\"}},{\"ip\":\"10.0.0.201\",\"nodeName\":\"node2\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-prscr\",\"uid\":\"4e65ed4e-0e36-421d-ae10-8481d12a4c88\",\"resourceVersion\":\"1022092100\"}}],\"ports\":[{\"name\":\"grpc\",\"port\":50051,\"protocol\":\"TCP\"},{\"name\":\"http\",\"port\":8080,\"protocol\":\"TCP\"}]}]}";

  static final List<String> mockWatchResponses =
      List.of(
          "{\"type\":\"ADDED\",\"object\":{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"ep1\",\"namespace\":\"ns1\",\"uid\":\"161b9670-4f11-412d-b8f8-3150c4827a99\",\"resourceVersion\":\"1022075955\",\"creationTimestamp\":\"2021-06-28T21:10:41Z\",\"labels\":{\"app\":\"app1\"},\"annotations\":{\"endpoints.kubernetes.io/last-change-trigger-time\":\"2022-09-14T19:52:18Z\"},\"managedFields\":[{\"manager\":\"kube-controller-manager\",\"operation\":\"Update\",\"apiVersion\":\"v1\",\"time\":\"2022-09-14T19:51:45Z\",\"fieldsType\":\"FieldsV1\",\"fieldsV1\":{\"f:metadata\":{\"f:annotations\":{\".\":{},\"f:endpoints.kubernetes.io/last-change-trigger-time\":{}},\"f:labels\":{\".\":{},\"f:app\":{},\"f:app.kubernetes.io/managed-by\":{},\"f:service.kubernetes.io/headless\":{}}},\"f:subsets\":{}}}]},\"subsets\":[{\"addresses\":[{\"ip\":\"10.0.0.101\",\"nodeName\":\"node1\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-47dtx\",\"uid\":\"db58349a-0ccc-4a3c-8782-5cea25163ef7\",\"resourceVersion\":\"1006548709\"}},{\"ip\":\"10.0.0.57\",\"nodeName\":\"node2\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-lqjbz\",\"uid\":\"afbcb1f1-5e4b-4096-bf04-659d30fdf385\",\"resourceVersion\":\"1022075951\"}}],\"ports\":[{\"name\":\"grpc\",\"port\":50051,\"protocol\":\"TCP\"},{\"name\":\"http\",\"port\":8080,\"protocol\":\"TCP\"}]}]}}",
          "{\"type\":\"MODIFIED\",\"object\":{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"ep1\",\"namespace\":\"ns1\",\"uid\":\"161b9670-4f11-412d-b8f8-3150c4827a99\",\"resourceVersion\":\"1022090971\",\"creationTimestamp\":\"2021-06-28T21:10:41Z\",\"labels\":{\"app\":\"app1\"},\"managedFields\":[{\"manager\":\"kube-controller-manager\",\"operation\":\"Update\",\"apiVersion\":\"v1\",\"time\":\"2022-09-14T20:00:20Z\",\"fieldsType\":\"FieldsV1\",\"fieldsV1\":{\"f:metadata\":{\"f:labels\":{\".\":{},\"f:app\":{},\"f:app.kubernetes.io/managed-by\":{},\"f:service.kubernetes.io/headless\":{}}},\"f:subsets\":{}}}]},\"subsets\":[{\"addresses\":[{\"ip\":\"10.0.0.101\",\"nodeName\":\"node1\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-47dtx\",\"uid\":\"db58349a-0ccc-4a3c-8782-5cea25163ef7\",\"resourceVersion\":\"1006548709\"}}],\"ports\":[{\"name\":\"grpc\",\"port\":50051,\"protocol\":\"TCP\"},{\"name\":\"http\",\"port\":8080,\"protocol\":\"TCP\"}]}]}}",
          "{\"type\":\"MODIFIED\",\"object\":{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"ep1\",\"namespace\":\"ns1\",\"uid\":\"161b9670-4f11-412d-b8f8-3150c4827a99\",\"resourceVersion\":\"1022091052\",\"creationTimestamp\":\"2021-06-28T21:10:41Z\",\"labels\":{\"app\":\"app1\"},\"annotations\":{\"endpoints.kubernetes.io/last-change-trigger-time\":\"2022-09-14T20:00:20Z\"},\"managedFields\":[{\"manager\":\"kube-controller-manager\",\"operation\":\"Update\",\"apiVersion\":\"v1\",\"time\":\"2022-09-14T20:00:21Z\",\"fieldsType\":\"FieldsV1\",\"fieldsV1\":{\"f:metadata\":{\"f:annotations\":{\".\":{},\"f:endpoints.kubernetes.io/last-change-trigger-time\":{}},\"f:labels\":{\".\":{},\"f:app\":{},\"f:app.kubernetes.io/managed-by\":{},\"f:service.kubernetes.io/headless\":{}}},\"f:subsets\":{}}}]},\"subsets\":[{\"addresses\":[{\"ip\":\"10.0.0.101\",\"nodeName\":\"node1\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-47dtx\",\"uid\":\"db58349a-0ccc-4a3c-8782-5cea25163ef7\",\"resourceVersion\":\"1006548709\"}}],\"notReadyAddresses\":[{\"ip\":\"10.0.0.201\",\"nodeName\":\"node2\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-prscr\",\"uid\":\"4e65ed4e-0e36-421d-ae10-8481d12a4c88\",\"resourceVersion\":\"1022091046\"}}],\"ports\":[{\"name\":\"grpc\",\"port\":50051,\"protocol\":\"TCP\"},{\"name\":\"http\",\"port\":8080,\"protocol\":\"TCP\"}]}]}}",
          "{\"type\":\"MODIFIED\",\"object\":{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"ep1\",\"namespace\":\"ns1\",\"uid\":\"161b9670-4f11-412d-b8f8-3150c4827a99\",\"resourceVersion\":\"1022092107\",\"creationTimestamp\":\"2021-06-28T21:10:41Z\",\"labels\":{\"app\":\"app1\"},\"annotations\":{\"endpoints.kubernetes.io/last-change-trigger-time\":\"2022-09-14T20:00:54Z\"},\"managedFields\":[{\"manager\":\"kube-controller-manager\",\"operation\":\"Update\",\"apiVersion\":\"v1\",\"time\":\"2022-09-14T20:00:21Z\",\"fieldsType\":\"FieldsV1\",\"fieldsV1\":{\"f:metadata\":{\"f:annotations\":{\".\":{},\"f:endpoints.kubernetes.io/last-change-trigger-time\":{}},\"f:labels\":{\".\":{},\"f:app\":{},\"f:app.kubernetes.io/managed-by\":{},\"f:service.kubernetes.io/headless\":{}}},\"f:subsets\":{}}}]},\"subsets\":[{\"addresses\":[{\"ip\":\"10.0.0.101\",\"nodeName\":\"node1\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-47dtx\",\"uid\":\"db58349a-0ccc-4a3c-8782-5cea25163ef7\",\"resourceVersion\":\"1006548709\"}},{\"ip\":\"10.0.0.201\",\"nodeName\":\"node2\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"ns1\",\"name\":\"ns1-v20220908233031-app1-7945fdf498-prscr\",\"uid\":\"4e65ed4e-0e36-421d-ae10-8481d12a4c88\",\"resourceVersion\":\"1022092100\"}}],\"ports\":[{\"name\":\"grpc\",\"port\":50051,\"protocol\":\"TCP\"},{\"name\":\"http\",\"port\":8080,\"protocol\":\"TCP\"}]}]}}");

  private final DefaultJsonDeserializer sut = new DefaultJsonDeserializer();

  @Test
  void get_response() {
    Endpoints result = sut.deserializeEndpoints(mockGetResponse);

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
        result.toString());
  }

  @Test
  void watch_responses() {
    List<EndpointWatchEvent> deserialized =
        mockWatchResponses.stream().map(sut::deserializeEndpointEvent).collect(Collectors.toList());

    assertEquals(
        "EndpointWatchEvent(type=ADDED, object=Endpoints(apiVersion=v1, kind=Endpoints,"
            + " metadata=ObjectMeta(clusterName=null, creationTimestamp=2021-06-28T21:10:41Z,"
            + " deletionTimestamp=null, labels={app=app1}, name=ep1, resourceVersion=1022075955,"
            + " selfLink=null, uid=161b9670-4f11-412d-b8f8-3150c4827a99),"
            + " subsets=[EndpointSubset(addresses=[EndpointAddress(hostname=null, ip=10.0.0.101,"
            + " nodeName=node1, targetRef=ObjectReference(apiVersion=null, fieldPath=null,"
            + " kind=Pod, name=ns1-v20220908233031-app1-7945fdf498-47dtx, namespace=ns1,"
            + " resourceVersion=1006548709, uid=db58349a-0ccc-4a3c-8782-5cea25163ef7)),"
            + " EndpointAddress(hostname=null, ip=10.0.0.57, nodeName=node2,"
            + " targetRef=ObjectReference(apiVersion=null, fieldPath=null, kind=Pod,"
            + " name=ns1-v20220908233031-app1-7945fdf498-lqjbz, namespace=ns1,"
            + " resourceVersion=1022075951, uid=afbcb1f1-5e4b-4096-bf04-659d30fdf385))],"
            + " notReadyAddresses=null, ports=[EndpointPort(appProtocol=null, name=grpc,"
            + " port=50051, protocol=TCP), EndpointPort(appProtocol=null, name=http, port=8080,"
            + " protocol=TCP)])]))",
        deserialized.get(0).toString());
    assertEquals(
        "EndpointWatchEvent(type=MODIFIED, object=Endpoints(apiVersion=v1, kind=Endpoints,"
            + " metadata=ObjectMeta(clusterName=null, creationTimestamp=2021-06-28T21:10:41Z,"
            + " deletionTimestamp=null, labels={app=app1}, name=ep1, resourceVersion=1022090971,"
            + " selfLink=null, uid=161b9670-4f11-412d-b8f8-3150c4827a99),"
            + " subsets=[EndpointSubset(addresses=[EndpointAddress(hostname=null, ip=10.0.0.101,"
            + " nodeName=node1, targetRef=ObjectReference(apiVersion=null, fieldPath=null,"
            + " kind=Pod, name=ns1-v20220908233031-app1-7945fdf498-47dtx, namespace=ns1,"
            + " resourceVersion=1006548709, uid=db58349a-0ccc-4a3c-8782-5cea25163ef7))],"
            + " notReadyAddresses=null, ports=[EndpointPort(appProtocol=null, name=grpc,"
            + " port=50051, protocol=TCP), EndpointPort(appProtocol=null, name=http, port=8080,"
            + " protocol=TCP)])]))",
        deserialized.get(1).toString());
    assertEquals(
        "EndpointWatchEvent(type=MODIFIED, object=Endpoints(apiVersion=v1, kind=Endpoints,"
            + " metadata=ObjectMeta(clusterName=null, creationTimestamp=2021-06-28T21:10:41Z,"
            + " deletionTimestamp=null, labels={app=app1}, name=ep1, resourceVersion=1022091052,"
            + " selfLink=null, uid=161b9670-4f11-412d-b8f8-3150c4827a99),"
            + " subsets=[EndpointSubset(addresses=[EndpointAddress(hostname=null, ip=10.0.0.101,"
            + " nodeName=node1, targetRef=ObjectReference(apiVersion=null, fieldPath=null,"
            + " kind=Pod, name=ns1-v20220908233031-app1-7945fdf498-47dtx, namespace=ns1,"
            + " resourceVersion=1006548709, uid=db58349a-0ccc-4a3c-8782-5cea25163ef7))],"
            + " notReadyAddresses=[EndpointAddress(hostname=null, ip=10.0.0.201, nodeName=node2,"
            + " targetRef=ObjectReference(apiVersion=null, fieldPath=null, kind=Pod,"
            + " name=ns1-v20220908233031-app1-7945fdf498-prscr, namespace=ns1,"
            + " resourceVersion=1022091046, uid=4e65ed4e-0e36-421d-ae10-8481d12a4c88))],"
            + " ports=[EndpointPort(appProtocol=null, name=grpc, port=50051, protocol=TCP),"
            + " EndpointPort(appProtocol=null, name=http, port=8080, protocol=TCP)])]))",
        deserialized.get(2).toString());
    assertEquals(
        "EndpointWatchEvent(type=MODIFIED, object=Endpoints(apiVersion=v1, kind=Endpoints,"
            + " metadata=ObjectMeta(clusterName=null, creationTimestamp=2021-06-28T21:10:41Z,"
            + " deletionTimestamp=null, labels={app=app1}, name=ep1, resourceVersion=1022092107,"
            + " selfLink=null, uid=161b9670-4f11-412d-b8f8-3150c4827a99),"
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
            + " protocol=TCP)])]))",
        deserialized.get(3).toString());
  }
}
