package com.github.uharaqo.k8s.discovery.data;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @see <a
 *     href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#endpointsubset-v1-core">API
 *     spec</a>
 */
@Data
@NoArgsConstructor
public class EndpointSubset {

  private List<EndpointAddress> addresses;
  private List<EndpointAddress> notReadyAddresses;
  private List<EndpointPort> ports;
}
