package dev.runabout.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for a constructor to build a {@link dev.runabout.RunaboutInput} from. Runabout will serialize fields from the class
 * and pass them into the annotated constructor.
 * <br>
 * This annotation must be used in conjunction with the {@link RunaboutParameter} annotation on all parameters
 * in the constructor signature. Each parameter annotation will contain a string which is the name of the field from
 * the class which should be used to populate the parameter.
 * <br>
 * <code>
 * For example:
 * <br>
 * <br> class Example {
 * <br>     final String name;
 * <br>    final int age;
 * <br>
 * <br>    Example(@RunaboutParameter("name") String text, @RunaboutParameter("age") int number) {
 * <br>        this.name = text;
 * <br>        this.age = number;
 * <br>    }
 * <br>
 *</code>
 * In the above example, the RunaboutService will serialize this class into a RunaboutInput by
 * serializing the field "name" and passing in the RunaboutInput's eval string in as
 * parameter "text". The same will be done for the field "age" and parameter "number".
 */
@Documented
@Target({ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface RunaboutEnabled {
}
