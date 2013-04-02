/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2013 Anthony M Sloane, Macquarie University.
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

import sbt._
import Keys._

object KiamaBuild extends Build {

    lazy val root =
        Project (
            id = "root",
            base = file (".")
        ) aggregate (core, kiama)

    lazy val core =
        Project (
            id = "core",
            base = file ("core")
        )

    lazy val kiama =
        Project (
            id = "kiama",
            base = file ("kiama")
        ) dependsOn (core % "compile-internal, test-internal") settings (
            mappings in (Compile, packageBin) <++= mappings in (core, Compile, packageBin),
            mappings in (Compile, packageSrc) <++= mappings in (core, Compile, packageSrc)
        )

}
