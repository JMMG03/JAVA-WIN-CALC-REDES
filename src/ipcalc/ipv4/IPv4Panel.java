package ipv4;

import model.IPv4Result;
import model.IPv4Subnet;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class IPv4Panel extends JPanel {

    // Entrada principal
    private final JTextField ipField   = new JTextField("83.33.128.6");
    private final JTextField maskField = new JTextField("255.255.255.0");
    private final JSpinner  cidrSpinner = new JSpinner(new SpinnerNumberModel(24, 0, 32, 1));
    // Prefijo base para colorear N/S/H
    private final JSpinner  basePrefixSpinner = new JSpinner(new SpinnerNumberModel(24, 0, 32, 1));

    // Botón calcular (lo necesitamos como field para estilizarlo)
    private final JButton calcBtn = new JButton("Calcular");

    // Resultados de cálculo
    private final JTextArea output = new JTextArea();
    private final JLabel hexLabel = new JLabel("-");
    private final JLabel hexMaskLabel = new JLabel("-");
    private final JLabel binHtml = new JLabel(" ");

    // Subneteo
    private final JRadioButton rbByCount = new JRadioButton("Dividir en N subredes");
    private final JSpinner spCount = new JSpinner(new SpinnerNumberModel(4, 1, 1_048_576, 1));
    private final JRadioButton rbByHosts = new JRadioButton("Para X hosts por subred");
    private final JSpinner spHosts = new JSpinner(new SpinnerNumberModel(50, 1, 1_000_000, 1));
    private final JButton btnSubnets = new JButton("Calcular subredes");
    private final JButton btnExport = new JButton("Exportar CSV");
    private final JTextArea subnetArea = new JTextArea();
    private List<IPv4Subnet> lastSubnets = new ArrayList<>();

    public IPv4Panel() {
        setLayout(new BorderLayout(12, 12));

        add(buildTopForm(), BorderLayout.NORTH);

        // Áreas de texto (respetan tema oscuro si está activo)
        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        subnetArea.setEditable(false);
        subnetArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        Color bg = UIManager.getColor("nimbusLightBackground");
        if (bg != null && bg.getRed() < 80) {
            // tema oscuro
            Color taBg = new Color(25,25,25);
            Color taFg = new Color(230,230,230);
            output.setBackground(taBg);  output.setForeground(taFg);
            subnetArea.setBackground(taBg); subnetArea.setForeground(taFg);
            // Mejor contraste en botones
            Color btnFg = new Color(240,240,240);
            calcBtn.setForeground(btnFg);
            btnSubnets.setForeground(btnFg);
            btnExport.setForeground(btnFg);
        }

        JPanel middle = new JPanel(new BorderLayout(8,8));
        JPanel visual = buildVisualPanel();
        middle.add(new JScrollPane(output), BorderLayout.CENTER);
        middle.add(visual, BorderLayout.EAST);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                middle, buildSubnetPanel());
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);

        // /bits -> máscara
        ((JSpinner.DefaultEditor) cidrSpinner.getEditor()).getTextField().setColumns(3);
        cidrSpinner.addChangeListener(e -> {
            int cidr = (Integer) cidrSpinner.getValue();
            maskField.setText(IPv4Utils.intToDotted(IPv4Utils.cidrToMask(cidr)));
            // limitar basePrefix a [0..cidr]
            ((SpinnerNumberModel) basePrefixSpinner.getModel()).setMaximum(cidr);
            int base = (Integer) basePrefixSpinner.getValue();
            if (base > cidr) basePrefixSpinner.setValue(cidr);
            refreshBinaryPreviewSafe();
        });

        // Cuando cambie el prefijo base, repinta binario
        basePrefixSpinner.addChangeListener(e -> refreshBinaryPreviewSafe());

        // Acción calcular
        calcBtn.addActionListener(e -> calcular());
    }

    /* ------------ Construcción de UI ------------ */

    private JPanel buildTopForm() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0; form.add(new JLabel("Dirección IP:"), c);
        c.gridx=1; c.weightx=1; form.add(ipField, c);

        c.gridx=0; c.gridy=1; c.weightx=0; form.add(new JLabel("Máscara:"), c);
        c.gridx=1; c.weightx=1; form.add(maskField, c);

        c.gridx=2; c.weightx=0; form.add(new JLabel("/bits:"), c);
        c.gridx=3; form.add(cidrSpinner, c);

        c.gridx=4; form.add(new JLabel("Prefijo base:"), c);
        c.gridx=5; form.add(basePrefixSpinner, c);

        calcBtn.setFocusable(false);
        c.gridx=0; c.gridy=2; c.gridwidth=6; form.add(calcBtn, c);

        return form;
    }

    private JPanel buildVisualPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new TitledBorder("Vista Hex / Binario"));

        JLabel l1 = new JLabel("IP (hex): ");
        JLabel l2 = new JLabel("Máscara (hex): ");
        JLabel l3 = new JLabel("IP (binario): ");

        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        l2.setAlignmentX(Component.LEFT_ALIGNMENT);
        l3.setAlignmentX(Component.LEFT_ALIGNMENT);
        hexLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        hexMaskLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        binHtml.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(l1); p.add(hexLabel);
        p.add(Box.createVerticalStrut(4));
        p.add(l2); p.add(hexMaskLabel);
        p.add(Box.createVerticalStrut(8));
        p.add(l3); p.add(binHtml);

        return p;
    }

    private JPanel buildSubnetPanel() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        JPanel options = new JPanel(new GridBagLayout());
        options.setBorder(new TitledBorder("Subneteo IPv4"));

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbByCount); bg.add(rbByHosts);
        rbByCount.setSelected(true);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx=0; c.gridy=0; options.add(rbByCount, c);
        c.gridx=1; options.add(spCount, c);

        c.gridx=0; c.gridy=1; options.add(rbByHosts, c);
        c.gridx=1; options.add(spHosts, c);

        btnSubnets.addActionListener(e -> calcularSubredes());
        btnExport.addActionListener(e -> exportCSV());
        btnExport.setEnabled(false);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btns.add(btnSubnets);
        btns.add(btnExport);

        wrap.add(options, BorderLayout.NORTH);
        wrap.add(btns, BorderLayout.CENTER);
        wrap.add(new JScrollPane(subnetArea), BorderLayout.SOUTH);
        return wrap;
    }

    /* ------------ Lógica ------------ */

    private void calcular() {
        output.setText("");
        try {
            String ip = ipField.getText().trim();
            String maskTxt = maskField.getText().trim();
            if (maskTxt.isEmpty()) maskTxt = "/" + cidrSpinner.getValue();

            IPv4Result r = IPv4Calculator.calculate(ip, maskTxt);

            // sincroniza controles
            cidrSpinner.setValue(r.cidr());
            maskField.setText(r.mask());
            ((SpinnerNumberModel) basePrefixSpinner.getModel()).setMaximum(r.cidr());
            if ((Integer) basePrefixSpinner.getValue() > r.cidr()) {
                basePrefixSpinner.setValue(r.cidr());
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== IPv4 ===\n");
            sb.append("IP / Prefijo:   ").append(r.inputIp()).append(" /").append(r.cidr()).append("\n");
            sb.append("MÁSCARA:        ").append(r.mask()).append("\n");
            sb.append("WILDCARD:       ").append(r.wildcard()).append("\n\n");
            sb.append("RED:            ").append(r.network()).append("\n");
            sb.append("BROADCAST:      ").append(r.broadcast()).append("\n");
            if (r.firstHost() != null) {
                sb.append("RANGO HOSTS:    ").append(r.firstHost()).append("  —  ").append(r.lastHost()).append("\n");
            }
            sb.append("HOSTS ÚTILES:   ").append(r.usableHosts()).append("\n\n");
            sb.append("CLASE:          ").append(r.ipClass()).append("\n");
            sb.append("TIPO:           ").append(r.isPrivate() ? "Privada (RFC1918)" : "Pública/No privada").append("\n");

            output.setText(sb.toString());

            // Vista Hex / Binario
            int ipInt   = IPv4Utils.dottedToInt(r.inputIp());
            int maskInt = IPv4Utils.dottedToInt(r.mask());
            hexLabel.setText(IPv4Utils.toHexDotted(ipInt));
            hexMaskLabel.setText(IPv4Utils.toHexDotted(maskInt));
            int base = (Integer) basePrefixSpinner.getValue();
            binHtml.setText(IPv4Utils.htmlColoredBinaryNSH(ipInt, base, r.cidr()));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calcularSubredes() {
        subnetArea.setText("");
        lastSubnets = new ArrayList<>();
        btnExport.setEnabled(false);

        try {
            String ip = ipField.getText().trim();
            String mask = maskField.getText().trim();
            if (mask.isEmpty()) mask = "/" + cidrSpinner.getValue();

            if (rbByCount.isSelected()) {
                int n = (Integer) spCount.getValue();
                lastSubnets = IPv4Subnetter.splitBySubnets(ip, mask, n);
            } else {
                long hosts = ((Number) spHosts.getValue()).longValue();
                lastSubnets = IPv4Subnetter.splitByHostsPerSubnet(ip, mask, hosts);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Generadas %d subred(es):\n", lastSubnets.size()));
            sb.append(String.format("%-6s %-20s %-16s %-16s %-16s %-16s %-12s%n",
                    "#", "CIDR", "Network", "Broadcast", "First Host", "Last Host", "Usable"));
            for (IPv4Subnet s : lastSubnets) {
                sb.append(String.format("%-6d %-20s %-16s %-16s %-16s %-16s %-12d%n",
                        s.index(), s.cidr(), s.network(), s.broadcast(), s.firstHost(), s.lastHost(), s.usableHosts()));
            }
            subnetArea.setText(sb.toString());
            btnExport.setEnabled(!lastSubnets.isEmpty());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Subneteo", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCSV() {
        if (lastSubnets.isEmpty()) return;

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar subredes como CSV");
        fc.setSelectedFile(new java.io.File("subredes.csv"));
        int res = fc.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
            fw.write("Index,CIDR,Network,Broadcast,FirstHost,LastHost,UsableHosts\n");
            for (IPv4Subnet s : lastSubnets) {
                fw.write(String.format("%d,%s,%s,%s,%s,%s,%d%n",
                        s.index(), s.cidr(), s.network(), s.broadcast(), s.firstHost(), s.lastHost(), s.usableHosts()));
            }
            JOptionPane.showMessageDialog(this, "CSV exportado:\n" + fc.getSelectedFile().getAbsolutePath(),
                    "Exportación", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el CSV:\n" + ex.getMessage(),
                    "Exportación", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Recalcula solo la vista binaria si hay datos válidos; ignora errores silenciosamente. */
    private void refreshBinaryPreviewSafe() {
        try {
            String ip = ipField.getText().trim();
            String maskTxt = maskField.getText().trim();
            if (maskTxt.isEmpty()) maskTxt = "/" + cidrSpinner.getValue();
            IPv4Result r = IPv4Calculator.calculate(ip, maskTxt);
            int base = Math.min((Integer) basePrefixSpinner.getValue(), r.cidr());
            basePrefixSpinner.setValue(base);
            int ipInt = IPv4Utils.dottedToInt(r.inputIp());
            binHtml.setText(IPv4Utils.htmlColoredBinaryNSH(ipInt, base, r.cidr()));
        } catch (Exception ignored) { /* sin-op */ }
    }
}
