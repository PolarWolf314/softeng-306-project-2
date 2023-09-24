package nz.ac.auckland.se306.group12.models;

public record CommandLineArguments(
    String inputDotGraph,
    int processorsCount,
    int algorithmProcessorsCount,
    boolean visualiseSearch,
    String outputDotGraph
) {

}
