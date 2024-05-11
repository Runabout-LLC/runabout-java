package dev.runabout;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultSerializerTests {

    @Test
    void testStringSerializer() {
        final String test = "testString";
        final RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test);

        final String eval = runaboutInstance.getEval();
        Assertions.assertEquals("\"testString\"", eval);
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());
    }

    @Test
    void testStringNested1Serializer() {
        final String test = "test:\"String\"";
        final RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test);

        final String eval = runaboutInstance.getEval();
        Assertions.assertEquals("\"test:\\\"String\\\"\"", eval);
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());
    }

    @Test
    void testStringNested2Serializer() {
        final String test = "test:\"a \\\"nested\\\" string\"";
        final RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test);

        final String eval = runaboutInstance.getEval();
        Assertions.assertEquals("\"test:\\\"a \\\\\\\"nested\\\\\\\" string\\\"\"", eval);
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());
    }

    @Test
    void testStringNested3Serializer() {
        final String test = "test:\"a \\\"very \\\\\\\"nested\\\\\\\" \\\" string\"";
        final RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test);

        final String eval = runaboutInstance.getEval();
        Assertions.assertEquals("\"test:\\\"a \\\\\\\"very \\\\\\\\\\\\\\\"nested\\\\\\\\\\\\\\\" \\\\\\\" string\\\"\"", eval);
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());
    }

    @Test
    void testIntegerSerializer() {
        final Integer test1 = 7;
        RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(int) 7", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());

        final int test2 = 8;
        runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(int) 8", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());
    }

    @Test
    void testLongSerializer() {
        final Long test1 = 8L;
        RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(long) 8", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());

        final long test2 = 9L;
        runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(long) 9", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());
    }

    @Test
    void testFloatSerializer() {
        final Float test1 = 8.2f;
        RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(float) 8.2", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());

        final float test2 = 8.4f;
        runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(float) 8.4", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());
    }

    @Test
    void testDoubleSerializer() {
        final Double test1 = 8.2;
        RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(double) 8.2", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());

        final double test2 = 8.4;
        runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(double) 8.4", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());
    }

    @Test
    void testByteSerializer() {
        final Byte test1 = 8;
        RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(byte) 8", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());

        final byte test2 = 9;
        runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(byte) 9", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());
    }

    @Test
    void testShortSerializer() {
        final Short test1 = 8;
        RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("(short) 8", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());

        final short test2 = 9;
        runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("(short) 9", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());
    }

    @Test
    void testCharacterSerializer() {
        final Character test = 'a';
        final RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test);
        Assertions.assertEquals("'a'", runaboutInstance.getEval());
        Assertions.assertTrue(runaboutInstance.getDependencies().isEmpty());
    }

    @Test
    void testEnumSerializer() {
        TestEnum test1 = TestEnum.VALUE1;
        RunaboutInstance runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test1);
        Assertions.assertEquals("dev.runabout.DefaultSerializerTests.TestEnum.VALUE1", runaboutInstance.getEval());
        Assertions.assertEquals(1, runaboutInstance.getDependencies().size());
        Assertions.assertTrue(runaboutInstance.getDependencies().contains(TestEnum.class.getCanonicalName()));

        TestEnum test2 = TestEnum.test2;
        runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test2);
        Assertions.assertEquals("dev.runabout.DefaultSerializerTests.TestEnum.test2", runaboutInstance.getEval());
        Assertions.assertEquals(1, runaboutInstance.getDependencies().size());
        Assertions.assertTrue(runaboutInstance.getDependencies().contains(TestEnum.class.getCanonicalName()));

        TestEnum test3 = TestEnum.V_3;
        runaboutInstance = DefaultSerializer.getInstance().toRunaboutGeneric(test3);
        Assertions.assertEquals("dev.runabout.DefaultSerializerTests.TestEnum.V_3", runaboutInstance.getEval());
        Assertions.assertEquals(1, runaboutInstance.getDependencies().size());
        Assertions.assertTrue(runaboutInstance.getDependencies().contains(TestEnum.class.getCanonicalName()));
    }

    @Test
    void testSimpleMap() {
        final Map<String, Integer> test = new HashMap<>();
        test.put("key1", 7);
        test.put("key2", 9);
        final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();
        final RunaboutInstance runaboutInstance = defaultSerializer.toRunaboutGenericRecursive(test, defaultSerializer::toRunaboutGeneric);
        Assertions.assertEquals("new HashMap<>() {{ put(\"key1\", (int) 7); put(\"key2\", (int) 9); }}", runaboutInstance.getEval());
        Assertions.assertEquals(1, runaboutInstance.getDependencies().size());
        Assertions.assertTrue(runaboutInstance.getDependencies().contains(HashMap.class.getCanonicalName()));
    }

    @Test
    void testMapContainingNull() {
        final Map<String, Integer> test = new HashMap<>();
        test.put("key1", 7);
        test.put("key2", null);
        final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();
        final RunaboutInstance runaboutInstance = defaultSerializer.toRunaboutGenericRecursive(test, defaultSerializer::toRunaboutGeneric);
        Assertions.assertEquals("new HashMap<>() {{ put(\"key1\", (int) 7); put(\"key2\", null); }}", runaboutInstance.getEval());
        Assertions.assertEquals(1, runaboutInstance.getDependencies().size());
        Assertions.assertTrue(runaboutInstance.getDependencies().contains(HashMap.class.getCanonicalName()));
    }

    @Test
    void testMapRecursiveSerializerFails() {
        final Map<String, Map<String, Integer>> test = new HashMap<>();
        test.put("key1", Map.of("key2", 7));
        final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();

        final RunaboutSerializer nullSerializer = o -> null;
        final RunaboutInstance runaboutInstance1 = defaultSerializer.toRunaboutGenericRecursive(test, nullSerializer);
        Assertions.assertEquals("", runaboutInstance1.getEval());
        Assertions.assertTrue(runaboutInstance1.getDependencies().isEmpty());

        final RunaboutSerializer emptySerializer = o -> RunaboutInstance.of("", Set.of());
        final RunaboutInstance runaboutInstance2 = defaultSerializer.toRunaboutGenericRecursive(test, emptySerializer);
        Assertions.assertEquals("", runaboutInstance2.getEval());
        Assertions.assertTrue(runaboutInstance2.getDependencies().isEmpty());

        final RunaboutSerializer throwsSerializer = o -> {
            throw new RuntimeException("Test");
        };
        Assertions.assertThrows(RuntimeException.class,
                () -> defaultSerializer.toRunaboutGenericRecursive(test, throwsSerializer));
    }

    @Test
    void testSimpleSet() {
        final Set<String> test = Set.of("test1", "test2");
        final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();
        final RunaboutInstance runaboutInstance = defaultSerializer.toRunaboutGenericRecursive(test, defaultSerializer::toRunaboutGeneric);

        final Set<String> potentialEvals = Set.of("new HashSet<>() {{ add(\"test1\"); add(\"test2\"); }}",
                "new HashSet<>() {{ add(\"test2\"); add(\"test1\"); }}");
        Assertions.assertTrue(potentialEvals.contains(runaboutInstance.getEval()));
        Assertions.assertEquals(1, runaboutInstance.getDependencies().size());
        Assertions.assertTrue(runaboutInstance.getDependencies().contains(HashSet.class.getCanonicalName()));
    }

    @Test
    void testSetContainingNull() {
        final Set<String> test = new HashSet<>();
        test.add("test1");
        test.add(null);
        final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();
        final RunaboutInstance runaboutInstance = defaultSerializer.toRunaboutGenericRecursive(test, defaultSerializer::toRunaboutGeneric);

        final Set<String> potentialEvals = Set.of("new HashSet<>() {{ add(\"test1\"); add(null); }}",
                "new HashSet<>() {{ add(null); add(\"test1\"); }}");
        Assertions.assertTrue(potentialEvals.contains(runaboutInstance.getEval()));
        Assertions.assertEquals(1, runaboutInstance.getDependencies().size());
        Assertions.assertTrue(runaboutInstance.getDependencies().contains(HashSet.class.getCanonicalName()));
    }

    @Test
    void testSetRecursiveSerializerFails() {
        final Set<String> test = Set.of("test1", "test2");
        final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();

        final RunaboutSerializer nullSerializer = o -> null;
        final RunaboutInstance runaboutInstance1 = defaultSerializer.toRunaboutGenericRecursive(test, nullSerializer);
        Assertions.assertEquals("", runaboutInstance1.getEval());
        Assertions.assertTrue(runaboutInstance1.getDependencies().isEmpty());

        final RunaboutSerializer emptySerializer = o -> RunaboutInstance.of("", Set.of());
        final RunaboutInstance runaboutInstance2 = defaultSerializer.toRunaboutGenericRecursive(test, emptySerializer);
        Assertions.assertEquals("", runaboutInstance2.getEval());
        Assertions.assertTrue(runaboutInstance2.getDependencies().isEmpty());

        final RunaboutSerializer throwsSerializer = o -> {
            throw new RuntimeException("Test");
        };
        Assertions.assertThrows(RuntimeException.class,
                () -> defaultSerializer.toRunaboutGenericRecursive(test, throwsSerializer));
    }

    @Test
    void testSimpleList() {
        final List<String> test = List.of("test1", "test2");
        final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();
        final RunaboutInstance runaboutInstance = defaultSerializer.toRunaboutGenericRecursive(test, defaultSerializer::toRunaboutGeneric);

        Assertions.assertEquals("new ArrayList<>() {{ add(\"test1\"); add(\"test2\"); }}", runaboutInstance.getEval());
        Assertions.assertEquals(1, runaboutInstance.getDependencies().size());
        Assertions.assertTrue(runaboutInstance.getDependencies().contains(ArrayList.class.getCanonicalName()));
    }

    @Test
    void testListContainingNull() {
        final List<String> test = new ArrayList<>();
        test.add("test1");
        test.add(null);
        final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();
        final RunaboutInstance runaboutInstance = defaultSerializer.toRunaboutGenericRecursive(test, defaultSerializer::toRunaboutGeneric);

        Assertions.assertEquals("new ArrayList<>() {{ add(\"test1\"); add(null); }}", runaboutInstance.getEval());
        Assertions.assertEquals(1, runaboutInstance.getDependencies().size());
        Assertions.assertTrue(runaboutInstance.getDependencies().contains(ArrayList.class.getCanonicalName()));
    }

    @Test
    void testListRecursiveSerializerFails() {
        final List<String> test = List.of("test1", "test2");
        final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();

        final RunaboutSerializer nullSerializer = o -> null;
        final RunaboutInstance runaboutInstance1 = defaultSerializer.toRunaboutGenericRecursive(test, nullSerializer);
        Assertions.assertEquals("", runaboutInstance1.getEval());
        Assertions.assertTrue(runaboutInstance1.getDependencies().isEmpty());

        final RunaboutSerializer emptySerializer = o -> RunaboutInstance.of("", Set.of());
        final RunaboutInstance runaboutInstance2 = defaultSerializer.toRunaboutGenericRecursive(test, emptySerializer);
        Assertions.assertEquals("", runaboutInstance2.getEval());
        Assertions.assertTrue(runaboutInstance2.getDependencies().isEmpty());

        final RunaboutSerializer throwsSerializer = o -> {
            throw new RuntimeException("Test");
        };
        Assertions.assertThrows(RuntimeException.class,
                () -> defaultSerializer.toRunaboutGenericRecursive(test, throwsSerializer));
    }

    private enum TestEnum {
        VALUE1,
        test2,
        V_3
    }
}
