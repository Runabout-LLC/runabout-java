package dev.runabout;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

class DefaultCallerSupplier implements Supplier<Method> {

    private static final Set<StackWalker.Option> options = Set.of(
            StackWalker.Option.RETAIN_CLASS_REFERENCE,
            StackWalker.Option.SHOW_REFLECT_FRAMES
    );

    private final Set<Class<?>> callerClassBlackList;

    DefaultCallerSupplier(Set<Class<?>> callerClassBlackList) {
        this.callerClassBlackList = callerClassBlackList;
    }

    @Override
    public Method get() {

        final AtomicBoolean failed = new AtomicBoolean(false);
        final AtomicReference<Method> method = new AtomicReference<>();

        StackWalker.getInstance(options).forEach(stackFrame -> {
            if (method.get() == null && !failed.get() && stackFrame.isNativeMethod() &&
                    stackFrame.getDeclaringClass().getPackage() != RunaboutService.class.getPackage() &&
                    !callerClassBlackList.contains(stackFrame.getDeclaringClass())) {
                try {
                    method.set(getMethodFromStackFrame(stackFrame));
                } catch (NoSuchMethodException | SecurityException | NullPointerException e) {
                    failed.set(true);
                }
            }
        });

        return method.get();
    }

    private static Method getMethodFromStackFrame(final StackWalker.StackFrame stackFrame) throws NoSuchMethodException {
        final Class<?> clazz = Objects.requireNonNull(stackFrame.getDeclaringClass());
        final String methodName = Objects.requireNonNull(stackFrame.getMethodName());
        final MethodType methodType = Objects.requireNonNull(stackFrame.getMethodType());
        final Class<?>[] parameterTypes = Objects.requireNonNull(methodType.parameterArray());
        return clazz.getDeclaredMethod(methodName, parameterTypes);
    }
}
