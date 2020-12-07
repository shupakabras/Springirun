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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.springirun.search.ResourceSearchStrategy;
import org.springirun.search.ResourceSearchStrategySelector;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reference to imported resources.
 *
 * @author Andrii Borovyk
 */
public class ImportReference extends PsiReferenceBase.Poly<PsiElement> {

    private ResourceSearchStrategySelector resourceSearchStrategySelector = new ResourceSearchStrategySelector();

    private ArrayList<ResolveResult> resolvedResults;

    public ImportReference(@NotNull final PsiElement element) {
        super(element);
        XmlAttribute xmlAttribute = (XmlAttribute) getElement().getParent();
        String reference = xmlAttribute.getValue();
        ResourceSearchStrategy resourceSearchStrategy = resourceSearchStrategySelector.getSearchStrategy(reference);
        PsiFile[] resolvedFiles = resourceSearchStrategy.resolveAcceptableFiles(xmlAttribute);
        resolvedResults = new ArrayList<ResolveResult>();
        for (PsiFile psiFile: resolvedFiles) {
            resolvedResults.add(new PsiElementResolveResult(psiFile));
        }
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        return resolvedResults.toArray(new ResolveResult[resolvedResults.size()]);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
