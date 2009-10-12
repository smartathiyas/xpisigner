package org.oregan.gui;

import org.oregan.gui.capi.XPIException;
import org.oregan.gui.capi.XPISigner;
import sun.security.x509.X500Name;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class MultiSigner extends JFrame
{
    private KeyStore store;
    private JTable msCertStoreTable;
    private Container cardPanel;
    private Container headerPanel;
    private FileSelectionPanel fileSelectionPanel;
    private ImageIcon certIcon;
    private JFileChooser fileChooser;
    private JButton nextButton;
    private JButton previousButton;
    private int currentPanel;
    private FirefoxPreview firefoxPreview;
    private String alias;
    private File selectedFile;
    private JTextPane summary;


    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e)
        {
        } catch (InstantiationException e)
        {
        } catch (IllegalAccessException e)
        {
        } catch (UnsupportedLookAndFeelException e)
        {
        }


        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                MultiSigner frame = new MultiSigner();

                frame.setVisible(true);

            }
        });
    }

    public MultiSigner() throws HeadlessException
    {
        super("MultiSigner - XPI");

        try
        {
            store = KeyStore.getInstance("Windows-MY", "SunMSCAPI");
            store.load(null, null);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        certIcon = new ImageIcon(getClass().getResource("certificate.png"));
        buildContent();

        showPanel(CERT);

        getContentPane().setBackground(Color.WHITE);

        pack();
        setLocationRelativeTo(null);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);

        setIconImage(new ImageIcon(getClass().getResource("signed-icon.png")).getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void buildContent()
    {
        headerPanel = buildHeader();

        add(headerPanel, BorderLayout.NORTH);

        cardPanel = buildMainPanel();

        add(cardPanel, BorderLayout.CENTER);

        add(buildButtons(), BorderLayout.SOUTH);


    }

    private Container buildMainPanel()
    {
        JPanel acardPanel = new JPanel();
        CardLayout cardLayout = new CardLayout();
        acardPanel.setLayout(cardLayout);
        acardPanel.add(buildCertSelect(), panels[CERT]);
        acardPanel.add(buildFileSelect(), panels[FILE]);
        acardPanel.add(buildGenerateXPI(), panels[XPI]);
        return acardPanel;
    }

    private Component buildGenerateXPI()
    {
        JPanel ret = new JPanel();
        ret.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        ret.setBackground(Color.white);

        final JPanel outer = new JPanel();
        ret.add(outer, BorderLayout.CENTER);
        outer.setBorder(BorderFactory.createEtchedBorder());
        outer.setBackground(Color.white);
        outer.setLayout(new GridLayout(2, 1));

        // Top

        JPanel topHalf = new JPanel();
        topHalf.setBackground(Color.white);
        topHalf.setLayout(new BorderLayout(0, 10));

        JPanel outputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        outputPanel.setBackground(Color.white);

        outputPanel.add(new JLabel("Save location: "));
        final JTextField outputField = new JTextField(30);
        outputField.setEnabled(false);
        outputPanel.add(outputField);

        Preferences registry = Preferences.userNodeForPackage(XPISigner.class);
        String storedFileName = registry.get("xpi.outputfile", "");
        if (storedFileName.length() > 0)
        {
            File f = new File(storedFileName);
            boolean b = f.getParentFile().exists();
            if(b)
            {
                outputField.setText(f.toString());
            }
        }


        JButton browse = new JButton("...");
        outputPanel.add(browse);

        topHalf.add(outputPanel, BorderLayout.NORTH);

        summary = new JTextPane();
//        summary.setBorder(BorderFactory.createLineBorder(Color.black));
        topHalf.add(summary, BorderLayout.CENTER);

        //Bottom
        JPanel bottomHalf = new JPanel(new BorderLayout());
        bottomHalf.setBackground(Color.white);

        JPanel generateButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        generateButton.setBackground(Color.white);
        JButton generate = new JButton("Generate XPI Installer");
        generateButton.add(generate);

        bottomHalf.add(generateButton, BorderLayout.NORTH);


        final JPanel switcher = new JPanel();
        switcher.setBackground(Color.white);
        final CardLayout switcherCardLayout = new CardLayout();
        switcher.setLayout(switcherCardLayout);

        JPanel progressBars = new JPanel(new FlowLayout(FlowLayout.CENTER));
        firefoxPreview = new FirefoxPreview();
        final JProgressBar mProgress = new JProgressBar();
        final JProgressBar aProgress = new JProgressBar();


        progressBars.setBackground(Color.white);
        progressBars.setLayout(new GridLayout(2, 2));
        progressBars.add(new JLabel("Analysing files and calculating manifests..."));
        progressBars.add(mProgress);
        progressBars.add(new JLabel("Packing files and saving archive..."));
        progressBars.add(aProgress);

        switcher.add(progressBars, "progress");

        switcher.add(firefoxPreview, "after");

        JPanel tmp = new JPanel(new BorderLayout());
        tmp.setBackground(Color.white);
        tmp.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        tmp.add(switcher, BorderLayout.NORTH);

        bottomHalf.add(tmp, BorderLayout.CENTER);

        outer.add(topHalf);
        outer.add(bottomHalf);

        // Browse Button
        browse.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                if (fileChooser == null)
                    fileChooser = new JFileChooser(".");

                if(outputField.getText().length()>0)
                {
                    fileChooser.setSelectedFile(new File(outputField.getText()));
                }
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileSelectionMode(
                    JFileChooser.FILES_ONLY);
                int status = fileChooser.showSaveDialog(outer);
                if (status == JFileChooser.APPROVE_OPTION)
                {
                    selectedFile = fileChooser.getSelectedFile();
                    firefoxPreview.setSource(selectedFile.getParent().toString());
                    firefoxPreview.setFilename(selectedFile.getName());
                    outputField.setText(selectedFile.toString());
                    try
                    {
                        Preferences registry = Preferences.userNodeForPackage(XPISigner.class);
                        registry.put("xpi.outputfile", selectedFile.getCanonicalPath());
                        registry.flush();
                    } catch (BackingStoreException e1)
                    {
                        System.out.println(e1);
                    } catch (IOException e1)
                    {
                        System.out.println(e1);
                    }

                }else
                {
                    selectedFile = null;
                    outputField.setText("");
                }
            }
        });

        generate.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                final ArrayList<String> files = fileSelectionPanel.getAllFiles();
                final File base = fileSelectionPanel.getBaseDir();

                SwingWorker worker = new SwingWorker()
                {

                    protected Object doInBackground() throws Exception
                    {
                        try
                        {
                            XPISigner signer = new XPISigner(alias, base, files, selectedFile);
                            signer.compileManifests(mProgress);
                            signer.archiveXPI(aProgress);
                        } catch (XPIException e1)
                        {
                            e1.printStackTrace();
                        }
                        return "OK";
                    }

                    protected void done()
                    {
                        switcherCardLayout.show(switcher, "after");
                    }
                };
                worker.execute();
            }
        });


        return ret;

/*

        final JPanel jPanel = new JPanel();
        jPanel.setBackground(Color.white);


        firefoxPreview = new FirefoxPreview();

        JPanel topPanel = new JPanel(new GridLayout(1,2,0,10));
        topPanel.add(firefoxPreview);

        JPanel rhs = new JPanel(new BorderLayout());
        JLabel x = new JLabel("Set filename and location");
        x.setForeground(Color.blue);

        x.addMouseListener(new MouseAdapter(){


            public void mouseEntered(MouseEvent e)
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent e)
            {
                setCursor(Cursor.getDefaultCursor());
            }
            public void mouseClicked(MouseEvent e)
            {
                if (fileChooser == null)
                    fileChooser = new JFileChooser(".");
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileSelectionMode(
                    JFileChooser.FILES_ONLY);
                int status = fileChooser.showSaveDialog(jPanel);
                if (status == JFileChooser.APPROVE_OPTION)
                {
                    selectedFile = fileChooser.getSelectedFile();
                    firefoxPreview.setSource(selectedFile.getParent().toString());
                    firefoxPreview.setFilename(selectedFile.getName());
                }
            }
        });


        JButton doIt = new JButton("Sign Package");
        final JTextArea output = new JTextArea(15, 40);

        final JProgressBar mProgress = new JProgressBar();
        final JProgressBar aProgress = new JProgressBar();

        doIt.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                final ArrayList<String> files = fileSelectionPanel.getAllFiles();
                final File base = fileSelectionPanel.getBaseDir();

                SwingWorker worker = new SwingWorker()
                {

                    protected Object doInBackground() throws Exception
                    {
                        try
                        {
                            XPISigner signer = new XPISigner(alias, base, files, selectedFile);
                            signer.compileManifests(mProgress);
                            signer.archiveXPI(aProgress);
                        } catch (XPIException e1)
                        {
                            e1.printStackTrace();
                        }
                        return "OK";
                    }

                    protected void done()
                    {
                    }
                };
                worker.execute();


            }
        });

        rhs.add(x, BorderLayout.NORTH);
        topPanel.add(rhs);
        jPanel.add(topPanel, BorderLayout.NORTH);



        
//        jPanel.add(doIt);

        JPanel misc = new JPanel();

//        jPanel.add(output, BorderLayout.CENTER);
        JPanel tmppanel = new JPanel(new GridLayout(2, 2));
        tmppanel.setBackground(Color.white);
//        tmppanel.setLayout(new BoxLayout(tmppanel, BoxLayout.Y_AXIS));
        tmppanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tmppanel.add(new JLabel("Analysing files and calculating manifests..."));
        tmppanel.add(mProgress);
        tmppanel.add(new JLabel("Packing files and saving archive..."));

        tmppanel.add(aProgress);
        misc.add(doIt, BorderLayout.NORTH);
        misc.add(aProgress, BorderLayout.SOUTH);

        jPanel.add(misc);

        return jPanel;
*/
    }


    private Component buildFileSelect()
    {
        fileSelectionPanel = new FileSelectionPanel(this);
        return fileSelectionPanel;
    }

    private Component buildButtons()
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Color.white);

        previousButton = new JButton("< Back");
        panel.add(previousButton);
        nextButton = new JButton("Next >");
        panel.add(nextButton);
        return panel;
    }

    private void next()
    {
        if (currentPanel != (panels.length - 1))
            showPanel(++currentPanel);
    }

    private void back()
    {
        if (currentPanel > 0)
            showPanel(--currentPanel);
    }

    private void showPanel(int index)
    {
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, panels[index]);
        ((CardLayout) headerPanel.getLayout()).show(headerPanel, panels[index]);
        this.currentPanel = index;
        nextButton.setAction(nextActions[currentPanel]);
        nextButton.setText(nextTitles[currentPanel]);
        previousButton.setAction(backActions[currentPanel]);
        previousButton.setText(backTitles[currentPanel]);
        previousButton.setEnabled(backEnabled[currentPanel]);
    }


    private Component buildCertSelect()
    {
        JPanel panel = new JPanel(new BorderLayout());

        JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP);
        panel.add(tabPane, BorderLayout.CENTER);
        panel.setBackground(Color.WHITE);
        Vector colNames = new Vector();
        colNames.add("Issued to");
        colNames.add("Issued by");
        colNames.add("Expiration");
        colNames.add("Friendly Name");

        DefaultTableModel dtm = new DefaultTableModel(colNames, 0);

        try
        {
            Enumeration<String> aliases = store.aliases();

            while (aliases.hasMoreElements())
            {
                String alias = aliases.nextElement();

                PrivateKey key = (PrivateKey) store.getKey(alias, null);

                X509Certificate cert = (X509Certificate) store.getCertificate(alias);
                String subjectCN = ((X500Name) cert.getSubjectDN()).getCommonName();
                String issuerCN = ((X500Name) cert.getIssuerDN()).getCommonName();
                if (issuerCN == null)
                    issuerCN = ((X500Name) cert.getIssuerDN()).getOrganizationalUnit();

                String dateFormat = cert.getNotAfter().toString();

                dateFormat = new SimpleDateFormat("dd/mm/yyyy").format(cert.getNotAfter());
                // Need a cell renderer
                Object[] row = {subjectCN, issuerCN, dateFormat, alias};
                dtm.addRow(row);

            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        msCertStoreTable = new JTable(dtm);
        msCertStoreTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        msCertStoreTable.setCellSelectionEnabled(false);
        msCertStoreTable.setColumnSelectionAllowed(false);
        msCertStoreTable.setRowSelectionAllowed(true);

        IcoLabelTable customRenderer = new IcoLabelTable(certIcon);
        msCertStoreTable.getColumnModel().getColumn(0).setCellRenderer(customRenderer);
        msCertStoreTable.getColumnModel().getColumn(1).setCellRenderer(customRenderer);
        msCertStoreTable.getColumnModel().getColumn(2).setCellRenderer(customRenderer);
        msCertStoreTable.getColumnModel().getColumn(3).setCellRenderer(customRenderer);

        JScrollPane pane = new JScrollPane(msCertStoreTable);
        pane.setBackground(Color.WHITE);
        pane.setBorder(BorderFactory.createEtchedBorder());

        JPanel forEtching = new JPanel(new BorderLayout());
        forEtching.setBackground(Color.WHITE);
        forEtching.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        forEtching.add(pane, BorderLayout.CENTER);

        tabPane.add(forEtching, "Personal");

        JPanel viewButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewButtonPanel.setBackground(Color.white);

        Preferences registry = Preferences.userNodeForPackage(XPISigner.class);
        String storedAlias = registry.get("mscertstore.alias", "");

        if (storedAlias.length() > 0)
        {
            for (int i = 0; i < msCertStoreTable.getRowCount(); i++)
            {
                if (storedAlias.equals(msCertStoreTable.getValueAt(i, 3)))
                    msCertStoreTable.getSelectionModel().setSelectionInterval(i, i);
            }
        } else
        {
            msCertStoreTable.getSelectionModel().setSelectionInterval(0, 0);
        }

        return panel;

    }

    private Container buildHeader()
    {
        CardLayout layout = new CardLayout();
        JPanel headerCardPanel = new JPanel(layout);

        HeaderPanel header = new HeaderPanel(/*icon,*/
                                             "Digital ID",
                                             "The certificates from your windows cert store are shown below.",
                                             "Select the certificate to use to sign your extension...");
        headerCardPanel.add(header, panels[CERT]);

        HeaderPanel header1 = new HeaderPanel(/*icon,*/
                                              "Extension source",
                                              "Add files and folders to your extension.",
                                              "You can mark files to be excluded");
        headerCardPanel.add(header1, panels[FILE]);
        HeaderPanel header2 = new HeaderPanel(/*icon,*/
                                              "Generate extension.",
                                              "Package and sign the files into an XPI",
                                              "Click 'Preview' to open the file in firefox");
        headerCardPanel.add(header2, panels[XPI]);


        layout.show(headerCardPanel, panels[CERT]); // First
        return headerCardPanel;

    }

    private String[] panels = {"cert-selection", "file-selection", "generate-xpi"};
    private boolean[] backEnabled = {false, true, true};
    private boolean[] nextAsNext = {true, true, false};
    private String[] backTitles = {"< Back", "< Back", "< Back"};
    private String[] nextTitles = {"Next >", "Next >", "Close"};
    private static int CERT = 0;
    private static int FILE = 1;
    private static int XPI = 2;

    private Action[] nextActions = setupNextActions();

    private Action[] setupNextActions()
    {
        Action[] actions = new Action[3];
        actions[CERT] = new AbstractAction()
        {

            public void actionPerformed(ActionEvent e)
            {
                alias = (String) msCertStoreTable.getValueAt(msCertStoreTable.getSelectedRow(), 3);
                String commonName = (String) msCertStoreTable.getValueAt(msCertStoreTable.getSelectedRow(), 0);
                firefoxPreview.setSigner(commonName);

                try
                {
                    Preferences registry = Preferences.userNodeForPackage(XPISigner.class);
                    registry.put("mscertstore.alias", alias);
                    registry.flush();
                } catch (BackingStoreException e1)
                {
                    System.out.println(e1);
                }

                next();
            }
        };
        actions[FILE] = new AbstractAction()
        {

            public void actionPerformed(ActionEvent e)
            {
                ArrayList<String> list = fileSelectionPanel.getAllFiles();
                if (list != null)
                {
                    StringBuffer summaryText = new StringBuffer();
                    summaryText.append("XPISigner v 2.0 beta\n")
                        .append("Generating XPI package from ").append(fileSelectionPanel.getBaseDir()).append("\n");

                    summaryText.append(list.size()).append(" file(s).\n");
                    summaryText.append("\n");

                    summaryText.append("Signing certifiate \"")
                        .append((String) msCertStoreTable.getValueAt(msCertStoreTable.getSelectedRow(), 3))
                        .append("\" issued to \"")
                        .append((String) msCertStoreTable.getValueAt(msCertStoreTable.getSelectedRow(), 0))
                        .append("\" by \"").append((String) msCertStoreTable.getValueAt(msCertStoreTable.getSelectedRow(), 1)).
                        append("\"\n");
                    summaryText.append("Certificate expires on " + (String) msCertStoreTable.getValueAt(msCertStoreTable.getSelectedRow(), 2));
                    summary.setText(summaryText.toString());

                    try
                    {
                        Preferences registry = Preferences.userNodeForPackage(XPISigner.class);
                        registry.put("xpi.basedir", fileSelectionPanel.getBaseDir().toString());
                        registry.flush();
                    } catch (BackingStoreException e1)
                    {
                        System.out.println(e1);
                    }


                    next();
                }
            }
        };
        actions[XPI] = new AbstractAction()
        {

            public void actionPerformed(ActionEvent e)
            {
                System.out.println("XPI >");
                System.out.println("e = " + e);
                System.exit(0);
            }
        };


        return actions;
    }

    private Action[] backActions = setupBackActions();

    private Action[] setupBackActions()
    {
        Action[] actions = new Action[3];
        actions[CERT] = new AbstractAction()
        {

            public void actionPerformed(ActionEvent e)
            {
                // Disabled
            }
        };
        actions[FILE] = new AbstractAction()
        {

            public void actionPerformed(ActionEvent e)
            {
                back();
            }
        };
        actions[XPI] = new AbstractAction()
        {

            public void actionPerformed(ActionEvent e)
            {
                back();
            }
        };


        return actions;
    }

}

class IcoLabelTable extends JLabel implements TableCellRenderer
{
    private Icon certIcon;
    private Font plainFont;
    private Font boldFont;

    public IcoLabelTable(Icon certIcon)
    {
        super();
        setOpaque(true);
        this.certIcon = certIcon;
        this.plainFont = getFont();
        this.boldFont = plainFont.deriveFont(Font.BOLD);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        if (column == 0)
            setIcon(this.certIcon);
        else
            setIcon(null);

        if (table.getSelectedRow() == row)
        {
            setBackground(Color.lightGray);
            //setFont(boldFont);
        } else
        {
            setBackground(Color.white);
//            setFont(plainFont);
        }
        setText((String) value);
        return this;
    }

    public void validate()
    {
    }

    public void revalidate()
    {
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
    }

    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue)
    {
    }


}