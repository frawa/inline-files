/*
 * Copyright 2022 Frank Wagner
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

package frawa.inlinefiles

import scala.quoted.*
import frawa.inlinefiles.compiletime.FileContents.*
import scala.collection.immutable.ListMap

object InlineFiles:
  import compiletime.FileContents.{given, *}
  import scala.language.experimental.macros

  inline def inlineTextFile(inline path: String): String = ${
    inlineTextFile_impl('path)
  }

  inline def inlineTextFiles(inline path: String, inline ext: String): Map[String, String] = ${
    inlineTextFiles_impl('path, 'ext)
  }

  inline def inlineDeepTextFiles(inline path: String, inline ext: String): Map[String, String] = ${
    inlineDeepTextFiles_impl('path, 'ext)
  }

  extension [T](inlined: Map[String, T])
    def folder(path: String): Map[String, T] =
      val prefix       = if path.endsWith("/") then path else path + "/"
      val prefixLength = prefix.length()
      inlined.view
        .filterKeys(_.startsWith(prefix))
        .map((p, v) => (p.substring(prefixLength), v))
        .toMap

    def files(): Map[String, T] =
      inlined.view
        .filterKeys(!_.contains("/"))
        .toMap

  private def inlineTextFile_impl(path: Expr[String])(using
      Quotes
  ): Expr[String] =
    Expr(readTextContentOf(path.valueOrAbort))

  private def inlineTextFiles_impl(path: Expr[String], ext: Expr[String])(using
      Quotes
  ): Expr[Map[String, String]] =
    Expr(readTextContentsIn(path.valueOrAbort, ext.valueOrAbort))

  private def inlineDeepTextFiles_impl(path: Expr[String], ext: Expr[String])(using
      Quotes
  ): Expr[Map[String, String]] =
    Expr(readDeepTextContentsIn(path.valueOrAbort, ext.valueOrAbort))

  // Scala 2 macros
  def inlineTextFile(path: String): String = macro Compat.inlineTextFileImpl
  def inlineTextFiles(path: String, ext: String): Map[String, String] = macro
    Compat.inlineTextFilesImpl
  def inlineDeepTextFiles(path: String, ext: String): Map[String, String] = macro
    Compat.inlineDeepTextFilesImpl

  private object Compat {
    import scala.language.experimental.macros
    import scala.reflect.macros.blackbox.Context

    def inlineTextFileImpl(c: Context)(path: c.Expr[String]): c.Tree = {
      import c.universe._

      val Literal(Constant(p: String)) = path.tree: @unchecked
      val content                      = readTextContentOf(p)
      Literal(Constant(content))
    }

    def inlineTextFilesImpl(c: Context)(path: c.Expr[String], ext: c.Expr[String]): c.Tree = {
      import c.universe._

      val Literal(Constant(p: String)) = path.tree: @unchecked
      val Literal(Constant(e: String)) = ext.tree: @unchecked
      val content                      = readTextContentsIn(p, e)
      reifyMap(c)(content)
    }

    def inlineDeepTextFilesImpl(c: Context)(path: c.Expr[String], ext: c.Expr[String]): c.Tree = {
      import c.universe._

      val Literal(Constant(p: String)) = path.tree: @unchecked
      val Literal(Constant(e: String)) = ext.tree: @unchecked
      val content                      = readDeepTextContentsIn(p, e)
      reifyMap(c)(content)
    }

    private def reifyMap(c: Context)(m: Map[String, String]): c.Tree = {
      import c.universe._

      val tuple2Apply = Select(Ident(TermName("Tuple2")), TermName("apply"))
      val mapApply    = Select(Ident(TermName("Map")), TermName("apply"))

      val pairs = m.map { (k, v) =>
        val key   = Literal(Constant(k))
        val value = Literal(Constant(v))
        Apply(tuple2Apply, List(key, value))
      }.toList

      Apply(mapApply, pairs)
    }
  }
