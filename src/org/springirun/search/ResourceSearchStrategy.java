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
package org.springirun.search;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlAttribute;

import java.util.ArrayList;

/**
 * Abstract strategy for PsiFiles by name resolving.
 *
 * @author Andrii Borovyk
 */
public abstract class ResourceSearchStrategy {

    private String reference;

    protected ResourceSearchStrategy(String reference) {
        this.reference = reference;
    }

    protected abstract VirtualFile[] prepareSourceRoots(XmlAttribute xmlAttribute);

    public PsiFile[] resolveAcceptableFiles(XmlAttribute xmlAttribute) {
        ArrayList<PsiFile> psiFiles = new ArrayList<PsiFile>();
        for (VirtualFile fileOrDir: prepareSourceRoots(xmlAttribute)) {
            VirtualFile virtualFile = fileOrDir.findFileByRelativePath(reference);
            if (virtualFile == null) {
                continue;
            }
            FileViewProvider fileViewProvider = PsiManager.getInstance(xmlAttribute.getProject()).findViewProvider(
                virtualFile);
            if (fileViewProvider != null) {
                psiFiles.add(fileViewProvider.getPsi(fileViewProvider.getBaseLanguage()));
            }
        }
        return psiFiles.toArray(new PsiFile[psiFiles.size()]);
    }
}
