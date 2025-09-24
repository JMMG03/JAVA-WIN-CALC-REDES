package model;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.prefs.Preferences;

public class HelpWindow extends JFrame {

    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Siempre visible (pin)");
    private final JCheckBox cbShowOnStart = new JCheckBox("Mostrar al iniciar");
    private final Preferences prefs = Preferences.userRoot().node("com.ejemplo.ipcalc");

    public HelpWindow() {
        super("Guía rápida • Cómo usar la Calculadora IP");
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setSize(520, 640);
        setLocationByPlatform(true);
        setAlwaysOnTop(prefs.getBoolean("help.alwaysOnTop", false));

        // Contenido HTML
        JEditorPane pane = new JEditorPane();
        pane.setEditable(false);
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        pane.setContentType("text/html");
        pane.setText(htmlDoc());
        pane.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Scroll
        JScrollPane scroll = new JScrollPane(pane);
        scroll.setBorder(new EmptyBorder(0,0,0,0));

        // Controles inferiores
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        cbAlwaysOnTop.setSelected(prefs.getBoolean("help.alwaysOnTop", false));
        cbShowOnStart.setSelected(prefs.getBoolean("help.showOnStart", true));
        JButton btnClose = new JButton("Cerrar");

        // Tema oscuro: mejorar contraste (si Nimbus dark)
        Color bg = UIManager.getColor("nimbusLightBackground");
        if (bg != null && bg.getRed() < 80) {
            Color btnFg = new Color(240,240,240);
            btnClose.setForeground(btnFg);
            cbAlwaysOnTop.setForeground(btnFg);
            cbShowOnStart.setForeground(btnFg);
        }

        cbAlwaysOnTop.addActionListener(e -> {
            boolean on = cbAlwaysOnTop.isSelected();
            setAlwaysOnTop(on);
            prefs.putBoolean("help.alwaysOnTop", on);
        });
        cbShowOnStart.addActionListener(e ->
                prefs.putBoolean("help.showOnStart", cbShowOnStart.isSelected())
        );
        btnClose.addActionListener(e -> setVisible(false));

        bottom.add(cbAlwaysOnTop);
        bottom.add(cbShowOnStart);
        bottom.add(Box.createHorizontalStrut(12));
        bottom.add(btnClose);

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private String htmlDoc() {
        String css = """
        <style>
          body { font-family: 'Segoe UI', Roboto, sans-serif; font-size: 13px; color:#e6e6e6; background:#1e1e1e; }
          h1 { font-size: 18px; margin: 0 0 10px 0; }
          h2 { font-size: 15px; margin: 18px 0 6px 0; }
          p, li { line-height: 1.5; }
          code, pre { font-family: Consolas, 'Courier New', monospace; }
          pre { background: #111; color:#ddd; padding:10px; border-radius:6px; overflow:auto; }
          .kbd { font-family: Consolas, monospace; background:#333; color:#fff; padding:1px 6px; border-radius:4px;}
          .pill { display:inline-block; padding:2px 6px; border-radius:10px; font-size:12px; color:#fff; }
          .pillA{ background:#5e35b1;} .pillB{ background:#3949ab;} .pillC{ background:#00897b;}
          .pillD{ background:#f9a825;} .pillE{ background:#6d4c41;}
          ul { margin-top:6px; }
          table { border-collapse: collapse; width: 100%; margin: 8px 0 12px 0;}
          th, td { border: 1px solid #444; padding: 6px 8px; text-align: left; }
          th { background: #2a2a2a; color: #eee; }
        </style>
        """;

        String html = css + """
        <h1>Cómo usar la Calculadora IP</h1>
        <p>La aplicación calcula datos de <b>IPv4</b> e <b>IPv6</b>. En IPv4 además puedes hacer <b>subneteo</b> y ver la IP en <b>hex</b> y <b>binario</b> con los bits
        de <span style="color:#ff8a80">red</span>, <span style="color:#90caf9">subred</span> y <span style="color:#a5d6a7">host</span> destacados.</p>

        <h2>IPv4 — pasos rápidos</h2>
        <ol>
          <li>Introduce <b>IP</b> (ej. <code>192.168.1.10</code>) y <b>Máscara</b> (<code>255.255.255.0</code>) o el <b>/bits</b>.</li>
          <li>Pulsa <span class="kbd">Calcular</span>. Verás <b>red</b>, <b>broadcast</b>, <b>rango de hosts</b>, <b>wildcard</b>, clase y tipo.</li>
          <li>En “Vista Hex/Binario” puedes leer la IP en <b>hex</b> y en <b>binario</b> coloreado. Ajusta “<b>Prefijo base</b>” para resaltar la parte de <i>subred</i>.</li>
          <li>Sección “Subneteo IPv4”:
            <ul>
              <li><b>Dividir en N subredes</b> → reparte la red en subredes iguales.</li>
              <li><b>Para X hosts por subred</b> → elige el prefijo mínimo que permita al menos X hosts.</li>
              <li>Puedes <b>exportar a CSV</b> el listado de subredes.</li>
            </ul>
          </li>
        </ol>

        <h2>Cómo se calculan los campos (IPv4)</h2>
        <ul>
          <li><b>Máscara</b> desde /n: n bits a 1 a la izquierda.</li>
          <li><b>Red</b>: <code>network = ip &amp; mask</code></li>
          <li><b>Wildcard</b>: <code>~mask</code> (inverso)</li>
          <li><b>Broadcast</b>: <code>network | (~mask)</code></li>
          <li><b>Primer/Último host</b>: <code>network+1</code> y <code>broadcast-1</code> (no aplica en /31 y /32)</li>
          <li><b>Hosts utilizables</b>: <code>2^(32-n) - 2</code> (salvo /31=2, /32=1)</li>
        </ul>

        <h2>Clases y rangos especiales</h2>
        <table>
          <tr><th>Clase</th><th>Rango</th><th>Prefijo def.</th></tr>
          <tr><td><span class="pill pillA">A</span></td><td>0.0.0.0 – 127.255.255.255</td><td>/8</td></tr>
          <tr><td><span class="pill pillB">B</span></td><td>128.0.0.0 – 191.255.255.255</td><td>/16</td></tr>
          <tr><td><span class="pill pillC">C</span></td><td>192.0.0.0 – 223.255.255.255</td><td>/24</td></tr>
          <tr><td><span class="pill pillD">D</span></td><td>224.0.0.0 – 239.255.255.255 (Multicast)</td><td>—</td></tr>
          <tr><td><span class="pill pillE">E</span></td><td>240.0.0.0 – 255.255.255.255 (Experimental)</td><td>—</td></tr>
        </table>
        <ul>
          <li><b>Privadas</b>: 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16</li>
          <li><b>Loopback</b>: 127.0.0.0/8 — <i>localhost</i></li>
          <li><b>Link-local (APIPA)</b>: 169.254.0.0/16</li>
          <li><b>Multicast</b>: 224.0.0.0/4</li>
          <li><b>CGNAT</b>: 100.64.0.0/10 (operadores)</li>
        </ul>

        <h2>Consejos</h2>
        <ul>
          <li>Si activas <b>Siempre visible</b>, esta ventana quedará encima de la app.</li>
          <li>Marca <b>Mostrar al iniciar</b> para ver esta guía cada vez que abras la app.</li>
          <li>El tema oscuro/herencia de fuentes depende del <i>Look&amp;Feel</i> (Nimbus oscuro recomendado).</li>
        </ul>
        """;
        return html;
    }

    /** Mostrar u ocultar respetando el estado guardado */
    public static void showAtStart(HelpWindow hw) {
        boolean show = hw.prefs.getBoolean("help.showOnStart", true);
        if (show) hw.setVisible(true);
    }
}
