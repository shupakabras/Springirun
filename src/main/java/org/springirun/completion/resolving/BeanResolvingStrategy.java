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
package org.springirun.completion.resolving;

import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.springirun.tool.ContextContainerEntity;

import java.util.List;

/**
 * Information about class functionality.
 *
 * @author Andrii Borovyk
 */
public abstract class BeanResolvingStrategy {

    public XmlTag resolveBeanInContext(String beanId, String contextFileName) {


        ContextContainerEntity contextContainerEntity;
        return null;
    }

    protected boolean isAllowedRootContainer(ContextContainerEntity rootContextContainerEntity,
        String contextFileName) {
        for (ContextContainerEntity contextContainerEntity : rootContextContainerEntity.getChildContextContainers()) {

        }

        return true;

    }

    protected XmlTag resolveBeanInFile(String beanId, XmlFile xmlFile) {
        return null;
    }

    protected abstract List<PsiFile> getPsiFileList(ContextContainerEntity rootContextContainer);

}
