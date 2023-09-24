package se306.group12;

import se306.group12.cli.CommandLineParser;

public class Main {

    public static void main(final String[] args) {
        final CommandLineParser parser = new CommandLineParser();
        parser.parse(args);
    }
}