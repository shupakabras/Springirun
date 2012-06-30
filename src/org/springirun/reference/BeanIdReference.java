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
package org.springirun.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NotNull;
import org.springirun.completion.SpringirunCompletionUtils;
import org.springirun.model.Alias;
import org.springirun.model.Bean;
import org.springirun.model.Beans;


/**
 * Reference to Bean id's and names.
 *
 * @author Andrii Borovyk
 */
public class BeanIdReference extends PsiReferenceBase<PsiElement> {

    private PsiElement bean;

    public BeanIdReference(@NotNull final PsiElement element) {
        super(element);

        XmlAttribute xmlAttribute = (XmlAttribute) getElement().getParent();

        final Beans beans = SpringirunCompletionUtils.getDocumentRoot(xmlAttribute);

        Bean bean = resolveBeanByName(beans, xmlAttribute.getValue());
        if (bean != null) {
            this.bean = bean.getXmlElement();
        }

        //no beans yet found... try to resolve using alias
        Alias alias = resolveAliasByName(beans, xmlAttribute.getValue());
        if (alias != null) {
            bean = resolveBeanByName(beans, alias.getName().getValue());
            if (bean != null) {
                this.bean = bean.getXmlElement();
            }
        }
    }

    @Override
    public PsiElement resolve() {
        return bean;
    }

    private Bean resolveBeanByName(Beans beans, @NotNull String name) {
        for (Bean bean : beans.getBeans()) {
            if (name.equals(bean.getId().getValue()) || name.equals(bean.getName().getValue())) {
                return bean;
            }
        }
        return null;
    }

    private Alias resolveAliasByName(Beans beans, @NotNull String name) {
        for (Alias alias : beans.getAliases()) {
            if (name.equals(alias.getAlias().getValue())) {
                return alias;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
