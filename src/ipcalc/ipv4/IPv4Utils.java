package ipv4;

public class IPv4Utils {

    public static int dottedToInt(String ip) {
        String[] p = ip.trim().split("\\.");
        if (p.length != 4) throw new IllegalArgumentException("IPv4 inválida: " + ip);
        int r = 0;
        for (String s : p) {
            if (!s.matches("\\d{1,3}")) throw new IllegalArgumentException("Octeto inválido: " + s);
            int v = Integer.parseInt(s);
            if (v < 0 || v > 255) throw new IllegalArgumentException("Octeto fuera de rango: " + v);
            r = (r << 8) | v;
        }
        return r;
    }

    public static String intToDotted(int v) {
        return String.format("%d.%d.%d.%d",
                (v >>> 24) & 0xFF, (v >>> 16) & 0xFF, (v >>> 8) & 0xFF, v & 0xFF);
    }

    public static int cidrToMask(int cidr) {
        if (cidr < 0 || cidr > 32) throw new IllegalArgumentException("CIDR fuera de rango");
        return (cidr == 0) ? 0 : (int)(0xFFFFFFFFL << (32 - cidr));
    }

    public static void validateContiguousMask(int mask) {
        // bits 1 contiguos desde MSB
        boolean contiguo = ((mask & -mask) - 1 & mask) == 0;
        if (!contiguo) throw new IllegalArgumentException("Máscara no válida (no contigua)");
    }

    public static String classOf(int ip) {
        int first = (ip >>> 24) & 0xFF;
        if (first >= 1 && first <= 126) return "A";
        if (first == 127) return "Loopback";
        if (first <= 191) return "B";
        if (first <= 223) return "C";
        if (first <= 239) return "D (Multicast)";
        return "E (Experimental)";
    }

    public static boolean isPrivate(int ip) {
        int a = (ip >>> 24) & 0xFF;
        int b = (ip >>> 16) & 0xFF;
        return a == 10 || (a == 172 && b >= 16 && b <= 31) || (a == 192 && b == 168);
    }
}
