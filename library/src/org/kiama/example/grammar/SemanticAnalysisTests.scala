/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2013-2014 Anthony M Sloane, Macquarie University.
 *
 * Kiama is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Kiama is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Kiama.  (See files COPYING and COPYING.LESSER.)  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.kiama
package example.grammar

import org.kiama.util.Tests

/**
 * Tests of grammar semantic analysis.
 */
class SemanticAnalysisTests extends Tests {

    import GrammarTree._
    import PrettyPrinter._
    import org.kiama.attribution.Attribution.initTree
    import org.kiama.util.Message
    import org.kiama.util.Messaging
    import scala.collection.immutable.Seq

    def S = NonTermSym (NonTermUse ("S"))
    def E = NonTermSym (NonTermUse ("E"))
    def Ep = NonTermSym (NonTermUse ("E'"))
    def T = NonTermSym (NonTermUse ("T"))
    def Tp = NonTermSym (NonTermUse ("T'"))
    def F = NonTermSym (NonTermUse ("F"))

    def plus = TermSym ("+")
    def star = TermSym ("*")
    def lparen = TermSym ("(")
    def rparen = TermSym (")")
    def id = TermSym ("id")

    /**
     *   S -> E $
     *   E -> E + T | T
     *   T -> T * F | F
     *   F  -> ( E ) | id
     */

    val g1r1 = mkRule (NonTermDef ("S"),  mkProd (E, EOI))
    val g1r2 = mkRule (NonTermDef ("E"),  mkProd (E, plus, T),
                                          mkProd (T))
    val g1r3 = mkRule (NonTermDef ("T"),  mkProd (T, star, F),
                                          mkProd (F))
    val g1r4 = mkRule (NonTermDef ("F"),  mkProd (lparen, E, rparen),
                                          mkProd (id))

    val g1 = Grammar (g1r1, Seq (g1r2, g1r3, g1r4))

    initTree (g1)
    val g1messaging = new Messaging ()
    val g1analysis = new SemanticAnalysis (g1messaging)
    g1analysis.check (g1)

    test ("g1: has no semantic errors") {
        assertResult (0) (g1messaging.messagecount)
    }

    test ("g1: S is not nullable") {
        assertResult (false) (g1analysis.nullable (g1r1))
    }

    test ("g1: E is not nullable") {
        assertResult (false) (g1analysis.nullable (g1r2))
    }

    test ("g1: T is not nullable") {
        assertResult (false) (g1analysis.nullable (g1r3))
    }

    test ("g1: F is not nullable") {
        assertResult (false) (g1analysis.nullable (g1r4))
    }

    test ("g1: FIRST (S) is correct") {
        assertResult (Set (lparen, id)) (g1analysis.first (g1r1))
    }

    test ("g1: FIRST (E) is correct") {
        assertResult (Set (lparen, id)) (g1analysis.first (g1r2))
    }

    test ("g1: FIRST (T) is correct") {
        assertResult (Set (lparen, id)) (g1analysis.first (g1r3))
    }

    test ("g1: FIRST (F) is correct") {
        assertResult (Set (lparen, id)) (g1analysis.first (g1r4))
    }

    test ("g1: FOLLOW (S) is correct") {
        assertResult (Set ()) (g1analysis.follow (g1r1.lhs))
    }

    test ("g1: FOLLOW (E) is correct") {
        assertResult (Set (EOI, rparen, plus)) (g1analysis.follow (g1r2.lhs))
    }

    test ("g1: FOLLOW (T) is correct") {
        assertResult (Set (EOI, rparen, plus, star)) (g1analysis.follow (g1r3.lhs))
    }

    test ("g1: FOLLOW (F) is correct") {
        assertResult (Set (EOI, rparen, plus, star)) (g1analysis.follow (g1r4.lhs))
    }

    /**
     *   S -> E $
     *   E  -> T E'
     *   E' -> + T E' | e
     *   T  -> F T'
     *   T' -> * F T' | e
     *   F  -> ( E ) | id
     */

    val g2r1 = mkRule (NonTermDef ("S"),  mkProd (E, EOI))
    val g2r2 = mkRule (NonTermDef ("E"),  mkProd (T, Ep))
    val g2r3 = mkRule (NonTermDef ("E'"), mkProd (plus, T, Ep),
                                          mkProd ())
    val g2r4 = mkRule (NonTermDef ("T"),  mkProd (F, Tp))
    val g2r5 = mkRule (NonTermDef ("T'"), mkProd (star, F, Tp),
                                          mkProd ())
    val g2r6 = mkRule (NonTermDef ("F"),  mkProd (lparen, E, rparen),
                                          mkProd (id))

    val g2 = Grammar (g2r1, Seq (g2r2, g2r3, g2r4, g2r5, g2r6))

    initTree (g2)
    val g2messaging = new Messaging ()
    val g2analysis = new SemanticAnalysis (g2messaging)
    g2analysis.check (g2)

    test ("g2: has no semantic errors") {
        assertResult (0) (g2messaging.messagecount)
    }

    test ("g2: S is not nullable") {
        assertResult (false) (g2analysis.nullable (g2r1))
    }

    test ("g2: E is not nullable") {
        assertResult (false) (g2analysis.nullable (g2r2))
    }

    test ("g2: Ep is nullable") {
        assertResult (true) (g2analysis.nullable (g2r3))
    }

    test ("g2: T is not nullable") {
        assertResult (false) (g2analysis.nullable (g2r4))
    }

    test ("g2: Tp is nullable") {
        assertResult (true) (g2analysis.nullable (g2r5))
    }

    test ("g2: F is not nullable") {
        assertResult (false) (g2analysis.nullable (g2r6))
    }

    test ("g2: FIRST (S) is correct") {
        assertResult (Set (lparen, id)) (g2analysis.first (g2r1))
    }

    test ("g2: FIRST (E) is correct") {
        assertResult (Set (lparen, id)) (g2analysis.first (g2r2))
    }

    test ("g2: FIRST (Ep) is correct") {
        assertResult (Set (plus)) (g2analysis.first (g2r3))
    }

    test ("g2: FIRST (T) is correct") {
        assertResult (Set (lparen, id)) (g2analysis.first (g2r4))
    }

    test ("g2: FIRST (Tp) is correct") {
        assertResult (Set (star)) (g2analysis.first (g2r5))
    }

    test ("g2: FIRST (F) is correct") {
        assertResult (Set (lparen, id)) (g2analysis.first (g2r6))
    }

    test ("g2: FOLLOW (S) is correct") {
        assertResult (Set ()) (g2analysis.follow (g2r1.lhs))
    }

    test ("g2: FOLLOW (E) is correct") {
        assertResult (Set (EOI, rparen)) (g2analysis.follow (g2r2.lhs))
    }

    test ("g2: FOLLOW (Ep) is correct") {
        assertResult (Set (EOI, rparen)) (g2analysis.follow (g2r3.lhs))
    }

    test ("g2: FOLLOW (T) is correct") {
        assertResult (Set (EOI, rparen, plus)) (g2analysis.follow (g2r4.lhs))
    }

    test ("g2: FOLLOW (Tp) is correct") {
        assertResult (Set (EOI, rparen, plus)) (g2analysis.follow (g2r5.lhs))
    }

    test ("g2: FOLLOW (F) is correct") {
        assertResult (Set (EOI, rparen, plus, star)) (g2analysis.follow (g2r6.lhs))
    }

    /**
     *   S -> E F $
     *   E -> +
     *   E -> *
     */

    val g3r1 = mkRule (NonTermDef ("S"),  mkProd (E, F, EOI))
    val g3r2 = mkRule (NonTermDef ("E"),  mkProd (plus))
    val g3r3 = mkRule (NonTermDef ("E"),  mkProd (star))

    val g3 = Grammar (g3r1, Seq (g3r2, g3r3))

    initTree (g3)
    val g3messaging = new Messaging ()
    val g3analysis = new SemanticAnalysis (g3messaging)
    g3analysis.check (g3)

    test ("g3: has three semantic errors") {
        assertMessages (g3messaging,
            (0, Message (0, 0, "F is not declared")),
            (1, Message (0, 0, "E is defined more than once")),
            (2, Message (0, 0, "E is defined more than once")))
    }

}
