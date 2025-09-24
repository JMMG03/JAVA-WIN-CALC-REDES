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
        return (cidr == 0) ? 0 : (int) (0xFFFFFFFFL << (32 - cidr));
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

    public static String toHexDotted(int v) {
        return String.format("%02X.%02X.%02X.%02X",
                (v >>> 24) & 0xFF, (v >>> 16) & 0xFF, (v >>> 8) & 0xFF, v & 0xFF);
    }

    public static String toBinaryDotted(int v) {
        return String.format("%8s.%8s.%8s.%8s",
                        Integer.toBinaryString((v >>> 24) & 0xFF),
                        Integer.toBinaryString((v >>> 16) & 0xFF),
                        Integer.toBinaryString((v >>> 8) & 0xFF),
                        Integer.toBinaryString(v & 0xFF))
                .replace(' ', '0');
    }

    /**
     * Devuelve HTML con los 32 bits de la IP, coloreando red (rojo) y host (verde).
     */
    public static String htmlColoredBinary(int ip, int cidr) {
        String bits = String.format("%32s", Integer.toBinaryString(ip)).replace(' ', '0');
        StringBuilder sb = new StringBuilder("<html><code style='font-family:monospace;font-size:12px'>");
        for (int i = 0; i < 32; i++) {
            if (i == cidr) sb.append("<span style='background:#222;margin:0 4px;padding:0 2px'>.</span>");
            char b = bits.charAt(i);
            String bg = (i < cidr) ? "#FFCDD2" : "#C8E6C9"; // rojo claro / verde claro
            sb.append("<span style='background:").append(bg).append(";padding:1px 2px;border-radius:2px'>").append(b).append("</span>");
            if (i % 8 == 7 && i != 31) sb.append("<span style='margin:0 6px'>.</span>");
        }
        sb.append("</code></html>");
        return sb.toString();
    }

    // Colorea los 32 bits con 3 zonas: [0..base) = RED, [base..cidr) = SUBRED, [cidr..32) = HOST
    public static String htmlColoredBinaryNSH(int ip, int basePrefix, int cidr) {
        if (basePrefix < 0 || basePrefix > cidr || cidr > 32)
            throw new IllegalArgumentException("Prefijos inválidos: base <= cidr <= 32");

        String bits = String.format("%32s", Integer.toBinaryString(ip)).replace(' ', '0');

        // Colores oscuros con texto BLANCO para buen contraste
        final String C_RED_BG = "#D32F2F";  // rojo fuerte
        final String C_SUB_BG = "#1976D2";  // azul
        final String C_HOST_BG = "#388E3C";  // verde
        final String C_TEXT = "#FFFFFF";

        StringBuilder sb = new StringBuilder(
                "<html><div style='font-family:monospace;font-size:13px;line-height:1.7;'>");

        for (int i = 0; i < 32; i++) {
            // separadores visuales entre octetos
            if (i > 0 && i % 8 == 0) sb.append("<span style='margin:0 6px'>.</span>");

            String bg;
            if (i < basePrefix) bg = C_RED_BG;
            else if (i < cidr) bg = C_SUB_BG;
            else bg = C_HOST_BG;

            char b = bits.charAt(i);
            sb.append("<span style='background:").append(bg)
                    .append(";color:").append(C_TEXT)
                    .append(";padding:2px 4px;border-radius:3px;display:inline-block;'>")
                    .append(b).append("</span>");
        }

        sb.append("</div></html>");
        return sb.toString();
    }
}
