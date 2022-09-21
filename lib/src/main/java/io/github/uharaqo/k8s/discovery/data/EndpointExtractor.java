package io.github.uharaqo.k8s.discovery.data;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EndpointExtractor {

  private static final Comparator<InetAddress> addrComparator = new InetAddrComparator();

  /**
   * @see #getAddressesForPort(Endpoints, Predicate, Predicate)
   */
  public static List<InetAddress> getAddressesForPort(
      Endpoints endpoints, Predicate<EndpointPort> port) {
    return getAddressesForPort(endpoints, port, x -> true);
  }

  /**
   * Extract and group IP addresses (v4 and/or v6) by each port. The list is already sorted and
   * de-duplicated. Invalid port and addresses get filtered out.
   *
   * @param endpoints endpoints fetched from the Kubernetes endpoint API
   * @param port filtering condition
   * @param address filtering condition
   * @return sorted and de-duplicated list of {@link InetAddress} for each port
   */
  public static List<InetAddress> getAddressesForPort(
      Endpoints endpoints, Predicate<EndpointPort> port, Predicate<EndpointAddress> address) {

    return endpoints.getSubsets().stream()
        .filter(ep -> ep.getPorts().stream().anyMatch(port))
        .flatMap(
            ep ->
                ep.getAddresses().stream()
                    .filter(address)
                    .map(EndpointAddress::getIp)
                    .map(EndpointExtractor::toInetAddress)
                    .filter(Objects::nonNull))
        .sorted(addrComparator)
        .distinct()
        .collect(Collectors.toUnmodifiableList());
  }

  private static InetAddress toInetAddress(String address) {
    try {
      return address == null ? null : InetAddress.getByName(address);
    } catch (Exception ignored) {
      return null;
    }
  }

  /**
   * {@link TreeSet} implementation for {@link InetAddress}.
   *
   * <ul>
   *   <li>{@link Inet4Address} < {@link Inet6Address}
   *   <li>null < {@link InetAddress}
   *   <li>if they're the same class, use {@link InetAddress#hashCode()} for comparison
   * </ul>
   */
  private static class InetAddrComparator implements Comparator<InetAddress> {

    @Override
    public int compare(InetAddress o1, InetAddress o2) {
      // check nullity: null < {addr}
      if (o1 == null) {
        return o2 == null ? 0 : -1;
      }
      if (o2 == null) {
        return 1; // o1 is not null
      }

      // v4 < v6
      if (o1 instanceof Inet4Address) {
        return o2 instanceof Inet4Address
            ? Integer.compare(o1.hashCode(), o2.hashCode()) // v4 vs v4
            : -1; // v4 < v6
      }
      if (o2 instanceof Inet4Address) {
        return 1; // v6 > v4
      }
      return Integer.compare(o1.hashCode(), o2.hashCode()); // v6 vs v6
    }
  }
}
