package interop.grind

import org.apache.xerces.parsers.DOMParser
import org.cyberneko.html.HTMLConfiguration
import grind.XmlParser

import scala.util.Try

package object nekohtml {
  implicit val defaultParser: XmlParser = XmlParser { s =>
    // Sane default configuration
    val conf = new HTMLConfiguration
    conf.setProperty("http://cyberneko.org/html/properties/names/elems", "lower")

    val parser = new DOMParser(conf)
    Try(parser.parse(s)).map(_ => parser.getDocument).toOption
  }
}
