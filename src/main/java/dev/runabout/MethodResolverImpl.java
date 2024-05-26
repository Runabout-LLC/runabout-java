package dev.runabout;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

class MethodResolverImpl implements MethodResolver {

    private static final Set<StackWalker.Option> options = Set.of(
            StackWalker.Option.RETAIN_CLASS_REFERENCE,
            StackWalker.Option.SHOW_REFLECT_FRAMES
    );
    private static final String LAMBDA_KEYWORD = "lambda$";

    private final Predicate<StackWalker.StackFrame> stackFramePredicate;

    MethodResolverImpl() {
        this(stackFrame -> true);
    }

    MethodResolverImpl(final Predicate<StackWalker.StackFrame> stackFramePredicate) {
        this.stackFramePredicate = stackFramePredicate;
    }

    @Override
    public Method get() {

        final AtomicBoolean failed = new AtomicBoolean(false);
        final AtomicReference<Method> method = new AtomicReference<>();

        StackWalker.getInstance(options).forEach(stackFrame -> {
            if (method.get() == null && !failed.get() && !isLambdaMethod(stackFrame) &&
                    !isAnonymousCaller(stackFrame) &&
                    stackFrame.getDeclaringClass().getPackage() != RunaboutService.class.getPackage() &&
                    stackFramePredicate.test(stackFrame)) {
                try {
                    method.set(getMethodFromStackFrame(stackFrame));
                } catch (NoSuchMethodException | SecurityException | NullPointerException e) {
                    failed.set(true);
                }
            }
        });

        return method.get();
    }

    public static Predicate<StackWalker.StackFrame> getCallerClassPredicate(final Set<Class<?>> callerClassBlackList) {
        return stackFrame -> stackFrame != null && stackFrame.getDeclaringClass() != null &&
                !callerClassBlackList.contains(stackFrame.getDeclaringClass());
    }

    private static boolean isLambdaMethod(final StackWalker.StackFrame stackFrame) {
        return stackFrame != null && stackFrame.getMethodName() != null &&
                stackFrame.getMethodName().contains(LAMBDA_KEYWORD);
    }

    private static boolean isAnonymousCaller(final StackWalker.StackFrame stackFrame) {
        return stackFrame != null && stackFrame.getDeclaringClass() != null &&
                stackFrame.getDeclaringClass().isAnonymousClass();
    }

    private static Method getMethodFromStackFrame(final StackWalker.StackFrame stackFrame) throws NoSuchMethodException {
        final Class<?> clazz = Objects.requireNonNull(stackFrame.getDeclaringClass());
        final String methodName = Objects.requireNonNull(stackFrame.getMethodName());
        final MethodType methodType = Objects.requireNonNull(stackFrame.getMethodType());
        final Class<?>[] parameterTypes = Objects.requireNonNull(methodType.parameterArray());
        return clazz.getDeclaredMethod(methodName, parameterTypes);
    }
}
