package ipv4;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class IPv4HelpPanel extends JPanel {

    public IPv4HelpPanel() {
        setLayout(new BorderLayout());
        var pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        pane.setFont(new Font("SansSerif", Font.PLAIN, 13));

        String css = """
        <style>
          body { font-family: 'Segoe UI', Roboto, sans-serif; font-size: 13px; margin: 0; }
          .wrap { padding: 16px 18px 24px 18px; }
          h1 { font-size: 18px; margin: 0 0 10px 0; }
          h2 { font-size: 15px; margin: 18px 0 6px 0; }
          code, pre { font-family: Consolas, 'Courier New', monospace; }
          pre { background: #1e1e1e; color: #ddd; padding: 10px; border-radius: 6px; overflow:auto; }
          table { border-collapse: collapse; width: 100%; margin: 8px 0 12px 0;}
          th, td { border: 1px solid #555; padding: 6px 8px; text-align: left; }
          th { background: #333; color: #eee; }
          .note { color:#bbb; font-size:12px; }
          .kbd { font-family: Consolas, monospace; background:#222; color:#fff; padding:2px 6px; border-radius:4px;}
          .ok { color:#9ccc65; }
          .warn { color:#ffa726; }
          .bad { color:#ef5350; }
          .pill { display:inline-block; padding:2px 6px; border-radius:10px; font-size:12px; color:#fff; }
          .pillA{ background:#5e35b1;} .pillB{ background:#3949ab;} .pillC{ background:#00897b;}
          .pillD{ background:#f9a825;} .pillE{ background:#6d4c41;}
        </style>
        """;

        String html = css + """
        <div class="wrap">
          <h1>Guía rápida IPv4: clases, rangos y cálculos</h1>

          <h2>Clases IPv4 (histórico)</h2>
          <table>
            <tr><th>Clase</th><th>Rango</th><th>Primer octeto</th><th>Prefijo por defecto</th></tr>
            <tr><td><span class="pill pillA">A</span></td><td>0.0.0.0 – 127.255.255.255</td><td>0–127</td><td>/8</td></tr>
            <tr><td><span class="pill pillB">B</span></td><td>128.0.0.0 – 191.255.255.255</td><td>128–191</td><td>/16</td></tr>
            <tr><td><span class="pill pillC">C</span></td><td>192.0.0.0 – 223.255.255.255</td><td>192–223</td><td>/24</td></tr>
            <tr><td><span class="pill pillD">D</span></td><td>224.0.0.0 – 239.255.255.255</td><td>224–239</td><td>Multicast</td></tr>
            <tr><td><span class="pill pillE">E</span></td><td>240.0.0.0 – 255.255.255.255</td><td>240–255</td><td>Experimental</td></tr>
          </table>
          <div class="note">Hoy se usa <b>CIDR</b> para prefijos arbitrarios, pero la “clase” aún se cita como referencia.</div>

          <h2>Rangos especiales útiles</h2>
          <ul>
            <li><b>Privadas (RFC1918)</b>: 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16</li>
            <li><b>Loopback</b>: 127.0.0.0/8 (localhost)</li>
            <li><b>Link-local (APIPA)</b>: 169.254.0.0/16</li>
            <li><b>Multicast</b>: 224.0.0.0/4</li>
            <li><b>Reservadas</b>: 240.0.0.0/4</li>
            <li><b>CGNAT</b> (carrier): 100.64.0.0/10</li>
          </ul>

          <h2>Cómo se calcula <i>red, broadcast, hosts</i></h2>
          <ol>
            <li><b>Máscara desde prefijo</b> (/n): poner <code>n</code> bits 1 a la izquierda.<br>
              Ej.: /24 → 255.255.255.0</li>
            <li><b>Red</b>: <code>network = ip &amp; mask</code> (AND bit a bit).</li>
            <li><b>Broadcast</b>: <code>broadcast = network | (~mask)</code> (OR con la wildcard).</li>
            <li><b>Wildcard</b>: <code>wildcard = ~mask</code> (inverso de la máscara).</li>
            <li><b>Primer/Último host</b> (si /0–/30):<br>
              <code>first = network + 1</code>, <code>last = broadcast - 1</code>.</li>
            <li><b>Hosts utilizables</b>: <code>2^(32-n) - 2</code> (excepto /31 y /32, ver abajo).</li>
          </ol>

          <h2>Casos especiales</h2>
          <ul>
            <li><b>/31</b>: punto a punto (RFC 3021). No hay broadcast “clásico”. Ambos extremos son utilizables (<span class="ok">2 hosts</span>).</li>
            <li><b>/32</b>: una sola dirección (<span class="ok">1 host</span>), red = broadcast = IP.</li>
          </ul>

          <h2>Ejemplo rápido</h2>
          <pre>
IP:      192.168.1.130
Prefijo: /24        → Máscara 255.255.255.0
Red:     192.168.1.0       (ip & mask)
Wildcard:0.0.0.255         (~mask)
BC:      192.168.1.255     (red | wildcard)
Rango:   192.168.1.1 – 192.168.1.254
Hosts:   2^(32-24)-2 = 254
          </pre>

          <h2>Subneteo</h2>
          <ul>
            <li><b>Por N subredes</b>: añade <code>s = ceil(log2(N))</code> bits al prefijo → nueva máscara.</li>
            <li><b>Por X hosts</b>: elige <code>h</code> tal que <code>2^h - 2 ≥ X</code>; nuevo prefijo = <code>32 - h</code>.</li>
          </ul>

          <h2>Validación de máscara</h2>
          Debe tener 1s contiguos a la izquierda (forma <code>111..1100..00</code>). Una máscara discontinua es <span class="bad">inválida</span>.
        </div>
        """;

        pane.setText(html);
        var scroll = new JScrollPane(pane);
        scroll.setBorder(new EmptyBorder(0,0,0,0));
        add(scroll, BorderLayout.CENTER);
    }
}
