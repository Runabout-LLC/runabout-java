package dev.runabout;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultSerializerTests {

    @Test
    void testStringSerializer() {
        final String test = "testString";
        final RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test);

        final String eval = runaboutInput.getEval();
        Assertions.assertEquals("\"testString\"", eval);
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());
    }

    @Test
    void testStringNested1Serializer() {
        final String test = "test:\"String\"";
        final RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test);

        final String eval = runaboutInput.getEval();
        Assertions.assertEquals("\"test:\\\"String\\\"\"", eval);
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());
    }

    @Test
    void testStringNested2Serializer() {
        final String test = "test:\"a \\\"nested\\\" string\"";
        final RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test);

        final String eval = runaboutInput.getEval();
        Assertions.assertEquals("\"test:\\\"a \\\\\\\"nested\\\\\\\" string\\\"\"", eval);
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());
    }

    @Test
    void testStringNested3Serializer() {
        final String test = "test:\"a \\\"very \\\\\\\"nested\\\\\\\" \\\" string\"";
        final RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test);

        final String eval = runaboutInput.getEval();
        Assertions.assertEquals("\"test:\\\"a \\\\\\\"very \\\\\\\\\\\\\\\"nested\\\\\\\\\\\\\\\" \\\\\\\" string\\\"\"", eval);
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());
    }

    @Test
    void testIntegerSerializer() {
        final Integer test1 = 7;
        RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(int) 7", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());

        final int test2 = 8;
        runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(int) 8", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());
    }

    @Test
    void testLongSerializer() {
        final Long test1 = 8L;
        RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(long) 8", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());

        final long test2 = 9L;
        runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(long) 9", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());
    }

    @Test
    void testFloatSerializer() {
        final Float test1 = 8.2f;
        RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(float) 8.2", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());

        final float test2 = 8.4f;
        runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(float) 8.4", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());
    }

    @Test
    void testDoubleSerializer() {
        final Double test1 = 8.2;
        RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(double) 8.2", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());

        final double test2 = 8.4;
        runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(double) 8.4", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());
    }

    @Test
    void testByteSerializer() {
        final Byte test1 = 8;
        RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(byte) 8", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());

        final byte test2 = 9;
        runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(byte) 9", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());
    }

    @Test
    void testShortSerializer() {
        final Short test1 = 8;
        RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(short) 8", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());

        final short test2 = 9;
        runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(short) 9", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());
    }

    @Test
    void testCharacterSerializer() {
        final Character test = 'a';
        final RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test);
        Assertions.assertEquals("'a'", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());
    }

    @Test
    void testEnumSerializer() {
        TestEnum test1 = TestEnum.VALUE1;
        RunaboutInput runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("dev.runabout.DefaultSerializerTests.TestEnum.VALUE1", runaboutInput.getEval());
        Assertions.assertEquals(1, runaboutInput.getDependencies().size());
        Assertions.assertTrue(runaboutInput.getDependencies().contains(TestEnum.class.getCanonicalName()));

        TestEnum test2 = TestEnum.test2;
        runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("dev.runabout.DefaultSerializerTests.TestEnum.test2", runaboutInput.getEval());
        Assertions.assertEquals(1, runaboutInput.getDependencies().size());
        Assertions.assertTrue(runaboutInput.getDependencies().contains(TestEnum.class.getCanonicalName()));

        TestEnum test3 = TestEnum.V_3;
        runaboutInput = DefaultSerializer.getInstance().toRunaboutGeneric(test3);
        Assertions.assertEquals("dev.runabout.DefaultSerializerTests.TestEnum.V_3", runaboutInput.getEval());
        Assertions.assertEquals(1, runaboutInput.getDependencies().size());
        Assertions.assertTrue(runaboutInput.getDependencies().contains(TestEnum.class.getCanonicalName()));
    }

    private enum TestEnum {
        VALUE1,
        test2,
        V_3
    }
}
