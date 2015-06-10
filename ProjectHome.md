## Overview ##

Kiama is a [Scala](http://www.scala-lang.org) library for language processing. It enables convenient analysis and transformation of structured data. The programming styles supported by the library are based on well-known formal language processing paradigms, including [attribute grammars](https://code.google.com/p/kiama/wiki/Attribution), [tree rewriting](https://code.google.com/p/kiama/wiki/Rewriting), [abstract state machines](https://code.google.com/p/kiama/wiki/Machines), and [pretty printing](https://code.google.com/p/kiama/wiki/PrettyPrinting).

Kiama is a project of the [Programming Languages Research Group](https://wiki.mq.edu.au/display/plrg/Welcome) in the [Department of Computing](http://www.comp.mq.edu.au/) at [Macquarie University](http://www.mq.edu.au/) and is led by [Tony Sloane](http://www.google.com/profiles/inkytonik) (inkytonik on GMail and Twitter). Other participants at Macquarie are Dominic Verity, Matthew Roberts and the PLRG group students.

Collaborators on the Kiama project have included the [Software Engineering Research Group](http://swerl.tudelft.nl/bin/view/Main/WebHome) at the [Delft University of Technology](http://www.tudelft.nl/) in The Netherlands, notably [Eelco Visser](http://swerl.tudelft.nl/bin/view/EelcoVisser/WebHome) and [Lennart Kats](http://www.lennartkats.nl/).

## Latest News ##

  * Nov 10, 2014:[Version 1.8.0](http://wiki.kiama.googlecode.com/hg/doc/1.8.0/notes.html) released
  * Aug 11, 2014: [Version 1.7.0](http://wiki.kiama.googlecode.com/hg/doc/1.7.0/notes.html) released
  * May 16, 2014: [Version 1.6.0](http://wiki.kiama.googlecode.com/hg/doc/1.6.0/notes.html) released
  * Apr 21, 2014: [Version 1.5.3](http://wiki.kiama.googlecode.com/hg/doc/1.5.3/notes.html) released
  * Dec 22, 2013: [Version 1.5.2](http://wiki.kiama.googlecode.com/hg/doc/1.5.2/notes.html) released
  * Jul 5, 2013: [Version 1.5.1](http://wiki.kiama.googlecode.com/hg/doc/1.5.1/notes.html) released
  * Jun 27, 2013: fp-syd pretty-printing trampolines talk slides [posted](http://wiki.kiama.googlecode.com/hg/talks/fp-syd13.pdf).
  * May 20, 2013: YOW! LambdaJam talk slides [posted](https://bitbucket.org/inkytonik/lambdajam13/raw/default/talk/lambdajam13.pdf).
  * May 14, 2013: [Version 1.5.0](http://wiki.kiama.googlecode.com/hg/doc/1.5.0/notes.html) released
  * Apr 11, 2013: ScalaSyd domain-specific names talk slides [posted](http://wiki.kiama.googlecode.com/hg/talks/scalasyd13b.pdf).
  * Feb 13, 2013: ScalaSyd string interpolation talk slides [posted](http://wiki.kiama.googlecode.com/hg/talks/scalasyd13.pdf).
  * Jan 2, 2013: [Version 1.4.0](http://wiki.kiama.googlecode.com/hg/doc/1.4.0/notes.html) released
  * Dec 22, 2012: Added downloads for Dash docsets for 1.3.0.
  * Sep 30, 2012: SLE 2012 conference paper and slides [posted](http://code.google.com/p/kiama/wiki/Research).
  * Aug 9, 2012: ScalaSyd sbt-rats talk slides [posted](http://wiki.kiama.googlecode.com/hg/talks/scalasyd12.pdf).
  * July 12, 2012: [Version 1.3.0](http://wiki.kiama.googlecode.com/hg/doc/1.3.0/notes.html) released
  * Mar 19, 2012: [giter8 project template](https://github.com/inkytonik/kiama.g8) released
  * Mar 8, 2012: [Version 1.2.0](http://wiki.kiama.googlecode.com/hg/doc/1.2.0/notes.html) released

## How to find out about Kiama ##

  * Examples: see further down this page
  * [Installation information](Installation.md)
  * [Documentation wiki](Documentation.md)
  * [Papers and presentations](Research.md)
  * Mailing lists:
    * The [kiama Google Group](http://groups.google.com/group/kiama) is a forum for announcements and general discussion about Kiama.
    * The [kiama-commit Google Group](http://groups.google.com/group/kiama-commit) receives commit messages and messages relating to continuous build problems.

## [Examples](http://code.google.com/p/kiama/wiki/Examples) ##

A traditional approach to language processing is to represent the data to be processed as a hierarchical structure (a tree).  Kiama provides different ways to operate on such trees to carry out typical language processing tasks.

**[Context-sensitive attribute equations](http://code.google.com/p/kiama/wiki/Attribution)**

Attribute equations define properties of tree nodes in terms of constants or the properties of other nodes.  In this example, the local and global minima of a binary tree (`locmin` and `globmin`) are defined using simple local equations.  The arrow notation denotes accessing an attribute (property) of a node.  The `attr` function provides caching and circularity checking to the equations. The `parent` field provides access to the parent of a node.

```
val locmin : Tree ==> Int =
    attr {
        case Fork (l, r) => (l->locmin) min (r->locmin)
        case Leaf (v)    => v
    }

val globmin : Tree ==> Int =
    attr {
        case t if t isRoot => t->locmin
        case t             => t.parent[Tree]->globmin
    }
```

**[Circular attribute equations](http://code.google.com/p/kiama/wiki/Dataflow)**

Sometimes it is useful to define attributes using a mutual dependency.  With `attr` this approach would lead to an error since it would not be possible to calculate the values of such attributes. The `circular` function goes further by implementing mutually dependent attributes via an iteration to a fixed point. In this example, we are calculating variable liveness information for a imperative language statements using the standard data flow equations.

```
val in : Stm ==> Set[Var] =
    circular (Set[Var]()) {
        case s => uses (s) ++ (out (s) -- defines (s))
    }

 val out : Stm ==> Set[Var] =
    circular (Set[Var]()) {
        case s => (s->succ) flatMap (in)
    }
```

**[Rewrite rules and higher-order rewriting strategies](http://code.google.com/p/kiama/wiki/Lambda2)**

While attributes provide a way to decorate a tree with information, rewriting is concerned with transforming trees, perhaps for translation or for optimisation. Kiama contains a strategy-based rewriting library heavily inspired by the [Stratego](http://strategoxt.org/) program transformation language. In this example, we are implementing an evaluation strategy for lambda calculus, using generic strategies such as innermost rewriting.

```
val beta =
    rule {
        case App (Lam (x, t, e1), e2) =>  Let (x, t, e2, e1)
    }

val lambda =
    beta + arithop + subsNum + subsVar + subsApp + subsLam + subsOpn

def innermost (s : => Strategy) : Strategy =
    all (innermost (s) <* attempt (s <* innermost (s)))

lazy val s : Strategy =
    innermost (lambda)
```

**[Pretty-printing](http://code.google.com/p/kiama/wiki/PrettyPrinting)**

Kiama's pretty-printing library provides a flexible way to describe how you want your output to be produced within constraint of line length. For example, the following describes how to pretty-print the constructs of a simple imperative language, where `group`, `nest` and `line` cooperate to produce nicely indented code that breaks lines  at sensible place when needed.

```
def show (t : ImperativeNode) : Doc =
    t match {
        case Num (d)      => value (d)
        case Var (s)      => s
        case Neg (e)      => parens ("-" <> show (e))
        case Add (l, r)   => showbin (l, "+", r)
        case Sub (l, r)   => showbin (l, "-", r)
        case Mul (l, r)   => showbin (l, "*", r)
        case Div (l, r)   => showbin (l, "/", r)
        case Null ()      => semi
        case Seqn (ss)    => group (braces (nest (line <> ssep (ss map show, line)) <> line))
        case Asgn (v, e)  => show (v) <+> "=" <+> show (e) <> semi
        case While (e, b) => "while" <+> parens (show (e)) <> group (nest (line <> show (b)))
    }

def showbin (l : ImperativeNode, op : String, r : ImperativeNode) : Doc =
    parens (show (l) <+> op <+> show (r))
```

## Supporters ##

Work on this project has been supported by the following Universities, funding agencies
and companies.

**Delft University of Technology, The Netherlands**

**Eindhoven University of Technology, The Netherlands**

**Netherlands Organization for Scientific Research**

  * Combining Attribute Grammars and Term Rewriting for Programming Abstractions project (040.11.001)
  * MoDSE: Model-Driven Software Evolution project (638.001.610)
  * TFA: Transformations for Abstractions project (612.063.512)

**YourKit**

[![](http://wiki.kiama.googlecode.com/hg/images/yklogo.png)](http://www.yourkit.com)

YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
<a href='http://www.yourkit.com/java/profiler/index.jsp'>YourKit Java Profiler</a> and
<a href='http://www.yourkit.com/dotnet/index.jsp'>YourKit .NET Profiler</a>.

**CloudBees**

[![](http://www.cloudbees.com/sites/default/files/Button-Built-on-CB-1.png)](http://cloudbees.com)

CloudBees provides
[generous support to FOSS projects](http://www.cloudbees.com/foss/index.cb)
for continuous builds and other services, for which we are very grateful.
[Kiama nightly builds](https://inkytonik.ci.cloudbees.com/job/Kiama) are built
on a CloudBees Jenkins instance, part of their DEV@cloud service.