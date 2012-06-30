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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.springirun.completion.SpringirunCompletionUtils;

/**
 * property#name reference support.
 *
 * @author Andrii Borovyk
 */
public class PNameReference extends PsiReferenceBase<PsiElement> {

    private PsiMethod resolvedMethod;

    public PNameReference(@NotNull PsiElement element) {
        super(element);

        if (!(element instanceof XmlAttribute)) {
            return;
        }
        String value = ((XmlAttribute) element).getValue();
        if (!(element.getParent().getParent() instanceof XmlTag)) {
            return;
        }
        final XmlTag parent = (XmlTag) element.getParent().getParent();
        PsiClass resolvedClass = SpringirunCompletionUtils.resolveBean(parent);
        resolvedMethod = SpringirunCompletionUtils.resolveSetterMethod(resolvedClass, value);

    }

    @Override
    public PsiElement resolve() {
        return resolvedMethod;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
