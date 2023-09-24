package nz.ac.auckland.se306.group12.models;

public record CommandLineArguments(
    String inputDotGraph,
    int processorCount,
    int parallelisationProcessorCount,
    boolean visualiseSearch,
    String outputDotGraph
) {

}
