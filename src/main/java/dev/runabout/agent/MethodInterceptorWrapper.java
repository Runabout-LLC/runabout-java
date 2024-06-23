package dev.runabout.agent;

import dev.runabout.RunaboutListener;
import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

class MethodInterceptorWrapper {

    private static final InstructionStore instructionStore = new InstructionStore();
    private static final AtomicBoolean disabled = new AtomicBoolean(false);

    private static MethodInterceptor interceptor = null;

    protected static void setInterceptor(MethodInterceptor interceptor) {
        MethodInterceptorWrapper.interceptor = interceptor;
    }

    protected static void enable() {
        disabled.set(false);
    }

    protected static void disable() {
        disabled.set(true);
    }

    protected static void updateInstructionStore(final Set<Instruction> instructions,
                                                 final BiConsumer<Class<?>, Method> removeInstructions,
                                                 final BiConsumer<Class<?>, Method> installInstructions,
                                                 final RunaboutListener listener) {
        instructionStore.update(instructions, removeInstructions, installInstructions, listener);
    }

    @Advice.OnMethodEnter(inline = false, suppress = Exception.class)
    public static void onMethodEnter(@Advice.Origin Class<?> clazz,
                                     @Advice.Origin Method method,
                                     @Advice.AllArguments(includeSelf = true) Object[] args) {
        if (disabled.get()) {
            return;
        }

        if (interceptor != null) {
            final Instant now = Instant.now();
            final Long timeout = instructionStore.getTimeout(clazz, method);

            if (timeout != null && timeout > now.toEpochMilli()) {
                interceptor.accept(clazz, method, args);
            }
        }
    }
}
