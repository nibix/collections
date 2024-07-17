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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public class ImmutableCompactSubSetImplTest {

    @Test
    public void of_sameInstance() {
        ImmutableCompactSubSet<String> instance1 =
                new CompactSubSetBuilder<String>(setOf("a", "b", "c", "d")).of(setOf("a", "b", "c"));
        ImmutableCompactSubSet<String> instance2 = ImmutableCompactSubSetImpl.of(instance1);

        Assert.assertTrue(instance1 == instance2);
    }

    @Test
    public void of_wrappedInstance() {
        IndexedImmutableSetImpl<String> instance1 = IndexedImmutableSetImpl.of(setOf("a", "b", "c", "d"));
        ImmutableCompactSubSet<String> instance2 = ImmutableCompactSubSetImpl.of(instance1);

        Assert.assertTrue(instance2.equals(instance1));
        Assert.assertEquals(instance1.hashCode(), instance2.hashCode());
        Assert.assertEquals(instance1.toString(), instance2.toString());
        Assert.assertEquals(instance1.isEmpty(), instance2.isEmpty());
        Assert.assertArrayEquals(instance1.toArray(), instance2.toArray());
    }

    @Test
    public void delegateMethods() {
        Iterator<String> testIterator = new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public String next() {
                return "";
            }
        };

        Object [] testArray = new Object[0];

        Stream<String> testStream = Stream.of("a");

        UnmodifiableSet<String> delegate = new UnmodifiableSet<String>() {
            @Override
            public int size() {
                return 9999;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<String> iterator() {
                return testIterator;
            }

            @Override
            public Object[] toArray() {
                return testArray;
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return (T[]) testArray;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public Stream<String> parallelStream() {
                return testStream;
            }

            @Override
            public Stream<String> stream() {
                return testStream;
            }
        };

        ImmutableCompactSubSet<String> subject = ImmutableCompactSubSetImpl.of(delegate);

        Assert.assertEquals(delegate.size(), subject.size());
        Assert.assertEquals(delegate.isEmpty(), subject.isEmpty());
        Assert.assertSame(delegate.iterator(), subject.iterator());
        Assert.assertSame(delegate.toArray(), subject.toArray());
        Assert.assertSame(delegate.toArray(new String[0]), subject.toArray(new String[0]));
        Assert.assertSame(delegate.parallelStream(), subject.parallelStream());
        Assert.assertSame(delegate.stream(), subject.stream());
    }

    static Set<String> setOf(String... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }
}
