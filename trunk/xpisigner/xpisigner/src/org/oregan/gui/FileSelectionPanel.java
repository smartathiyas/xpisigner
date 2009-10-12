package org.oregan.gui;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

public class FileSelectionPanel extends JPanel implements ListSelectionListener
{
    private MultiSigner parentRef;
    private DefaultTreeModel treeModel;
    private DefaultListModel paths;
    private JTree tree;
    private File baseDir;
    private ArrayList<String> filePaths;
    private JFileChooser fileChooser;

    public FileSelectionPanel(MultiSigner parent)
    {
        super();
        this.parentRef = parent;
        buildContent();

        SwingUtilities.invokeLater(new Runnable(){

            public void run()
            {
                fileChooser = new JFileChooser(".");
            }
        });

    }


    public ArrayList<String> getAllFiles()
    {

        generateListing();    

        return filePaths;
    }

    private void buildContent()
    {
        setLayout(new BorderLayout());

        setBackground(Color.white);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // LHS
        // List of content roots (start with one)
        // Button at top to allow adding new folder.

        JPanel lhsPanel = new JPanel(new BorderLayout());
        lhsPanel.setBorder(BorderFactory.createEtchedBorder());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addContentButton = new JButton("Add Content Root...");
        buttons.add(addContentButton);
        lhsPanel.add(buttons, BorderLayout.NORTH);


        paths = new DefaultListModel();
        final JList list = new JList(paths);

        addContentButton.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {

                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setFileSelectionMode(
                    JFileChooser.DIRECTORIES_ONLY);
                int status = fileChooser.showOpenDialog(parentRef);
                if (status == JFileChooser.APPROVE_OPTION)
                {
                    File selectedFile =
                        fileChooser.getSelectedFile();
                    ContentRootInfo info = new ContentRootInfo(selectedFile);
                    paths.addElement(info);
                    list.setSelectedValue(info, true);
                }


            }
        });

        list.addListSelectionListener(this);
        list.setCellRenderer(new ContentRootsCellRenderer());
        JScrollPane listScroller = new JScrollPane(list);


        lhsPanel.add(listScroller, BorderLayout.CENTER);

        // RHS
        // Tree showing currently selected content root.
        // has buttons at the top to mark as excluded.
        this.treeModel = new DefaultTreeModel(new ExcludableTreeNode(""));
        tree = new JTree(treeModel);
        tree.setShowsRootHandles(false);
        tree.setRootVisible(false);
        tree.setCellRenderer(new MyTreeCellRenderer());
        JScrollPane treeScroller = new JScrollPane(tree);

        JPanel rhsPanel = new JPanel(new BorderLayout());
        rhsPanel.add(treeScroller, BorderLayout.CENTER);

        JPanel button2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton inButton = new JButton("Include");
        button2.add(inButton);
        inButton.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                if (!tree.isSelectionEmpty())
                {
                    ExcludableTreeNode root = (ExcludableTreeNode) tree.getModel().getRoot();
                    ContentRootInfo info = (ContentRootInfo) root.getUserObject();

                    DefaultListModel listModel = ((DefaultListModel) list.getModel());

                    int index = listModel.indexOf(info);

                    TreePath[] selected = tree.getSelectionPaths();
                    for (int i = 0; i < selected.length; i++)
                    {
                        TreePath treePath = selected[i];
                        if (treePath.getPathCount() > 1)
                        {
                            ExcludableTreeNode node = (ExcludableTreeNode) treePath.getLastPathComponent();
                            if(node.isExcluded())
                            {
                                node.setExcluded(false);
                                File toBeExcluded = (File) node.getUserObject();
                                info.removeExclusion(toBeExcluded);

                            }
                        }
                    }
                    listModel.setElementAt(info, index);
                }
            }
        });
        JButton exButton = new JButton("Exclude");

        exButton.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                if (!tree.isSelectionEmpty())
                {
                    ExcludableTreeNode root = (ExcludableTreeNode) tree.getModel().getRoot();
                    ContentRootInfo info = (ContentRootInfo) root.getUserObject();

                    DefaultListModel listModel = ((DefaultListModel) list.getModel());

                    int index = listModel.indexOf(info);

                    TreePath[] selected = tree.getSelectionPaths();
                    for (int i = 0; i < selected.length; i++)
                    {
                        TreePath treePath = selected[i];
                        if (treePath.getPathCount() > 1)
                        {
                            ExcludableTreeNode node = (ExcludableTreeNode) treePath.getLastPathComponent();
                            File toBeExcluded = (File) node.getUserObject();
                            info.exclude(toBeExcluded);
                            node.setExcluded(true);
                        }
                    }
                    listModel.setElementAt(info, index);
                }
            }
        });
        exButton.setFocusPainted(false);
        button2.add(exButton);
        rhsPanel.add(button2, BorderLayout.NORTH);
        rhsPanel.setBorder(BorderFactory.createEtchedBorder());

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lhsPanel, rhsPanel);
        splitter.setBorder(null);
        add(splitter, BorderLayout.CENTER);

    }

    private void generateListing()
    {
        if (!paths.isEmpty())
        {
            Enumeration en = paths.elements();
            while (en.hasMoreElements())
            {
                try
                {
                    ContentRootInfo cri = (ContentRootInfo) en.nextElement();
                    ArrayList<File> allFiles = new ArrayList<File>();

                    baseDir = cri.getRootFile();

                    File c14nBaseDir = new File(baseDir.getAbsoluteFile().getCanonicalPath());
                    String c14nBasePath = c14nBaseDir.getPath();
                    int basePathLength = c14nBasePath.length() + 1;

                    process(baseDir, allFiles);

                    exclude(cri.getExclusions(), allFiles);

                    filePaths = new ArrayList<String>(allFiles.size());

                    for (File file : allFiles)
                    {
                        String s = file.getPath();
                        s = s.substring(basePathLength);
                        s = s.replace('\\', '/');
                        filePaths.add(s);
                    }
                    ArrayList<String> removalList = new ArrayList<String>();

                    for (String path : filePaths)
                    {
                        if (path.startsWith("META-INF"))
                        {
                            removalList.add(path);
                        }
                    }
                    System.out.println("Excluding: " + removalList);
                    for (String path : removalList)
                    {
                        filePaths.remove(path);
                    }


                } catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
        else
        {
            baseDir = null;
            filePaths = null;
        }
    }

    private void exclude(File[] exclusions, ArrayList<File> allFiles)
    {
        for (int i = 0; i < exclusions.length; i++)
        {
            File exclusion = exclusions[i];
            allFiles.remove(exclusion);
        }
    }

    private void process(File dir, ArrayList<File> allFiles)
    {
        if (!dir.isDirectory())
        {
            allFiles.add(dir);
        } else
        {
            File[] list = dir.listFiles();
            for (int i = 0; i < list.length; i++)
            {
                File file = list[i];
                process(file, allFiles);
            }
        }
    }

    public void valueChanged(ListSelectionEvent e)
    {
        JList list = ((JList) e.getSource());
        if (!list.isSelectionEmpty())
        {
            ContentRootInfo current = (ContentRootInfo) list.getSelectedValue();
            ExcludableTreeNode ROOT = new ExcludableTreeNode(current);
            treeModel.setRoot(ROOT);
            tree.setRootVisible(true);

            File f = current.getRootFile();

            addFolderToTree(f, ROOT);

            treeModel.reload();
        }
    }

    private void addFolderToTree(File directory, ExcludableTreeNode parent)
    {
        String fstr = directory.getAbsolutePath();

        if (directory.isDirectory())
        {
            File[] allFiles = directory.listFiles();
            for (int i = 0; i < allFiles.length; i++)
            {
                File allFile = allFiles[i];
                ExcludableTreeNode child = new ExcludableTreeNode(allFile);
                treeModel.insertNodeInto(child, parent, parent.getChildCount());
                if (allFile.isDirectory())
                {
                    addFolderToTree(allFile, child);
                }
            }
        }
    }

    public File getBaseDir()
    {
        return baseDir;
    }
}

