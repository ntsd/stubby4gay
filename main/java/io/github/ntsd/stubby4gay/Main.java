/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.ntsd.stubby4gay;

import io.github.ntsd.stubby4gay.cli.ANSITerminal;
import io.github.ntsd.stubby4gay.cli.CommandLineInterpreter;
import io.github.ntsd.stubby4gay.server.StubbyManager;
import io.github.ntsd.stubby4gay.server.StubbyManagerFactory;
import io.github.ntsd.stubby4gay.utils.ConsoleUtils;
import io.github.ntsd.stubby4gay.utils.DateTimeUtils;
import io.github.ntsd.stubby4gay.yaml.YamlParseResultSet;
import io.github.ntsd.stubby4gay.yaml.YamlParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.github.ntsd.stubby4gay.utils.FileUtils.BR;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    private static CommandLineInterpreter commandLineInterpreter;

    private Main() {

    }

    public static void main(String[] args) {
        commandLineInterpreter = new CommandLineInterpreter();

        //String[] args2 = new String[]{"--data", "/Users/alexanderzagniotov/yaml/stubs.yaml"};
        //parseCommandLineArgs(args2);
        parseCommandLineArgs(args);
        if (printHelpIfRequested() || printVersionIfRequested()) {
            return;
        }

        verifyYamlDataProvided();
        startstubby4gayUsingCommandLineArgs();
    }

    private static void parseCommandLineArgs(final String[] args) {
        try {
            commandLineInterpreter.parseCommandLine(args);
        } catch (final ParseException ex) {
            final String msg =
                    String.format("Could not parse provided command line arguments, error: %s",
                            ex.toString());

            throw new IllegalArgumentException(msg);
        }
    }

    private static boolean printHelpIfRequested() {
        if (!commandLineInterpreter.isHelp()) {
            return false;
        }

        commandLineInterpreter.printHelp();

        return true;
    }

    private static boolean printVersionIfRequested() {
        if (!commandLineInterpreter.isVersion()) {
            return false;
        }

        commandLineInterpreter.printVersion();

        return true;
    }

    private static void verifyYamlDataProvided() {
        if (commandLineInterpreter.isYamlProvided()) {
            return;
        }
        final String msg =
                String.format("YAML data was not provided using command line option '--%s'. %s"
                                + "To see all command line options run again with option '--%s'",
                        CommandLineInterpreter.OPTION_CONFIG, BR, CommandLineInterpreter.OPTION_HELP);

        throw new IllegalArgumentException(msg);
    }

    private static void startstubby4gayUsingCommandLineArgs() {
        try {

            final long initialStart = System.currentTimeMillis();
            final Map<String, String> commandLineArgs = commandLineInterpreter.getCommandlineParams();
            final String configFilename = commandLineArgs.get(CommandLineInterpreter.OPTION_CONFIG);

            ANSITerminal.muteConsole(commandLineInterpreter.isMute());
            ConsoleUtils.enableDebug(commandLineInterpreter.isDebug());

            final File configFile = new File(configFilename);

            final CompletableFuture<YamlParseResultSet> stubLoadComputation = CompletableFuture.supplyAsync(() -> {
                try {
                    return new YamlParser().parse(configFile.getParent(), configFile);
                } catch (IOException ioEx) {
                    throw new UncheckedIOException(ioEx);
                }
            }, EXECUTOR_SERVICE);

            final StubbyManager stubbyManager = new StubbyManagerFactory().construct(configFile, commandLineArgs, stubLoadComputation);
            stubbyManager.startJetty();
            final long totalEnd = System.currentTimeMillis();

            ANSITerminal.status(String.format(BR + "stubby4gay successfully started after %s milliseconds at %s", (totalEnd - initialStart), DateTimeUtils.systemDefault()) + BR);
            LOGGER.debug("stubby4gay successfully started after {} milliseconds.", totalEnd - initialStart);

            stubbyManager.statuses().forEach(ANSITerminal::status);
            stubbyManager.statuses().forEach(LOGGER::debug);

            ANSITerminal.info(BR + "Quit: ctrl-c" + BR);
            LOGGER.info("Quit: ctrl-c");

        } catch (final Exception ex) {
            final String msg =
                    String.format("Could not init stubby4gay, error: %s", ex.toString());

            throw new IllegalStateException(msg, ex);
        }
    }
}
