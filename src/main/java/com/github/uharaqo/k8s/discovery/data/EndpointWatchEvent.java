package com.github.uharaqo.k8s.discovery.data;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @see <a
 * href="https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#watchevent-v1-meta">API
 * spec</a>
 */
@Data
@NoArgsConstructor
public class EndpointWatchEvent {

  /**
   * Type of the event
   *
   * @see {@link Type}
   */
  private Type type;
  /**
   * Same as the responses for the GET endpoint API.
   */
  private Endpoints object;

  /**
   * Type of the event. Though 5 types are defined in <a
   * href="https://github.com/kubernetes-client/java/blob/master/util/src/main/java/io/kubernetes/client/informer/EventType.java">the
   * official client</a>, the API only returns the first two types for the endpoint API.
   */
  public enum Type {
    /**
     * Returned when connected. {@link #object} is the same as the response for the GET endpoint
     * API.
     */
    ADDED,
    /**
     * Returned when there's a change including an addition or removal of a pod.
     */
    MODIFIED,
    /**
     * Hasn't been observed for the endpoint API.
     */
    DELETED,
    /**
     * Hasn't been observed for the endpoint API.
     */
    BOOKMARK,
    /**
     * Hasn't been observed for the endpoint API.
     */
    ERROR,
    ;
  }
}
