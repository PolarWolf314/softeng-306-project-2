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
        Namespace namespace = null;
        try {
            namespace = this.parser.parseArgs(args);
        } catch (final ArgumentParserException e) {
            this.parser.handleError(e);
            System.exit(1);
        }

        final String inputDotGraph = this.withDotExtension(namespace.getString("inputDotGraph"));
        final int numberOfProcessors = namespace.getInt("processes");
        final int algorithmProcesses = namespace.getInt("parallelProcesses");
        final boolean visualiseSearch = namespace.getBoolean("visualise");
        String outputDotGraph = this.withDotExtension(namespace.getString("output"));
        if (outputDotGraph == null) {
            outputDotGraph = this.withoutDotExtension(inputDotGraph) + "-output.dot";
        }

        return new CommandLineArguments(
            inputDotGraph,
            numberOfProcessors,
            algorithmProcesses,
            visualiseSearch,
            outputDotGraph);
    }

    /**
     * Adds the <code>.dot</code> file extension to the filename if it doesn't already have it. If
     * the filename is <code>null</code> then <code>null</code> is returned.
     *
     * @param filename The filename to add the extension to
     * @return The filename with the <code>.dot</code> extension
     */
    private String withDotExtension(String filename) {
        if (filename == null) {
            return null;
        }

        if (!filename.endsWith(".dot")) {
            filename += ".dot";
        }
        return filename;
    }

    /**
     * Removes the <code>.dot</code> file extension from the filename if it has it. If the filename
     * is <code>null</code> then <code>null</code> is returned.
     *
     * @param filename The filename to remove the extension from
     * @return The filename without the <code>.dot</code> extension
     */
    private String withoutDotExtension(String filename) {
        if (filename == null) {
            return null;
        }

        if (filename.endsWith(".dot")) {
            filename = filename.substring(0, filename.length() - 4);
        }
        return filename;
    }
}
