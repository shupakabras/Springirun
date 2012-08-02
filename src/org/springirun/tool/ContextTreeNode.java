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

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Information about class functionality.
 *
 * @author Andrey Borovik
 */
public class ContextTreeNode implements TreeNode {

    private ContextContainerEntity contextContainerEntity;

    public ContextTreeNode(final ContextContainerEntity contextContainerEntity) {
        this.contextContainerEntity = contextContainerEntity;
    }

    @Override
    public TreeNode getChildAt(final int i) {
        return new ContextTreeNode(contextContainerEntity.getChildContextContainers().get(i));
    }

    @Override
    public int getChildCount() {
        return contextContainerEntity.getChildContextContainers() == null ? 0 : contextContainerEntity
            .getChildContextContainers().size();
    }

    @Override
    public TreeNode getParent() {
        return contextContainerEntity.isRoot() ? null : new ContextTreeNode(contextContainerEntity
            .getParentContextContainerEntity());
    }

    @Override
    public int getIndex(final TreeNode treeNode) {
        return 0;
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Enumeration children() {
        return Collections.enumeration(contextContainerEntity.getChildContextContainers());
    }

    @Override
    public String toString() {
        return contextContainerEntity.toString();
    }
}
