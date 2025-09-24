package ipv6;

import model.IPv6Result;

import javax.swing.*;
import java.awt.*;

public class IPv6Panel extends JPanel {
    private final JTextField cidrField = new JTextField("2001:db8::1/64");
    private final JTextArea output = new JTextArea();

    public IPv6Panel() {
        setLayout(new BorderLayout(12, 12));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0; form.add(new JLabel("IPv6/prefijo:"), c);
        c.gridx=1; c.weightx=1; form.add(cidrField, c);

        JButton calcBtn = new JButton("Calcular");
        calcBtn.addActionListener(e -> calcular());
        c.gridx=0; c.gridy=1; c.gridwidth=2; form.add(calcBtn, c);

        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(form, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
    }

    private void calcular() {
        output.setText("");
        try {
            IPv6Result r = IPv6Calculator.calculate(cidrField.getText().trim());

            StringBuilder sb = new StringBuilder();
            sb.append("=== Resultado IPv6 ===\n");
            sb.append("IP:              ").append(r.inputIp()).append("\n");
            sb.append("Prefijo:         /").append(r.prefix()).append("\n");
            sb.append("Red:             ").append(r.network()).append("\n");
            sb.append("Direcciones:     2^").append(128 - r.prefix())
                    .append(" = ").append(r.totalAddresses()).append("\n");
            sb.append("Tipo:            ").append(r.type()).append("\n");

            output.setText(sb.toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
