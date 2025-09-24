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
    private final JTextField ipField = new JTextField("192.168.1.10");
    private final JTextField maskField = new JTextField("/24"); // admite /CIDR o mask
    private final JTextArea output = new JTextArea();

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

        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        subnetArea.setEditable(false);
        subnetArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(output), buildSubnetPanel());
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildTopForm() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0; form.add(new JLabel("IP:"), c);
        c.gridx=1; c.weightx=1; form.add(ipField, c);

        c.gridx=0; c.gridy=1; c.weightx=0; form.add(new JLabel("Máscara / CIDR:"), c);
        c.gridx=1; c.weightx=1; form.add(maskField, c);

        JButton calcBtn = new JButton("Calcular");
        calcBtn.addActionListener(e -> calcular());
        c.gridx=0; c.gridy=2; c.gridwidth=2; form.add(calcBtn, c);
        return form;
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

        subnetArea.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));

        wrap.add(options, BorderLayout.NORTH);
        wrap.add(btns, BorderLayout.CENTER);
        wrap.add(new JScrollPane(subnetArea), BorderLayout.SOUTH);
        return wrap;
    }

    private void calcular() {
        output.setText("");
        try {
            String ip = ipField.getText().trim();
            String mask = maskField.getText().trim();
            IPv4Result r = IPv4Calculator.calculate(ip, mask);

            StringBuilder sb = new StringBuilder();
            sb.append("=== Resultado IPv4 ===\n");
            sb.append("IP:             ").append(r.inputIp()).append("\n");
            sb.append("Máscara:        ").append(r.mask()).append(" (/").append(r.cidr()).append(")\n");
            sb.append("Wildcard:       ").append(r.wildcard()).append("\n");
            sb.append("Red:            ").append(r.network()).append("\n");
            sb.append("Broadcast:      ").append(r.broadcast()).append("\n");
            if (r.firstHost() != null) sb.append("Primer host:    ").append(r.firstHost()).append("\n");
            if (r.lastHost()  != null) sb.append("Último host:    ").append(r.lastHost()).append("\n");
            sb.append("Hosts útiles:   ").append(r.usableHosts()).append("\n");
            sb.append("Clase:          ").append(r.ipClass()).append("\n");
            sb.append("Ámbito:         ").append(r.isPrivate() ? "Privada (RFC1918)" : "Pública/No privada").append("\n");

            output.setText(sb.toString());
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
            JOptionPane.showMessageDialog(this, "CSV exportado correctamente:\n" + fc.getSelectedFile().getAbsolutePath(),
                    "Exportación", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el CSV:\n" + ex.getMessage(),
                    "Exportación", JOptionPane.ERROR_MESSAGE);
        }
    }
}
