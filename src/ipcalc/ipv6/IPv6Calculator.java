package ipv6;

import model.IPv6Result;

import java.math.BigInteger;
import java.net.Inet6Address;

public class IPv6Calculator {

    public static IPv6Result calculate(String input) {
        String[] parts = input.split("/");
        if (parts.length != 2) throw new IllegalArgumentException("Usa IPv6/prefijo, ej: 2001:db8::1/64");

        String ipStr = parts[0].trim();
        int prefix;
        try { prefix = Integer.parseInt(parts[1].trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Prefijo inválido"); }
        if (prefix < 0 || prefix > 128) throw new IllegalArgumentException("Prefijo fuera de rango (0..128)");

        Inet6Address addr = IPv6Utils.parseIPv6(ipStr);
        BigInteger ipNum = IPv6Utils.toBigInt(addr.getAddress());
        BigInteger mask = IPv6Utils.prefixMask(prefix);
        BigInteger network = ipNum.and(mask);
        BigInteger total = BigInteger.ONE.shiftLeft(128 - prefix);

        String type;
        if (addr.isMulticastAddress()) type = "Multicast (FF00::/8)";
        else if (addr.isLinkLocalAddress()) type = "Link-local (FE80::/10)";
        else if (addr.isSiteLocalAddress()) type = "Site-local (deprecated)";
        else type = "Global Unicast";

        return new IPv6Result(
                ipStr,
                prefix,
                IPv6Utils.fromBigInt(network),
                total,
                type
        );
    }
}
