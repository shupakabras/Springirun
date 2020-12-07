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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.springirun.completion.SpringirunCompletionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about class functionality.
 *
 * @author Andrii Borovyk
 */
@State(
        name = "SpringirunConfiguration",
        storages = @Storage("springirun.xml")
)
public class ContextPersistentStateComponent implements PersistentStateComponent<Element> {

    private ContextContainer contextContainer = new ContextContainer();

    protected Project project;

    public ContextPersistentStateComponent(Project project) {
        this.project = project;
    }

    public static ContextPersistentStateComponent getInstance(Project project) {
        return ServiceManager.getService(project, ContextPersistentStateComponent.class);
    }

    @Override
    public Element getState() {
        Element contentContainerElement = new Element("ContextContainer");
        if (contextContainer.getContextContainerRootEntities() != null) {
            for (ContextContainerEntity contextContainerEntity : contextContainer.getContextContainerRootEntities()) {
                contentContainerElement.addContent(createContextContainerEntityElement(contextContainerEntity));
            }
        }
        return contentContainerElement;
    }

    public void loadState(final ContextContainer contextContainer) {
        this.contextContainer = contextContainer;
    }

    public ContextContainer cloneState() {
        ContextContainer contextContainer = new ContextContainer();
        Element state = getState();
        try {
            for (Object element : state.getChildren("ContextContainerEntity")) {
                contextContainer.getContextContainerRootEntities()
                        .add(createContextContainerEntity((Element) element, null));
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }
        return contextContainer;
    }

    @Override
    public void loadState(final Element state) {
        contextContainer.getContextContainerRootEntities().clear();
        try {
            for (Object element : state.getChildren("ContextContainerEntity")) {
                contextContainer.getContextContainerRootEntities()
                        .add(createContextContainerEntity((Element) element, null));
            }
        } catch (DataConversionException e) {
            e.printStackTrace();
        }

    }

    private ContextContainerEntity createContextContainerEntity(Element element, ContextContainerEntity parentEntity)
            throws DataConversionException {
        ContextContainerEntity contextContainerEntity = new ContextContainerEntity();
        contextContainerEntity.setName(element.getAttribute("name").getValue());
        contextContainerEntity.setRoot(element.getAttribute("root").getBooleanValue());
        if (!contextContainerEntity.isRoot()) {
            contextContainerEntity.setContextPath(element.getAttribute("contextPath").getValue());
            contextContainerEntity.setContextFile(SpringirunCompletionUtils.resolvePsiFile(project,
                    contextContainerEntity.getContextPath()));
        }
        contextContainerEntity.setParentContextContainerEntity(parentEntity);
        List<ContextContainerEntity> contextContainerEntityList = new ArrayList<ContextContainerEntity>();
        for (Object childEntity : element.getChildren("ContextContainerEntity")) {
            contextContainerEntityList.add(createContextContainerEntity((Element) childEntity,
                    contextContainerEntity));
        }
        contextContainerEntity.setChildContextContainers(contextContainerEntityList);
        return contextContainerEntity;
    }

    private Element createContextContainerEntityElement(ContextContainerEntity contextContainer) {
        Element element = new Element("ContextContainerEntity");
        element.setAttribute("name", contextContainer.getName());
        element.setAttribute("root", String.valueOf(contextContainer.isRoot()));
        if (!contextContainer.isRoot()) {
            element.setAttribute("contextPath", contextContainer.getContextPath());
        }
        if (contextContainer.getChildContextContainers() != null) {
            for (ContextContainerEntity contextContainerEntity : contextContainer.getChildContextContainers()) {
                element.addContent(createContextContainerEntityElement(contextContainerEntity));
            }

        }
        return element;
    }
}
