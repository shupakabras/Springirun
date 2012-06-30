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

import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.springirun.completion.SpringirunCompletionUtils;

/**
 * Spring configuration files reference contributor.
 *
 * @author Andrii Borovyk
 */
public class SpringirunReferenceContributor extends PsiReferenceContributor {

    PsiReferenceProvider pNameReferenceProvider = new PsiReferenceProvider() {
        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
            return new PsiReference[] {new PNameReference(element)};
        }
    };

    PsiReferenceProvider pContextReferenceProvider = new PsiReferenceProvider() {
        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final
            ProcessingContext context) {
            return new PsiReference[] {new PContextReference(element)};
        }
    };

    PsiReferenceProvider methodNameReferenceProvider = new PsiReferenceProvider() {
        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
            return new PsiReference[] {new MethodNameReference(element)};
        }
    };

    PsiReferenceProvider beanReferenceProvider = new PsiReferenceProvider() {
        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
            return new PsiReference[] {new BeanIdReference(element)};
        }
    };

    PsiReferenceProvider resourceReferenceProvider = new PsiReferenceProvider() {
        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
            return new PsiReference[]{new ImportReference(element)};
        }
    };



    @Override
    public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {

        registrar.registerReferenceProvider(XmlPatterns.xmlAttribute(SpringirunCompletionUtils.NAME).withParent(
            XmlPatterns.xmlTag().withLocalName(SpringirunCompletionUtils.PROPERTY)), pNameReferenceProvider);

        registrar.registerReferenceProvider(XmlPatterns.xmlAttribute().withNamespace(SpringirunCompletionUtils.P_NAMESPACE),
            pContextReferenceProvider);

        registrar.registerReferenceProvider(XmlPatterns.xmlAttribute().withName(StandardPatterns.string().oneOf(
            SpringirunCompletionUtils.FACTORY_METHOD, SpringirunCompletionUtils.INIT_METHOD)).inside(
            XmlPatterns.xmlTag().withLocalName(SpringirunCompletionUtils.BEAN).withNamespace(
                SpringirunCompletionUtils.BEAN_NAMESPACE)), methodNameReferenceProvider);


        registrar.registerReferenceProvider(XmlPatterns.xmlAttributeValue()
            .withParent(XmlPatterns.xmlAttribute().withName(StandardPatterns.string()
            .oneOf(SpringirunCompletionUtils.VALUE_REF, SpringirunCompletionUtils.BEAN_REF,
            SpringirunCompletionUtils.PARENT, SpringirunCompletionUtils.FACTORY_BEAN)))
            ,beanReferenceProvider);

        registrar.registerReferenceProvider(XmlPatterns.xmlAttributeValue().withParent(
            XmlPatterns.xmlAttribute(SpringirunCompletionUtils.BEAN).withParent(XmlPatterns.xmlTag().withLocalName
            (SpringirunCompletionUtils.REF).withNamespace(SpringirunCompletionUtils.BEAN_NAMESPACE)))
            ,beanReferenceProvider);

        registrar.registerReferenceProvider(XmlPatterns.xmlAttributeValue().withParent(
            XmlPatterns.xmlAttribute(SpringirunCompletionUtils.REF).withParent(XmlPatterns.xmlTag().withLocalName(
                SpringirunCompletionUtils.PROPERTY).withNamespace(SpringirunCompletionUtils.BEAN_NAMESPACE)))
            ,beanReferenceProvider);

        registrar.registerReferenceProvider(XmlPatterns.xmlAttributeValue().withParent(
            XmlPatterns.xmlAttribute().withName(StandardPatterns.string().endsWith(SpringirunCompletionUtils._REF))
            .withNamespace(SpringirunCompletionUtils.P_NAMESPACE))
            ,beanReferenceProvider);

        registrar.registerReferenceProvider(XmlPatterns.xmlAttributeValue().withParent(
            XmlPatterns.xmlAttribute(SpringirunCompletionUtils.NAME).withParent(XmlPatterns.xmlTag().withLocalName
            (SpringirunCompletionUtils.ALIAS).withNamespace(SpringirunCompletionUtils.BEAN_NAMESPACE)))
            ,beanReferenceProvider);

        registrar.registerReferenceProvider(XmlPatterns.xmlAttributeValue().withParent(
            XmlPatterns.xmlAttribute(SpringirunCompletionUtils.RESOURCE).withParent(XmlPatterns.xmlTag().withLocalName(
            SpringirunCompletionUtils.IMPORT).withNamespace(SpringirunCompletionUtils.BEAN_NAMESPACE)))
            ,resourceReferenceProvider);

    }
}
