kubernetes-service-discovery-java
=================================

[![coverage](https://raw.githubusercontent.com/uharaqo/kubernetes-service-discovery-java/badges/jacoco.svg)](https://github.com/uharaqo/kubernetes-service-discovery-java/actions/workflows/build.yml)
[![license](https://img.shields.io/badge/license-Apache%202-blue")](./LICENSE)

Calls [the Kubernetes Endpoints API](https://kubernetes.io/docs/reference/kubernetes-api/service-resources/endpoints-v1/)
to get or watch endpoints for a specified name.

JSON-B binding should be provided. For example, include [yasson](https://projects.eclipse.org/projects/ee4j.yasson)
in your classpath which is an official reference implementation of JSON-B.
