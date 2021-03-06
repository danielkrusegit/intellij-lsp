package org.jetbrains.plugins.scala.lang.scaladoc.psi.api

import _root_.org.jetbrains.plugins.scala.lang.psi.ScalaPsiElement
import com.intellij.psi.javadoc.PsiDocTagValue

/**
 * User: Alexander Podkhalyuzin
 * Date: 22.07.2008
 */
 
trait ScDocInlinedTag extends ScalaPsiElement {
  def getValueElement: PsiDocTagValue
}