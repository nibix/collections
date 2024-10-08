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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Enclosed.class)
public class CompactMapGroupBuilderTest {
    public static class BasicTest {

        @Test
        public void get_missingValueProvider() {
            CompactMapGroupBuilder<String, List<String>> subject =
                    new CompactMapGroupBuilder<>(setOf("a", "b", "c", "d"), k -> new ArrayList<>());
            CompactMapGroupBuilder.MapBuilder<String, List<String>> builder = subject.createMapBuilder();

            builder.get("a").add("x");
            Assert.assertEquals(Arrays.asList("x"), builder.get("a"));

            builder.get("a").add("y");
            Assert.assertEquals(Arrays.asList("x", "y"), builder.get("a"));

            Map<String, List<String>> map = builder.build();
            Assert.assertEquals(Arrays.asList("x", "y"), map.get("a"));
        }

        @Test
        public void containsValue_positive() {
            CompactMapGroupBuilder<String, String> subject = new CompactMapGroupBuilder<>(setOf("a", "b", "c", "d"));
            Map<String, String> map = subject.of(mapOf("a", "aa", "b", "bb"));
            Assert.assertTrue(map.containsValue("aa"));
        }

        @Test
        public void containsValue_negative() {
            CompactMapGroupBuilder<String, String> subject = new CompactMapGroupBuilder<>(setOf("a", "b", "c", "d"));
            Map<String, String> map = subject.of(mapOf("a", "aa", "b", "bb"));
            Assert.assertFalse(map.containsValue("zz"));
        }

        @Test
        public void isEmpty() {
            CompactMapGroupBuilder<String, String> subject = new CompactMapGroupBuilder<>(setOf("a", "b", "c", "d"));
            Map<String, String> map = subject.of(mapOf("a", "aa", "b", "bb"));
            Assert.assertFalse(map.isEmpty());
        }

        @Test
        public void builder_put_existing() {
            CompactMapGroupBuilder<String, String> subject = new CompactMapGroupBuilder<>(setOf("a", "b", "c", "d"));
            CompactMapGroupBuilder.MapBuilder<String, String> builder = subject.createMapBuilder();
            Assert.assertEquals(0, builder.size());
            builder.put("a", "aa");
            Assert.assertEquals(1, builder.size());
            Assert.assertEquals("aa", builder.get("a"));
            builder.put("a", "aaa");
            Assert.assertEquals(1, builder.size());
            Assert.assertEquals("aaa", builder.get("a"));
            builder.put("b", "bb");
            Assert.assertEquals(2, builder.size());
            Assert.assertEquals("bb", builder.get("b"));
        }

        @Test(expected = IllegalArgumentException.class)
        public void builder_put_invalidKey() {
            CompactMapGroupBuilder<String, String> subject = new CompactMapGroupBuilder<>(setOf("a", "b", "c", "d"));
            subject.createMapBuilder().put("x", "y");
        }

        @Test(expected = IllegalArgumentException.class)
        public void builder_put_null() {
            CompactMapGroupBuilder<String, String> subject = new CompactMapGroupBuilder<>(setOf("a", "b", "c", "d"));
            subject.createMapBuilder().put("a", null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void builder_get_invalidKey() {
            CompactMapGroupBuilder<String, String> subject =
                    new CompactMapGroupBuilder<>(setOf("a", "b", "c", "d"), k -> "a");
            subject.createMapBuilder().get("x");
        }

        @Test(expected = IllegalStateException.class)
        public void builder_build_reuse() {
            CompactMapGroupBuilder<String, String> subject = new CompactMapGroupBuilder<>(setOf("a", "b", "c", "d"));
            CompactMapGroupBuilder.MapBuilder<String, String> builder = subject.createMapBuilder();
            builder.put("a", "1");
            builder.build();
            builder.put("b", "2");
        }
    }

    @RunWith(Parameterized.class)
    public static class ParameterizedTest {
        final Set<String> keySuperSet;
        final Map<String, String> referenceMap;
        final CompactMapGroupBuilder<String, String> subject;

        @Test
        public void basic() {
            Map<String, String> map = subject.of(referenceMap);
            Assert.assertEquals(referenceMap, map);
            Assert.assertTrue(map.equals(referenceMap));
        }

        @Test
        public void size() {
            Map<String, String> map = subject.of(referenceMap);
            Assert.assertEquals(map.size(), referenceMap.size());
        }

        @Test
        public void containsKey() {
            Map<String, String> map = subject.of(referenceMap);

            for (String key : keySuperSet) {
                if (referenceMap.containsKey(key)) {
                    Assert.assertTrue(map.containsKey(key));
                } else {
                    Assert.assertFalse(map.containsKey(key));
                }
            }
        }

        @Test
        public void keySet_contains() {
            Map<String, String> map = subject.of(referenceMap);

            for (String key : keySuperSet) {
                if (referenceMap.containsKey(key)) {
                    Assert.assertTrue(map.keySet().contains(key));
                } else {
                    Assert.assertFalse(map.keySet().contains(key));
                }
            }
        }

        @Test
        public void entrySet_contains() {
            Map<String, String> map = subject.of(referenceMap);

            for (String key : keySuperSet) {
                if (referenceMap.containsKey(key)) {
                    Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>(key, referenceMap.get(key));
                    Assert.assertTrue(map.entrySet().contains(entry));
                    Map.Entry<String, String> wrongEntry = new AbstractMap.SimpleEntry<>(key, "xxxx");
                    Assert.assertFalse(map.entrySet().contains(wrongEntry));
                } else {
                    Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>(key, "xxxx");
                    Assert.assertFalse(map.entrySet().contains(entry));
                }
            }
        }

        @Test
        public void get() {
            Map<String, String> map = subject.of(referenceMap);

            for (String key : keySuperSet) {
                String value = map.get(key);
                Assert.assertEquals(referenceMap.get(key), value);
            }
        }

        @Test
        public void entrySet() {
            Map<String, String> map = subject.of(referenceMap);
            Assert.assertEquals(referenceMap.entrySet(), map.entrySet());
        }

        @Test
        public void keySet() {
            Map<String, String> map = subject.of(referenceMap);
            Assert.assertEquals(referenceMap.keySet(), map.keySet());
        }

        @Test
        public void forEach() {
            Map<String, String> map = subject.of(referenceMap);
            Map<String, String> forEachSink = new HashMap<>();
            map.forEach((k, v) -> forEachSink.put(k, v));
            Assert.assertEquals(referenceMap, forEachSink);
        }

        public ParameterizedTest(Set<String> superSet, Map<String, String> referenceMap) {
            this.keySuperSet = superSet;
            this.referenceMap = referenceMap;
            this.subject = new CompactMapGroupBuilder<>(this.keySuperSet);
        }

        @Parameterized.Parameters(name = "{0}/{1}")
        public static Collection<Object[]> params() {
            ArrayList<Object[]> result = new ArrayList<>();

            result.add(new Object[] {setOf("a"), mapOf("a", "a_val")});
            result.add(new Object[] {setOf("a", "b"), mapOf("a", "a_val")});
            result.add(new Object[] {setOf("a", "b"), mapOf("a", "a_val", "b", "b_val")});
            result.add(new Object[] {stringSet(100), mapOf("a", "a_val")});
            result.add(new Object[] {stringSet(100), mapOf("x", "x_val")});
            result.add(new Object[] {stringSet(100), stringMap(90)});
            result.add(new Object[] {stringSet(700), stringMap(70)});
            result.add(new Object[] {stringSet(700), stringMap(700)});

            return result;
        }
    }

    @RunWith(Parameterized.class)
    public static class RandomizedTest {
        final int superSetSize;
        final int mapSize;
        final Random random;
        final Set<String> keySuperSet;
        final Map<String, String> referenceMap;
        final CompactMapGroupBuilder<String, String> subject;

        @Test
        public void basic() {
            Map<String, String> map = subject.of(referenceMap);
            Assert.assertEquals(referenceMap, map);
            Assert.assertTrue(map.equals(referenceMap));
        }

        @Test
        public void size() {
            Map<String, String> map = subject.of(referenceMap);
            Assert.assertEquals(map.size(), referenceMap.size());
        }

        @Test
        public void containsKey() {
            Map<String, String> map = subject.of(referenceMap);

            for (String key : keySuperSet) {
                if (referenceMap.containsKey(key)) {
                    Assert.assertTrue(map.containsKey(key));
                } else {
                    Assert.assertFalse(map.containsKey(key));
                }
            }
        }

        @Test
        public void get() {
            Map<String, String> map = subject.of(referenceMap);

            for (String key : keySuperSet) {
                String value = map.get(key);
                Assert.assertEquals(referenceMap.get(key), value);
            }
        }

        public RandomizedTest(int seed, int superSetSize, int mapSize) {
            this.superSetSize = superSetSize;
            this.mapSize = mapSize;
            this.random = new Random(seed);
            this.keySuperSet = createSuperSet(superSetSize);
            this.referenceMap = createReference(this.keySuperSet, mapSize);
            this.subject = new CompactMapGroupBuilder<>(this.keySuperSet);
        }

        @Parameterized.Parameters(name = "seed: {0}; superSetSize: {1}; mapSize: {2}")
        public static Collection<Object[]> seeds() {
            ArrayList<Object[]> result = new ArrayList<>();

            for (int superSetSize = 1; superSetSize < 10; superSetSize++) {
                for (int mapSize = 0; mapSize <= superSetSize; mapSize++) {
                    for (int i = 101; i < 111; i++) {
                        result.add(new Object[] {i, superSetSize, mapSize});
                    }
                }
            }

            for (int superSetSize = 10; superSetSize < 100; superSetSize += 20) {
                for (int mapSize = 1; mapSize <= superSetSize; mapSize *= 2) {
                    for (int i = 101; i < 111; i++) {
                        result.add(new Object[] {i, superSetSize, mapSize});
                    }
                }
            }

            for (int superSetSize = 100; superSetSize < 1000; superSetSize += 200) {
                for (int mapSize = 1; mapSize <= superSetSize; mapSize *= 2) {
                    for (int i = 101; i < 111; i++) {
                        result.add(new Object[] {i, superSetSize, mapSize});
                    }
                }
            }

            for (int superSetSize = 1000; superSetSize < 10000; superSetSize += 2000) {
                for (int mapSize = 1; mapSize <= superSetSize; mapSize *= 2) {
                    for (int i = 101; i < 111; i++) {
                        result.add(new Object[] {i, superSetSize, mapSize});
                    }
                }
            }

            return result;
        }

        static Set<String> createSuperSet(int size) {
            Set<String> superSet = new HashSet<>();

            for (int i = 0; i < size; i++) {
                superSet.add("a" + i);
            }

            return superSet;
        }

        Map<String, String> createReference(Set<String> superSet, int size) {
            HashMap<String, String> result = new HashMap<>();

            while (result.size() < size) {
                for (String e : superSet) {
                    if (random.nextFloat() < ((double) size) / superSet.size() * 0.3) {
                        result.put(e, e + "_val");
                    }
                }
            }

            return result;
        }
    }
}
