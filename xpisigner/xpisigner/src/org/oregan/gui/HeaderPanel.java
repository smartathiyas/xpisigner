package org.oregan.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

public class HeaderPanel extends JPanel
{

    private ImageIcon icon;

    public HeaderPanel(/*ImageIcon icon,*/
                       String title,
                       String help1,
                       String help2) {
        super(new BorderLayout());

//        this.icon = icon;
        icon = new ImageIcon(getClass().getResource("signed-icon.png"));
        JPanel titlesPanel = new JPanel(new GridLayout(3, 1));
        titlesPanel.setOpaque(false);
        titlesPanel.setBorder(new EmptyBorder(12, 0, 12, 0));

        JLabel headerTitle = new JLabel(title);
        headerTitle.setForeground(Color.white);
        Font police = headerTitle.getFont().deriveFont(Font.BOLD);
        headerTitle.setFont(police);
        headerTitle.setBorder(new EmptyBorder(0, 12, 0, 0));
        titlesPanel.add(headerTitle);

        JLabel message;

        titlesPanel.add(message = new JLabel(help1));
        police = headerTitle.getFont().deriveFont(Font.PLAIN);
        message.setFont(police);
        message.setForeground(Color.white);
        message.setBorder(new EmptyBorder(0, 24, 0, 0));

        titlesPanel.add(message = new JLabel(help2));
        police = headerTitle.getFont().deriveFont(Font.PLAIN);
        message.setFont(police);
        message.setForeground(Color.white);
        message.setBorder(new EmptyBorder(0, 24, 0, 0));

        message = new JLabel(this.icon);
        message.setBorder(new EmptyBorder(0, 0, 0, 12));

        add(BorderLayout.WEST, titlesPanel);
        add(BorderLayout.EAST, message);
        add(BorderLayout.SOUTH, new JSeparator(JSeparator.HORIZONTAL));

        setPreferredSize(new Dimension(500, 48 + 24));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isOpaque()) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Paint storedPaint = g2.getPaint();
        g2.setPaint(new GradientPaint(0, 0, new Color(48,87,180), 0, height, new Color(11,25,108)));
        g2.fillRect(0, 0, width, height);
        g2.setPaint(storedPaint);

        GeneralPath path = new GeneralPath();
        path.moveTo(0,0);
        path.lineTo(0,height);
        path.lineTo(20,height);

        path.curveTo( 30,30,80,10 ,100,0);
        path.closePath();

        Color white20PerCent = new Color(1.0f, 1.0f, 1.0f, 0.2f);
        Color white0PerCent = new Color(1.0f, 1.0f, 1.0f, 0.0f);

        g2.setPaint(new GradientPaint(0, height, white0PerCent, 0, 0, white20PerCent));
        g2.fill(path);
        g2.setPaint(storedPaint);

        
    }
}
