package com.github.uharaqo.k8s.discovery.data;

import java.util.List;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * @see <a
 *     href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#endpoints-v1-core">API
 *     spec</a>
 */
@Data
@NoArgsConstructor
@Generated
public class Endpoints {

  private String apiVersion;
  private String kind;
  private ObjectMeta metadata;

  /**
   * The set of all endpoints is the union of all subsets. Addresses are placed into subsets
   * according to the IPs they share. A single address with multiple ports, some of which are ready
   * and some of which are not (because they come from different containers) will result in the
   * address being displayed in different subsets for the different ports. No address will appear in
   * both Addresses and NotReadyAddresses in the same subset. Sets of addresses and ports that
   * comprise a service.
   */
  private List<EndpointSubset> subsets;
}
