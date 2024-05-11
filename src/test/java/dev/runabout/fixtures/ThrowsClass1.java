package dev.runabout.fixtures;

import dev.runabout.RunaboutInstance;
import dev.runabout.ToRunabout;

public class ThrowsClass1 {

    public static final String EXCEPTION_MESSAGE = "Thrown from ThrowsClass1";

    private final String firstName;
    private final String lastName;

    public ThrowsClass1(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @ToRunabout
    RunaboutInstance toRunaboutInput() {
        throw new RuntimeException(EXCEPTION_MESSAGE);
    }
}
