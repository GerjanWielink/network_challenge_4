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
        // Note: the first byte is already extracted into a primitive array by the rootMask
        masks = new ArrayList<>(Arrays.asList(
                new Mask(16, 0xFF, 20), // 0000 0000 1111 1111 0000 .... 0000
                new Mask(12, 0xF, 32) // 0000 0000 0000 0000 1100 0000 .... 0000
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

    /**
     * Helper to format ip to human readable form.
     * @param ip ip in decimal form
     * @return human readable ip string.
     */
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

class RouteSet {
    private HashMap<Integer, RouteSet> children = new HashMap<>();
    private RouteSet parent;
    private List<Route> routes = new ArrayList<>();
    private List<Mask> childMasks;
    private Mask keyMask;

    private RouteSet(RouteSet parent, List<Mask> keyMasks) {
        this.parent = parent;
        this.keyMask = keyMasks.get(0);
        this.childMasks = new ArrayList<>(keyMasks);
        this.childMasks.remove(0);
    }

    /**
     * Constructor for the root level RouteSet
     */
    RouteSet(List<Mask> keyMasks) {
        this(null, keyMasks);
    }

    /**
     * Add a route to the RouteSet and have it cascade down into it's children recursively until the last mask is reached.
     * @param route Route object
     */
    void addRoute(Route route) {
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

    /**
     * Looks into the tree of RouteSets for the longest prefix match
     * @param ip Ip to match
     * @return destination port.
     */
    Integer getPortForIp(int ip) {
        int maskedKey = this.keyMask.extractByte(ip);

        // If we have a more specific child set we look into that
        if (children.containsKey(maskedKey)) {
            return children.get(maskedKey).getPortForIp(ip);
        }

        // If we are at maximum depth we start looking backwards for the best match
        return this.searchMatchUpwards(ip);
    }

    /**
     * Look into your own routes for a match. If none are present look into the routes of your parent
     * @param ip destination Ip
     * @return Destination port.
     */
    private int searchMatchUpwards(int ip) {
        // check if the parent has a match for us
        int bestPort = findBestMatch(this.routes, ip);

        // if we have found a node or we are the root set we return the best port at that point.
        if (bestPort != -1 || this.parent == null) {
            return bestPort;
        }

        // have the parent look into it's own parent if no match is found.
        return this.parent.searchMatchUpwards(ip);
    }


    /**
     * Find the first (thus best is sorted) match for an ip in a list of routes
     * @param routes routes to match to.
     * @param ip destnation ip.
     * @return destination port. -1 if none is found.
     */
    private int findBestMatch(List<Route> routes, int ip) {
        for(Route route : routes) {
            if (route.matches(ip)) {
                return  route.getPort();
            }
        }
        return -1;
    }

    /**
     * Recursively sort the routes in the set and those of it's children.
     */
    void sortTree() {
        Collections.sort(this.routes);

        children.keySet().forEach(key -> {
            children.get(key).sortTree();
        });
    }
}

/**
 * Helper class to extract identifying bits.
 */
class Mask {
    private int offset;
    private int mask;
    private int maxPrefix;

    Mask(int offset, int mask, int maxPrefix) {
        this.mask = mask;
        this.offset = offset;
        this.maxPrefix = maxPrefix;
    }

    int extractByte(int ip) {
        return (ip >> offset) & mask;
    }

    int getMaxPrefix() {
        return this.maxPrefix;
    }

    @Override
    public String toString() {
        return this.offset + ";" + Integer.toHexString(mask);
    }
}

/**
 * Route helper class to properly store route, prefix, port
 */
class Route implements Comparable<Route> {
    private final int relevantPartOfIp;
    private final int ip;
    private final byte prefix;
    private final int port;

    Route(int ip, byte prefix, int port) {
        this.ip = ip;
        this.relevantPartOfIp = ip >> (32 - prefix);
        this.prefix = prefix;
        this.port = port;
    }

    byte getPrefix() {
        return prefix;
    }

    int getPort() {
        return port;
    }

    int getIp() {
        return this.ip;
    }

    boolean matches (int otherIp) {
        return (otherIp >> (32 - this.prefix)) == relevantPartOfIp;
    }

    @Override
    public int compareTo(Route route) {
        return route.getPrefix() - this.prefix;
    }
}
