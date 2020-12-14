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
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericAttributeValue;
import org.apache.velocity.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springirun.model.Alias;
import org.springirun.model.Bean;
import org.springirun.model.Beans;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
  public static final Predicate<PsiMethod> accessible =
      (m -> m.getModifierList().hasModifierProperty(PsiModifier.PUBLIC));
  public static final Predicate<PsiMethod> noReturn =
      (m -> m.getReturnType().equals(PsiPrimitiveType.VOID));
  public static final Predicate<PsiMethod> oneArg =
      (m -> m.getParameterList().getParametersCount() == 1);
  public static final Predicate<PsiMethod> valueReturn =
      (m -> !m.getReturnType().equals(PsiPrimitiveType.VOID));
  public static final Predicate<PsiMethod> noArgs = (m -> m.getParameterList().isEmpty());
  public static final Predicate<PsiMethod> anyArgs = (m -> !m.getParameterList().isEmpty());

  public static final Condition<PsiElement> attribute = (psi -> psi instanceof XmlAttribute);

  public static <T> Optional<T> or(Supplier<Optional<T>>... optionals) {
    return Arrays.stream(optionals).map(Supplier::get).filter(Optional::isPresent).findFirst()
        .orElseGet(Optional::empty);
  }

  public static final Condition<PsiElement> tagWithName(String name) {
    return (psi -> psi instanceof XmlTag && XmlTag.class.cast(psi).getName().equals(name));
  }

  public static <T> Optional<T> firstParentOf(Class<T> type, PsiElement element) {
    return Optional.ofNullable(PsiTreeUtil.findFirstParent(element, type::isInstance))
        .map(type::cast);
  }

  public static <T> Optional<T> firstParentOf(Class<T> type, Optional<? extends PsiElement> element,
      Condition<PsiElement> condition) {
    if (element.isPresent()) {
      return Optional.ofNullable(
          PsiTreeUtil.findFirstParent(element.get(), c -> type.isInstance(c) && condition.value(c)))
          .map(type::cast);
    }
    return Optional.empty();
  }

  public static final Predicate<PsiMethod> withNamePrefix(String prefix) {
    return (m -> m.getName().startsWith(prefix));
  }

  public static final Predicate<PsiMethod> withName(String name) {
    return (m -> m.getName().equals(name));
  }

  public static final Predicate<PsiMethod> setter(String prefix) {
    return method.and(accessible).and(noReturn).and(oneArg).and(m -> m.getName()
        .startsWith(SET + (prefix.isEmpty() ? "" : StringUtils.capitalizeFirstLetter(prefix))));
  }

  private static Predicate<Bean> withBeanId(String id) {
    return (b -> b.getId() != null && id.equals(b.getId().getValue()));
  }

  private static Predicate<Bean> withBeanName(String id) {
    return (b -> b.getId() != null && id.equals(b.getId().getValue()));
  }

  private static Predicate<Alias> withAliasName(String id) {
    return (a -> a.getName() != null && id.equals(a.getName().getValue()));
  }

  public static Optional<Bean> resolveBeanByName(@NotNull Optional<Beans> beans,
      @NotNull String name) {
    return beans.map(Beans::getBeans).map(
        list -> list.stream().filter(withBeanId(name).or(withBeanName(name))).findAny()
            .orElse(null));
  }

  public static Optional<Alias> resolveAliasByName(@NotNull Optional<Beans> beans,
      @NotNull String name) {
    return beans.map(Beans::getAliases)
        .map(list -> list.stream().filter(withAliasName(name)).findAny().orElse(null));
  }

  public static Optional<Bean> resolveBeanByAlias(@NotNull Optional<Beans> beans,
      @NotNull String name) {
    return resolveAliasByName(beans, name).map(Alias::getAlias).map(GenericAttributeValue::getValue)
        .map(id -> resolveBeanByName(beans, id).orElse(null));
  }

  public static List<String> resolveSetters(PsiClass psiClass, String namePrefix) {
    return resolveSetters(psiClass, namePrefix, "");
  }

  public static List<String> resolveMethods(PsiClass psiClass, String namePrefix) {
    return Arrays.stream(psiClass.getAllMethods()).filter(method.and(accessible).and(noReturn))
        .map(PsiMethod::getName).filter(m -> m.startsWith(namePrefix)).collect(Collectors.toList());

  }

  public static List<String> resolveSetters(PsiClass psiClass, String namePrefix,
      String namespace) {
    return Arrays.stream(psiClass.getAllMethods()).filter(setter(namePrefix))
        .map(PsiMethod::getName).map(m -> new StringBuilder(namespace)
            .append(String.valueOf(new char[] {m.charAt(3)}).toLowerCase()).append(m.substring(4))
            .toString()).collect(Collectors.toList());
  }

  //TODO: resolve all beans in all files hierarchy, instead of just current one
  public static Optional<Beans> getDocumentRoot(Optional<? extends PsiElement> psiElement) {
    return psiElement.map(psi -> PsiTreeUtil.findFirstParent(psi, tagWithName(BEANS)))
        .filter(XmlTag.class::isInstance).map(XmlTag.class::cast)
        .map(o -> DomManager.getDomManager(psiElement.get().getProject()).getDomElement(o))
        .filter(Beans.class::isInstance).map(Beans.class::cast);

  }

  public static Optional<XmlTag> getXmlTagBeansRoot(Optional<? extends PsiElement> psiElement) {
    return Optional.ofNullable(PsiTreeUtil.findFirstParent(psiElement.get(), tagWithName(BEANS)))
        .filter(XmlTag.class::isInstance).map(XmlTag.class::cast);
  }

  public static Optional<PsiClass> resolveBeanClassByName(Optional<Beans> beans, String name) {
    return resolveBeanByName(beans, name).map(Bean::getXmlElement).map(XmlTag.class::isInstance)
        .map(XmlTag.class::cast).map(t -> t.getAttribute(CLASS))
        .map(SpringirunCompletionUtils::resolvePsiClass);
  }

  public static Optional<PsiClass> resolveBeanClassByAlias(Optional<Beans> beans, String alias) {
    return resolveBeanByAlias(beans, alias).map(Bean::getXmlElement).map(XmlTag.class::isInstance)
        .map(XmlTag.class::cast).map(t -> t.getAttribute(CLASS))
        .map(SpringirunCompletionUtils::resolvePsiClass);
  }


  public static PsiMethod resolveMethod(@NotNull PsiClass psiClass, String methodName) {

    if (psiClass == null) {
      return null;
    }
    StringBuilder mName = new StringBuilder();
    if (methodName.endsWith(_REF)) {
      mName.append(methodName.substring(0, methodName.indexOf(_REF)));
    } else {
      mName.append(methodName);
    }


    PsiMethod[] methods = psiClass.getAllMethods();
    for (PsiMethod method : methods) {
      if (method.getName().equals(mName.toString())) {
        return method;
      }
    }
    return null;
  }

  public static PsiParameter resolveArgumentByName(PsiClass psiClass, String argumentName,
      int totalArgs) {
    if (psiClass == null) {
      return null;
    }
    return Arrays.stream(psiClass.getAllMethods()).filter(constructor.and(accessible).and(anyArgs))
        .map(PsiMethod::getParameterList).sorted((l1, l2) -> l1.getParametersCount() == totalArgs ?
            -1 :
            l2.getParametersCount() == totalArgs ? 1 : 0).map(PsiParameterList::getParameters)
        .flatMap(Arrays::stream).filter(m -> m.getName().equals(argumentName)).findFirst()
        .orElse(null);

  }

  public static PsiMethod resolveSetterMethod(PsiClass psiClass, String methodName) {
    StringBuilder mName = new StringBuilder(SET);
    if (methodName != null && methodName.length() > 0) {
      mName.append(String.copyValueOf(new char[] {methodName.charAt(0)}).toUpperCase());
      mName.append(methodName.substring(1));
    }
    return resolveMethod(psiClass, mName.toString());
  }


  public static PsiClass resolveBean(XmlTag beanTag) {
    return resolveBean(beanTag, Optional.empty());
  }


  private static PsiClass nestedBeanClassResolving(XmlAttribute parent,
      XmlAttribute factoryMethodAttribute) {
    Optional<Beans> beans = getDocumentRoot(Optional.ofNullable(parent));
    Optional<String> v = Optional.ofNullable(parent).map(XmlAttribute::getValue);

    if (v.isPresent()) {
      return or(() -> resolveBeanClassByName(beans, v.get()),
          () -> resolveBeanClassByAlias(beans, v.get()))
          .map(psi -> resolveMethodReturnTypeByFullName(psi, factoryMethodAttribute)).orElse(null);
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
      return resolveMethodReturnTypeByFullName(resolvePsiClass(classAttribute),
          factoryMethodAttribute);
    }
    if (factoryBean != null) {
      return nestedBeanClassResolving(factoryBean, factoryMethodAttribute);
    }
    if (parentAttribute != null) {
      return nestedBeanClassResolving(parentAttribute, factoryMethodAttribute);
    }

    return null;
  }

  public static PsiClass resolveMethodReturnTypeByFullName(PsiClass psiClass,
      XmlAttribute methodAttribute) {
    if (methodAttribute == null) {
      return psiClass;
    }
    PsiMethod[] methods = psiClass.getAllMethods();
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
      if (psiElement instanceof PsiClass) {
        return (PsiClass) psiElement;

      }
    }
    return null;
  }

  public static PsiFile resolvePsiFile(Project project, VirtualFile virtualFile) {
    FileViewProvider fileViewProvider =
        PsiManager.getInstance(project).findViewProvider(virtualFile);
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
