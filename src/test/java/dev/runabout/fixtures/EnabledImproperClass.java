package dev.runabout.fixtures;

import dev.runabout.RunaboutInput;
import dev.runabout.annotations.RunaboutEnabled;
import dev.runabout.annotations.RunaboutParameter;
import dev.runabout.annotations.ToRunabout;

import java.util.List;
import java.util.Set;

public class EnabledImproperClass {

    private final int number;
    private final String name;
    private final boolean flag;
    private final List<String> list;

    @RunaboutEnabled
    public EnabledImproperClass(@RunaboutParameter("number") int number,
                                @RunaboutParameter("name") String name,
                                boolean flag,
                                @RunaboutParameter("list") List<String> list) {
        this.number = number;
        this.name = name;
        this.flag = flag;
        this.list = list;
    }

    @ToRunabout
    RunaboutInput fallbackSerializer() {
        return RunaboutInput.of("TEST", Set.of("TEST"));
    }
}
