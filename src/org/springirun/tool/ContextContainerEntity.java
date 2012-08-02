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

import java.util.List;

/**
 * Information about class functionality.
 *
 * @author Andrey Borovik
 */
public class ContextContainerEntity {

    private String name;

    private boolean isRoot;

    private ContextContainerEntity parentContextContainerEntity;

    private List<ContextContainerEntity> childContextContainers;

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

    @Override
    public String toString() {
        return getName();
    }
}