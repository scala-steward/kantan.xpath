/*
 * Copyright 2016 Nicolas Rinaudo
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

package kantan.xpath

import kantan.codecs.Result
import kantan.codecs.Result.{Failure, Success}
import kantan.codecs.laws.CodecValue
import kantan.xpath.laws.discipline.NodeValue
import kantan.xpath.laws.discipline.arbitrary._
import kantan.xpath.ops._
import org.scalacheck.Arbitrary
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import scala.util.Try

class XmlSourceOpsTests extends FunSuite with GeneratorDrivenPropertyChecks {
  type Value[A] = CodecValue[Node, A]

  // This is needed to get tests to compile under 2.10
  implicit val arbIntValue: Arbitrary[Value[Int]] = CodecValue.arbValue[Node, Int]
  implicit val arbStringValue: Arbitrary[CodecValue[String, Node]] = CodecValue.arbValue[String, Node]

  private def cmp[A, F, E](value: CodecValue[E, A], res: Result[F, A]): Boolean = (value, res) match {
    case (CodecValue.LegalValue(_, n1), Success(n2)) ⇒ n1 == n2
    case (CodecValue.IllegalValue(_), Failure(_))    ⇒ true
    case _                                           ⇒ false
  }

  private def cmp[A, F, E](value: CodecValue[E, A], res: Try[A]): Boolean = cmp(value, Result.fromTry(res))


  test("XmlSource instances should have a working asNode method") {
    forAll { value: Value[Int] ⇒ assert(cmp(value, value.encoded.asNode.flatMap(_.evalXPath[Int]("/element")))) }
  }

  // This test is not as good as it could be - we're not comparing decoded XML. The reason for that is that, apparently,
  // Node equality is not something the JDK deals with.
  test("XmlSource instances should have a working asUnsafeNode method") {
    forAll { value: CodecValue[String, Node] ⇒
      assert((value, Result.fromTry(Try(value.encoded.asUnsafeNode))) match {
        case (CodecValue.LegalValue(_, _), Success(_)) ⇒ true
        case (CodecValue.IllegalValue(_), Failure(_))  ⇒ true
        case _                                         ⇒ false
      })
    }
  }

  test("XmlSource instances should have a working evalXPath(String) method") {
    forAll { value: Value[Int] ⇒ assert(cmp(value, value.encoded.evalXPath[Int]("/element"))) }
  }

  test("XmlSource instances should have a working evalXPath(Expression) method") {
    forAll { value: Value[Int] ⇒ assert(cmp(value, value.encoded.evalXPath("/element".xpath[Int]))) }
  }

  test("XmlSource instances should have a working unsafeEvalXPath(String) method") {
    forAll { value: Value[Int] ⇒ assert(cmp(value, Try(value.encoded.unsafeEvalXPath[Int]("/element")))) }
  }

  test("XmlSource instances should have a working unsafeEvalXPath(Expression) method") {
    forAll { value: Value[Int] ⇒ assert(cmp(value, Try(value.encoded.unsafeEvalXPath(("/element").xpath[Int])))) }
  }
}
