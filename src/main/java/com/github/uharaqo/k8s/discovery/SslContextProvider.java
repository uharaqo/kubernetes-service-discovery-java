package com.github.uharaqo.k8s.discovery;

import javax.net.ssl.SSLContext;

public interface SslContextProvider {

  SSLContext create() throws Exception;
}
