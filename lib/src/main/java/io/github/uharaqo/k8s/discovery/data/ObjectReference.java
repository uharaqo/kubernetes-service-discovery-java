package io.github.uharaqo.k8s.discovery.data;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * @see <a
 *     href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#objectreference-v1-core">API
 *     spec</a>
 */
@Data
@NoArgsConstructor
@Generated
public class ObjectReference {

  private String apiVersion;
  private String fieldPath;
  private String kind;
  private String name;
  private String namespace;
  private String resourceVersion;
  private String uid;
}
