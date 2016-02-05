# kantan.xpath

[![Build Status](https://travis-ci.org/nrinaudo/kantan.xpath.svg?branch=master)](https://travis-ci.org/nrinaudo/kantan.xpath)
[![codecov.io](http://codecov.io/github/nrinaudo/kantan.xpath/coverage.svg?branch=master)](http://codecov.io/github/nrinaudo/kantan.xpath)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.nrinaudo/kantan.xpath_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.nrinaudo/kantan.xpath_2.11)
[![Join the chat at https://gitter.im/nrinaudo/kantan.xpath](https://img.shields.io/badge/gitter-join%20chat-52c435.svg)](https://gitter.im/nrinaudo/kantan.xpath)


I find myself having to scrap some website or other with some regularity, and Scala always makes the whole process
more painful than it really needs to be - the standard XML API is ok, I suppose, but the lack of XPath support
(or actually usable XPath-like DSL) is frustrating.

kantan.xpath is a thin wrapper around the Java XPath API that attempts to be type safe, pleasant to use and hide the nasty
Java XML types whenever possible.
