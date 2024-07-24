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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Enclosed.class)
public class ImmutableMapImplTest {
    static final TestValues testValues = new TestValues();

    @RunWith(Parameterized.class)
    public static class RandomizedTestBig {
        private final int seed;
        private final int size;
        private final Random random;
        private final HashMap<String, String> reference = new HashMap<>();

        @Parameterized.Parameters(name = "{0}; size: {1}")
        public static Collection<Object[]> seeds() {
            Random random = new Random(1);
            ArrayList<Object[]> result = new ArrayList<>(10000);

            for (int i = 1000; i < 10000; i++) {
                for (int size : getSizes(random)) {
                    result.add(new Object[] {i, size});
                }
            }

            return result;
        }

        public RandomizedTestBig(int seed, int size) {
            this.seed = seed;
            this.random = new Random(seed);

            for (int k = 0; k < size / 2; k++) {
                String key = testValues.randomLocationName(random);

                if (reference.containsKey(key)) {
                    key = testValues.randomLocationName(random);
                }

                if (reference.containsKey(key)) {
                    key = key + " " + (random.nextInt(9) + 1);
                }

                if (!reference.containsKey(key)) {
                    reference.put(key, testValues.randomIpAddress(random));
                }

                String ip;

                for (int i = 0; i < 10; i++) {
                    ip = testValues.randomIpAddress(random);
                    if (!reference.containsKey(ip)) {
                        reference.put(ip, testValues.randomIpAddress(random));
                        break;
                    }
                }
            }

            this.size = size;
        }

        @Test
        public void builder_with() {
            ImmutableMapImpl.InternalBuilder<String, String> subject = ImmutableMapImpl.InternalBuilder.create(10);
            HashMap<String, String> reference2 = new HashMap<>();

            int k = 0;
            int kcap = size / 100;

            for (String key : reference.keySet()) {
                String value = reference.get(key);
                subject = subject.with(key, value);
                reference2.put(key, value);

                k++;

                if (k > kcap) {
                    assertEquals(reference2, subject);
                    k = 0;
                }
            }

            assertEquals(reference, subject);
            assertEquals(reference, subject.build());
        }

        @Test
        public void get() {
            ImmutableMapImpl<String, String> subject = ImmutableMapImpl.of(reference);

            assertEquals(reference, subject);
            List<String> referenceKeyList = new ArrayList<>(reference.keySet());
            Collections.shuffle(referenceKeyList, random);

            for (int k = 0; k < Math.min(reference.size(), 100); k++) {
                String string1 = referenceKeyList.get(k);
                Assert.assertEquals(reference.get(string1), subject.get(string1));

                String string2 = string1 + "X";
                Assert.assertEquals(reference.get(string2), subject.get(string2));
            }

            for (int k = 0; k < 10; k++) {
                String string3 = testValues.randomString(random);
                Assert.assertEquals(reference.get(string3), subject.get(string3));
            }
        }

        @Test
        public void keySet_iterator() {
            ImmutableMapImpl<String, String> subject = ImmutableMapImpl.of(reference);
            assertEquals(reference, subject);

            Set<String> subjectCopy = new HashSet<>(subject.size());

            for (String e : subject.keySet()) {
                subjectCopy.add(e);
            }

            Assert.assertEquals(reference.keySet(), subjectCopy);
        }

        @Test
        public void entrySet_iterator() {
            ImmutableMapImpl<String, String> subject = ImmutableMapImpl.of(reference);
            assertEquals(reference, subject);

            Map<String, String> subjectCopy = new HashMap<>(subject.size());

            for (Map.Entry<String, String> entry : subject.entrySet()) {
                subjectCopy.put(entry.getKey(), entry.getValue());
            }

            Assert.assertEquals(reference, subjectCopy);
        }
    }

    @RunWith(Parameterized.class)
    public static class RandomizedTestSmall {
        private final int seed;
        private final int size;
        private final Random random;
        private final HashMap<String, String> reference = new HashMap<>();

        @Parameterized.Parameters(name = "{0}; size: {1}")
        public static Collection<Object[]> seeds() {
            Random random = new Random(1);
            ArrayList<Object[]> result = new ArrayList<>(10000);

            for (int i = 100; i < 1000; i++) {
                for (int size : getSizes(random)) {
                    result.add(new Object[] {i, size});
                }
            }

            return result;
        }

        public RandomizedTestSmall(int seed, int size) {
            this.seed = seed;
            this.random = new Random(seed);

            for (int k = 0; k < size / 2; k++) {
                String key = testValues.randomLocationName(random);

                if (reference.containsKey(key)) {
                    key = testValues.randomLocationName(random);
                }

                if (reference.containsKey(key)) {
                    key = key + " " + (random.nextInt(9) + 1);
                }

                if (!reference.containsKey(key)) {
                    reference.put(key, testValues.randomIpAddress(random));
                }

                String ip;

                for (int i = 0; i < 10; i++) {
                    ip = testValues.randomIpAddress(random);
                    if (!reference.containsKey(ip)) {
                        reference.put(ip, testValues.randomIpAddress(random));
                        break;
                    }
                }
            }

            this.size = size;
        }

        @Test
        public void containsKey() {
            ImmutableMapImpl<String, String> subject = ImmutableMapImpl.of(reference);

            assertEquals(reference, subject);
            List<String> referenceKeyList = new ArrayList<>(reference.keySet());
            Collections.shuffle(referenceKeyList, random);

            for (int k = 0; k < Math.min(reference.size() / 2, 20); k++) {
                String string1 = referenceKeyList.get(k);
                assertTrue("String " + string1 + " not found in " + subject, subject.containsKey(string1));

                String string2 = string1 + "X";
                Assert.assertEquals(
                        "String " + string2 + " found in " + subject,
                        reference.containsKey(string2),
                        subject.containsKey(string2));
            }

            for (int k = 0; k < 10; k++) {
                String string3 = testValues.randomString(random);
                Assert.assertEquals(
                        "String " + string3 + " found in " + subject,
                        reference.containsKey(string3),
                        subject.containsKey(string3));
            }
        }

        @Test
        public void hashCodeEquals() {
            ImmutableMapImpl<String, String> subject = ImmutableMapImpl.of(reference);
            assertEquals(reference, subject);
            Assert.assertEquals(reference, subject);
            Assert.assertEquals(reference.hashCode(), subject.hashCode());
        }
    }

    public static class BasicTest {

        @Test(expected = IllegalArgumentException.class)
        public void builder_nullElement() {
            ImmutableMapImpl.InternalBuilder<String, String> builder = ImmutableMapImpl.InternalBuilder.create(10);
            builder = builder.with(null, "a");
        }

        @Test
        public void builder_get_positive() {
            ImmutableMapImpl.InternalBuilder<String, String> builder = ImmutableMapImpl.InternalBuilder.create(10);
            builder = builder.with("a", "aa").with("b", "bb").with("c", "cc");
            Assert.assertEquals("aa", builder.get("a"));
        }

        @Test
        public void builder_get_negative() {
            ImmutableMapImpl.InternalBuilder<String, String> builder = ImmutableMapImpl.InternalBuilder.create(10);
            Assert.assertNull(builder.get("xyz"));
            builder = builder.with("a", "aa").with("b", "bb").with("c", "cc");
            Assert.assertNull(builder.get("xyz"));
        }

        @Test
        public void builder_keySet_iterator_empty() {
            ImmutableMapImpl.InternalBuilder<String, String> builder = ImmutableMapImpl.InternalBuilder.create(10);
            Assert.assertFalse(builder.keySet().iterator().hasNext());
        }

        @Test(expected = NoSuchElementException.class)
        public void builder_keySet_iterator_exhausted() {
            ImmutableMapImpl.InternalBuilder<String, String> builder =
                    ImmutableMapImpl.InternalBuilder.<String, String>create(10).with("a", "aa");
            Iterator<String> iter = builder.keySet().iterator();

            while (iter.hasNext()) {
                iter.next();
            }

            iter.next();
        }

        @Test
        public void of_self() {
            ImmutableMapImpl<String, String> set1 = ImmutableMapImpl.of("a", "aa");
            ImmutableMapImpl<String, String> set2 = ImmutableMapImpl.of(set1);

            Assert.assertEquals(set1, set2);
        }

        @Test(expected = IllegalArgumentException.class)
        public void builder_addNull() {
            ImmutableMapImpl.InternalBuilder<String, String> builder =
                    ImmutableMapImpl.InternalBuilder.<String, String>create(10).with("a", "aa");
            builder.with(null, "a");
        }

        @Test
        public void builder_add1() {
            ImmutableMapImpl.InternalBuilder<String, String> builder =
                    ImmutableMapImpl.InternalBuilder.<String, String>create(10).with("a", "aa");

            Assert.assertEquals(ImmutableMapImpl.of("a", "aa"), builder.build());
        }

        @Test
        public void builder_add2() {
            ImmutableMapImpl.InternalBuilder<String, String> builder =
                    ImmutableMapImpl.InternalBuilder.<String, String>create(10)
                            .with("a", "aa")
                            .with("b", "bb");

            Assert.assertEquals(ImmutableMapImpl.of("a", "aa", "b", "bb"), builder.build());
        }

        @Test
        public void builder_grow() {
            ImmutableMapImpl.InternalBuilder<String, String> builder =
                    ImmutableMapImpl.InternalBuilder.<String, String>create(10).probingOverheadFactor((short) 1);
            Map<String, String> reference = new HashMap<>(4100);

            for (int i = 0; i < 4100; i++) {
                String e = "a" + i;
                builder = builder.with(e, e);
                reference.put(e, e);
            }

            Assert.assertEquals(reference, builder.build());
        }
    }

    private static <K, V> void assertEquals(Map<K, V> expected, ImmutableMapImpl.InternalBuilder<K, V> actual) {
        for (K k : expected.keySet()) {
            if (!actual.containsKey(k)) {
                Assert.fail("Not found in actual: "
                        + k
                        + ";\nexpected ("
                        + expected.size()
                        + "): "
                        + expected
                        + "\nactual ("
                        + actual.size()
                        + "): "
                        + actual);
            }

            V expectedValue = expected.get(k);
            V actualValue = actual.get(k);

            if (!expectedValue.equals(actualValue)) {
                Assert.fail("Value differs for key " + k + ";expected: " + expectedValue + "; actual: " + actualValue);
            }
        }

        for (K k : actual.keySet()) {
            if (!expected.containsKey(k)) {
                Assert.fail("Not found in expected: "
                        + k
                        + ";\nexpected ("
                        + expected.size()
                        + "): "
                        + expected
                        + "\nactual ("
                        + actual.size()
                        + "): "
                        + actual);
            }

            V expectedValue = expected.get(k);
            V actualValue = actual.get(k);

            if (!expectedValue.equals(actualValue)) {
                Assert.fail("Value differs for key " + k + ";expected: " + expectedValue + "; actual: " + actualValue);
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

    private static <K, V> void assertEquals(Map<K, V> expected, ImmutableMapImpl<K, V> actual) {
        for (K k : expected.keySet()) {
            if (!actual.containsKey(k)) {
                Assert.fail("Not found in actual: "
                        + k
                        + ";\nexpected ("
                        + expected.size()
                        + "): "
                        + expected
                        + "\nactual ("
                        + actual.size()
                        + "): "
                        + actual);
            }

            V expectedValue = expected.get(k);
            V actualValue = actual.get(k);

            if (!expectedValue.equals(actualValue)) {
                Assert.fail("Value differs for key " + k + ";expected: " + expectedValue + "; actual: " + actualValue);
            }
        }

        for (K k : actual.keySet()) {
            if (!expected.containsKey(k)) {
                Assert.fail("Not found in expected: "
                        + k
                        + ";\nexpected ("
                        + expected.size()
                        + "): "
                        + expected
                        + "\nactual ("
                        + actual.size()
                        + "): "
                        + actual);
            }

            V expectedValue = expected.get(k);
            V actualValue = actual.get(k);

            if (!expectedValue.equals(actualValue)) {
                Assert.fail("Value differs for key " + k + ";expected: " + expectedValue + "; actual: " + actualValue);
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

    private static Collection<Integer> getSizes(Random random) {
        TreeSet<Integer> result = new TreeSet<>();

        for (int k = 0; k < 5; k++) {
            int size = random.nextInt(200) + 4;
            if (random.nextFloat() < 0.2) {
                size += random.nextInt(800);

                if (random.nextFloat() < 0.5) {
                    size += random.nextInt(8000);

                    if (random.nextFloat() < 0.2) {
                        size += random.nextInt(200000);
                    }
                }
            }

            result.add(size);
        }

        return result;
    }
}
