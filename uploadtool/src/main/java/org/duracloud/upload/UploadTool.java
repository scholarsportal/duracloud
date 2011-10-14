/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.upload;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 10/13/11
 */
public class UploadTool {

    private String host;
    private String username;
    private String password;
    private String spaceId;

    private JTable itemTable;
    private DefaultTableModel itemTableModel;
    private JButton addItemButton;
    private JButton removeItemButton;
    private JButton uploadButton;
    private JFileChooser fileChooser;
    private ChangeListener listener;
    private Uploader uploader;
    private boolean uploading = false;

    public UploadTool(String host,
                      String username,
                      String password,
                      String spaceId) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.spaceId = spaceId;
    }

    public void start() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createUI();
            }
        });
    }

    private void createUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("DuraCloud Upload Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents(new ChangeListener());
        JPanel panel = createFormLayout();
        frame.getContentPane().add(panel);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    private void initComponents(ActionListener actionListener) {
        String[] itemColumnNames = {"Folder Name", "Total Size", "Location"};
        itemTableModel = new DefaultTableModel(itemColumnNames, 0);
        itemTable = new JTable(itemTableModel);

        addItemButton = new JButton("Add Folders For Upload");
        URL addIcon = this.getClass().getClassLoader().getResource("add.png");
        addItemButton.setIcon(new ImageIcon(addIcon));
        addItemButton.addActionListener(actionListener);

        removeItemButton = new JButton("Remove Selected Folders");
        URL removeIcon =
            this.getClass().getClassLoader().getResource("minus.png");
        removeItemButton.setIcon(new ImageIcon(removeIcon));
        removeItemButton.addActionListener(actionListener);

        uploadButton = new JButton("Start Upload");
        URL startUploadIcon =
            this.getClass().getClassLoader().getResource("arrow_up.png");
        uploadButton.setIcon(new ImageIcon(startUploadIcon));
        uploadButton.addActionListener(actionListener);

        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    private JPanel createFormLayout() {
        String columnSpecs= // 6 columns
            "left:max(40dlu;pref)," + "5dlu," + "left:max(40dlu;pref)," +
            "5dlu," + "left:max(40dlu;pref)," + "pref:grow";
        String rowSpecs= // 3 rows
            "90dlu:grow," + "5dlu," + "pref";
        FormLayout layout = new FormLayout(columnSpecs, rowSpecs);

        JPanel panel = new JPanel(layout);
        JScrollPane tablePane = new JScrollPane(itemTable);

        CellConstraints cc = new CellConstraints();
        panel.add(tablePane, cc.xyw(1, 1, 6));
        panel.add(addItemButton, cc.xy(1, 3));
        panel.add(removeItemButton, cc.xy(3, 3));
        panel.add(uploadButton, cc.xy(5, 3));

        return panel;
    }

    private class ChangeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == addItemButton) {
                int returnVal = fileChooser.showOpenDialog(itemTable);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File dir = fileChooser.getSelectedFile();
                    // Add file to itemTable
                    String name = dir.getName();
                    String size = FileUtils
                        .byteCountToDisplaySize(FileUtils.sizeOfDirectory(dir));
                    String location = dir.getAbsolutePath();
                    itemTableModel.addRow(new String[]{name, size, location});
                }
            } else if(e.getSource() == removeItemButton) {
                int[] selectedRows = itemTable.getSelectedRows();
                Arrays.sort(selectedRows);
                for(int i = selectedRows.length-1; i >= 0; i--) {
                    itemTableModel.removeRow(selectedRows[i]);
                }
            } else if(e.getSource() == uploadButton && !uploading) {
                List<File> dirs = new ArrayList();
                int rowCount = itemTableModel.getRowCount();
                for(int i=0; i<rowCount; i++) {
                    String path =
                        String.valueOf(itemTableModel.getValueAt(i, 2));
                    File directory = new File(path);
                    if(directory.exists() && directory.isDirectory()) {
                        dirs.add(directory);
                    }
                }
                setButtonsUploading();
                startUpload(dirs);
                uploading = true;
            } else if(e.getSource() == uploadButton && uploading) {
                uploader.stopUpload();
                setButtonsPreUpload();
                uploading = false;
            }
        }
    }

    private void setButtonsPreUpload() {
        URL startUploadIcon =
            this.getClass().getClassLoader().getResource("arrow_up.png");
        uploadButton.setIcon(new ImageIcon(startUploadIcon));
        uploadButton.setText("Start Upload");
        addItemButton.setEnabled(true);
        removeItemButton.setEnabled(true);
    }

    private void setButtonsUploading() {
        URL startUploadIcon =
            this.getClass().getClassLoader().getResource("cancel.png");
        uploadButton.setIcon(new ImageIcon(startUploadIcon));
        uploadButton.setText("Stop Upload");
        addItemButton.setEnabled(false);
        removeItemButton.setEnabled(false);
    }

    private void startUpload(List<File> dirs) {
        try {
            uploader = new Uploader(host, username, password, spaceId, dirs);
            uploader.startUpload();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(itemTable, e.getMessage());
        }
    }

    public static void main(String[] args) {
        if(args.length != 4) {
            System.out.println("Parameters expected: host, username, " +
                               "password, spaceId");
            System.exit(1);
        }

        String host = args[0];
        String username = args[1];
        String password = args[2];
        String spaceId = args[3];

        UploadTool uploadTool =
            new UploadTool(host, username, password, spaceId);
        uploadTool.start();
    }

}
