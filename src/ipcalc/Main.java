
import ipv4.IPv4Panel;
import ipv6.IPv6Panel;
import model.HelpWindow;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // (opcional) Nimbus + colores oscuros si lo estás usando ya
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
                UIManager.put("nimbusLightBackground", new Color(30,30,30));
                UIManager.put("text", new Color(230,230,230));
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
            } catch (Exception ignored) {}

            JFrame frame = new JFrame("Calculadora IP (IPv4 / IPv6)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(760, 560));

            // Pestañas principales
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("IPv4", new IPv4Panel());
            tabs.addTab("IPv6", new IPv6Panel());

            // Barra de menú con acceso a la guía
            JMenuBar mb = new JMenuBar();
            JMenu ayuda = new JMenu("Ayuda");
            JMenuItem miGuia = new JMenuItem("Mostrar guía (F1)");
            ayuda.add(miGuia);
            mb.add(ayuda);
            frame.setJMenuBar(mb);

            frame.setContentPane(tabs);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Ventana de ayuda (no modal)
            HelpWindow help = new HelpWindow();
            HelpWindow.showAtStart(help);         // se abre si el usuario lo tiene marcado
            miGuia.addActionListener(e -> help.setVisible(true));

            // Atajo F1
            frame.getRootPane().registerKeyboardAction(
                    e -> help.setVisible(true),
                    KeyStroke.getKeyStroke("F1"),
                    JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        });
    }
}
