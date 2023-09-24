package se306.group12.models;

public record CommandLineArguments(
    String inputDotGraph,
    int numberOfProcessors,
    int algorithmProcesses,
    boolean visualiseSearch,
    String outputDotGraph
) {

}
