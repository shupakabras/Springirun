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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NotNull;
import org.springirun.completion.SpringirunCompletionUtils;
import org.springirun.model.Alias;
import org.springirun.model.Bean;
import org.springirun.model.Beans;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;


/**
 * Reference to Bean id's and names.
 *
 * @author Andrii Borovyk
 */
public class BeanIdReference extends PsiReferenceBase<PsiElement> {

    private PsiElement bean;

    public BeanIdReference(@NotNull final PsiElement element) {
        super(element);
    }

    @Override
    public PsiElement resolve() {
        Optional<XmlAttribute> attribute = SpringirunCompletionUtils.firstParentOf(XmlAttribute.class, getElement());
        if (attribute.isPresent()) {
            final Optional<Beans> beans = SpringirunCompletionUtils.getDocumentRoot(attribute);

            return SpringirunCompletionUtils.or(
                () -> SpringirunCompletionUtils.resolveBeanByName(beans, attribute.get().getValue()),
                () -> SpringirunCompletionUtils.resolveBeanByAlias(beans, attribute.get().getValue())
            ).map(Bean::getXmlElement).orElse(null);
        }
        return null;
    }



    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
