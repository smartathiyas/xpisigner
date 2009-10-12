package org.oregan.xpi.tools;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.swing.*;

public class ListDemo extends JFrame
{
    private JTextField fileName;

    public ListDemo()
    {
        super("Cert Demo");
        try
        {

            DefaultListModel model = new DefaultListModel();

            JList list = new JList(model);

            JScrollPane listScrollPane = new JScrollPane(list);

            //Create the list and put it in a scroll pane

            KeyStore store = KeyStore.getInstance("Windows-MY","SunMSCAPI");

            store.load(null,null);

            Enumeration<String> aliases = store.aliases();

            while (aliases.hasMoreElements())
            {
                String alias = aliases.nextElement();
                model.addElement ((store.isKeyEntry(alias)?"K":"C") + " " + alias);
            }


            Container contentPane = getContentPane();
            contentPane.add(listScrollPane, BorderLayout.CENTER);
        } catch (KeyStoreException e)
        {
            e.printStackTrace();
        } catch (NoSuchProviderException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        } catch (CertificateException e)
        {
            e.printStackTrace();
        }
    }



    public static void main(String s[])
    {
        JFrame frame = new ListDemo();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}
