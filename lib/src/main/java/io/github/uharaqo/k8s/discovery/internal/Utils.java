package io.github.uharaqo.k8s.discovery.internal;

import static io.github.uharaqo.k8s.discovery.ServiceDiscoveryException.ErrorCause.SETUP;

import io.github.uharaqo.k8s.discovery.ServiceDiscoveryException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Utils {

  public static Path toPath(String path) {
    try {
      Path p = Paths.get(path);
      if (Files.isReadable(p)) {
        return p;
      }
      throw new IllegalArgumentException("File is not readable");
    } catch (Exception e) {
      throw new ServiceDiscoveryException(SETUP, "Failed to open CA cert file: " + path, null);
    }
  }
}
