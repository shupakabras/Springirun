/*
 * Copyright 2012 Andrii Borovyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springirun.tool;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Information about class functionality.
 *
 * @author Andrii Borovyk
 */
public class ContextManagerEditorDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTree currentContextTree;
    private JTable contextTable;
    private JButton buttonAdd;
    private JButton buttonRemove;
    private JPopupMenu treePopupMenu;

    private Project project;
    private ContextContainer contextContainer;

    public ContextManagerEditorDialog(final Project project) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        this.project = project;
        this.contextContainer = ContextPersistentStateComponent.getInstance(project).cloneState();
        contextTable.setModel(new ContextTableModel(contextContainer));
        currentContextTree.setModel(new DefaultTreeModel(null));

        buttonRemove.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent mouseEvent) {
                ContextContainerEntity contextContainerEntity =
                    (ContextContainerEntity) contextTable.getValueAt(contextTable.getSelectedRow(), -1);

                ((ContextTableModel) contextTable.getModel()).getContextContainer().getContextContainerRootEntities()
                                                             .remove(contextContainerEntity);

                contextTable.updateUI();
                currentContextTree.setModel(new DefaultTreeModel(null));
                currentContextTree.updateUI();
            }
        });

        contextTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent mouseEvent) {
                if (!(((ContextTableModel)contextTable.getModel()).getContextContainer()
                        .getContextContainerRootEntities().isEmpty())) {
                    ContextContainerEntity contextContainerEntity =
                    (ContextContainerEntity) contextTable.getModel().getValueAt(contextTable.getSelectedRow(), 1);
                    currentContextTree.setModel(new DefaultTreeModel(new ContextTreeNode(contextContainerEntity)));
                    currentContextTree.updateUI();
                }
            }
        });

        currentContextTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent mouseEvent) {
                if (SwingUtilities.isRightMouseButton(mouseEvent)) {
                    int row = currentContextTree.getClosestRowForLocation(mouseEvent.getX(), mouseEvent.getY());
                    currentContextTree.setSelectionRow(row);
                    treePopupMenu.show(currentContextTree, mouseEvent.getX(), mouseEvent.getY());
                }
            }
        });

        treePopupMenu = new JPopupMenu();
        JMenuItem addToContext = new JMenuItem("Add to context");
        JMenuItem removeFromContext = new JMenuItem("Remove from context");
        treePopupMenu.add(addToContext);
        treePopupMenu.add(removeFromContext);

        removeFromContext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ContextTreeNode contextTreeNode = (ContextTreeNode) currentContextTree.getLastSelectedPathComponent();
                if (contextTreeNode.getParent() != null) {
                    ((ContextTreeNode) contextTreeNode.getParent()).remove(contextTreeNode);
                    currentContextTree.updateUI();
                    TreeUtil.expandAll(currentContextTree);
                }

            }
        });

        addToContext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                ContextTreeNode contextTreeNode = (ContextTreeNode) currentContextTree.getLastSelectedPathComponent();
                final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(
                    XmlFileType.INSTANCE);
                final VirtualFile file = FileChooser.chooseFile(project, descriptor);
                if (file != null) {
                    ContextContainerEntity contextContainerEntity = new ContextContainerEntity();

                    String relPath = FileUtil.getRelativePath(project.getBasePath(), file.getPresentableUrl(),
                        File.separatorChar);
                    contextContainerEntity.setName(relPath);
                    contextContainerEntity.setParentContextContainerEntity(contextTreeNode.getContextContainerEntity());
                    List<ContextContainerEntity> children = contextTreeNode.getContextContainerEntity().getChildContextContainers();
                    if (children == null) {
                        children = new ArrayList<ContextContainerEntity>();
                        contextTreeNode.getContextContainerEntity().setChildContextContainers(children);
                    }
                    contextTreeNode.insert(new ContextTreeNode(contextContainerEntity), -1);
                    currentContextTree.updateUI();
                    TreeUtil.expandAll(currentContextTree);
                }
            }
        });
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        ContextPersistentStateComponent.getInstance(project).loadState(contextContainer);
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

}
