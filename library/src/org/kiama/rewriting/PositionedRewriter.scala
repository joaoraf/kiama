/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2013-2015 Anthony M Sloane, Macquarie University.
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
package rewriting

/**
 * Strategy-based term rewriting that copies positions to rewritten terms.
 * The positions are stored in the Kiama `Positions` object.
 *
 * Specifically, this kind of rewriter will record positions of nodes
 * when they are (a) rewrittten as part of a generic traversal (e.g.,
 * `all`), or (b) rewritten as part of a `rule` or similar (e.g., `rulefs`).
 *
 * In each case both the start and finish positions of the old node are
 * copied across to the new node into which it is rewritten. In case (b)
 * no attempt is made to assign positions to nodes that represent sub-terms
 * of the term that results from a successful application of the rule.
 * Override the `rewriting` method to add more specific behaviour.
 */
trait PositionedRewriter extends CallbackRewriter {

    import org.kiama.util.Positions.dupPos

    /**
     * Use the `Positioned` support to set the start and finish positions
     * of the new term to be those of the old term. Always return the new
     * term.
     */
    def rewriting[T] (oldTerm : T, newTerm : T) : T = {
        dupPos (oldTerm, newTerm)
        newTerm
    }

}

/**
 * Strategy-based term rewriting for Kiama `Positioned` terms.
 */
object PositionedRewriter extends PositionedRewriter
