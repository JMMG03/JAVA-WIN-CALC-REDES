package model;

import ipv4.IPv4Calculator;
import ipv4.IPv4Utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class IPv4MembershipPanel extends JPanel {

    private final JTextField ipField = new JTextField("192.168.1.20");
    private final JTextField netField = new JTextField("192.168.1.0/24");
    private final JButton btnCalc = new JButton("Calcular");
    private final JButton btnCopy = new JButton("Copiar");
    private final JLabel verdictLabel = new JLabel(" ");
    private final JTextArea output = new JTextArea();
    private final JLabel binHtml = new JLabel(" ");

    public IPv4MembershipPanel() {
        setLayout(new BorderLayout(12, 12));

        // Top form
        add(buildTopForm(), BorderLayout.NORTH);

        // Center split: explanation + binary view
        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane outScroll = new JScrollPane(output);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new TitledBorder("Binario (IP)"));
        binHtml.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(binHtml);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, outScroll, right);
        split.setResizeWeight(0.68);
        add(split, BorderLayout.CENTER);

        // Dark theme tweaks (texto botones)
        Color bg = UIManager.getColor("nimbusLightBackground");
        if (bg != null && bg.getRed() < 80) {
            Color btnFg = new Color(240,240,240);
            btnCalc.setForeground(btnFg);
            btnCopy.setForeground(btnFg);
            output.setBackground(new Color(25,25,25));
            output.setForeground(new Color(230,230,230));
        }

        // Actions
        btnCalc.addActionListener(e -> calcular());
        btnCopy.addActionListener(e -> copyToClipboard());

        // Keyboard shortcut Ctrl+Enter
        registerKeyboardAction(e -> calcular(),
                KeyStroke.getKeyStroke("control ENTER"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JPanel buildTopForm() {
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; top.add(new JLabel("IP:"), c);
        c.gridx = 1; c.weightx = 1; top.add(ipField, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; top.add(new JLabel("Red / CIDR:"), c);
        c.gridx = 1; c.weightx = 1; top.add(netField, c);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.add(btnCalc);
        btns.add(btnCopy);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.weightx = 0; top.add(btns, c);

        verdictLabel.setFont(verdictLabel.getFont().deriveFont(Font.BOLD, 18f));
        verdictLabel.setHorizontalAlignment(SwingConstants.LEFT);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; top.add(verdictLabel, c);

        return top;
    }

    private void calcular() {
        output.setText("");
        binHtml.setText(" ");
        verdictLabel.setText(" ");

        try {
            String ipStr = ipField.getText().trim();
            String netStr = netField.getText().trim();

            // Parsear entrada de red (acepta "x.x.x.x/yy" o "x.x.x.x m.m.m.m")
            String[] parts = netStr.contains("/") ? netStr.split("/") : netStr.split("\\s+");
            if (parts.length != 2) throw new IllegalArgumentException("Usa 'red/prefijo' o 'red máscara'.");

            String netIpStr = parts[0].trim();
            String maskOrCidr = parts[1].trim();
            String canonicalMask = maskOrCidr.startsWith("/") ? // normalizar para mostrar
                    IPv4Utils.intToDotted(IPv4Utils.cidrToMask(Integer.parseInt(maskOrCidr.substring(1)))) :
                    maskOrCidr;

            // Cálculo de la red contenedora usando IPv4Calculator
            IPv4Result netRes = IPv4Calculator.calculate(netIpStr, maskOrCidr);
            int cidr = netRes.cidr();
            int maskInt = IPv4Utils.cidrToMask(cidr);
            int netInt = IPv4Utils.dottedToInt(netRes.network());

            // Comparar pertenencia
            int ipInt = IPv4Utils.dottedToInt(ipStr);
            boolean pertenece = (ipInt & maskInt) == netInt;

            // Construir salida explicativa
            StringBuilder sb = new StringBuilder();
            sb.append("=== Pertenece a subred ===\n");
            sb.append("IP:              ").append(ipStr).append("\n");
            sb.append("Red/CIDR:        ").append(netRes.network()).append("/").append(cidr).append("\n");
            sb.append("Máscara:         ").append(canonicalMask).append("\n");
            sb.append("Wildcard:        ").append(IPv4Utils.intToDotted(~maskInt)).append("\n\n");

            sb.append("Cálculo:\n");
            sb.append("  network = ip & mask\n");
            sb.append("          = ").append(ipStr).append(" & ").append(canonicalMask).append("\n");
            sb.append("          = ").append(IPv4Utils.intToDotted(ipInt & maskInt)).append("\n\n");

            sb.append("Red esperada:    ").append(netRes.network()).append("\n");
            sb.append("Broadcast:       ").append(netRes.broadcast()).append("\n");
            if (cidr >= 31) {
                sb.append("Rango hosts:     ").append(netRes.firstHost()).append(" — ").append(netRes.lastHost()).append("  (caso /")
                        .append(cidr).append(")\n");
                sb.append("Hosts utilizables: ").append(netRes.usableHosts()).append("\n");
            } else {
                sb.append("Primer host:     ").append(netRes.firstHost()).append("\n");
                sb.append("Último host:     ").append(netRes.lastHost()).append("\n");
                sb.append("Hosts utilizables: ").append(netRes.usableHosts()).append("\n");
            }
            sb.append("\nResultado: ").append(pertenece ? "SÍ" : "NO").append("\n");

            output.setText(sb.toString());

            // Veredicto grande y con color
            verdictLabel.setText(pertenece ? "✅ SÍ pertenece" : "⛔ NO pertenece");
            verdictLabel.setForeground(pertenece ? new Color(76, 175, 80) : new Color(229, 57, 53));

            // Vista binaria de la IP coloreada (red/subred/host). Aquí usamos base=cidr para no distinguir subred.
            binHtml.setText(IPv4Utils.htmlColoredBinaryNSH(ipInt, cidr, cidr));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Pertenece a subred", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyToClipboard() {
        StringSelection sel = new StringSelection(output.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        JOptionPane.showMessageDialog(this, "Resultado copiado al portapapeles.", "Copiar", JOptionPane.INFORMATION_MESSAGE);
    }
}
