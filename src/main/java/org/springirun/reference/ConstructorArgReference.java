package org.springirun.reference;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import static org.springirun.completion.SpringirunCompletionUtils.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConstructorArgReference extends PsiReferenceBase<PsiElement> {


  public ConstructorArgReference(@NotNull PsiElement element) {
    super(element);
  }

  @Override public PsiElement resolve() {
    Optional<XmlAttribute> attribute = firstParentOf(XmlAttribute.class, myElement);
    Optional<XmlTag> bean = firstParentOf(XmlTag.class, attribute, tagWithName(BEAN));

    Optional<List<XmlTag>> args = bean.map(b -> PsiTreeUtil.findChildrenOfType(b, XmlTag.class))
        .map(c -> c.stream().filter(
            t -> t.getName().equals(CONSTRUCTOR_ARG) && t.getNamespace().equals(BEAN_NAMESPACE))
            .collect(Collectors.toList()));
    if (args.isPresent()) {
      PsiClass resolvedClass = resolveBean(bean.get(), attribute);
      return resolveArgumentByName(resolvedClass, attribute.get().getValue(), args.get().size());

    }

    return null;
  }

  @NotNull @Override public Object[] getVariants() {
    return new Object[0];
  }
}
