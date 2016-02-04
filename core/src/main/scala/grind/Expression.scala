package grind

import javax.xml.xpath.{XPathConstants, XPathExpression}

import scala.collection.generic.CanBuildFrom

class Expression private[grind] (val expr: XPathExpression) { self =>
  def first[A: NodeDecoder](n: Node): DecodeResult[A] = Expression.first(n, expr)

  def all[A, F[_]](n: Node)(implicit da: NodeDecoder[A], cbf: CanBuildFrom[Nothing, DecodeResult[A], F[DecodeResult[A]]]): F[DecodeResult[A]] =
    Expression.all(n, expr)(da.decode)

  def unsafe: UnsafeExpression = new UnsafeExpression(expr)

  def liftFirst[A: NodeDecoder]: (Node => DecodeResult[A]) = n => first(n)
  def liftAll[F[_], A: NodeDecoder](implicit cbf: CanBuildFrom[Nothing, DecodeResult[A], F[DecodeResult[A]]]): (Node => F[DecodeResult[A]]) =
    n => all(n)
}

class UnsafeExpression private[grind] (val expr: XPathExpression) {
  def first[A: NodeDecoder](n: Node): A = Expression.first(n, expr).get
  def all[A, F[_]](n: Node)(implicit da: NodeDecoder[A], cbf: CanBuildFrom[Nothing, A, F[A]]): F[A] =
    Expression.all(n, expr)(n => da.decode(n).get)
  def liftFirst[A: NodeDecoder]: (Node => A) = n => first(n)
  def liftAll[F[_], A: NodeDecoder](implicit cbf: CanBuildFrom[Nothing, A, F[A]]): (Node => F[A]) = n => all(n)
  def safe: Expression = new Expression(expr)
}

object Expression {
  def apply(str: String)(implicit compiler: XPathCompiler): Option[Expression] = compiler.compile(str)
  def apply(expr: XPathExpression): Expression = new Expression(expr)

  private[grind] def first[A](n: Node, expr: XPathExpression)(implicit da: NodeDecoder[A]): DecodeResult[A] = {
    val res = expr.evaluate(n, XPathConstants.NODE).asInstanceOf[Node]
    if(res == null) DecodeResult.NotFound
    else            da.decode(res)
  }

  private[grind] def all[A, F[_]](n: Node, expr: XPathExpression)(f: Node => A)(implicit cbf: CanBuildFrom[Nothing, A, F[A]]): F[A] = {
    val res = expr.evaluate(n, XPathConstants.NODESET).asInstanceOf[NodeList]
    val out = cbf()
    for(i <- 0 until res.getLength) out += f(res.item(i))
    out.result()
  }
}