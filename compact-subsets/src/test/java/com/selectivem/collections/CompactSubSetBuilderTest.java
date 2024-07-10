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

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RunWith(Enclosed.class)
public class CompactSubSetBuilderTest {
    @RunWith(Parameterized.class)
    public static class ParameterizedTest {
        final Set<String> superSet;
        final Set<String> subSetCandidates;
        final CompactSubSetBuilder<String> subject;
        final Set<String> subSetReference;

        @Test
        public void basic() {
            ImmutableCompactSubSet<String> subSet = subject.of(subSetCandidates);
            Assert.assertEquals(subSetReference, subSet);
            Assert.assertTrue(subSet.equals(subSetReference));
        }

        @Test
        public void size() {
            ImmutableCompactSubSet<String> subSet = subject.of(subSetCandidates);
            Assert.assertEquals(subSetReference.size(), subSet.size());
        }

        @Test
                public void contains() {
            ImmutableCompactSubSet<String> subSet = subject.of(subSetCandidates);

            for (String subSetCandidate : subSetCandidates) {
                if (superSet.contains(subSetCandidate)) {
                    Assert.assertTrue(subSet.contains(subSetCandidate));
                } else {
                    Assert.assertFalse(subSet.contains(subSetCandidate));
                }
            }

            for (String superSetElement : superSet) {
                if (subSetReference.contains(superSetElement)) {
                    Assert.assertTrue(subSet.contains(superSetElement));
                } else {
                    Assert.assertFalse(subSet.contains(superSetElement));
                }
            }
        }

        @Test
                public void equalsBetweenSameClass() {
            ImmutableCompactSubSet<String> subSet1 = subject.of(subSetCandidates);
            ImmutableCompactSubSet<String> subSet2 = subject.of(subSetCandidates);
            ImmutableCompactSubSet<String> subSetX = subject.of(superSet);

            Assert.assertTrue(subSet1.equals(subSet2));

            if (subSetReference.size() < superSet.size()) {
                Assert.assertFalse(subSet1.equals(subSetX));
            }
        }

       public ParameterizedTest(Set<String> superSet, Set<String> subSetCandidates) {
            this.superSet = superSet;
            this.subSetCandidates = subSetCandidates;
            this.subject = new CompactSubSetBuilder<>(this.superSet);
            this.subSetReference = intersection(superSet, subSetCandidates);
        }

        @Parameterized.Parameters(name = "{0}/{1}")
        public static Collection<Object[]> params() {
            ArrayList<Object[]> result = new ArrayList<>();

            result.add(new Object [] {setOf("a"), setOf("a")});
            result.add(new Object [] {setOf("a"), setOf("x")});
            result.add(new Object [] {setOf("a"), setOf("a", "xx")});
            result.add(new Object [] {setOf("a", "b"), setOf("x")});
            result.add(new Object [] {setOf("a", "b"), setOf("a", "xx")});
            result.add(new Object [] {stringSet(100), setOf("an", "xx")});
            result.add(new Object [] {stringSet(100), setOf("a", "xx")});

            return result;
        }

        static Set<String> setOf(String ... elements) {
            return new HashSet<>(Arrays.asList(elements));
        }

        static Set<String> intersection(Set<String> set1, Set<String> set2) {
            HashSet<String> result = new HashSet<>(set1);
            result.retainAll(set2);
            return result;
        }


        static Set<String> stringSet(int size) {
            HashSet<String> result = new HashSet<>(size);

            for (char c = 'a'; c <= 'z'; c++) {
                result.add(String.valueOf(c));

                if (result.size() >= size) {
                    return result;
                }
            }

            for (char c1 = 'a'; c1 <= 'z'; c1++) {
                for (char c2 = 'a'; c2 <= 'z'; c2++) {
                    result.add(String.valueOf(c1) + String.valueOf(c2));

                    if (result.size() >= size) {
                        return result;
                    }
                }
            }

            for (char c1 = 'a'; c1 <= 'z'; c1++) {
                for (char c2 = 'a'; c2 <= 'z'; c2++) {
                    for (char c3 = 'a'; c3 <= 'z'; c3++) {

                        result.add(String.valueOf(c1) + String.valueOf(c2) + String.valueOf(c3));

                        if (result.size() >= size) {
                            return result;
                        }
                    }
                }
            }

            return result;
        }
    }
}
