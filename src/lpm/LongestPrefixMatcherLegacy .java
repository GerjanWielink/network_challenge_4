//package lpm;
//
//import java.util.*;
//
//public class LongestPrefixMatcher {
//    private List<Route>[][] routes = new ArrayList[256][257];
//    private Integer[] firstBytes = new Integer[256];
//    private int routesRead = 0;
//    private RouteSet routeSet;
//
//  /**
//   * You can use this function to initialize variables.
//   */
//    public LongestPrefixMatcher() {
//        for(int i = 0; i < 256; i++) {
//            routes[i][256] = new ArrayList<>();
//        }
//
//        List<Integer> masks = new ArrayList<>(Arrays.asList(0xf, 0xf0));
//        this.routeSet = new RouteSet(masks);
//    }
//
//    /**
//     * Looks up an IP address in the routing tables
//     * @param ip The IP address to be looked up in integer representation
//     * @return The port number this IP maps to
//     */
//    public int lookup(int ip) {
//       int firstByteOfIp = (ip >> 24) + 128;
//       int secondByteOfIp = (ip >> 16) & 0xFF;
//
//       if (firstBytes[firstByteOfIp] == null) {
//           return -1;
//       }
//
//       int bestPort;
//
//       if(routes[firstByteOfIp][secondByteOfIp] != null) {
//           bestPort = findBestMatch(routes[firstByteOfIp][secondByteOfIp], ip);
//
//           if (bestPort != -1) {
//               return bestPort;
//           }
//       }
//
//       return findBestMatch(routes[firstByteOfIp][256], ip);
//    }
//
//    private int findBestMatch(List<Route> routes, int ip) {
//        for(Route route : routes) {
//            if (route.matches(ip)) {
//                return  route.getPort();
//            }
//        }
//
//        return -1;
//    }
//
//    /**
//     * Adds a route to the routing tables
//     * @param ip The IP the block starts at in integer representation
//     * @param prefixLength The number of bits indicating the network part
//     *                     of the address range (notation ip/prefixLength)
//     * @param portNumber The port number the IP block should route to
//     */
//    public void addRoute(int ip, byte prefixLength, int portNumber) {
//       int firstByte = (ip >> 24) + 128;
//       int secondByte = (ip >> 16) & 0xFF;
//
//       firstBytes[firstByte] = firstByte;
//
//       Route currentRoute = new Route(ip, prefixLength, portNumber);
//
//       if(routes[firstByte][secondByte] == null) {
//           routes[firstByte][secondByte] = new ArrayList<>();
//       }
//
//       routes[firstByte][secondByte].add(currentRoute);
//       routes[firstByte][256].add(currentRoute);
//
//       routesRead++;
//
//       if(routesRead == 420972) {
//           for(List<Route>[] routesList: routes) {
//               for(List<Route> routesListList: routesList) {
//                   if(routesListList == null) {
//                       continue;
//                   }
//                   Collections.sort(routesListList);
//               }
//           }
//       }
//    }
//}
//
///**
// * Route helper class to properly store route, prefix, port
// */
//class Roete implements Comparable {
//    private final int releventPartOfIp;
//    private final int ip;
//    private final byte prefix;
//    private final int port;
//
//    public Roete(int ip, byte prefix, int port) {
//        this.ip = ip;
//        this.releventPartOfIp = ip >> (32 - prefix);
//        this.prefix = prefix;
//        this.port = port;
//    }
//
//    public byte getPrefix() {
//        return prefix;
//    }
//
//    public int getPort() {
//        return port;
//    }
//
//    public int getIp() {
//        return this.ip;
//    }
//
//    public boolean matches (int otherIp) {
//        return (otherIp >> (32 - this.prefix)) == releventPartOfIp;
//    }
//
//    @Override
//    public int compareTo(Object route) {
//        Route otherRoute = (Route) route;
//
//        return otherRoute.getPrefix() - this.prefix;
//    }
//}