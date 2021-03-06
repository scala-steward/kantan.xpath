---
layout: tutorial
title: "Decoding nodes as case classes"
section: tutorial
sort_order: 3
---
We've seen in a [previous tutorial](nodes_as_tuples.html) how to extract tuples from XML documents. The next step up
from tuples is case classes, which work in a very similar fashion.

In order to show how that works, we'll first need some sample XML data, which we'll get from this project's resources:

```scala mdoc:silent
val rawData: java.net.URL = getClass.getResource("/simple.xml")
```

This is what we're working with:

```scala mdoc
scala.io.Source.fromURL(rawData).mkString
```

We'll be trying to turn each `element` node into values of the following type:

```scala mdoc:silent
final case class El(id: Int, enabled: Boolean)
```

In the same way that we had to declare a [`NodeCoder[(Int, Boolean)]`][`NodeDecoder`] to decode tuples, we'll need a
[`NodeDecoder[El]`][`NodeDecoder`] for this case class, which we can easily create through the [`decoder`] method:


```scala mdoc:silent
import kantan.xpath._
import kantan.xpath.implicits._

// There is no need to specify type parameters here, the Scala compiler works them out from El.apply.
implicit val elDecoder: NodeDecoder[El] = NodeDecoder.decoder(xp"./@id", xp"./@enabled")(El.apply)
```

Now that we have told kantan.xpath how to decode an XML node to an instance of `El`, we can simply call
[`evalXPath`] with the right type parameters:

```scala mdoc
rawData.evalXPath[List[El]](xp"//element")
```

[`NodeDecoder`]:{{ site.baseurl }}/api/kantan/xpath/NodeDecoder$.html
[`decoder`]:{{ site.baseurl }}/api/kantan/xpath/NodeDecoder$.html#decoder[I1,I2,O](x1:kantan.xpath.Query[kantan.xpath.DecodeResult[I1]],x2:kantan.xpath.Query[kantan.xpath.DecodeResult[I2]])(f:(I1,I2)=>O):kantan.xpath.NodeDecoder[O]
[`CompileResult`]:{{ site.baseurl }}/api/kantan/xpath/CompileResult$.html
[`get`]:https://nrinaudo.github.io/kantan.codecs/api/kantan/codecs/Result.html#get:S
[`evalXPath`]:{{ site.baseurl }}/api/kantan/xpath/ops/XmlSourceOps.html#evalXPath[B](expr:kantan.xpath.XPathExpression)(implicitevidence$2:kantan.xpath.Compiler[B],implicitsource:kantan.xpath.XmlSource[A]):kantan.xpath.XPathResult[B]
