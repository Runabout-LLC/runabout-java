package dev.runabout.fixtures;

public class ThrowsClass2 {

    public static final String EXCEPTION_MESSAGE = "Thrown from ThrowsClass2";

    private final String firstName;
    private final String lastName;

    public ThrowsClass2(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
