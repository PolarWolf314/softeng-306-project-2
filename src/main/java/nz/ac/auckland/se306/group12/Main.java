package nz.ac.auckland.se306.group12;

import nz.ac.auckland.se306.group12.cli.CommandLineParser;
import nz.ac.auckland.se306.group12.models.CommandLineArguments;

public class Main {
    
    public static void main(final String[] args) {
        final CommandLineParser parser = new CommandLineParser();
        final CommandLineArguments arguments = parser.parse(args);
        System.out.println(arguments);
    }

}
