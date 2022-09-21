/*
 *    Copyright [2018] [The authors]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.uharaqo.k8s.discovery;

import io.github.uharaqo.k8s.discovery.data.EndpointWatchEvent;
import io.github.uharaqo.k8s.discovery.data.Endpoints;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import javax.annotation.Nonnull;

/**
 * Makes asynchronous calls to Kubernetes API server
 *
 * @see <a
 *     href="https://kubernetes.io/docs/reference/kubernetes-api/service-resources/endpoints-v1/#get-read-the-specified-endpoints">the
 *     official document:</a>
 */
public interface ServiceDiscoveryHttpHandler extends AutoCloseable {

  @Nonnull
  CompletableFuture<Endpoints> getEndpoints(HttpRequest request);

  @Nonnull
  Publisher<EndpointWatchEvent> watchEndpoints(HttpRequest request);
}
