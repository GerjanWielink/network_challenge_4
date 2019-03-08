// LEGACY (AND GOOD) VERSION

//package lpm;
//
//import java.util.*;
//
//public class LongestPrefixMatcher {
//    private List<Route>[][] routes = new ArrayList[256][257];
//    private Integer[] firstBytes = new Integer[256];
//    private int routesRead = 0;
//
//    /**
//     * You can use this function to initialize variables.
//     */
//    public LongestPrefixMatcher() {
//        for(int i = 0; i < 256; i++) {
//            routes[i][256] = new ArrayList<>();
//        }
//    }
//
//    /**
//     * Looks up an IP address in the routing tables
//     * @param ip The IP address to be looked up in integer representation
//     * @return The port number this IP maps to
//     */
//    public int lookup(int ip) {
//        int firstByteOfIp = (ip >> 24) + 128;
//        int secondByteOfIp = (ip >> 16) & 0xFF;
//
//        if (firstBytes[firstByteOfIp] == null) {
//            return -1;
//        }
//
//        int bestPort;
//
//        if(routes[firstByteOfIp][secondByteOfIp] != null) {
//            bestPort = findBestMatch(routes[firstByteOfIp][secondByteOfIp], ip);
//
//            if (bestPort != -1) {
//                return bestPort;
//            }
//        }
//
//        return findBestMatch(routes[firstByteOfIp][256], ip);
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
//        int firstByte = (ip >> 24) + 128;
//        int secondByte = (ip >> 16) & 0xFF;
//
//        firstBytes[firstByte] = firstByte;
//
//        Route currentRoute = new Route(ip, prefixLength, portNumber);
//
//        if(routes[firstByte][secondByte] == null) {
//            routes[firstByte][secondByte] = new ArrayList<>();
//        }
//
//        routes[firstByte][secondByte].add(currentRoute);
//        if(currentRoute.getPrefix() <= 16) {
//            routes[firstByte][256].add(currentRoute);
//        }
//
//        routesRead++;
//
//        if(routesRead == 420972) {
//            for(List<Route>[] routesList: routes) {
//                for(List<Route> routesListList: routesList) {
//                    if(routesListList == null) {
//                        continue;
//                    }
//                    Collections.sort(routesListList);
//                }
//            }
//        }
//    }
//}
//
///**
// * Route helper class to properly store route, prefix, port
// */
//class Route implements Comparable {
//    private final int releventPartOfIp;
//    private final int ip;
//    private final byte prefix;
//    private final int port;
//
//    public Route(int ip, byte prefix, int port) {
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


// NEW BAD VERSION!

package lpm;

import java.util.*;

public class LongestPrefixMatcher {
    private int routesRead = 0;
    private RouteSet[] routeSets = new RouteSet[256];
    private Mask rootMask = new Mask(24, 0xFF, 32);
    private List<Mask> masks;

    /**
     * You can use this function to initialize variables.
     */
    public LongestPrefixMatcher() {
        masks = new ArrayList<>(Arrays.asList(
                new Mask(16, 0xFF, 20),
                new Mask(12, 0xF, 32)
        ));
    }

    /**
     * Looks up an IP address in the routing tables
     * @param ip The IP address to be looked up in integer representation
     * @return The port number this IP maps to
     */
    public int lookup(int ip) {
//        System.out.println(ipToHuman(ip));
        int firstByte = rootMask.extractByte(ip);
        if(routeSets[firstByte] != null) {
            return routeSets[firstByte].getPortForIp(ip);
        }

        return -1;
    }

    private String ipToHuman(int ip) {
        return (ip >> 24 & 0xff) + "." +
                (ip >> 16 & 0xff) + "." +
                (ip >> 8 & 0xff) + "." +
                (ip & 0xff);
    }

    /**
     * Adds a route to the routing tables
     * @param ip The IP the block starts at in integer representation
     * @param prefixLength The number of bits indicating the network part
     *                     of the address range (notation ip/prefixLength)
     * @param portNumber The port number the IP block should route to
     */
    public void addRoute(int ip, byte prefixLength, int portNumber) {
        int firstByte = rootMask.extractByte(ip);

        if(routeSets[firstByte] == null) {
            routeSets[firstByte] = new RouteSet(masks);
        }

        routeSets[firstByte].addRoute(new Route(ip, prefixLength, portNumber));
        routesRead++;

        if(routesRead == 420972) {
            for(RouteSet routeSet: routeSets){
                if (routeSet != null) {
                    routeSet.sortTree();
                }
            }
        }
    }
}

/**
 * Route helper class to properly store route, prefix, port
 */
class Route implements Comparable {
    private final int relevantPartOfIp;
    private final int ip;
    private final byte prefix;
    private final int port;

    public Route(int ip, byte prefix, int port) {
        this.ip = ip;
        this.relevantPartOfIp = ip >> (32 - prefix);
        this.prefix = prefix;
        this.port = port;
    }

    public byte getPrefix() {
        return prefix;
    }

    public int getPort() {
        return port;
    }

    public int getIp() {
        return this.ip;
    }

    public boolean matches (int otherIp) {
        return (otherIp >> (32 - this.prefix)) == relevantPartOfIp;
    }

    @Override
    public int compareTo(Object route) {
        Route otherRoute = (Route) route;

        return otherRoute.getPrefix() - this.prefix;
    }
}

class RouteSet {
    private HashMap<Integer, RouteSet> children = new HashMap<>();
    private RouteSet parent;
    private List<Route> routes = new ArrayList<>();
    private List<Mask> childMasks;
    private Mask keyMask;

    public RouteSet(RouteSet parent, List<Mask> keyMasks) {
        this.parent = parent;
        this.keyMask = keyMasks.get(0);
        this.childMasks = new ArrayList<>(keyMasks);
        this.childMasks.remove(0);
    }

    public RouteSet(List<Mask> keyMasks) {
        this(null, keyMasks);
    }

    public void addRoute(Route route) {
        int maskedKey = this.keyMask.extractByte(route.getIp());

        if (this.keyMask.getMaxPrefix() >= route.getPrefix()) {
            this.routes.add(route);
        }

        if(childMasks.size() > 0) {
            if (!children.containsKey(maskedKey)) {
                children.put(maskedKey, new RouteSet(this, this.childMasks));
            }

            children.get(maskedKey).addRoute(route);
        }
    }

    public List<Route> getRoutes() {
        return this.routes;
    }

    public Integer getPortForIp(int ip) {
        int maskedKey = this.keyMask.extractByte(ip);

        if (children.containsKey(maskedKey)) {
            return children.get(maskedKey).getPortForIp(ip);
        }

        int bestPort;

        bestPort = findBestMatch(this.routes, ip);


        if (bestPort != -1 || this.parent == null) {
            return bestPort;
        }

        return this.parent.searchMatchUpwards(ip);
    }

    private int searchMatchUpwards(int ip) {
        // check if the parent has a match for us
        int bestPort = findBestMatch(this.routes, ip);

        if (bestPort != -1 || this.parent == null) {
            return bestPort;
        }

        // have the parent look into it's own parent if no match is found.
        return this.parent.searchMatchUpwards(ip);
    }


    private int findBestMatch(List<Route> routes, int ip) {
        for(Route route : routes) {
            if (route.matches(ip)) {
                return  route.getPort();
            }
        }
        return -1;
    }

    public void sortTree() {
        Collections.sort(this.routes);

        children.keySet().forEach(key -> {
            children.get(key).sortTree();
        });
    }
}

class Mask {
    private int offset;
    private int mask;
    private int maxPrefix;

    public Mask(int offset, int mask, int maxPrefix) {
        this.mask = mask;
        this.offset = offset;
        this.maxPrefix = maxPrefix;
    }

    public int extractByte(int ip) {
        return (ip >> offset) & mask;
    }

    public int getMaxPrefix() {
        return this.maxPrefix;
    }

    @Override
    public String toString() {
        return this.offset + ";" + Integer.toHexString(mask);
    }
}
