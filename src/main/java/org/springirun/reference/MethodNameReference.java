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
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.springirun.completion.SpringirunCompletionUtils;

import java.util.Optional;

/**
 * init-method, factory-method reference support.
 *
 * @author Andrii Borovyk
 */
public class MethodNameReference extends PsiReferenceBase<PsiElement> {


  public MethodNameReference(@NotNull PsiElement element) {
    super(element);
  }

  @Override public PsiElement resolve() {
    Optional<XmlAttribute> attribute = Optional.ofNullable(myElement)
        .filter(XmlAttributeValue.class::isInstance).map(XmlAttributeValue.class::cast)
        .map(XmlAttributeValue::getParent).filter(XmlAttribute.class::isInstance).map(XmlAttribute.class::cast);

    Optional<XmlTag> parent = attribute.map(XmlAttribute::getParent).filter(XmlTag.class::isInstance)
        .map(XmlTag.class::cast);

    if (parent.isPresent()) {
      PsiClass resolvedClass = SpringirunCompletionUtils.resolveBean(parent.get(), attribute);
      return SpringirunCompletionUtils.resolveMethod(resolvedClass, attribute.get().getValue());
    }
    return null;
  }

  @NotNull @Override public Object[] getVariants() {
    return new Object[0];
  }
}
