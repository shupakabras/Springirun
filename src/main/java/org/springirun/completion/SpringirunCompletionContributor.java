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

package org.springirun.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.springirun.model.Alias;
import org.springirun.model.Bean;
import org.springirun.model.Beans;

import java.util.Arrays;
import java.util.Optional;

import static org.springirun.completion.SpringirunCompletionUtils.*;

/**
 * Spring configuration files completion contributor.
 *
 * @author Andrii Borovyk
 */
public class SpringirunCompletionContributor extends CompletionContributor {

  // p-context contributor
  CompletionProvider<CompletionParameters> pContextCompletionProvider =
      new CompletionProvider<CompletionParameters>() {
        @Override protected void addCompletions(@NotNull final CompletionParameters parameters,
            final ProcessingContext context, @NotNull final CompletionResultSet result) {

          final PsiElement element = parameters.getPosition();

          if (!(element.getParent().getParent() instanceof XmlTag)) {
            return;
          }
          final XmlTag parent = (XmlTag) element.getParent().getParent();
          final String prefix = result.getPrefixMatcher().getPrefix();
          final int pos = prefix.indexOf(':');
          final String namespacePrefix = pos > 0 ? prefix.substring(0, pos + 1) : "";
          final String namePrefix = prefix.length() > pos ? prefix.substring(pos + 1) : "";

          PsiClass psiClass = SpringirunCompletionUtils.resolveBean(parent);

          if (psiClass != null) {
            for (String method : SpringirunCompletionUtils
                .resolveSetters(psiClass, namePrefix, namespacePrefix)) {
              result.addElement(
                  LookupElementBuilder.create(method).withIcon(PlatformIcons.METHOD_ICON));
              result.addElement(LookupElementBuilder.create(method + _REF)
                  .withIcon(BEAN_METHOD_ICON));
            }
          }
        }
      };

  //property#name contributor
  CompletionProvider<CompletionParameters> propertyNameCompletionProvider =
      new CompletionProvider<CompletionParameters>() {

        @Override protected void addCompletions(@NotNull final CompletionParameters parameters,
            final ProcessingContext context, @NotNull final CompletionResultSet result) {
          final PsiElement element = parameters.getPosition();

          if (!(element.getParent().getParent().getParent().getParent() instanceof XmlTag)) {
            return;
          }
          final XmlTag parent = (XmlTag) element.getParent().getParent().getParent().getParent();
          final String prefix = result.getPrefixMatcher().getPrefix();

          PsiClass psiClass = SpringirunCompletionUtils.resolveBean(parent);

          if (psiClass != null) {
            for (String method : SpringirunCompletionUtils.resolveSetters(psiClass, prefix)) {
              result.addElement(
                  LookupElementBuilder.create(method).withIcon(PlatformIcons.PROPERTY_ICON));
            }
          }

        }
      };

  //factory-method contributor
  CompletionProvider<CompletionParameters> valuedMethodCompletionProvider =
      new CompletionProvider<CompletionParameters>() {
        @Override protected void addCompletions(@NotNull final CompletionParameters parameters,
            final ProcessingContext context, @NotNull final CompletionResultSet result) {
          final PsiElement element = parameters.getPosition();

          //TODO: find a better way to resolve XmlTag and XmlAttribute
          Optional<XmlTag> parent =
              Optional.ofNullable(element).map(PsiElement::getParent).map(PsiElement::getParent).map(PsiElement::getParent)
                  .filter(XmlTag.class::isInstance).map(XmlTag.class::cast);
          if (!parent.isPresent()) {
            return;
          }
          Optional<XmlAttribute> attribute = Optional.ofNullable(element)
              .map(PsiElement::getParent).map(PsiElement::getParent).filter(XmlAttribute.class::isInstance).map(XmlAttribute.class::cast);
          final String prefix = result.getPrefixMatcher().getPrefix();

          PsiClass psiClass = SpringirunCompletionUtils.resolveBean(parent.get(), attribute);

          if (psiClass != null) {
            Arrays.stream(psiClass.getAllMethods())
                .filter(method.and(accessible).and(valueReturn).and(noArgs).and(withNamePrefix(prefix)))
                .map(PsiMethod::getName)
                .map(m -> LookupElementBuilder.create(m).withIcon(PlatformIcons.METHOD_ICON))
                .forEach(result::addElement);

          }
        }
      };

  //init-method contributor
  CompletionProvider<CompletionParameters> voidMethodCompletionProvider =
      new CompletionProvider<CompletionParameters>() {
        @Override protected void addCompletions(@NotNull final CompletionParameters parameters,
            final ProcessingContext context, @NotNull final CompletionResultSet result) {
          final PsiElement element = parameters.getPosition();

          //TODO: find a better way to resolve XmlTag and XmlAttribute
          Optional<XmlAttribute> attribute = Optional.ofNullable(element)
              .map(PsiElement::getParent).map(PsiElement::getParent).filter(XmlAttribute.class::isInstance).map(XmlAttribute.class::cast);

          Optional<XmlTag> parent =
              attribute.map(PsiElement::getParent).filter(XmlTag.class::isInstance).map(XmlTag.class::cast);
          if (!parent.isPresent()) {
            return;
          }
          final String prefix = result.getPrefixMatcher().getPrefix();

          PsiClass psiClass = SpringirunCompletionUtils.resolveBean(parent.get(), attribute);

          if (psiClass != null) {
            Arrays.stream(psiClass.getAllMethods())
                .filter(method.and(accessible).and(noReturn).and(noArgs).and(withNamePrefix(prefix)))
                .map(PsiMethod::getName)
                .map(m -> LookupElementBuilder.create(m).withIcon(PlatformIcons.METHOD_ICON))
                .forEach(result::addElement);
          }
        }
      };

  CompletionProvider<CompletionParameters> constructorArgumentCompletionProvider =
      new CompletionProvider<CompletionParameters>() {
        @Override protected void addCompletions(@NotNull final CompletionParameters parameters,
            final ProcessingContext context, @NotNull final CompletionResultSet result) {
          final PsiElement element = parameters.getPosition();

          Optional<XmlAttribute> attribute = Optional.ofNullable(element)
              .map(PsiElement::getParent).map(PsiElement::getParent).filter(XmlAttribute.class::isInstance).map(XmlAttribute.class::cast);

          Optional<XmlTag> parent =
              attribute.map(PsiElement::getParent).map(PsiElement::getParent)
                  .filter(XmlTag.class::isInstance).map(XmlTag.class::cast);
          if (!parent.isPresent()) {
            return;
          }

          final String prefix = result.getPrefixMatcher().getPrefix();

          PsiClass psiClass = SpringirunCompletionUtils.resolveBean(parent.get(), attribute);

          if (psiClass != null) {
            Arrays.stream(psiClass.getAllMethods())
                .filter(constructor.and(accessible).and(anyArgs))
                .map(PsiMethod::getParameterList)
                .map(PsiParameterList::getParameters)
                .flatMap(Arrays::stream)
                .map(PsiParameter::getName)
                .filter(n -> n.startsWith(prefix))
                .map(m -> LookupElementBuilder.create(m).withIcon(PlatformIcons.FIELD_ICON))
                .forEach(result::addElement);
          }
        }
      };

  CompletionProvider<CompletionParameters> beansReferenceCompletionProvider =
      new CompletionProvider<CompletionParameters>() {
        @Override

        protected void addCompletions(@NotNull final CompletionParameters parameters,
            final ProcessingContext context, @NotNull final CompletionResultSet result) {
          final PsiElement element = parameters.getPosition();
          final String prefix = result.getPrefixMatcher().getPrefix();

          XmlAttribute xmlAttribute = (XmlAttribute) element.getParent().getParent();

          final Beans beans = SpringirunCompletionUtils.getDocumentRoot(xmlAttribute);

          for (Bean bean : beans.getBeans()) {
            if (bean.getId().getValue() != null && bean.getId().getValue().startsWith(prefix)) {
              result.addElement(LookupElementBuilder.create(bean.getId().getValue())
                  .withIcon(SpringirunCompletionUtils.BEAN_ICON));
            }
            if (bean.getName().getValue() != null && bean.getName().getValue().startsWith(prefix)) {
              result.addElement(LookupElementBuilder.create(bean.getName().getValue())
                  .withIcon(SpringirunCompletionUtils.BEAN_ICON));
            }
          }
          for (Alias alias : beans.getAliases()) {
            if (alias.getAlias().getValue() != null && alias.getAlias().getValue()
                .startsWith(prefix)) {
              result.addElement(LookupElementBuilder.create(alias.getAlias().getValue())
                  .withIcon(SpringirunCompletionUtils.BEAN_ALIAS_ICON));
            }
          }
        }
      };

  public SpringirunCompletionContributor() {
    extend(CompletionType.BASIC,
        XmlPatterns.psiElement().inside(XmlPatterns.xmlAttribute().withNamespace(P_NAMESPACE)),
        pContextCompletionProvider);

    extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()
            .inside(XmlPatterns.xmlAttribute(NAME)
                .inside(XmlPatterns.xmlTag().withLocalName(PROPERTY).withNamespace(BEAN_NAMESPACE)))),
        propertyNameCompletionProvider);

    extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()
            .inside(XmlPatterns.xmlAttribute(NAME)
                .inside(XmlPatterns.xmlTag().withLocalName(CONSTRUCTOR_ARG).withNamespace(BEAN_NAMESPACE)))),
        constructorArgumentCompletionProvider);

    extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()
            .inside(XmlPatterns.xmlAttribute().withName(StandardPatterns.string().oneOf(FACTORY_METHOD))
                .inside(XmlPatterns.xmlTag().withLocalName(BEAN).withNamespace(BEAN_NAMESPACE)))),
        valuedMethodCompletionProvider);

    extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()
            .inside(XmlPatterns.xmlAttribute()
                .withName(StandardPatterns.string().oneOf(INIT_METHOD, DESTROY_METHOD))
                .inside(XmlPatterns.xmlTag().withLocalName(BEAN).withNamespace(BEAN_NAMESPACE)))),
        voidMethodCompletionProvider);

    extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()
            .withParent(XmlPatterns.xmlAttribute().withName(StandardPatterns.string()
                .oneOf(VALUE_REF, BEAN_REF, PARENT, SpringirunCompletionUtils.FACTORY_BEAN)))),
        beansReferenceCompletionProvider);

    extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()
            .withParent(XmlPatterns.xmlAttribute(BEAN)
                .withParent(XmlPatterns.xmlTag().withLocalName(REF).withNamespace(BEAN_NAMESPACE)))),
        beansReferenceCompletionProvider);

    extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()
            .withParent(XmlPatterns.xmlAttribute(REF).withParent(
                XmlPatterns.xmlTag().withLocalName(PROPERTY).withNamespace(BEAN_NAMESPACE)))),
        beansReferenceCompletionProvider);

    extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()
        .withParent(XmlPatterns.xmlAttribute().withName(StandardPatterns.string().endsWith(_REF))
            .withNamespace(P_NAMESPACE))), beansReferenceCompletionProvider);

    extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()
            .withParent(XmlPatterns.xmlAttribute(NAME)
                .withParent(XmlPatterns.xmlTag().withLocalName(ALIAS).withNamespace(BEAN_NAMESPACE)))),
        beansReferenceCompletionProvider);

  }
}
