package ipv6;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Arrays;

public class IPv6Utils {

    public static Inet6Address parseIPv6(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            if (!(addr instanceof Inet6Address)) throw new IllegalArgumentException("No es IPv6 válida.");
            return (Inet6Address) addr;
        } catch (Exception e) { throw new IllegalArgumentException("IPv6 inválida: " + e.getMessage()); }
    }

    public static BigInteger toBigInt(byte[] addr) {
        return new BigInteger(1, addr);
    }

    public static String fromBigInt(BigInteger value) {
        try {
            byte[] bytes = to16Bytes(value);
            return InetAddress.getByAddress(bytes).getHostAddress();
        } catch (Exception e) { throw new IllegalArgumentException("Conversión IPv6 falló."); }
    }

    private static byte[] to16Bytes(BigInteger value) {
        byte[] b = value.toByteArray();
        if (b.length == 16) return b;
        if (b.length > 16) return Arrays.copyOfRange(b, b.length - 16, b.length);
        byte[] out = new byte[16];
        System.arraycopy(b, 0, out, 16 - b.length, b.length);
        return out;
    }

    public static BigInteger prefixMask(int prefix) {
        if (prefix < 0 || prefix > 128) throw new IllegalArgumentException("Prefijo fuera de rango");
        BigInteger allOnes = BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE);
        BigInteger hostMask = (prefix == 128) ? BigInteger.ZERO : BigInteger.ONE.shiftLeft(128 - prefix).subtract(BigInteger.ONE);
        return allOnes.xor(hostMask);
    }
}
