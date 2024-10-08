/*
 * Copyright 2024 Nils Bandener
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.selectivem.collections;

import static com.selectivem.collections.TestUtils.*;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Enclosed.class)
public class IndexedImmutableSetTest {
    static final TestValues testValues = new TestValues();

    @RunWith(Parameterized.class)
    public static class RandomizedTestBig {
        @Parameterized.Parameter
        public Integer seed;

        @Parameterized.Parameters(name = "{0}")
        public static Collection<Integer> seeds() {
            ArrayList<Integer> result = new ArrayList<>(10000);

            for (int i = 1000; i < 10000; i++) {
                result.add(i);
            }

            return result;
        }

        @Test
        public void builder() {
            Random random = new Random(seed);

            HashSet<String> reference = new HashSet<>();
            IndexedImmutableSetImpl.InternalBuilder<String> subject = IndexedImmutableSetImpl.builder(10);

            int size = random.nextInt(100) + 4;

            for (int k = 0; k < size; k++) {
                String string = testValues.randomString(random);

                reference.add(string);
                subject = subject.with(string);
            }

            assertEquals(reference, subject);

            int insertionCount = random.nextInt(30) + 1;

            for (int k = 0; k < insertionCount; k++) {
                String string = testValues.randomString(random);

                reference.add(string);
                subject = subject.with(string);
                assertEquals(reference, subject);
            }

            insertionCount = random.nextInt(30) + 1;

            for (int k = 0; k < insertionCount; k++) {
                String string = testValues.randomString(random);

                reference.add(string);
                subject = subject.with(string);
                assertEquals(reference, subject);
            }

            assertEquals(reference, subject);
            assertEquals(reference, subject.build());
        }

        @Test
        public void contains() {
            Random random = new Random(seed);

            HashSet<String> reference = new HashSet<>();
            List<String> referenceList = new ArrayList<>();

            int size = random.nextInt(200) + 4;
            if (random.nextFloat() < 0.1) {
                size += random.nextInt(800);
            }

            for (int k = 0; k < size; k++) {
                String string = testValues.randomString(random);

                if (!reference.contains(string)) {
                    reference.add(string);
                    referenceList.add(string);
                }
            }

            IndexedImmutableSet<String> subject = IndexedImmutableSet.of(reference);

            assertEquals(reference, subject);
            Collections.shuffle(referenceList, random);

            for (int k = 0; k < reference.size(); k++) {
                String string1 = referenceList.get(k);
                assertTrue("String " + string1 + " not found in " + subject, subject.contains(string1));

                String string2 = string1 + "X";
                Assert.assertEquals(
                        "String " + string2 + " found in " + subject,
                        reference.contains(string2),
                        subject.contains(string2));

                String string3 = testValues.randomString(random);
                Assert.assertEquals(
                        "String " + string3 + " found in " + subject,
                        reference.contains(string3),
                        subject.contains(string3));
            }
        }

        @Test
        public void iterator() {
            Random random = new Random(seed);

            HashSet<String> reference = new HashSet<>();

            int size = random.nextInt(200) + 4;
            if (random.nextFloat() < 0.1) {
                size += random.nextInt(800);
            }

            for (int k = 0; k < size; k++) {
                String string = testValues.randomString(random);
                reference.add(string);
            }

            IndexedImmutableSet<String> subject = IndexedImmutableSet.of(reference);

            assertEquals(reference, subject);
            Assert.assertEquals(reference.size(), subject.size());
            Set<String> subjectCopy = new HashSet<>(subject.size());

            for (String e : subject) {
                subjectCopy.add(e);
            }

            Assert.assertEquals(reference, subjectCopy);
        }

        @Test
        public void elementToIndex() {
            Random random = new Random(seed + 2);

            LinkedHashMap<String, Integer> reference = new LinkedHashMap<>();
            List<String> referenceList = new ArrayList<>();

            int size = random.nextInt(200) + 4;
            if (random.nextFloat() < 0.1) {
                size += random.nextInt(800);
            }

            for (int k = 0; k < size; k++) {
                String string = testValues.randomString(random);

                if (!reference.containsKey(string)) {
                    reference.put(string, reference.size());
                    referenceList.add(string);
                }
            }

            IndexedImmutableSet<String> subject = IndexedImmutableSet.of(reference.keySet());

            assertEquals(reference.keySet(), subject);

            List<String> shuffledReferenceList = new ArrayList<>(referenceList);
            Collections.shuffle(shuffledReferenceList, random);

            for (int k = 0; k < reference.size(); k++) {
                String string1 = referenceList.get(k);
                int index = subject.elementToIndex(string1);
                Assert.assertEquals(reference.get(string1).intValue(), index);
                String reverseString = subject.indexToElement(index);
                Assert.assertEquals(string1, reverseString);

                String string2 = string1 + "X";
                if (!reference.containsKey(string2)) {
                    Assert.assertEquals(-1, subject.elementToIndex(string2));
                }
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class RandomizedTestSmall {
        @Parameterized.Parameter
        public Integer seed;

        @Parameterized.Parameters(name = "{0}")
        public static Collection<Integer> seeds() {
            ArrayList<Integer> result = new ArrayList<>(1000);

            for (int i = 100; i < 1000; i++) {
                result.add(i);
            }

            return result;
        }

        @Test
        public void toArray() {
            Random random = new Random(seed);

            int initialCount = initialCount(random);

            List<String> initialContent = new ArrayList<>();

            for (int i = 0; i < initialCount; i++) {
                initialContent.add(testValues.randomString(random));
            }

            HashSet<String> reference = new HashSet<>(initialContent);
            IndexedImmutableSet<String> subject = IndexedImmutableSet.of(reference);

            assertEquals(reference, subject);

            Object[] subjectArray = subject.toArray();
            Assert.assertEquals(
                    reference,
                    new HashSet<>(Arrays.asList(subjectArray).stream()
                            .map((e) -> e.toString())
                            .collect(Collectors.toSet())));

            String[] subjectStringArray = subject.toArray(new String[0]);
            Assert.assertEquals(reference, new HashSet<>(Arrays.asList(subjectStringArray)));

            String[] subjectStringArray2 = subject.toArray(new String[subject.size()]);
            Assert.assertEquals(reference, new HashSet<>(Arrays.asList(subjectStringArray2)));
        }

        @Test
        public void hashCodeEquals() {
            Random random = new Random(seed);

            int initialCount = initialCount(random);

            List<String> initialContent = new ArrayList<>();

            for (int i = 0; i < initialCount; i++) {
                initialContent.add(testValues.randomString(random));
            }

            HashSet<String> reference = new HashSet<>(initialContent);
            IndexedImmutableSet<String> subject = IndexedImmutableSet.of(reference);

            Assert.assertEquals(reference, subject);
            Assert.assertEquals(reference.hashCode(), subject.hashCode());
        }

        private static int initialCount(Random random) {
            float f = random.nextFloat();

            if (f < 0.3) {
                return random.nextInt(10);
            } else if (f < 0.7) {
                return random.nextInt(40);
            } else {
                return random.nextInt(300);
            }
        }
    }

    public static class BasicTest {
        @Test
        public void builder_toString() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder = IndexedImmutableSetImpl.builder(10);
            builder = builder.with("a").with("b").with("c");
            Assert.assertEquals("[a, b, c]", builder.toString());
        }

        @Test(expected = IllegalArgumentException.class)
        public void builder_nullElement() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder = IndexedImmutableSetImpl.builder(10);
            builder = builder.with(null);
        }

        @Test
        public void builder_contains_positive() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder = IndexedImmutableSetImpl.builder(10);
            builder = builder.with("a").with("b").with("c");
            Assert.assertTrue(builder.contains("a"));
        }

        @Test
        public void builder_contains_negative() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder = IndexedImmutableSetImpl.builder(10);
            Assert.assertFalse(builder.contains("xyz"));
            builder = builder.with("a").with("b").with("c");
            Assert.assertFalse(builder.contains("xyz"));
        }

        @Test
        public void builder_iterator_empty() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder = IndexedImmutableSetImpl.builder(10);
            Assert.assertFalse(builder.iterator().hasNext());
        }

        @Test(expected = NoSuchElementException.class)
        public void builder_iterator_exhausted() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder =
                    IndexedImmutableSetImpl.<String>builder(10).with("a");
            Iterator<String> iter = builder.iterator();

            while (iter.hasNext()) {
                iter.next();
            }

            iter.next();
        }

        @Test
        public void of_self() {
            IndexedImmutableSet<String> set1 = IndexedImmutableSet.of("a");
            IndexedImmutableSet<String> set2 = IndexedImmutableSet.of(set1);

            Assert.assertEquals(set1, set2);
        }

        @Test
        public void of_same() {
            IndexedImmutableSet<String> set1 = IndexedImmutableSet.of("a", "a");
            Assert.assertEquals(1, set1.size());
            Assert.assertEquals(IndexedImmutableSet.of("a"), set1);
        }

        @Test(expected = IllegalArgumentException.class)
        public void builder_addNull() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder =
                    IndexedImmutableSetImpl.<String>builder(10).with("a");
            builder.with(null);
        }

        @Test
        public void builder_add1() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder =
                    IndexedImmutableSetImpl.<String>builder(10).with("a");

            Assert.assertEquals(IndexedImmutableSet.of("a"), builder.build());
        }

        @Test
        public void builder_add2() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder =
                    IndexedImmutableSetImpl.<String>builder(10).with("a").with("b");

            Assert.assertEquals(IndexedImmutableSet.of("a", "b"), builder.build());
        }

        @Test
        public void builder_grow() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder =
                    IndexedImmutableSetImpl.<String>builder(10).probingOverheadFactor((short) 1);
            Set<String> reference = new HashSet<>(33000);

            for (int i = 0; i < 33000; i++) {
                String e = "a" + i;
                builder = builder.with(e);
                reference.add(e);
            }

            Assert.assertEquals(reference, builder.build());
        }

        @Test
        public void builder_toString_setBacked() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder =
                    IndexedImmutableSetImpl.<String>builder(33000).with("a");
            Assert.assertEquals("[a]", builder.toString());
        }

        @Test
        public void builder_contains_setBacked() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder =
                    IndexedImmutableSetImpl.<String>builder(33000).with("a");
            Assert.assertTrue(builder.contains("a"));
            Assert.assertFalse(builder.contains("b"));
        }

        @Test
        public void builder_empty_setBacked() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder = IndexedImmutableSetImpl.<String>builder(33000);
            Assert.assertEquals(0, builder.size());
            Assert.assertEquals(IndexedImmutableSetImpl.empty(), builder.build());
        }

        @Test(expected = IllegalArgumentException.class)
        public void of3_null() {
            IndexedImmutableSet.of(new HashSet<>(Arrays.asList("a", "b", null)));
        }

        @Test
        public void empty() {
            IndexedImmutableSet<String> set1 = IndexedImmutableSet.empty();
            Assert.assertEquals(0, set1.size());
            Assert.assertFalse(set1.contains("a"));
            Assert.assertFalse(set1.iterator().hasNext());
            Assert.assertEquals(0, set1.toArray().length);
            Assert.assertEquals(0, set1.toArray(new String[0]).length);
            Assert.assertTrue(set1.isEmpty());
        }
    }

    @RunWith(Parameterized.class)
    public static class ParameterizedTest {
        final IndexedImmutableSet<String> subject;
        final Set<String> reference;

        @Test
        public void equals() {
            Assert.assertEquals(reference, subject);
        }

        @Test
        public void isEmpty() {
            Assert.assertEquals(reference.isEmpty(), subject.isEmpty());
        }

        @Test
        public void contains_positive() {
            Assert.assertEquals(reference.contains("a"), subject.contains("a"));
        }

        @Test
        public void contains_negative() {
            Assert.assertEquals(reference.contains("ä"), subject.contains("ä"));
        }

        @Test
        public void containsAll_positive() {
            Assert.assertTrue(subject.containsAll(reference));
        }

        @Test
        public void containsAll_negative() {
            HashSet<String> more = new HashSet<>(reference);
            more.add("deos_not_exist");
            Assert.assertFalse(subject.containsAll(more));
        }

        @Test
        public void elementToIndex_notExists() {
            Assert.assertEquals(-1, subject.elementToIndex("does_not_exist"));
        }

        @Test
        public void indexToElement_notExists() {
            Assert.assertNull(subject.indexToElement(99999));
            Assert.assertNull(subject.indexToElement(-1));
        }

        @Test
        public void toArray() {
            Assert.assertArrayEquals(reference.toArray(), subject.toArray());
        }

        @Test
        public void toArray_param() {
            Assert.assertArrayEquals(reference.toArray(new String[0]), subject.toArray(new String[0]));
        }

        @Test
        public void elementToIndex() {
            int index = subject.elementToIndex("a");

            if (!reference.contains("a")) {
                return;
            }

            Assert.assertNotEquals(-1, index);
            String element = subject.indexToElement(index);
            Assert.assertEquals("a", element);
        }

        @Test
        public void elementToIndexB() {
            int index = subject.elementToIndex("b");

            if (!reference.contains("b")) {
                return;
            }

            Assert.assertNotEquals(-1, index);
            String element = subject.indexToElement(index);
            Assert.assertEquals("b", element);
        }

        @Test
        public void builder() {
            IndexedImmutableSetImpl.InternalBuilder<String> builder = IndexedImmutableSetImpl.builder(10);

            for (String e : reference) {
                Assert.assertFalse(builder.contains(e));
                builder = builder.with(e);
                Assert.assertTrue(builder.contains(e));
            }

            IndexedImmutableSetImpl<String> result = builder.build();
            Assert.assertEquals(reference, result);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void add() {
            subject.add("x");
        }

        @Test(expected = UnsupportedOperationException.class)
        public void addAll() {
            subject.addAll(Arrays.asList("x"));
        }

        @Test(expected = UnsupportedOperationException.class)
        public void clear() {
            subject.clear();
        }

        @Test(expected = UnsupportedOperationException.class)
        public void remove() {
            subject.remove("x");
        }

        @Test(expected = UnsupportedOperationException.class)
        public void removeAll() {
            subject.removeAll(Arrays.asList("x"));
        }

        @Test(expected = UnsupportedOperationException.class)
        public void retainAll() {
            subject.retainAll(Arrays.asList("x"));
        }

        @Test(expected = NoSuchElementException.class)
        public void iterator_exhausted() {
            Iterator<String> iter = subject.iterator();

            while (iter.hasNext()) {
                iter.next();
            }

            iter.next();
        }

        public ParameterizedTest(Set<String> reference, IndexedImmutableSet<String> subject) {
            this.subject = subject;
            this.reference = reference;
        }

        @Parameterized.Parameters(name = "{0}")
        public static Collection<Object[]> params() {
            ArrayList<Object[]> result = new ArrayList<>();

            result.add(new Object[] {new HashSet<>(), IndexedImmutableSetImpl.empty()});
            result.add(new Object[] {
                new HashSet<>(), IndexedImmutableSetImpl.builder(10).build()
            });
            result.add(new Object[] {new HashSet<>(Arrays.asList("a")), IndexedImmutableSet.of("a")});
            result.add(new Object[] {new HashSet<>(Arrays.asList("a", "b")), IndexedImmutableSet.of("a", "b")});
            result.add(new Object[] {
                new HashSet<>(Arrays.asList("a", "b", "c")),
                IndexedImmutableSet.of(new HashSet<>(Arrays.asList("a", "b", "c")))
            });
            result.add(new Object[] {
                new HashSet<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l")),
                IndexedImmutableSet.of(
                        new HashSet<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l")))
            });
            Set<String> set1000 = TestUtils.stringSet(1000);
            result.add(new Object[] {set1000, IndexedImmutableSet.of(set1000)});
            Set<String> set2000 = TestUtils.stringSet(2000);
            result.add(new Object[] {set2000, IndexedImmutableSet.of(set2000)});
            Set<String> set3000 = TestUtils.stringSet(3000);
            result.add(new Object[] {set3000, IndexedImmutableSet.of(set3000)});
            Set<String> set4000 = TestUtils.stringSet(4000);
            result.add(new Object[] {set4000, IndexedImmutableSet.of(set4000)});
            Set<String> set5000 = TestUtils.stringSet(5000);
            result.add(new Object[] {set5000, IndexedImmutableSet.of(set5000)});
            Set<String> set16000 = TestUtils.stringSet(16000);
            result.add(new Object[] {set16000, IndexedImmutableSet.of(set16000)});
            Set<String> set33000 = TestUtils.stringSet(33000);
            result.add(new Object[] {set33000, IndexedImmutableSet.of(set33000)});

            return result;
        }
    }

    private static <E> void assertEquals(HashSet<E> expected, IndexedImmutableSetImpl.InternalBuilder<E> actual) {
        for (E e : expected) {
            if (!actual.contains(e)) {
                Assert.fail("Not found in actual: "
                        + e
                        + ";\nexpected ("
                        + expected.size()
                        + "): "
                        + expected
                        + "\nactual ("
                        + actual.size()
                        + "): "
                        + actual);
            }
        }

        for (E e : actual) {
            if (!expected.contains(e)) {
                Assert.fail("Not found in expected: "
                        + e
                        + ";\nexpected ("
                        + expected.size()
                        + "): "
                        + expected
                        + "\nactual ("
                        + actual.size()
                        + "): "
                        + actual);
            }
        }

        if (expected.size() != actual.size()) {
            Assert.fail("Size does not match: "
                    + expected.size()
                    + " vs "
                    + actual.size()
                    + ";\nexpected: "
                    + expected
                    + "\nactual: "
                    + actual);
        }
    }

    private static <E> void assertEquals(Set<E> expected, IndexedImmutableSet<E> actual) {
        for (E e : expected) {
            if (!actual.contains(e)) {
                Assert.fail("Not found in actual: "
                        + e
                        + ";\nexpected ("
                        + expected.size()
                        + "): "
                        + expected
                        + "\nactual ("
                        + actual.size()
                        + "): "
                        + actual);
            }
        }

        for (E e : actual) {
            if (!expected.contains(e)) {
                Assert.fail("Not found in expected: "
                        + e
                        + ";\nexpected ("
                        + expected.size()
                        + "): "
                        + expected
                        + "\nactual ("
                        + actual.size()
                        + "): "
                        + actual);
            }
        }

        if (expected.size() != actual.size()) {
            Assert.fail("Size does not match: "
                    + expected.size()
                    + " vs "
                    + actual.size()
                    + ";\nexpected: "
                    + expected
                    + "\nactual: "
                    + actual);
        }
    }
}
