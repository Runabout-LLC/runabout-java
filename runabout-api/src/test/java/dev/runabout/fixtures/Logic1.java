package dev.runabout.fixtures;

import dev.runabout.RunaboutService;

import java.util.List;
import java.util.Map;

public class Logic1 {

    private static final Logger logger = new Logger();

    final Map<String, String> map;

    public Logic1(Map<String, String> map) {
        this.map = map;
    }

    public void printValuesLayerLogger(final ConcreteClass1 cc1, final ConcreteClass2 cc2) {
        logger.runaboutInfo(this, cc1, cc2);
        for (final String key : cc1.getKeys()) {
            System.out.println(key);
        }
        for (final Map.Entry<String, String> value : cc2.getData().entrySet()) {
            System.out.println(value);
        }
    }

    public void printValuesLambdaLogger(final ConcreteClass1 cc1, final ConcreteClass2 cc2) {
        logger.info(() -> RunaboutService.getService().toRunaboutString(this, cc1, cc2));
        cc1.getKeys().forEach(System.out::println);
        cc2.getData().entrySet().forEach(System.out::println);
    }

    private void printValues(final ConcreteClass1 cc1, final ConcreteClass2 cc2) {
        for (final String key : cc1.getKeys()) {
            System.out.println(key);
        }
        for (final Map.Entry<String, String> value : cc2.getData().entrySet()) {
            System.out.println(value);
        }
    }
}
