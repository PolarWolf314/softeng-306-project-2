package se306.group12;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {

    public static void main(final String[] args) {
        final ArgumentParser parser = ArgumentParsers.newFor("scheduler.jar").build()
            .description(
                "An algorithm for finding the optimal schedule for a given set of tasks and processors.");
        parser.addArgument("inputDotGraph")
            .metavar("INPUT.dot")
            .required(true)
            .help("A task graph with integer weights in the dot format");
        parser.addArgument("processes")
            .metavar("P")
            .required(true)
            .type(Integer.class)
            .help("The number of processes to schedule the INPUT graph on");
        parser.addArgument("-p", "--parallel")
            .metavar("N")
            .type(Integer.class)
            .dest("parallelProcesses")
            .setDefault(1)
            .help("Use N cores for execution in parallel (default is sequential)");
        parser.addArgument("-v", "--visualise")
            .action(Arguments.storeTrue())
            .dest("visualise")
            .help("Visualise the search");
        parser.addArgument("-o", "--output")
            .metavar("OUTPUT")
            .dest("output")
            .help("The output file to write the schedule to (default is INPUT-output.dot)");

        try {
            final Namespace namespace = parser.parseArgs(args);
            System.out.println(namespace);
        } catch (final ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
    }
}