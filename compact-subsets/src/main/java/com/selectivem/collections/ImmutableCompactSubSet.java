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

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ImmutableCompactSubSet<E> extends UnmodifiableSet<E> {

    static <E> ImmutableCompactSubSet<E> of(UnmodifiableSet<E> delegate) {
        if (delegate instanceof ImmutableCompactSubSet) {
            return (ImmutableCompactSubSet<E>) delegate;
        }

        return new ImmutableCompactSubSet<E>() {

            public boolean containsAny(Collection<E> elements) {
                return delegate.containsAny(elements);
            }

            public int size() {
                return delegate.size();
            }

            public boolean isEmpty() {
                return delegate.isEmpty();
            }

            public boolean contains(Object o) {
                return delegate.contains(o);
            }

            public Iterator<E> iterator() {
                return delegate.iterator();
            }

            public Object[] toArray() {
                return delegate.toArray();
            }

            public <T> T[] toArray(T[] a) {
                return delegate.toArray(a);
            }

            @Override
            public boolean equals(Object o) {
                return delegate.equals(o);
            }

            public boolean containsAll(Collection<?> c) {
                return delegate.containsAll(c);
            }

            @Override
            public int hashCode() {
                return delegate.hashCode();
            }

            public Spliterator<E> spliterator() {
                return delegate.spliterator();
            }

            public Stream<E> parallelStream() {
                return delegate.parallelStream();
            }

            public Stream<E> stream() {
                return delegate.stream();
            }

            public void forEach(Consumer<? super E> action) {
                delegate.forEach(action);
            }
        };
    }
}
