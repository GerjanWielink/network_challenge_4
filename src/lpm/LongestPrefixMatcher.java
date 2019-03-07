package lpm;

import java.util.*;

public class LongestPrefixMatcher {
    private int routesRead = 0;
    private RouteSet routeSet;

    /**
     * You can use this function to initialize variables.
     */
    public LongestPrefixMatcher() {
        List<Mask> masks = new ArrayList<>(Arrays.asList(
                new Mask(24, 0xFF),
                new Mask(20, 0xF),
                new Mask(16, 0xF),
                new Mask(12, 0xF),
                new Mask(8, 0xF)
        ));

        this.routeSet = new RouteSet(masks);
    }

    /**
     * Looks up an IP address in the routing tables
     * @param ip The IP address to be looked up in integer representation
     * @return The port number this IP maps to
     */
    public int lookup(int ip) {
        return this.routeSet.getPortForIp(ip);
    }

    /**
     * Adds a route to the routing tables
     * @param ip The IP the block starts at in integer representation
     * @param prefixLength The number of bits indicating the network part
     *                     of the address range (notation ip/prefixLength)
     * @param portNumber The port number the IP block should route to
     */
    public void addRoute(int ip, byte prefixLength, int portNumber) {
        this.routeSet.addRoute(new Route(ip, prefixLength, portNumber));
        routesRead++;

        if(routesRead == 420972) {
            this.routeSet.sortTree();
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

        this.routes.add(route);

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

        // TODO: Only looks back one level now, could not be enough...
        return searchMatchUpwards(ip);
    }

    private int searchMatchUpwards(int ip) {
        if (this.parent == null || this.parent.last()) {
            return -1;
        }

        int bestPort = findBestMatch(this.parent.getRoutes(), ip);

        if (bestPort != -1) {
            return bestPort;
        }

        return this.parent.searchMatchUpwards(ip);
    }

    public boolean last() {
        return this.parent == null;
    }

    private int findBestMatch(List<Route> routes, int ip) {
        System.out.println("Best match lookup of size: " + routes.size());

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

    public Mask(int offset, int mask) {
        this.mask = mask;
        this.offset = offset;
    }

    public int extractByte(int ip) {
        return (ip >> offset) & mask;
    }
}
