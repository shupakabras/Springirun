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

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.List;

/**
 * Information about class functionality.
 *
 * @author Andrii Borovyk
 */
@Tag("ContextContainerEntity")
public class ContextContainerEntity {

    @Attribute("name")
    private String name;

    @Attribute("root")
    private boolean isRoot;

    @Attribute("contextPath")
    private String contextPath;

    @Transient
    private PsiFile contextFile;

    @Transient
    private ContextContainerEntity parentContextContainerEntity;

    @AbstractCollection(elementTag = "ChildContextContainers")
    private List<ContextContainerEntity> childContextContainers;

    public ContextContainerEntity() {
    }

    public ContextContainerEntity(final String name, final boolean root,
        final ContextContainerEntity parentContextContainerEntity,
        final List<ContextContainerEntity> childContextContainers) {
        this.name = name;
        isRoot = root;
        this.parentContextContainerEntity = parentContextContainerEntity;
        this.childContextContainers = childContextContainers;
    }

    public String getName() {
        return name;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public ContextContainerEntity getParentContextContainerEntity() {
        return parentContextContainerEntity;
    }

    public List<ContextContainerEntity> getChildContextContainers() {
        return childContextContainers;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setRoot(final boolean root) {
        isRoot = root;
    }

    public void setParentContextContainerEntity(final ContextContainerEntity parentContextContainerEntity) {
        this.parentContextContainerEntity = parentContextContainerEntity;
    }

    public void setChildContextContainers(final List<ContextContainerEntity> childContextContainers) {
        this.childContextContainers = childContextContainers;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }

    public PsiFile getContextFile() {
        return contextFile;
    }

    public void setContextFile(final PsiFile contextFile) {
        this.contextFile = contextFile;
    }

    @Override
    public String toString() {
        return getName();
    }
}
