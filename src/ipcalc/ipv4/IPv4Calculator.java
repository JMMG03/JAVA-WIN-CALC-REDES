package ipv4;


import model.IPv4Result;

public class IPv4Calculator {

    public static IPv4Result calculate(String ipStr, String maskOrCidr) {
        int ip = IPv4Utils.dottedToInt(ipStr);

        int mask;
        if (maskOrCidr.startsWith("/")) {
            int cidr = Integer.parseInt(maskOrCidr.substring(1).trim());
            mask = IPv4Utils.cidrToMask(cidr);
        } else {
            mask = IPv4Utils.dottedToInt(maskOrCidr);
        }
        IPv4Utils.validateContiguousMask(mask);
        int cidr = Integer.bitCount(mask);

        int network = ip & mask;
        int broadcast = network | ~mask;

        String firstHost = null, lastHost = null;
        long usable;
        if (cidr == 32) {
            usable = 1; firstHost = IPv4Utils.intToDotted(network); lastHost = firstHost;
        } else if (cidr == 31) {
            usable = 2; firstHost = IPv4Utils.intToDotted(network); lastHost = IPv4Utils.intToDotted(broadcast);
        } else {
            long total = 1L << (32 - cidr);
            usable = Math.max(0, total - 2);
            firstHost = IPv4Utils.intToDotted(network + 1);
            lastHost  = IPv4Utils.intToDotted(broadcast - 1);
        }

        String wildcard = IPv4Utils.intToDotted(~mask);
        String ipClass  = IPv4Utils.classOf(ip);
        boolean priv    = IPv4Utils.isPrivate(ip);

        return new IPv4Result(
                ipStr,
                IPv4Utils.intToDotted(mask),
                cidr,
                wildcard,
                IPv4Utils.intToDotted(network),
                IPv4Utils.intToDotted(broadcast),
                firstHost,
                lastHost,
                usable,
                ipClass,
                priv
        );
    }
}
