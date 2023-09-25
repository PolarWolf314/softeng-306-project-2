package nz.ac.auckland.se306.group12.cli;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;

import java.io.File;
import java.io.FileInputStream;

public class CommandLineParser {

    private static class Keys {

        private static final String INPUT_DOT_GRAPH = "inputDotGraph";
        private static final String PROCESSOR_COUNT = "processorCount";
        private static final String PARALLELISATION_PROCESSOR_COUNT = "parallelisationProcessorCount";
        private static final String VISUALISE_SEARCH = "visualise";
        private static final String OUTPUT_DOT_GRAPH = "output";

    }

    private final ArgumentParser parser;

    public CommandLineParser() {
        this.parser = ArgumentParsers.newFor("scheduler.jar")
            .build()
            .description(
                "An algorithm for finding the optimal schedule for a given set of tasks and processors.");
        this.parser.addArgument(Keys.INPUT_DOT_GRAPH)
            .metavar("INPUT.dot")
            .required(true)
            .help("A task graph with integer weights in the dot format");
        this.parser.addArgument(Keys.PROCESSOR_COUNT)
            .metavar("P")
            .required(true)
            .type(Integer.class)
            .help("The number of processors to schedule the INPUT graph on");
        this.parser.addArgument("-p", "--parallel")
            .metavar("N")
            .type(Integer.class)
            .dest(Keys.PARALLELISATION_PROCESSOR_COUNT)
            .setDefault(1)
            .help("Use N cores for execution in parallel (default is sequential)");
        this.parser.addArgument("-v", "--visualise")
            .action(Arguments.storeTrue())
            .dest(Keys.VISUALISE_SEARCH)
            .help("Visualise the search");
        this.parser.addArgument("-o", "--output")
            .metavar("OUTPUT")
            .dest(Keys.OUTPUT_DOT_GRAPH)
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
        try {
            final Namespace namespace = this.parser.parseArgs(args);

            final String inputDotGraph = this.withDotExtension(
                namespace.getString(Keys.INPUT_DOT_GRAPH));
            String outputDotGraph = this.withDotExtension(
                namespace.getString(Keys.OUTPUT_DOT_GRAPH));
            final int parallelisationProcessorCount = namespace.getInt(
                Keys.PARALLELISATION_PROCESSOR_COUNT);
            final int processorCount = namespace.getInt(Keys.PROCESSOR_COUNT);
            final boolean visualiseSearch = namespace.getBoolean(Keys.VISUALISE_SEARCH);

            if (outputDotGraph == null) {
                outputDotGraph = this.withoutDotExtension(inputDotGraph) + "-output.dot";
            }

            final CommandLineArguments arguments = new CommandLineArguments(
                new File(inputDotGraph),
                processorCount,
                parallelisationProcessorCount,
                visualiseSearch,
                new File(outputDotGraph));

            this.validateArguments(arguments);
            return arguments;
        } catch (final ArgumentParserException e) {
            this.parser.handleError(e);
            // Don't exit with a failure status code if the user was just using `--help`
            System.exit(e instanceof HelpScreenException ? 0 : 1);
        }

        // This will never be reached but Java can't tell that.
        return null;
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

    /**
     * Validates that the specified command line arguments have valid values. If they are invalid
     * then an {@link ArgumentParserException} is thrown describing why.
     *
     * @param arguments The command line arguments to validate
     * @throws ArgumentParserException If the arguments are invalid
     */
    private void validateArguments(final CommandLineArguments arguments)
        throws ArgumentParserException {
        if (arguments.processorCount() < 1) {
            throw new ArgumentParserException(
                "The number of processors (P) must be greater than 0", this.parser);
        }
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (arguments.parallelisationProcessorCount() < 1 ||
            arguments.parallelisationProcessorCount() > availableProcessors
        ) {
            throw new ArgumentParserException(
                String.format("The number of parallel processors (-p N) must be greater than 0 and "
                        + "less than or equal to the number of available processors (%d)",
                    availableProcessors), this.parser);
        }

        if (!arguments.inputDotGraph().exists()) {
            throw new ArgumentParserException(
                String.format("The input dot graph file (%s) does not exist",
                    arguments.inputDotGraph().getPath()), this.parser);
        }

        // TODO: Validate that the input file exists
    }

}
