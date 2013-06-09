package com.sk89q.mclauncher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import com.sk89q.mclauncher.config.Configuration;

public class ConfigurationCellRenderer implements ListCellRenderer<Object> {
    
    private static final int PAD = 5;
    private static BufferedImage defaultIcon;
    private Font font = new Font("Ubuntu", Font.PLAIN, 12);
    
    static {
        try {
            InputStream in = Launcher.class.getResourceAsStream("/resources/config_icon.png");
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("/resources/Ubuntu-R.ttf")));
            if (in != null) {
                defaultIcon = ImageIO.read(in);
            }
        } catch (Exception e) {
        }
    }

    public Component getListCellRendererComponent(final JList<?> list, final Object value,
            int index, final boolean isSelected, boolean cellHasFocus) {
        final Configuration configuration = (Configuration) value;
        
        JIconPanel panel = new JIconPanel(configuration.getIcon());
        panel.setLayout(new GridLayout(2, 1, 0, 1));
        panel.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        panel.setBorder(BorderFactory.createEmptyBorder(PAD, PAD * 2 + 32, PAD, PAD));
        
        JLabel titleLabel = new JLabel();
        titleLabel.setText(configuration.getName());
        titleLabel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        Font fontTemp = titleLabel.getFont();
        fontTemp = fontTemp.deriveFont((float) (font.getSize() * 1.2)).deriveFont(Font.BOLD);
        titleLabel.setFont(fontTemp);
        panel.add(titleLabel);
        
        String infoText;
        if (configuration.isUsingDefaultPath()) {
            infoText = "Default Minecraft installation";
        } else if (configuration.getUpdateUrl() != null) {
            infoText = "via " + configuration.getUpdateUrl().getHost();
        } else {
            infoText = "Custom installation";
        }
        
        JLabel infoLabel = new JLabel();
        infoLabel.setText(infoText);
        infoLabel.setFont(font);
        Color color = isSelected ? list.getSelectionForeground() : list.getForeground();
        infoLabel.setForeground(color);
        panel.add(infoLabel);
        
        return panel;
    }
    
    private static class JIconPanel extends JPanel {
        
        private static final long serialVersionUID = 6455230127195332368L;
        private BufferedImage icon;
        
        public JIconPanel(BufferedImage icon) {
            this.icon = icon;
        }

        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension dim = getPreferredSize();
            if (icon != null) {
                g.drawImage(icon, PAD, (int) ((dim.getHeight() - 32) / 2), null);    
            } else if (defaultIcon != null) {
                g.drawImage(defaultIcon, PAD, (int) ((dim.getHeight() - 32) / 2), null);    
            }
        }
        
    }

}
