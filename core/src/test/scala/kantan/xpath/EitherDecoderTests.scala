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

import kantan.xpath.laws.discipline.DisciplineSuite
import kantan.xpath.laws.discipline.NodeDecoderTests
import kantan.xpath.laws.discipline.SerializableTests
import kantan.xpath.laws.discipline.arbitrary._

class EitherDecoderTests extends DisciplineSuite {
  checkAll("NodeDecoder[Either[Int, Boolean]]", NodeDecoderTests[Either[Int, Boolean]].decoder[Int, Int])
  checkAll("NodeDecoder[Either[Int, Boolean]]", SerializableTests[NodeDecoder[Either[Int, Boolean]]].serializable)
}
