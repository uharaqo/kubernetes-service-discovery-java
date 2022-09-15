package com.github.uharaqo.k8s.discovery;

import javax.annotation.Nonnull;
import lombok.Value;

@Value
public class KubernetesApiClientRequest {
  @Nonnull public String namespace;
  @Nonnull public String endpoint;
}
