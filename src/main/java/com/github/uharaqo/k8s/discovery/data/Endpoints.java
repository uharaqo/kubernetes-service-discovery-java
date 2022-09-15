package com.github.uharaqo.k8s.discovery.data;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @see <a
 *     href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#endpoints-v1-core">API
 *     spec</a>
 */
@Data
@NoArgsConstructor
public class Endpoints {

  private String apiVersion;
  private String kind;
  private ObjectMeta metadata;
  private List<EndpointSubset> subsets;
}
