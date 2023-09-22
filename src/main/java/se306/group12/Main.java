package se306.group12;

import se306.group12.models.Test;

public class Main {

    public static void main(final String[] args) {
        System.out.println("Hello world!");

        final Test test = new Test("John", "20");
        System.out.println(test.getName() + " is " + test.getAge() + " years old.");
    }
}