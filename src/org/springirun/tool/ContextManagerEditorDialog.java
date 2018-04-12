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
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ui.tree.TreeUtil;
import org.springirun.completion.SpringirunCompletionUtils;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.*;
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

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

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
                if (!(((ContextTableModel) contextTable.getModel()).getContextContainer()
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

        removeFromContext.addActionListener(e -> {
            ContextTreeNode contextTreeNode = (ContextTreeNode) currentContextTree.getLastSelectedPathComponent();
            if (contextTreeNode.getParent() != null) {
                ((ContextTreeNode) contextTreeNode.getParent()).remove(contextTreeNode);
                currentContextTree.updateUI();
                TreeUtil.expandAll(currentContextTree);
            }
        });

        addToContext.addActionListener(actionEvent -> {
            ContextTreeNode contextTreeNode = (ContextTreeNode) currentContextTree.getLastSelectedPathComponent();
            final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(
                    XmlFileType.INSTANCE);
            final VirtualFile file = FileChooser.chooseFile(descriptor, project, null);
            if (file != null) {
                PsiFile psiFile = SpringirunCompletionUtils.resolvePsiFile(project, file);
                if (psiFile == null ||
                        !(psiFile instanceof XmlFile) ||
                        !XmlPatterns.xmlTag().withLocalName(SpringirunCompletionUtils.BEANS).withNamespace(
                                SpringirunCompletionUtils.BEAN_NAMESPACE).accepts(((XmlFile) psiFile).getRootTag())) {
                    Messages.showMessageDialog(project, "File is not valid spring beans file.", "Springirun",
                            null);
                    return;
                }
                ContextContainerEntity contextContainerEntity = createContextContainerEntity(
                        contextTreeNode.getContextContainerEntity(), file, project);
                contextContainerEntity.setContextFile(psiFile);
                contextTreeNode.insert(new ContextTreeNode(contextContainerEntity), -1);
                currentContextTree.updateUI();
                TreeUtil.expandAll(currentContextTree);
            }
        });
        buttonAdd.addActionListener(e -> {
            String context = Messages.showInputDialog(project, "Create new context", "Springirun", null);
            if (context != null && !context.isEmpty()) {
                ContextContainerEntity contextContainerEntity = new ContextContainerEntity();
                contextContainerEntity.setName(context);
                contextContainerEntity.setRoot(true);
                contextContainer.getContextContainerRootEntities().add(contextContainerEntity);
                contextTable.updateUI();
            }
        });
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private ContextContainerEntity createContextContainerEntity(final ContextContainerEntity parentContextContainer,
                                                                final VirtualFile file, final Project project) {
        ContextContainerEntity contextContainerEntity = new ContextContainerEntity();

        String relPath = FileUtil.getRelativePath(project.getBasePath(), file.getPresentableUrl(), File.separatorChar);
        contextContainerEntity.setName(file.getName());
        contextContainerEntity.setContextPath(relPath);
        contextContainerEntity.setParentContextContainerEntity(parentContextContainer);
        List<ContextContainerEntity> children = parentContextContainer.getChildContextContainers();
        if (children == null) {
            children = new ArrayList<ContextContainerEntity>();
            parentContextContainer.setChildContextContainers(children);
        }
        return contextContainerEntity;
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
