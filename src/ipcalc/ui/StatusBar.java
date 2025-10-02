package ui;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {
    private final JLabel msg = new JLabel("Listo.");

    public StatusBar() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        add(msg, BorderLayout.WEST);
    }

    public void info(String text) {
        msg.setForeground(UIManager.getColor("text"));
        msg.setText(text);
    }

    public void ok(String text) {
        msg.setForeground(new Color(76,175,80)); // verde
        msg.setText(text);
    }

    public void warn(String text) {
        msg.setForeground(new Color(255,179,0)); // ámbar
        msg.setText(text);
    }

    public void error(String text) {
        msg.setForeground(new Color(229,57,53)); // rojo
        msg.setText(text);
    }
}
