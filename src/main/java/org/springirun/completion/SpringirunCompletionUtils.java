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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.extractor.Utils;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.JavaPsiTestCase;
import com.intellij.util.xml.DomManager;
import org.apache.velocity.util.StringUtils;
import org.springirun.model.Alias;
import org.springirun.model.Bean;
import org.springirun.model.Beans;
import org.springirun.tool.ContextContainer;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring configuration files processing utility.
 *
 * @author Andrii Borovyk
 */
public class SpringirunCompletionUtils {

    public static final String CLASS = "class";
    public static final String FACTORY_BEAN = "factory-bean";
    public static final String FACTORY_METHOD = "factory-method";
    public static final String PARENT = "parent";
    public static final String _REF = "-ref";
    public static final String REF = "ref";
    public static final String SET = "set";
    public static final String PROPERTY = "property";
    public static final String CONSTRUCTOR_ARG = "constructor-arg";
    public static final String NAME = "name";
    public static final String BEAN = "bean";
    public static final String BEANS = "beans";
    public static final String INIT_METHOD = "init-method";
    public static final String DESTROY_METHOD = "destroy-method";
    public static final String BEAN_REF = "bean-ref";
    public static final String VALUE_REF = "value-ref";
    public static final String ALIAS = "alias";
    public static final String RESOURCE = "resource";
    public static final String IMPORT = "import";
    public static final String BEAN_NAMESPACE = "http://www.springframework.org/schema/beans";
    public static final String P_NAMESPACE = "http://www.springframework.org/schema/p";

    public static Icon BEAN_ICON = IconLoader.getIcon("/images/bean.png");
    public static Icon BEAN_ALIAS_ICON = IconLoader.getIcon("/images/bean--arrow.png");
    public static Icon BEAN_METHOD_ICON = IconLoader.getIcon("/images/bean-green.png");

    public static final Predicate<PsiMethod> method = (m -> !m.isConstructor());
    public static final Predicate<PsiMethod> constructor = (m -> m.isConstructor());
    public static final Predicate<PsiMethod> accessible = (m -> m.getModifierList().hasModifierProperty(PsiModifier.PUBLIC));
    public static final Predicate<PsiMethod> noReturn = (m -> m.getReturnType().equals(PsiPrimitiveType.VOID));
    public static final Predicate<PsiMethod> oneArg = (m -> m.getParameterList().getParametersCount() == 1);
    public static final Predicate<PsiMethod> valueReturn = (m -> !m.getReturnType().equals(PsiPrimitiveType.VOID));
    public static final Predicate<PsiMethod> noArgs = (m -> m.getParameterList().isEmpty());
    public static final Predicate<PsiMethod> anyArgs = (m -> !m.getParameterList().isEmpty());

    public static final Predicate<PsiMethod> withNamePrefix(String prefix) {
        return (m -> m.getName().startsWith(prefix));
    }


    public static final Predicate<PsiMethod> setter(String prefix) {
        return method.and(accessible).and(noReturn).and(oneArg).and(m -> m.getName().startsWith(SET + StringUtils.capitalizeFirstLetter(prefix)));
    }


    public static List<String> resolveSetters(PsiClass psiClass, String namePrefix) {
        return  resolveSetters(psiClass, namePrefix, "");
    }

    public static List<String> resolveMethods(PsiClass psiClass, String namePrefix) {
        return Arrays.stream(psiClass.getAllMethods())
            .filter(method.and(accessible).and(noReturn))
            .map(PsiMethod::getName)
            .filter(m -> m.startsWith(namePrefix))
            .collect(Collectors.toList());

    }


    public static List<String> resolveSetters(PsiClass psiClass, String namePrefix, String namespace) {
        return Arrays.stream(psiClass.getAllMethods())
            .filter(setter(namePrefix))
            .map(PsiMethod::getName)
            .map(m -> new StringBuilder(namespace).append(String.valueOf(new char[]{m.charAt(3)}).toLowerCase())
                .append(m.substring(4)).toString())
            .collect(Collectors.toList());
    }

    public static Beans getDocumentRoot(XmlAttribute xmlAttribute) {
        XmlTag parent = xmlAttribute.getParent();

        while (parent.getParentTag() != null) {
            parent = parent.getParentTag();
        }

        return (Beans) DomManager.getDomManager(xmlAttribute.getProject()).getDomElement(parent);
    }

    public static PsiClass resolveBeanClassByName(Beans beans, String name) {
        for (Bean bean: beans.getBeans()) {
            if (name.equals(bean.getId().getValue()) || name.equals(bean.getName().getValue())) {
                XmlTag beanTag = (XmlTag) bean.getXmlElement();
                return resolvePsiClass(beanTag.getAttribute("class"));
            }
        }
        return null;
    }

    public static Alias resolveBeanAliasByName(Beans beans, String name) {
        for (Alias alias: beans.getAliases()) {
            if (name.equals(alias.getAlias().getValue())) {
                return alias;
            }
        }
        return null;
    }

    public static PsiMethod resolveMethod(PsiClass psiClass, String methodName) {

        if (psiClass == null) {
            return null;
        }
        StringBuilder mName = new StringBuilder();
        if (methodName.endsWith(_REF)) {
            mName.append(methodName.substring(0, methodName.indexOf(_REF)));
        } else {
            mName.append(methodName);
        }

        PsiMethod[]  methods = psiClass.getAllMethods();
        for (PsiMethod method : methods) {
            if (method.getName().equals(mName.toString())) {
                return method;
            }
        }
        return null;
    }

    public static PsiParameter resolveArgument(PsiClass psiClass, String argumentName) {
        if (psiClass == null) {
            return null;
        }
        return Arrays.stream(psiClass.getAllMethods())
            .filter(constructor.and(accessible).and(anyArgs))
            .map(PsiMethod::getParameterList)
            .map(PsiParameterList::getParameters)
            .flatMap(Arrays::stream)
            .filter(m -> m.getName().equals(argumentName))
            .findAny()
            .orElse(null);

    }

    public static PsiMethod resolveSetterMethod(PsiClass psiClass, String methodName) {
        StringBuilder mName = new StringBuilder(SET);
        if (methodName != null && methodName.length() > 0) {
            mName.append(String.copyValueOf(new char[]{methodName.charAt(0)}).toUpperCase());
            mName.append(methodName.substring(1));
        }
        return resolveMethod(psiClass, mName.toString());
    }


    public static PsiClass resolveBean(XmlTag beanTag) {
        return resolveBean(beanTag, Optional.empty());
    }

    public static final void mutliResolve() {
        ContextContainer contextContainer;
    }

    private static PsiClass nestedBeanClassResolving(XmlAttribute parent, XmlAttribute factoryMethodAttribute) {
        Beans beans = getDocumentRoot(parent);
        PsiClass resolvedClass = resolveBeanClassByName(beans, parent.getValue());
        if (resolvedClass == null) {
            Alias alias = resolveBeanAliasByName(beans, parent.getValue());
            if (alias != null) {
                resolvedClass = resolveBeanClassByName(beans, alias.getName().getValue());
            }
        }
        if (resolvedClass != null) {
            return resolveMethodReturnTypeByFullName(resolvedClass, factoryMethodAttribute);
        }
        return null;
    }

    public static PsiClass resolveBean(XmlTag beanTag, Optional<XmlAttribute> attribute) {
        XmlAttribute classAttribute = beanTag.getAttribute(CLASS);
        XmlAttribute factoryBean = beanTag.getAttribute(FACTORY_BEAN);
        XmlAttribute factoryMethodAttribute = beanTag.getAttribute(FACTORY_METHOD);
        XmlAttribute parentAttribute = beanTag.getAttribute(PARENT);

        if (classAttribute != null) {
            if (attribute.isPresent() && attribute.get().getLocalName().equals(FACTORY_METHOD)) {
                //do not resolve bean as factory method type, if we are trying to resolve factory-method itself
                factoryMethodAttribute = null;
            }
            return resolveMethodReturnTypeByFullName(resolvePsiClass(classAttribute), factoryMethodAttribute);
        }
        if (factoryBean != null) {
           return nestedBeanClassResolving(factoryBean, factoryMethodAttribute);
        }
        if (parentAttribute != null) {
            return nestedBeanClassResolving(parentAttribute, factoryMethodAttribute);
        }

        return null;
    }

    public static PsiClass resolveMethodReturnTypeByFullName(PsiClass psiClass, XmlAttribute methodAttribute) {
        if (methodAttribute == null) {
            return psiClass;
        }
        PsiMethod[]  methods = psiClass.getAllMethods();
        for (PsiMethod method : methods) {
            if (method.getName().equals(methodAttribute.getValue())) {
                PsiType methodReturnType = method.getReturnType();
                if (methodReturnType instanceof PsiClassType) {
                    return ((PsiClassType) methodReturnType).resolve();
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public static PsiClass resolvePsiClass(XmlAttribute xmlAttribute) {
        if (xmlAttribute == null) {
            return null;
        }
        PsiReference[] references = xmlAttribute.getValueElement().getReferences();

        for (PsiReference reference : references) {
            PsiElement psiElement = reference.resolve();
            if (psiElement instanceof  PsiClass) {
                return (PsiClass) psiElement;

            }
        }
        return null;
    }

    public static PsiFile resolvePsiFile(Project project, VirtualFile virtualFile) {
        FileViewProvider fileViewProvider = PsiManager.getInstance(project).findViewProvider(virtualFile);
        if (fileViewProvider != null) {
            return fileViewProvider.getPsi(fileViewProvider.getBaseLanguage());
        }
        return null;
    }

    public static PsiFile resolvePsiFile(Project project, String contextPath) {
        for (VirtualFile fileOrDir : OrderEnumerator.orderEntries(project).getAllSourceRoots()) {
            VirtualFile virtualFile = fileOrDir.findFileByRelativePath(contextPath);
            if (virtualFile != null && !virtualFile.isDirectory()) {
                return resolvePsiFile(project, virtualFile);
            }
        }
        return null;
    }
}
