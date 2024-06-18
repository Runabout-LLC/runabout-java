package dev.runabout.fixtures;

import java.util.function.Supplier;

public interface SupplierWrapper extends Supplier<String> {

    @Override
    String get();

    String getOther();
}
