package nz.ac.auckland.se306.group12.models;

import java.io.File;

public record CommandLineArguments(
    File inputDotGraph,
    int processorCount,
    int parallelisationProcessorCount,
    boolean visualiseSearch,
    File outputDotGraph
) {

}
