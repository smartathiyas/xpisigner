package org.oregan.gui;

import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JDialog;
import javax.swing.BorderFactory;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Paint;
import java.awt.GradientPaint;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;

public class Splash extends JPanel
{
    private static JDialog dlg;
    private Timer timer;

    public static JDialog getSplash()
    {
        if(dlg == null)
        {
           new Splash();
        }
        return dlg;
    }

    private ImageIcon icon;

    public Splash() {
        super();

        dlg = new JDialog();
        dlg.add(this);
        dlg.setUndecorated(true);
        dlg.pack();
        dlg.setLocationRelativeTo(null);


        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0,30,20,0));
        setForeground(new Color(1.0f,1.0f,1.0f,.75f));
        JLabel prodName = new JLabel("XPISigner");
        prodName.setFont(prodName.getFont().deriveFont(Font.BOLD,36));
        prodName.setForeground(Color.white);
        add(prodName,BorderLayout.CENTER);
        JLabel title = new JLabel("Firefox Extension Packager");
        title.setForeground(new Color(1.0f,1.0f,1.0f,.75f));
        add(title,BorderLayout.SOUTH);
        timer = new Timer(2500,new ActionListener(){

                    public void actionPerformed(ActionEvent e)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                dlg.setVisible(false);
                                dlg.dispose();
                            }
                        });
                    }
                });
                timer.setRepeats(false);
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
        Color darker = new Color(11, 25, 108);
        Color lighter = new Color(48, 87, 180);

        g2.setColor(darker);
        g2.drawRect(0,0,getWidth(),getHeight());

        g2.setPaint(new GradientPaint(0, 0, lighter, 0, height, darker));
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

    public Dimension getPreferredSize()
    {
        return new Dimension(400,100);
    }


    public void setVisible(boolean aFlag)
    {
        if(aFlag)
        {
            timer.start();
        }
        super.setVisible(aFlag);
    }
}
