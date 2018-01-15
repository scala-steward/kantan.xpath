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
package scalaz

import _root_.scalaz.Maybe
import _root_.scalaz.scalacheck.ScalazArbitrary._
import laws.discipline._, arbitrary._

class MaybeDecoderTests extends DisciplineSuite {
  checkAll("NodeDecoder[Maybe[Int]]", NodeDecoderTests[Maybe[Int]].decoder[Int, Int])
}
