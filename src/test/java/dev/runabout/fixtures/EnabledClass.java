package dev.runabout.fixtures;

import dev.runabout.annotations.RunaboutEnabled;
import dev.runabout.annotations.RunaboutParameter;

import java.util.List;

public class EnabledClass {

    private final int number;
    private final String name;
    private final boolean flag;
    private final List<String> list;

    @RunaboutEnabled
    public EnabledClass(@RunaboutParameter("number") int number,
                        @RunaboutParameter("name") String name,
                        @RunaboutParameter("flag") boolean flag,
                        @RunaboutParameter("list") List<String> list) {
        this.number = number;
        this.name = name;
        this.flag = flag;
        this.list = list;
    }
}
