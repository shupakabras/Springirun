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

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.xml.XmlAttributeImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.springirun.completion.SpringirunCompletionUtils;

import java.util.Optional;

import static org.springirun.completion.SpringirunCompletionUtils.*;

/**
 * p-context reference support.
 *
 * @author Andrii Borovyk
 */
public class PContextReference extends PsiReferenceBase<PsiElement> {

    public PContextReference(@NotNull PsiElement element) {
        super(element);
    }

    @Override
    public PsiElement resolve() {

        Optional<XmlAttribute> attribute = firstParentOf(XmlAttribute.class, myElement);
        Optional<XmlTag> bean = firstParentOf(XmlTag.class, attribute, tagWithName(BEAN));

        return bean.map(SpringirunCompletionUtils::resolveBean)
            .map(psi -> resolveSetterMethod(psi, attribute.get().getLocalName())).orElse(null);

    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    public TextRange getRangeInElement() {
        if (getElement().getReferences().length > 1) {
            return getElement().getReferences()[1].getRangeInElement();
        }
        return super.getRangeInElement();
    }
}
