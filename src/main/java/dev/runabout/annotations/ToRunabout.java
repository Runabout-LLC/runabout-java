package dev.runabout.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for methods that convert instances of a class to a Runabout input.
 * The method must be:
 * - An instance method
 * - Take no parameters
 * - Return a RunaboutInput representing the instance
 * The method can have any access level.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ToRunabout {
}
