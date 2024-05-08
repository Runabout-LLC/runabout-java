package dev.runabout.fixtures;

import dev.runabout.RunaboutInput;
import dev.runabout.RunaboutSerializer;
import dev.runabout.RunaboutService;

import java.util.HashSet;
import java.util.Set;

public class GenericSerializer implements RunaboutSerializer {

    @Override
    public RunaboutInput toRunaboutGeneric(Object object) {

        RunaboutInput input = null;

        if (object instanceof ConcreteClass2) {
            final ConcreteClass2 concreteClass2 = (ConcreteClass2) object;
            final RunaboutInput idInput = RunaboutService.getService("test").serialize(concreteClass2.getId());
            final RunaboutInput dataInput = RunaboutService.getService("test").serialize(concreteClass2.getData());
            final String eval = String.format("new ConcreteClass2(%1$s, %2$s)", idInput.getEval(), dataInput.getEval());
            final Set<String> dependencies = new HashSet<>(idInput.getDependencies());
            dependencies.addAll(dataInput.getDependencies());
            dependencies.add(ConcreteClass2.class.getCanonicalName());
            input = RunaboutInput.of(eval, dependencies);
        } else if (object instanceof ThrowsClass2) {
            throw new RuntimeException(ThrowsClass2.EXCEPTION_MESSAGE);
        }

        return input;
    }
}