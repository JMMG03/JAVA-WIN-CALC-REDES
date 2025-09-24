
import ipv4.IPv4Panel;
import ipv6.IPv6Panel;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Calculadora IP (IPv4 / IPv6)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(720, 520));

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("IPv4", new IPv4Panel());
            tabs.addTab("IPv6", new IPv6Panel());

            frame.setContentPane(tabs);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
