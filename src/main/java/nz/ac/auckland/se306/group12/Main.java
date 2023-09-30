package nz.ac.auckland.se306.group12;

import java.io.IOException;
import java.util.List;
import nz.ac.auckland.se306.group12.cli.CommandLineParser;
import nz.ac.auckland.se306.group12.io.DotGraphIO;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.models.Graph;
import nz.ac.auckland.se306.group12.models.Node;

public class Main {

  public static void main(String[] args) {
    CommandLineParser parser = new CommandLineParser();
    DotGraphIO dotGraphIO = new DotGraphIO();

    CommandLineArguments arguments = parser.parse(args);
    try {
      Graph graph = dotGraphIO.readDotGraph(arguments.inputDotGraph());
      System.out.println(graph);

      List<List<Node>> nodes = List.of(
          List.of(new Node("A", 1)),
          List.of(new Node("A", 1))
      );

      dotGraphIO.writeDotGraph(arguments, nodes);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}
