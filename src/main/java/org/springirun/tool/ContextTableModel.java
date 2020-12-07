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

import javax.swing.table.AbstractTableModel;

/**
 * Information about class functionality.
 *
 * @author Andrii Borovyk
 */
class ContextTableModel extends AbstractTableModel {

    private ContextContainer contextContainer;

    ContextTableModel(final ContextContainer contextContainer) {
        this.contextContainer = contextContainer;
    }

    public void setContextContainer(final ContextContainer contextContainer) {
        this.contextContainer = contextContainer;
    }

    public ContextContainer getContextContainer() {
        return contextContainer;
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
