package org.springirun.reference;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.springirun.completion.SpringirunCompletionUtils;

import java.util.Optional;

public class ConstructorArgReference extends PsiReferenceBase<PsiElement> {


  public ConstructorArgReference(@NotNull PsiElement element) {
    super(element);
  }

  @Override public PsiElement resolve() {
    Optional<XmlAttribute> attribute = Optional.ofNullable(myElement)
        .filter(XmlAttributeValue.class::isInstance).map(XmlAttributeValue.class::cast)
        .map(XmlAttributeValue::getParent).filter(XmlAttribute.class::isInstance).map(XmlAttribute.class::cast);

    Optional<XmlTag> parent = attribute.map(PsiElement::getParent).map(PsiElement::getParent).filter(XmlTag.class::isInstance)
        .map(XmlTag.class::cast);

    if (parent.isPresent()) {
      PsiClass resolvedClass = SpringirunCompletionUtils.resolveBean(parent.get(), attribute);
      return SpringirunCompletionUtils.resolveArgument(resolvedClass, attribute.get().getValue());
    }
    return null;
  }

  @NotNull @Override public Object[] getVariants() {
    return new Object[0];
  }
}
