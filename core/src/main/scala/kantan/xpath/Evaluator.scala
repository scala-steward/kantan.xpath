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

import scala.collection.generic.CanBuildFrom

/** Evaluates an expression on a given node.
  *
  * This is a bit of a crutch and should not really be used directly. Its sole purpose is to allow compound decoders
  * to work for lists and other collection types.
  */
trait Evaluator[A] {
  def evaluate(exp: Expression, node: Node): DecodeResult[A]
}

object Evaluator {
  implicit def collection[A: NodeDecoder, F[_]](implicit cbf: CanBuildFrom[Nothing, A, F[A]]): Evaluator[F[A]] =
    new Evaluator[F[A]] {
      override def evaluate(exp: Expression, node: Node) = exp.all[F, A](node)
    }

  implicit def node[A: NodeDecoder]: Evaluator[A] = new Evaluator[A] {
    override def evaluate(exp: Expression, node: Node): DecodeResult[A] = exp.first(node)
  }
}
