package com.github.uharaqo.k8s.discovery.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.jupiter.api.Test;

class EndpointExtractorTest {

  @Test
  void extract() throws UnknownHostException {
    EndpointPort port1 = new EndpointPort();
    port1.setPort(1111);
    EndpointPort port2 = new EndpointPort();
    port2.setPort(2222);

    EndpointAddress addr1 = new EndpointAddress();
    addr1.setHostname("localhost");
    addr1.setIp("11.11.11.99");
    addr1.setNodeName("node1");
    EndpointAddress addr2 = new EndpointAddress();
    addr2.setHostname("localhost");
    addr2.setIp("11.11.11.11");
    addr2.setNodeName("node2");

    EndpointSubset ss1 = new EndpointSubset();
    ss1.setPorts(List.of(port1));
    ss1.setAddresses(List.of(addr1, addr2));
    EndpointSubset ss2 = new EndpointSubset();
    ss2.setPorts(List.of(port1, port2));
    ss2.setAddresses(List.of(addr1, addr2));
    List<EndpointSubset> subsets = List.of(ss1, ss2);

    Endpoints eps = new Endpoints();
    eps.setSubsets(subsets);

    List<InetAddress> results =
        EndpointExtractor.getAddressesForPort(eps, p -> p.getPort() == 2222);

    assertEquals(
        List.of(InetAddress.getByName(addr2.getIp()), InetAddress.getByName(addr1.getIp())),
        results);
  }
}
