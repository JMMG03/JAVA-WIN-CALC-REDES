import ipv4.IPv4Panel;
import ipv6.IPv6Panel;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Usar Nimbus LookAndFeel
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }

                // Colores oscuros globales
                UIManager.put("control", new Color(45,45,45));
                UIManager.put("info", new Color(60,60,60));
                UIManager.put("nimbusBase", new Color(18,30,49));
                UIManager.put("nimbusAlertYellow", new Color(248,187,0));
                UIManager.put("nimbusDisabledText", new Color(128,128,128));
                UIManager.put("nimbusFocus", new Color(115,164,209));
                UIManager.put("nimbusGreen", new Color(176,179,50));
                UIManager.put("nimbusInfoBlue", new Color(66,139,221));
                UIManager.put("nimbusLightBackground", new Color(30,30,30));
                UIManager.put("nimbusOrange", new Color(191,98,4));
                UIManager.put("nimbusRed", new Color(169,46,34));
                UIManager.put("nimbusSelectedText", new Color(255,255,255));
                UIManager.put("nimbusSelectionBackground", new Color(104,93,156));
                UIManager.put("text", new Color(230,230,230));

            } catch (Exception e) {
                System.err.println("No se pudo aplicar Nimbus oscuro: " + e.getMessage());
            }

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
