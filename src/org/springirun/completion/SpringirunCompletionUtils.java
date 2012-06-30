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

import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomManager;
import org.springirun.model.Alias;
import org.springirun.model.Bean;
import org.springirun.model.Beans;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;

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
    public static final String NAME = "name";
    public static final String BEAN = "bean";
    public static final String INIT_METHOD = "init-method";
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



    public static List<String> resolveSetters(PsiClass psiClass, String namePrefix) {
        return  resolveSetters(psiClass, namePrefix, "");
    }

    public static List<String> resolveMethods(PsiClass psiClass, String namePrefix) {
        return resolveMethods(psiClass, namePrefix, MethodReturnType.ANY);
    }
    public static List<String> resolveMethods(PsiClass psiClass, String namePrefix, MethodReturnType methodReturnType) {
        PsiMethod[]  methods = psiClass.getAllMethods();
        List<String> resolvedSetters = new ArrayList<String>();
        for (PsiMethod method : methods) {
            //search only for public methods
            if (method.isConstructor() || !method.getModifierList().hasModifierProperty(PsiModifier.PUBLIC)) {
                continue;
            }
            switch (methodReturnType) {
                case VOID:
                    if (!method.getReturnType().equals(PsiPrimitiveType.VOID)) {
                        continue;
                    }
                    break;
                case VALUED:
                    if (method.getReturnType().equals(PsiPrimitiveType.VOID)) {
                        continue;
                    }
                    break;
                case ANY:
                default:
            }
            if (method.getName().startsWith(namePrefix)) {
                resolvedSetters.add(method.getName());
            }
        }
        return resolvedSetters;
    }

    public static List<String> resolveSetters(PsiClass psiClass, String namePrefix, String namespace) {

        StringBuilder nameBuilder = new StringBuilder(SET);
        if (namePrefix.length() > 0) {
            nameBuilder.append(String.copyValueOf(new char[]{namePrefix.charAt(0)}).toUpperCase());
            nameBuilder.append(namePrefix.substring(1));
        }
        List<String> original = resolveMethods(psiClass, nameBuilder.toString());
        List<String> methods = new ArrayList<String>(original.size());
        for (String method : original) {
            methods.add(new StringBuilder(namespace).append(String.valueOf(new char[]{method.charAt(3)}).toLowerCase())
                                                    .append(method.substring(4)).toString());
        }

        return methods;
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

    public static PsiMethod resolveSetterMethod(PsiClass psiClass, String methodName) {
        StringBuilder mName = new StringBuilder(SET);
        if (methodName != null && methodName.length() > 0) {
            mName.append(String.copyValueOf(new char[]{methodName.charAt(0)}).toUpperCase());
            mName.append(methodName.substring(1));
        }
        return resolveMethod(psiClass, mName.toString());
    }


    public static PsiClass resolveBean(XmlTag beanTag) {
        return resolveBean(beanTag, null);
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

    public static PsiClass resolveBean(XmlTag beanTag, XmlAttribute attribute) {
        XmlAttribute classAttribute = beanTag.getAttribute(CLASS);
        XmlAttribute factoryBean = beanTag.getAttribute(FACTORY_BEAN);
        XmlAttribute factoryMethodAttribute = beanTag.getAttribute(FACTORY_METHOD);
        XmlAttribute parentAttribute = beanTag.getAttribute(PARENT);

        if (classAttribute != null) {
            if (attribute != null && attribute.getLocalName().equals(FACTORY_METHOD)) {
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
}
