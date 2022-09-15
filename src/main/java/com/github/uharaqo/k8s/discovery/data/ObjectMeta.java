package com.github.uharaqo.k8s.discovery.data;

import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @see <a href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#objectmeta-v1-meta">API spec</a>
 */
@Data
@NoArgsConstructor
public class ObjectMeta {

  private String clusterName;
  private String creationTimestamp;
  private String deletionTimestamp;
  private Map<String, String> labels;
  private String name;
  private String resourceVersion;
  private String selfLink;
  private String uid;
}
