package nz.ac.auckland.se306.group12;

import nz.ac.auckland.se306.group12.cli.CommandLineParser;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;
import nz.ac.auckland.se306.group12.parser.DotGraphIO;

public class Main {
    
  public static void main(final String[] args) {
    final CommandLineParser parser = new CommandLineParser();
    final DotGraphIO dotGraphIO = new DotGraphIO();

    final CommandLineArguments arguments = parser.parse(args);
    dotGraphIO.readDotGraph(arguments.inputDotGraph());

//
//        System.out.println(arguments);
  }

}
