package dev.runabout.fixtures;

import dev.runabout.RunaboutServiceBuilder;

import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public class Logic1 {

    private final Logger logger;
    private final Map<String, String> map;

    public Logic1(final OutputStream outputStream, final Map<String, String> map) {
        this.logger = new Logger(outputStream);
        this.map = map;
    }

    public String concatValuesLayerLogger(final ConcreteClass1 cc1, final ConcreteClass2 cc2) {
        logger.runaboutInfo(this, cc1, cc2);
        return concatValues(cc1, cc2);
    }

    public String concatValuesLambdaLogger(final ConcreteClass1 cc1, final ConcreteClass2 cc2) {
        logger.info(() -> RunaboutServiceBuilder.getDefaultBuilder("test")
                .setCallerClassBlacklist(Set.of(Logger.class))
                .build().toScenario(this, cc1, cc2).toJson());
        return concatValues(cc1, cc2);
    }

    public String evaluateSupplierWrapper(SupplierWrapper supplier) {
        logger.runaboutInfo(this, supplier);
        return supplier.get() + " " + supplier.getOther();
    }

    private String concatValues(final ConcreteClass1 cc1, final ConcreteClass2 cc2) {
        final StringBuilder builder = new StringBuilder(", ");

        for (final String value : map.values()) {
            builder.append(value);
        }

        for (final String key : cc1.getKeys()) {
            builder.append(key);
        }

        for (final Map.Entry<String, String> value : cc2.getData().entrySet()) {
            builder.append(value);
        }

        return builder.toString();
    }
}
