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

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

abstract class ImmutableSetImpl<E> extends UnmodifiableSetImpl<E> implements UnmodifiableSet<E> {
    static <E> ImmutableSetImpl<E> of(E e1) {
        if (e1 == null) {
            throw new IllegalArgumentException("Does not support null elements");
        }

        return new OneElementSet<>(e1);
    }

    static <E> ImmutableSetImpl<E> of(E e1, E e2) {
        if (e1 == null || e2 == null) {
            throw new IllegalArgumentException("Does not support null elements");
        }

        if (!e1.equals(e2)) {
            return new TwoElementSet<>(e1, e2);
        } else {
            return new OneElementSet<>(e1);
        }
    }

    static <E> ImmutableSetImpl<E> empty() {
        @SuppressWarnings("unchecked")
        ImmutableSetImpl<E> result = (ImmutableSetImpl<E>) EMPTY;
        return result;
    }

    static class OneElementSet<E> extends ImmutableSetImpl<E> {

        private final E element;

        OneElementSet(E element) {
            this.element = element;
        }

        @Override
        public boolean contains(Object o) {
            return element.equals(o);
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < 1;
                }

                @Override
                public E next() {
                    if (i == 0) {
                        i++;
                        return element;
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }

        @Override
        public Object[] toArray() {
            return new Object[] {element};
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            T[] result = a.length >= 1
                    ? a
                    : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), 1);

            result[0] = (T) element;

            return result;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    static class TwoElementSet<E> extends ImmutableSetImpl<E> {

        private final E e1;
        private final E e2;

        TwoElementSet(E e1, E e2) {
            this.e1 = e1;
            this.e2 = e2;
        }

        @Override
        public boolean contains(Object o) {
            return e1.equals(o) || e2.equals(o);
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {

                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < 2;
                }

                @Override
                public E next() {
                    if (i == 0) {
                        i++;
                        return e1;
                    } else if (i == 1) {
                        i++;
                        return e2;
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }

        @Override
        public Object[] toArray() {
            return new Object[] {e1, e2};
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            T[] result = a.length >= 2
                    ? a
                    : (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), 2);

            result[0] = (T) e1;
            result[1] = (T) e2;

            return result;
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private static final Set<Object> EMPTY = new ImmutableSetImpl<Object>() {
        @Override
        public Iterator<Object> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }
    };
}
