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

import InlineFiles.*
import scala.collection.immutable.Seq

import munit.FunSuite

class InlineFilesTest extends FunSuite:

  test("inline a file") {
    val inlined = inlineTextFile("./test-files/inlined.txt")
    assertEquals(inlined, "This file content will be inlined.")
  }

  test("inline files in a folder") {
    val inlined = inlineTextFiles("./test-files/folder", ".txt")
    assertEquals(
      inlined,
      Map(
        "inlined1.txt" -> "First",
        "inlined2.txt" -> "Second"
      )
    )
  }

  test("inline files in nested folders") {
    val inlined = inlineDeepTextFiles("./test-files/folder", ".txt")
    assertEquals(
      inlined,
      Map(
        "inlined1.txt"      -> "First",
        "inlined2.txt"      -> "Second",
        "deep/inlined3.txt" -> "Third\nand more"
      )
    )
  }

  test("filter inline files in nested folders") {
    val inlined = inlineDeepTextFiles("./test-files/folder", ".txt").folder("deep")
    assertEquals(
      inlined,
      Map(
        "inlined3.txt" -> "Third\nand more"
      )
    )
  }

  test("filter inline files without nested folders") {
    val inlined = inlineDeepTextFiles("./test-files/folder", ".txt").files()
    assertEquals(
      inlined,
      Map(
        "inlined1.txt" -> "First",
        "inlined2.txt" -> "Second"
      )
    )
  }

  test("compile-time mapped inline files in nested folders") {
    val inlined: Map[String, Seq[Word]] = Words.inlineWords("./test-files/folder")
    assertEquals(
      inlined,
      Map(
        "inlined1.txt"      -> Seq(Word("First")),
        "inlined2.txt"      -> Seq(Word("Second")),
        "deep/inlined3.txt" -> Seq(Word("Third"), Word("and"), Word("more"))
      )
    )
  }
