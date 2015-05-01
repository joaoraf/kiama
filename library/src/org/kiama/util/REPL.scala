/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2008-2015 Anthony M Sloane, Macquarie University.
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
package util

import scala.util.parsing.combinator.RegexParsers

/**
 * General support for applications that implement read-eval-print loops (REPLs).
 */
trait REPLBase[C <: REPLConfig] extends Profiler {

    import scala.annotation.tailrec
    import scala.io.Source

    /**
     * Banner message that is printed before the REPL starts.
     */
    def banner : String

    /**
     * The entry point for this REPL.
     */
    def main (args : Array[String]) {
        driver (args)
    }

    /**
     * Create the configuration for a particular run of the REPL. If supplied, use
     * `emitter` instead of a standard output emitter.
     */
    def createConfig (args : Seq[String],
                      output : Emitter = new OutputEmitter,
                      error : Emitter = new ErrorEmitter) : C

    /**
     * Create and initialise the configuration for a particular run of the REPL.
     * If supplied, use `emitter` instead of a standard output emitter. Default:
     * call `createConfig` and then initialise the resulting configuration.
     */
    def createAndInitConfig (args : Seq[String],
                             output : Emitter = new OutputEmitter,
                             error : Emitter = new ErrorEmitter) : C = {
        val config = createConfig (args, output, error)
        config.afterInit ()
        config
    }

    /**
     * Driver for this REPL. First, use the argument list to create a
     * configuration for this execution. If the arguments parse ok, then
     * print the REPL banner. Read lines from the console and pass non-null ones
     * to `processline`. If `ignoreWhitespaceLines` is true, do not pass lines that
     * contain just whitespace, otherwise do. Continue until `processline`
     * returns false. Call `prompt` each time input is about to be read.
     */
    def driver (args : Seq[String]) {

        // Set up the configuration
        val config = createAndInitConfig (args)

        // Process any filename arguments
        processfiles (config)

        // Enter interactive phase
        config.output.emitln (banner)
        if (config.profile.get != None) {
            val dimensions = parseProfileOption (config.profile ())
            profile (processlines (config), dimensions, config.logging ())
        } else if (config.time ())
            time (processlines (config))
        else
            processlines (config)

        config.output.emitln

    }

    /**
     * Define the prompt (default: `"> "`).
     */
    def prompt : String =
        "> "

    /**
     * Process interactively entered lines, one by one, until end of file.
     * Prompt with the given prompt.
     */
    def processlines (config : C) {
        processconsole (config.console (), prompt, config)
    }

    /**
     * Process the files one by one, allowing config to be updated each time
     * and updated config to be used by the next file.
     */
    final def processfiles (config : C) : C = {

        @tailrec
        def loop (filenames : List[String], config : C) : C =
            filenames match {
                case filename +: rest =>
                    loop (rest, processfile (filename, config))
                case _ =>
                    config
            }

        loop (config.filenames (), config)

    }

    /**
     * Process a file argument by passing its contents line-by-line to
     * `processline`.
     */
    def processfile (filename : String, config : C) : C =
        processconsole (new FileConsole (filename), "", config)

    /**
     * Process interactively entered lines, one by one, until end of file.
     */
    @tailrec
    final def processconsole (console : Console, prompt : String, config : C) : C = {
        val line = console.readLine (prompt)
        if (line == null)
            config
        else {
            processline (line, console, config) match {
                case Some (newConfig) =>
                    processconsole (console, prompt, newConfig)
                case _ =>
                    config
            }
        }
    }

    /**
     * Process a user input line. The return value allows the processing to
     * optionally return a new configuration that will be used in subsequent
     * processing. A return value of `None` indicates that no more lines
     * from the current console should be processed.
     */
    def processline (line : String, console : Console, config : C) : Option[C]

}

/**
 * General support for applications that implement read-eval-print loops (REPLs).
 */
trait REPL extends REPLBase[REPLConfig] {

    def createConfig (args : Seq[String],
                      out : Emitter = new OutputEmitter,
                      err : Emitter = new ErrorEmitter) : REPLConfig =
        new REPLConfig (args) {
            lazy val output = out
            lazy val error = err
        }

}

/**
 * A REPL that parses its input lines into a value (such as an abstract syntax
 * tree), then processes them. Output is emitted using a configurable emitter.
 */
trait ParsingREPLBase[T, C <: REPLConfig] extends REPLBase[C] with RegexParsers {

    /**
     * Process a user input line by parsing it to get a value of type `T`,
     * then passing it to the `process` method. Returns the configuration
     * unchanged.
     */
    def processline (line : String, console : Console, config : C) : Option[C] = {
        if (config.processWhitespaceLines () || (line.trim.length != 0)) {
            parseAll (parser, line) match {
                case Success (e, in) if in.atEnd =>
                    process (e, config)
                case Success (_, in) =>
                    config.error.emitln (s"extraneous input at ${in.pos}")
                case f =>
                    config.error.emitln (f)
            }
        }
        Some (config)
    }

    /**
     * The parser to use to convert user input lines into values.
     */
    def parser : Parser[T]

    /**
     * Process a user input value in the given configuration.
     */
    def process (t : T, config : C)

}

/**
 * A REPL that parses its input lines into a value (such as an abstract syntax
 * tree), then processes them. `C` is the type of the configuration.
 */
trait ParsingREPLWithConfig[T, C <: REPLConfig] extends ParsingREPLBase[T,C]

/**
 * A REPL that parses its input lines into a value (such as an abstract syntax
 * tree), then processes them. Output is emitted to standard output.
 */
trait ParsingREPL[T ] extends ParsingREPLWithConfig[T,REPLConfig] {

    def createConfig (args : Seq[String],
                      out : Emitter = new OutputEmitter,
                      err : Emitter = new ErrorEmitter) : REPLConfig =
        new REPLConfig (args) {
            lazy val output = out
            lazy val error = err
        }

}
