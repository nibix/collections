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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;

public class UnmodifiableSetImplTest {
    @Test(expected = UnsupportedOperationException.class)
    public void add() {
        ConcreteUnmodifiableSetImpl.of("a", "b").add("x");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addAll() {
        ConcreteUnmodifiableSetImpl.of("a", "b").addAll(Arrays.asList("x"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void clear() {
        ConcreteUnmodifiableSetImpl.of("a", "b").clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        ConcreteUnmodifiableSetImpl.of("a", "b").remove("x");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeAll() {
        ConcreteUnmodifiableSetImpl.of("a", "b").removeAll(Arrays.asList("x"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void retainAll() {
        ConcreteUnmodifiableSetImpl.of("a", "b").retainAll(Arrays.asList("x"));
    }

    @Test
    public void containsAny() {
        ConcreteUnmodifiableSetImpl<String> subject = ConcreteUnmodifiableSetImpl.of("a", "b");
        Assert.assertTrue(subject.containsAny(Arrays.asList("a")));
        Assert.assertTrue(subject.containsAny(Arrays.asList("b", "a")));
        Assert.assertTrue(subject.containsAny(Arrays.asList("c", "a")));
        Assert.assertFalse(subject.containsAny(Arrays.asList("c")));
    }

    static class ConcreteUnmodifiableSetImpl<E> extends UnmodifiableSetImpl<E> {
        private final Set<E> delegate = new HashSet<>();

        ConcreteUnmodifiableSetImpl(Collection<E> elements) {
            this.delegate.addAll(elements);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return delegate.iterator();
        }

        @Override
        public Stream<E> stream() {
            return delegate.stream();
        }

        @Override
        public Stream<E> parallelStream() {
            return delegate.parallelStream();
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            delegate.forEach(action);
        }

        static <E> ConcreteUnmodifiableSetImpl<E> of(E... elements) {
            return new ConcreteUnmodifiableSetImpl<>(Arrays.asList(elements));
        }
    }
}
