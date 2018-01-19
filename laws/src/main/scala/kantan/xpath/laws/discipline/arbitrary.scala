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
package laws
package discipline

import DecodeError.TypeError
import ParseError.{IOError, SyntaxError}
import imp.imp
import kantan.codecs.laws._, CodecValue.{IllegalValue, LegalValue}
import ops._
import org.scalacheck._, Arbitrary.{arbitrary ⇒ arb}, Gen._
import org.scalacheck.rng.Seed

object arbitrary extends ArbitraryInstances

trait ArbitraryInstances
    extends kantan.codecs.laws.discipline.ArbitraryInstances with kantan.xpath.laws.discipline.ArbitraryArities {
  // - Arbitrary errors ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  implicit val arbCompileError: Arbitrary[CompileError] = Arbitrary(genException.map(CompileError.apply))
  implicit val arbTypeError: Arbitrary[TypeError]       = Arbitrary(genException.map(TypeError.apply))
  implicit val arbDecodeError: Arbitrary[DecodeError] =
    Arbitrary(oneOf(const(DecodeError.NotFound), arbTypeError.arbitrary))
  implicit val arbSyntaxError: Arbitrary[SyntaxError] = Arbitrary(genException.map(SyntaxError.apply))
  implicit val arbIOError: Arbitrary[IOError]         = Arbitrary(genIoException.map(IOError.apply))
  implicit val arbParseError: Arbitrary[ParseError] =
    Arbitrary(oneOf(arbSyntaxError.arbitrary, arbIOError.arbitrary))
  implicit val arbReadError: Arbitrary[ReadError] =
    Arbitrary(oneOf(arb[DecodeError], arb[ParseError]))
  implicit val arbXPathError: Arbitrary[XPathError] =
    Arbitrary(oneOf(arb[ReadError], arb[CompileError]))

  implicit val cogenCsvDecodeError: Cogen[DecodeError] = Cogen { (seed: Seed, err: DecodeError) ⇒
    err match {
      case DecodeError.NotFound       ⇒ seed
      case DecodeError.TypeError(msg) ⇒ imp[Cogen[String]].perturb(seed, msg)
    }
  }

  // - Arbitrary values ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private def asCDataNode(value: String): Node = {
    val n = "<element></element>".asUnsafeNode.getFirstChild.asInstanceOf[Element]
    n.setTextContent(value)
    n
  }

  implicit val arbLegalXml: Arbitrary[LegalValue[String, Node, codecs.type]] = Arbitrary(for {
    element ← Gen.identifier
    content ← Gen.identifier
  } yield {
    val n = s"<$element>$content</$element>"
    LegalValue(n, n.asUnsafeNode)
  })

  implicit val arbIllegalXml: Arbitrary[IllegalValue[String, Node, codecs.type]] =
    Arbitrary(Gen.alphaStr.suchThat(_.asNode.isLeft).map(IllegalValue.apply))

  implicit def arbLegalFoundNode[A](
    implicit la: Arbitrary[LegalString[A]]
  ): Arbitrary[LegalValue[Node, A, codecs.type]] =
    Arbitrary(la.arbitrary.map(_.mapEncoded(asCDataNode).tag[codecs.type]))

  implicit def arbLegalNode[A](implicit la: Arbitrary[LegalValue[Node, A, codecs.type]]): Arbitrary[LegalNode[A]] =
    Arbitrary(la.arbitrary.map(_.mapEncoded(Option.apply)))

  implicit def arbIllegalFoundNode[A](
    implicit ia: Arbitrary[IllegalString[A]]
  ): Arbitrary[IllegalValue[Node, A, codecs.type]] =
    Arbitrary(ia.arbitrary.map(_.mapEncoded(asCDataNode).tag[codecs.type]))

  implicit def arbIllegalNode[A](
    implicit ia: Arbitrary[IllegalValue[Node, A, codecs.type]]
  ): Arbitrary[IllegalNode[A]] =
    Arbitrary(ia.arbitrary.map(_.mapEncoded(Option.apply).tag[codecs.type]))

  implicit def arbLegalNodeOpt[A](implicit la: Arbitrary[LegalString[A]]): Arbitrary[LegalNode[Option[A]]] =
    Arbitrary(
      Gen.oneOf(
        la.arbitrary.map(_.mapEncoded(e ⇒ Option(asCDataNode(e))).mapDecoded(Option.apply).tag[codecs.type]),
        Gen.const(CodecValue.LegalValue[Option[Node], Option[A], codecs.type](Option.empty, Option.empty))
      )
    )

  implicit def arbIllegalNodeOpt[A](implicit la: Arbitrary[IllegalString[A]]): Arbitrary[IllegalNode[Option[A]]] =
    Arbitrary(la.arbitrary.map(_.mapEncoded(e ⇒ Option(asCDataNode(e))).mapDecoded(Option.apply).tag[codecs.type]))

  // - Misc. arbitraries -----------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def arbNode[A: Arbitrary](f: A ⇒ String): Arbitrary[Node] =
    Arbitrary(Arbitrary.arbitrary[A].map(a ⇒ s"<root>${f(a)}</root>".asUnsafeNode))

  implicit val cogenNode: Cogen[Node] = {
    def accumulate(node: Node, acc: List[(String, Short)]): List[(String, Short)] = {
      val children = node.getChildNodes
      (0 to children.getLength).foldLeft((node.getNodeName, node.getNodeType) :: acc) { (a, i) ⇒
        accumulate(children.item(i), a)
      }
    }

    Cogen.cogenList[(String, Short)].contramap(n ⇒ accumulate(n, List.empty))
  }

  implicit def arbNodeDecoder[A: Arbitrary]: Arbitrary[NodeDecoder[A]] =
    Arbitrary(arb[Option[Node] ⇒ DecodeResult[A]].map(f ⇒ NodeDecoder.from(f)))

  implicit def arbQuery[A: Arbitrary]: Arbitrary[Query[A]] =
    Arbitrary(implicitly[Arbitrary[Node ⇒ A]].arbitrary.map(f ⇒ Query(f)))
}
