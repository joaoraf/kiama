/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2009-2011 Anthony M Sloane, Macquarie University.
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

/**
 * sbt project configuration for kiama
 */
class KiamaProject (info: ProjectInfo) extends DefaultProject (info)
    with posterous.Publish
{
    // Configure basic paths
    override def mainScalaSourcePath = "src"
    override def testScalaSourcePath = "src"

    // Configure extra paths
    def exampleFilesPath = (mainScalaSourcePath ##) / "org" / "kiama" / "example"

    // Specyify how to find source and test files.  Main sources are
    //    - all .scala files, except
    // Test sources, which are
    //    - Scala files within the examples src, which are example sources
    //    - files whose names end in Tests.scala, which are actual test sources
    def exampleSources = descendents (exampleFilesPath, "*.scala")
    def testSourceFilter = "*Tests.scala"
    def mainSourceFilter = "*.scala" && -testSourceFilter
    override def mainSources = descendents (mainSourceRoots, mainSourceFilter) ---
                               exampleSources
    override def testSources = descendents (testSourceRoots, testSourceFilter) +++
                               exampleSources

    // Test resources are any non-Scala files in the examples
    override def testResources = descendents (exampleFilesPath, -"*.scala")

    // Set compiler options
    override def compileOptions = super.compileOptions ++ Seq (Unchecked)

    // Include www.scala-tools.org snapshot repository in search
    val scalaToolsSnapshots = ScalaToolsSnapshots

    // Declare dependencies on other libraries
    val scalacheck = "org.scala-tools.testing" %% "scalacheck" % "1.8"
    val scalatest = "org.scalatest" % "scalatest" % "1.3"
    val junit = "junit" % "junit" % "4.8.1"
    val jline = "jline" % "jline" % "0.9.94"

    // Add extra files to included resources
    def extraResources = "COPYING" +++ "COPYING.LESSER" +++ "README.txt"
    override def mainResources = super.mainResources +++ extraResources

    // By default, only log warnings or worse
    log.setLevel (Level.Warn)

    // Action to run a specified main (usually an example)
    lazy val main =
        task {
            args =>
                if (args.length >= 1)
                    runTask (Some (args (0)), testClasspath, args drop 1) dependsOn (testCompile, copyResources)
                else
                    task { Some ("usage: main foo.bar.Main arg...") }
        } describedAs ("Run a specific main with arguments") completeWith (classes)

    // List for completion of main task, all main and test sources turned into packages
    lazy val classes =
        (mainSources +++ testSources).getRelativePaths.toSeq.map (_.replace ("/", ".").replace (".scala", ""))

    // Publish to Maven style repo at scala-tools.org
    override def managedStyle = ManagedStyle.Maven
    val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"

    // Get credentials from here
    Credentials (Path.userHome / ".ivy2" / ".credentials", log)

    // Publish sources and scaladocs too
    override def packageDocsJar = defaultJarPath ("-scaladoc.jar")
    override def packageSrcJar = defaultJarPath ("-sources.jar")
    val sourceArtifact = Artifact (artifactID, "src", "jar", "sources")
    val docsArtifact = Artifact (artifactID, "docs", "jar", "scaladoc")
    val testArtifact = Artifact (artifactID, "jar", "jar", "test")
    val testSrcArtifact = Artifact (artifactID, "src", "jar", "test-src")
    override def packageToPublishActions =
        super.packageToPublishActions ++
            Seq (packageDocs, packageSrc, packageTest, packageTestSrc)
}
