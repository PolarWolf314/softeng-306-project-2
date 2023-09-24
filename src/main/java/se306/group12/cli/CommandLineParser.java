package se306.group12.cli;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import se306.group12.models.CommandLineArguments;

public class CommandLineParser {

    private final ArgumentParser parser;

    public CommandLineParser() {
        this.parser = ArgumentParsers.newFor("scheduler.jar").build()
            .description(
                "An algorithm for finding the optimal schedule for a given set of tasks and processors.");
        this.parser.addArgument("inputDotGraph")
            .metavar("INPUT.dot")
            .required(true)
            .help("A task graph with integer weights in the dot format");
        this.parser.addArgument("processes")
            .metavar("P")
            .required(true)
            .type(Integer.class)
            .help("The number of processes to schedule the INPUT graph on");
        this.parser.addArgument("-p", "--parallel")
            .metavar("N")
            .type(Integer.class)
            .dest("parallelProcesses")
            .setDefault(1)
            .help("Use N cores for execution in parallel (default is sequential)");
        this.parser.addArgument("-v", "--visualise")
            .action(Arguments.storeTrue())
            .dest("visualise")
            .help("Visualise the search");
        this.parser.addArgument("-o", "--output")
            .metavar("OUTPUT")
            .dest("output")
            .help("The output file to write the schedule to (default is INPUT-output.dot)");
    }

    /**
     * Parses the commandline arguments into a {@link CommandLineArguments} object. If the arguments
     * are invalid the program will print out the error and exit. This means the returned instance
     * will always contain valid arguments.
     *
     * @param args The commandline arguments to parse
     * @return A {@link CommandLineArguments} object representing the parsed arguments
     */
    public CommandLineArguments parse(final String[] args) {
        final Namespace namespace;
        try {
            namespace = this.parser.parseArgs(args);
            System.out.println(namespace);
        } catch (final ArgumentParserException e) {
            this.parser.handleError(e);
            System.exit(1);
        }

        // TODO: Extract the values into a CommandLineArguments object
        return null;
    }
}
