package ipv4;

import model.IPv4Result;

import javax.swing.*;
import java.awt.*;

public class IPv4Panel extends JPanel {
    private final JTextField ipField = new JTextField("192.168.1.10");
    private final JTextField maskField = new JTextField("/24"); // admite /CIDR o mask
    private final JTextArea output = new JTextArea();

    public IPv4Panel() {
        setLayout(new BorderLayout(12, 12));
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

        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(form, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
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
}
