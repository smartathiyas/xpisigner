package org.oregan.gui;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Dimension;

public class FirefoxPreview extends JPanel
{
    private JLabel url;
    private JLabel fname;
    private JLabel signer;

    public FirefoxPreview()
    {
        setOpaque(false);
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        JPanel innerPadding = new JPanel(new BorderLayout());
        innerPadding.setOpaque(false);
        innerPadding.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JLabel ffJigSaw = new JLabel(new ImageIcon(getClass().getResource("jigsaw.png")));
        innerPadding.add(ffJigSaw,BorderLayout.WEST);

        url = new JLabel();
        fname = new JLabel();
        fname.setFont(fname.getFont().deriveFont(Font.BOLD));
        signer = new JLabel();

        signer.setFont(signer.getFont().deriveFont(Font.ITALIC));
        signer.setHorizontalAlignment(JLabel.RIGHT);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(fname,BorderLayout.WEST);
        top.add(signer,BorderLayout.EAST);
        JPanel rhs = new JPanel(new BorderLayout());
        rhs.setOpaque(false);
        rhs.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
        rhs.add(top,BorderLayout.NORTH);
        rhs.add(url,BorderLayout.SOUTH);
        innerPadding.add(rhs,BorderLayout.CENTER);
        add(innerPadding,BorderLayout.CENTER);
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(300,55);
    }
    public Dimension getMaximumSize()
    {
        return new Dimension(300,55);
    }

    public void setSigner(String commonName)
    {
        signer.setText(commonName);
    }
    public void setSource(String source)
    {
        url.setText("from:   " + source);
    }
    public void setFilename(String filename)
    {
        fname.setText(filename);
    }
}
