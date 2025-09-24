package ipv4;

import model.IPv4Subnet;

import java.util.ArrayList;
import java.util.List;

public class IPv4Subnetter {

    // Divide la red contenedora (ip + máscara) en 'n' subredes iguales
    public static List<IPv4Subnet> splitBySubnets(String ipStr, String maskOrCidr, int n) {
        if (n <= 0) throw new IllegalArgumentException("El número de subredes debe ser > 0");

        // Red base
        int ip = IPv4Utils.dottedToInt(ipStr);
        int mask = maskFrom(maskOrCidr);
        IPv4Utils.validateContiguousMask(mask);
        int basePrefix = Integer.bitCount(mask);
        int baseNetwork = ip & mask;

        // bits extra necesarios
        int s = ceilLog2(n);
        if (basePrefix + s > 32) throw new IllegalArgumentException("No caben " + n + " subredes dentro de esta red.");
        int newPrefix = basePrefix + s;

        return generateSubnets(baseNetwork, basePrefix, newPrefix, n);
    }

    // Elige el prefijo mínimo para soportar 'hostsRequired' por subred (IPv4 tradicional: -2)
    public static List<IPv4Subnet> splitByHostsPerSubnet(String ipStr, String maskOrCidr, long hostsRequired) {
        if (hostsRequired < 1) throw new IllegalArgumentException("Hosts requeridos debe ser >= 1");

        int ip = IPv4Utils.dottedToInt(ipStr);
        int mask = maskFrom(maskOrCidr);
        IPv4Utils.validateContiguousMask(mask);
        int basePrefix = Integer.bitCount(mask);
        int baseNetwork = ip & mask;

        // calcular hostBits con la fórmula: 2^h - 2 >= hostsRequired  -> h >= ceil(log2(hosts+2))
        int hostBits = ceilLog2(Math.addExact(hostsRequired, 2L));
        // No permitir /31 ni /32 en cálculo clásico
        if (hostBits > 30) throw new IllegalArgumentException("Demasiados pocos hosts para formar una subred útil.");
        int newPrefix = 32 - hostBits;

        if (newPrefix < basePrefix)
            throw new IllegalArgumentException("La subred requerida es más grande que la red base.");

        long subnetsInside = 1L << (newPrefix - basePrefix);
        if (subnetsInside > Integer.MAX_VALUE) subnetsInside = Integer.MAX_VALUE;

        return generateSubnets(baseNetwork, basePrefix, newPrefix, (int) subnetsInside);
    }

    // ----------------- helpers -----------------
    private static int maskFrom(String maskOrCidr) {
        if (maskOrCidr.trim().startsWith("/")) {
            int cidr = Integer.parseInt(maskOrCidr.trim().substring(1));
            return IPv4Utils.cidrToMask(cidr);
        }
        return IPv4Utils.dottedToInt(maskOrCidr);
    }

    private static int ceilLog2(long x) {
        if (x <= 1) return 0;
        int p = 64 - Long.numberOfLeadingZeros(x - 1);
        return p;
    }

    private static List<IPv4Subnet> generateSubnets(int baseNetwork, int basePrefix, int newPrefix, int desiredCount) {
        List<IPv4Subnet> out = new ArrayList<>();
        int blockSize = (newPrefix == 32) ? 1 : (1 << (32 - newPrefix));
        long maxSubnets = 1L << (newPrefix - basePrefix);
        int count = (int) Math.min(desiredCount, maxSubnets);

        for (int i = 0; i < count; i++) {
            int net = baseNetwork + i * blockSize;
            int bcast = (newPrefix == 32) ? net : (net + blockSize - 1);

            String firstHost, lastHost;
            long usable;
            if (newPrefix == 32) {
                firstHost = IPv4Utils.intToDotted(net);
                lastHost  = firstHost;
                usable = 1;
            } else if (newPrefix == 31) {
                firstHost = IPv4Utils.intToDotted(net);
                lastHost  = IPv4Utils.intToDotted(bcast);
                usable = 2;
            } else {
                firstHost = IPv4Utils.intToDotted(net + 1);
                lastHost  = IPv4Utils.intToDotted(bcast - 1);
                usable = Math.max(0, (1L << (32 - newPrefix)) - 2);
            }

            out.add(new IPv4Subnet(
                    i + 1,
                    IPv4Utils.intToDotted(net) + "/" + newPrefix,
                    IPv4Utils.intToDotted(net),
                    IPv4Utils.intToDotted(bcast),
                    firstHost,
                    lastHost,
                    usable
            ));
        }
        return out;
    }
}
