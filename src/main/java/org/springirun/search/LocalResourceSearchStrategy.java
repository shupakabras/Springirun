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
import com.intellij.psi.xml.XmlAttribute;

/**
 * Resolving PsiFiles in local directory.
 *
 * @author Andrii Borovyk
 */
public class LocalResourceSearchStrategy extends ResourceSearchStrategy {

    public LocalResourceSearchStrategy(final String reference) {
        super(reference);
    }

    @Override
    protected VirtualFile[] prepareSourceRoots(final XmlAttribute xmlAttribute) {
        return new VirtualFile[] {xmlAttribute.getContainingFile().getVirtualFile().getParent()};
    }
}
