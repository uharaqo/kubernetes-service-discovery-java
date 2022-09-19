package com.github.uharaqo.k8s.discovery.data;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * @see <a
 *     href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#endpointaddress-v1-core">API
 *     spec</a>
 */
@Data
@NoArgsConstructor
@Generated
public class EndpointAddress {

  private String hostname;
  private String ip;
  private String nodeName;
  private ObjectReference targetRef;
}
