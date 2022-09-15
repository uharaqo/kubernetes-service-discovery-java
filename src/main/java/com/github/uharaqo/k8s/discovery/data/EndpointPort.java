package com.github.uharaqo.k8s.discovery.data;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @see <a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#endpointport-v1-core">API spec</a>
 */
@Data
@NoArgsConstructor
public class EndpointPort {

  private String appProtocol;
  private String name;
  private int port;
  private String protocol;
}
