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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.springirun.model.Alias;
import org.springirun.model.Bean;
import org.springirun.model.Beans;

/**
 * Spring configuration files completion contributor.
 *
 * @author Andrii Borovyk
 */
public class SpringirunCompletionContributor extends CompletionContributor {

    // p-context contributor
    CompletionProvider<CompletionParameters> pContextCompletionProvider
            = new CompletionProvider<CompletionParameters>() {
        @Override
        protected void addCompletions(
                @NotNull final CompletionParameters parameters, final ProcessingContext context,
                @NotNull final CompletionResultSet result) {

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
                for (String method : SpringirunCompletionUtils.resolveSetters(psiClass, namePrefix, namespacePrefix)) {
                    result.addElement(LookupElementBuilder.create(method).withIcon(PlatformIcons.METHOD_ICON));
                    result.addElement(LookupElementBuilder.create(method + SpringirunCompletionUtils._REF).withIcon(SpringirunCompletionUtils.BEAN_METHOD_ICON));
                }
            }
        }
    };

    //property#name contributor
    CompletionProvider<CompletionParameters> propertyNameCompletionProvider
            = new CompletionProvider<CompletionParameters>() {

        @Override
        protected void addCompletions(
                @NotNull final CompletionParameters parameters, final ProcessingContext context,
                @NotNull final CompletionResultSet result) {
            final PsiElement element = parameters.getPosition();

            if (!(element.getParent().getParent().getParent().getParent() instanceof XmlTag)) {
                return;
            }
            final XmlTag parent = (XmlTag) element.getParent().getParent().getParent().getParent();
            final String prefix = result.getPrefixMatcher().getPrefix();

            PsiClass psiClass = SpringirunCompletionUtils.resolveBean(parent);

            if (psiClass != null) {
                for (String method : SpringirunCompletionUtils.resolveSetters(psiClass, prefix)) {
                    result.addElement(LookupElementBuilder.create(method).withIcon(PlatformIcons.PROPERTY_ICON));
                }
            }

        }
    };

    //factory-method contributor
    CompletionProvider<CompletionParameters> valuedMethodCompletionProvider
            = new CompletionProvider<CompletionParameters>() {
        @Override
        protected void addCompletions(
                @NotNull final CompletionParameters parameters, final ProcessingContext context,
                @NotNull final CompletionResultSet result) {
            final PsiElement element = parameters.getPosition();

            if (!(element.getParent().getParent().getParent() instanceof XmlTag)) {
                return;
            }
            final XmlTag parent = (XmlTag) element.getParent().getParent().getParent();
            final XmlAttribute attribute = (XmlAttribute) element.getParent().getParent();
            final String prefix = result.getPrefixMatcher().getPrefix();

            PsiClass psiClass = SpringirunCompletionUtils.resolveBean(parent, attribute);

            if (psiClass != null) {
                for (String method : SpringirunCompletionUtils.resolveMethods(psiClass, prefix,
                        MethodReturnType.VALUED)) {
                    result.addElement(LookupElementBuilder.create(method).withIcon(PlatformIcons.METHOD_ICON));
                }
            }
        }
    };

    //init-method contributor
    CompletionProvider<CompletionParameters> voidMethodCompletionProvider
            = new CompletionProvider<CompletionParameters>() {
        @Override
        protected void addCompletions(
                @NotNull final CompletionParameters parameters, final ProcessingContext context,
                @NotNull final CompletionResultSet result) {
            final PsiElement element = parameters.getPosition();

            if (!(element.getParent().getParent().getParent() instanceof XmlTag)) {
                return;
            }
            final XmlTag parent = (XmlTag) element.getParent().getParent().getParent();
            final XmlAttribute attribute = (XmlAttribute) element.getParent().getParent();
            final String prefix = result.getPrefixMatcher().getPrefix();

            PsiClass psiClass = SpringirunCompletionUtils.resolveBean(parent, attribute);

            if (psiClass != null) {
                for (String method : SpringirunCompletionUtils.resolveMethods(psiClass, prefix,
                        MethodReturnType.VOID)) {
                    result.addElement(LookupElementBuilder.create(method).withIcon(PlatformIcons.METHOD_ICON));
                }
            }
        }
    };

    CompletionProvider<CompletionParameters> beansReferenceCompletionProvider = new CompletionProvider<CompletionParameters>() {
        @Override

        protected void addCompletions(@NotNull final CompletionParameters parameters, final ProcessingContext context, @NotNull final CompletionResultSet result) {
            final PsiElement element = parameters.getPosition();
            final String prefix = result.getPrefixMatcher().getPrefix();

            XmlAttribute xmlAttribute = (XmlAttribute) element.getParent().getParent();

            final Beans beans = SpringirunCompletionUtils.getDocumentRoot(xmlAttribute);

            for (Bean bean : beans.getBeans()) {
                if (bean.getId().getValue() != null && bean.getId().getValue().startsWith(prefix)) {
                    result.addElement(LookupElementBuilder.create(bean.getId().getValue()).withIcon(SpringirunCompletionUtils.BEAN_ICON));
                }
                if (bean.getName().getValue() != null && bean.getName().getValue().startsWith(prefix)) {
                    result.addElement(LookupElementBuilder.create(bean.getName().getValue()).withIcon(SpringirunCompletionUtils.BEAN_ICON));
                }
            }
            for (Alias alias : beans.getAliases()) {
                if (alias.getAlias().getValue() != null && alias.getAlias().getValue().startsWith(prefix)) {
                    result.addElement(LookupElementBuilder.create(alias.getAlias().getValue()).withIcon(SpringirunCompletionUtils.BEAN_ALIAS_ICON));
                }
            }
        }
    };

    public SpringirunCompletionContributor() {
        extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttribute().withNamespace(
                SpringirunCompletionUtils.P_NAMESPACE)), pContextCompletionProvider);

        extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue().inside(
                XmlPatterns.xmlAttribute(SpringirunCompletionUtils.NAME).inside(XmlPatterns.xmlTag().withLocalName(
                        SpringirunCompletionUtils.PROPERTY).withNamespace(SpringirunCompletionUtils.BEAN_NAMESPACE)))),
                propertyNameCompletionProvider);

        extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue().inside(
                XmlPatterns.xmlAttribute().withName(StandardPatterns.string().oneOf(
                        SpringirunCompletionUtils.FACTORY_METHOD)).inside(XmlPatterns.xmlTag().withLocalName(
                        SpringirunCompletionUtils.BEAN).withNamespace(SpringirunCompletionUtils.BEAN_NAMESPACE)))),
                valuedMethodCompletionProvider);

        extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue().inside(
                XmlPatterns.xmlAttribute().withName(StandardPatterns.string().oneOf(
                        SpringirunCompletionUtils.INIT_METHOD)).inside(XmlPatterns.xmlTag().withLocalName(
                        SpringirunCompletionUtils.BEAN).withNamespace(SpringirunCompletionUtils.BEAN_NAMESPACE)))),
                voidMethodCompletionProvider);

        extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName(StandardPatterns.string().oneOf(SpringirunCompletionUtils.VALUE_REF,
                        SpringirunCompletionUtils.BEAN_REF, SpringirunCompletionUtils.PARENT, SpringirunCompletionUtils.FACTORY_BEAN)))),
                beansReferenceCompletionProvider);

        extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute(SpringirunCompletionUtils.BEAN).withParent(XmlPatterns.xmlTag().withLocalName
                        (SpringirunCompletionUtils.REF).withNamespace(SpringirunCompletionUtils.BEAN_NAMESPACE)))),
                beansReferenceCompletionProvider);

        extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute(SpringirunCompletionUtils.REF).withParent(XmlPatterns.xmlTag().withLocalName
                        (SpringirunCompletionUtils.PROPERTY).withNamespace(SpringirunCompletionUtils.BEAN_NAMESPACE)))),
                beansReferenceCompletionProvider);

        extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute().withName(StandardPatterns.string().endsWith(SpringirunCompletionUtils._REF))
                        .withNamespace(SpringirunCompletionUtils.P_NAMESPACE))),
                beansReferenceCompletionProvider);

        extend(CompletionType.BASIC, XmlPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue().withParent(
                XmlPatterns.xmlAttribute(SpringirunCompletionUtils.NAME).withParent(XmlPatterns.xmlTag().withLocalName
                        (SpringirunCompletionUtils.ALIAS).withNamespace(SpringirunCompletionUtils.BEAN_NAMESPACE)))),
                beansReferenceCompletionProvider);

    }
}
