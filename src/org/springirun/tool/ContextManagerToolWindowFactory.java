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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Information about class functionality.
 *
 * @author Andrey Borovik
 */
public class ContextManagerToolWindowFactory implements ToolWindowFactory {

    private JTree currentContextTree;
    private JTable contextTable;
    private JButton btnAddNewContext;
    private JButton btnRemoveContext;
    private JButton btnOk;
    private JButton btnCancel;
    private JButton btnApply;
    private JPanel toolWindowContent;
    private JPopupMenu treePopupMenu;

    class ContextTableModel extends AbstractTableModel {

        private ContextContainer contextContainer;

        ContextTableModel(final ContextContainer contextContainer) {
            this.contextContainer = contextContainer;
            treePopupMenu = new JPopupMenu();
            treePopupMenu.add(new JMenuItem("add to context"));
            treePopupMenu.add(new JMenuItem("remove from tree"));
        }

        @Override
        public int getRowCount() {
            return contextContainer.getContextContainerRootEntities().size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            try {
                return contextContainer.getContextContainerRootEntities().get(rowIndex);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    @Override
    public void createToolWindowContent(final Project project, final ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);


        final ContextContainer contextContainer = createMockContextContainer();
        contextTable.setModel(new ContextTableModel(contextContainer));
        currentContextTree.setModel(new DefaultTreeModel(null));

        btnRemoveContext.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent mouseEvent) {
                ContextContainerEntity contextContainerEntity =
                    (ContextContainerEntity) contextTable.getValueAt(contextTable.getSelectedRow(), -1);
                contextContainer.getContextContainerRootEntities().remove(contextContainerEntity);
                contextTable.updateUI();
                currentContextTree.setModel(new DefaultTreeModel(null));
                currentContextTree.updateUI();
            }
        });

        contextTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent mouseEvent) {
                ContextContainerEntity contextContainerEntity =
                    (ContextContainerEntity) contextTable.getModel().getValueAt(contextTable.getSelectedRow(), 1);
                for (ContextContainerEntity containerEntity : contextContainer.getContextContainerRootEntities()) {
                    if (containerEntity == contextContainerEntity) {
                        currentContextTree.setModel(new DefaultTreeModel(new ContextTreeNode(containerEntity)));
                        currentContextTree.updateUI();
                        break;
                    }
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
    }

    private ContextContainer createMockContextContainer() {
        ContextContainer contextContainer = new ContextContainer();


        ContextContainerEntity rootOne = null;
        ContextContainerEntity childOneRootOne = new ContextContainerEntity("child_one.xml", false, rootOne, null);
        ContextContainerEntity childTwoRootOne = new ContextContainerEntity("child_two.xml", false, rootOne, null);
        rootOne = new ContextContainerEntity("context one", true, null, Arrays.asList(childOneRootOne, childTwoRootOne));

        ContextContainerEntity rootTwo = null;
        ContextContainerEntity childTwoRootTwo = null;
        ContextContainerEntity childOneRootTwo = new ContextContainerEntity("child_three.xml", false, rootTwo, null);
        ContextContainerEntity childOneChildTwoRootTwo = new ContextContainerEntity("child_five.xml", false,
            childTwoRootTwo, null);
        childTwoRootTwo = new ContextContainerEntity("child_four.xml", false, rootTwo, Arrays.asList(childOneChildTwoRootTwo));
        rootTwo = new ContextContainerEntity("context two", true, null, Arrays.asList(childOneRootTwo,
            childTwoRootTwo));


        List<ContextContainerEntity> contextContainerRootEntities = new ArrayList<ContextContainerEntity>();
        contextContainerRootEntities.add(rootOne);
        contextContainerRootEntities.add(rootTwo);
        contextContainer.setContextContainerRootEntities(contextContainerRootEntities);
        return contextContainer;
    }

}
