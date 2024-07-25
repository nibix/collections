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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

abstract class IndexedImmutableSetImpl<E> extends UnmodifiableSetImpl<E> implements UnmodifiableSet<E> {
    static <E> IndexedImmutableSetImpl<E> of(E e1) {
        return new OneElementSet<>(e1);
    }

    static <E> IndexedImmutableSetImpl<E> of(E e1, E e2) {
        return new TwoElementSet<>(e1, e2);
    }

    static <E> IndexedImmutableSetImpl<E> of(Set<E> set) {
        if (set instanceof IndexedImmutableSetImpl) {
            return (IndexedImmutableSetImpl<E>) set;
        }

        int size = set.size();

        if (size == 0) {
            return empty();
        } else if (size == 1) {
            return of(set.iterator().next());
        } else if (size == 2) {
            Iterator<E> iter = set.iterator();
            return of(iter.next(), iter.next());
        } else if (size < 5) {
            return new ArrayBackedSet<>(set);
        } else {
            int hashTableSize = Hashing.hashTableSize(size);
            if (hashTableSize != -1) {
                IndexedImmutableSetImpl.InternalBuilder<E> internalBuilder =
                        new HashArrayBackedSet.Builder<>(hashTableSize, size);

                for (E e : set) {
                    internalBuilder = internalBuilder.with(e);
                }

                return internalBuilder.build();
            } else {
                return new SetBackedSet.Builder<>(set).build();
            }
        }
    }

    static <E> IndexedImmutableSetImpl.InternalBuilder<E> builder(int size) {
        int hashTableSize = Hashing.hashTableSize(size);

        if (hashTableSize != -1) {
            return new HashArrayBackedSet.Builder<>(hashTableSize, size);
        } else {
            return new SetBackedSet.Builder<>(size);
        }
    }

    static <E> IndexedImmutableSetImpl<E> empty() {
        @SuppressWarnings("unchecked")
        IndexedImmutableSetImpl<E> result = (IndexedImmutableSetImpl<E>) EMPTY;
        return result;
    }

    private final int size;

    IndexedImmutableSetImpl(int size) {
        this.size = size;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    abstract int elementToIndex(Object element);

    abstract E indexToElement(int i);

    static final Set<Object> EMPTY = new IndexedImmutableSetImpl<Object>(0) {

        @Override
        public Iterator<Object> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        int elementToIndex(Object element) {
            return -1;
        }

        @Override
        Object indexToElement(int i) {
            return null;
        }
    };

    abstract static class InternalBuilder<E> implements Iterable<E> {
        abstract IndexedImmutableSetImpl.InternalBuilder<E> with(E e);

        abstract IndexedImmutableSetImpl.InternalBuilder<E> with(E[] flat, int size);

        abstract boolean contains(Object o);

        abstract IndexedImmutableSetImpl<E> build();

        abstract int size();

        public abstract Iterator<E> iterator();

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("[");
            boolean first = true;

            for (E e : this) {
                if (first) {
                    first = false;
                } else {
                    result.append(", ");
                }

                result.append(e);
            }

            result.append("]");

            return result.toString();
        }

        IndexedImmutableSetImpl.InternalBuilder<E> probingOverheadFactor(short probingOverheadFactor) {
            return this;
        }
    }

    static class OneElementSet<E> extends IndexedImmutableSetImpl<E> {

        private final E element;

        OneElementSet(E element) {
            super(1);
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

        @Override
        int elementToIndex(Object element) {
            if (element.equals(this.element)) {
                return 0;
            } else {
                return -1;
            }
        }

        @Override
        E indexToElement(int i) {
            if (i == 0) {
                return element;
            } else {
                return null;
            }
        }
    }

    static class TwoElementSet<E> extends IndexedImmutableSetImpl<E> {

        private final E e1;
        private final E e2;

        TwoElementSet(E e1, E e2) {
            super(2);
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

        @Override
        int elementToIndex(Object element) {
            if (element.equals(this.e1)) {
                return 0;
            } else if (element.equals(this.e2)) {
                return 1;
            } else {
                return -1;
            }
        }

        @Override
        E indexToElement(int i) {
            if (i == 0) {
                return e1;
            } else if (i == 1) {
                return e2;
            } else {
                return null;
            }
        }
    }

    static final class ArrayBackedSet<E> extends IndexedImmutableSetImpl<E> {
        private final E[] elements;

        @SuppressWarnings("unchecked")
        ArrayBackedSet(Set<E> elements) {
            super(elements.size());
            this.elements = (E[]) elements.toArray();

            for (int i = 0; i < this.elements.length; i++) {
                if (this.elements[i] == null) {
                    throw new IllegalArgumentException("Does not support null elements");
                }
            }
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            for (int i = 0; i < elements.length; i++) {
                if (elements[i].equals(o)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {

                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < elements.length;
                }

                @Override
                public E next() {
                    if (i >= elements.length) {
                        throw new NoSuchElementException();
                    }

                    E element = elements[i];
                    i++;
                    return element;
                }
            };
        }

        @Override
        public Object[] toArray() {
            return GenericArrays.copyAsObjectArray(elements);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return GenericArrays.copyAsTypedArray(this.elements, a);
        }

        @Override
        int elementToIndex(Object element) {
            int l = elements.length;

            for (int i = 0; i < l; i++) {
                if (elements[i].equals(element)) {
                    return i;
                }
            }

            return -1;
        }

        @Override
        E indexToElement(int i) {
            if (i >= 0 && i < elements.length) {
                return elements[i];
            } else {
                return null;
            }
        }
    }

    static final class HashArrayBackedSet<E> extends IndexedImmutableSetImpl<E> {

        final int tableSize;
        private final int size;
        private final short maxProbingDistance;

        private final E[] table;
        private final E[] flat;
        private final short[] indices;

        HashArrayBackedSet(int tableSize, int size,  short maxProbingDistance, E[] table, short[] indices, E[] flat) {
            super(size);
            this.tableSize = tableSize;
            this.size = size;
            this.maxProbingDistance = maxProbingDistance;
            this.table = table;
            this.indices = indices;
            this.flat = flat;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return checkTable(o, hashPosition(o)) < 0;
        }

        @Override
        int elementToIndex(Object o) {
            int hashPosition = hashPosition(o);

            if (table[hashPosition] == null) {
                return -1;
            } else if (table[hashPosition].equals(o)) {
                return indices[hashPosition];
            }

            int max = hashPosition + maxProbingDistance;

            for (int i = hashPosition + 1; i <= max; i++) {
                if (table[i] == null) {
                    return -1;
                } else if (table[i].equals(o)) {
                    return indices[i];
                }
            }

            return -1;
        }

        @Override
        E indexToElement(int i) {
            if (i >= 0 && i < flat.length) {
                return flat[i];
            } else {
                return null;
            }
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < HashArrayBackedSet.this.size;
                }

                @Override
                public E next() {
                    if (i >= HashArrayBackedSet.this.size) {
                        throw new NoSuchElementException();
                    }

                    E element = HashArrayBackedSet.this.flat[i];

                    i++;

                    return element;
                }
            };
        }

        @Override
        public Object[] toArray() {
            return GenericArrays.copyAsObjectArray(flat, size);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return GenericArrays.copyAsTypedArray(flat, a, size);
        }

        int hashPosition(Object e) {
            return Hashing.hashPosition(tableSize, e);
        }

        int checkTable(Object e, int hashPosition) {
            return Hashing.checkTable(table, e, hashPosition, maxProbingDistance);
        }

        static class Builder<E> extends IndexedImmutableSetImpl.InternalBuilder<E> {
            private E[] table;
            private E[] flat;
            private short[] indices;
            private short size = 0;
            private int probingOverhead;
            private short probingOverheadFactor = 3;
            private final int tableSize;
            private final short maxProbingDistance;

            public Builder(int tableSize) {
                this.tableSize = tableSize;
                this.maxProbingDistance = Hashing.maxProbingDistance(tableSize);
            }

            public Builder(int tableSize, int flatSize) {
                this.tableSize = tableSize;
                this.maxProbingDistance = Hashing.maxProbingDistance(tableSize);
                if (flatSize > 0) {
                    this.flat = GenericArrays.create(flatSize);
                }
            }

            public IndexedImmutableSetImpl.InternalBuilder<E> with(E e) {
                if (e == null) {
                    throw new IllegalArgumentException("Null elements are not supported");
                }

                if (table == null) {
                    int hashPosition = hashPosition(e);
                    table = GenericArrays.create(tableSize + this.maxProbingDistance);
                    indices = new short[tableSize + this.maxProbingDistance];

                    if (flat == null) {
                        flat = GenericArrays.create(tableSize <= 64 ? tableSize : tableSize / 2);
                    }

                    table[hashPosition] = e;
                    indices[hashPosition] = 0;
                    flat[0] = e;
                    size++;
                    return this;
                } else {
                    int position = hashPosition(e);

                    if (table[position] == null) {
                        table[position] = e;
                        indices[position] = size;
                        extendFlat();
                        flat[size] = e;
                        size++;
                        return this;
                    } else if (table[position].equals(e)) {
                        // done
                        return this;
                    } else {
                        // collision
                        int check = checkTable(e, position);

                        if (check < 0) {
                            // done
                            return this;
                        } else if (check == Hashing.NO_SPACE) {
                            int newTableSize = Hashing.nextSize(tableSize);
                            if (newTableSize != -1) {
                                return new Builder<E>(newTableSize)
                                        .probingOverheadFactor(this.probingOverheadFactor)
                                        .with(flat, size)
                                        .with(e);
                            } else {
                                return new SetBackedSet.Builder<E>(this.size)
                                        .with(flat, size)
                                        .with(e);
                            }
                        } else {
                            // check != position
                            table[check] = e;
                            indices[check] = size;
                            extendFlat();
                            flat[size] = e;
                            size++;

                            this.probingOverhead += check - position;

                            if (this.size >= 12 && this.probingOverhead > this.size * this.probingOverheadFactor) {
                                // probing overhead exceeds threshold
                                int newTableSize = Hashing.nextSize(tableSize);
                                if (newTableSize != -1) {
                                    return new HashArrayBackedSet.Builder<E>(newTableSize)
                                            .probingOverheadFactor(this.probingOverheadFactor)
                                            .with(flat, size);
                                } else {
                                    return new SetBackedSet.Builder<E>(this.size).with(flat, size);
                                }
                            }
                        }

                        return this;
                    }
                }
            }

            @Override
            IndexedImmutableSetImpl.InternalBuilder<E> with(E[] flat, int size) {
                if (this.flat == null) {
                    this.flat = flat;
                }

                IndexedImmutableSetImpl.InternalBuilder<E> builder = this;

                for (int i = 0; i < size; i++) {
                    builder = builder.with(flat[i]);
                }

                return builder;
            }

            public IndexedImmutableSetImpl<E> build() {
                if (size == 0) {
                    return IndexedImmutableSetImpl.empty();
                } else if (size == 1) {
                    return new OneElementSet<>(this.flat[0]);
                } else if (size == 2) {
                    return new TwoElementSet<>(this.flat[0], this.flat[1]);
                } else {
                    E[] flat = this.flat;
                    if (flat.length > size + 16) {
                        flat = GenericArrays.create(size);
                        System.arraycopy(this.flat, 0, flat, 0, size);
                    }
                    return new HashArrayBackedSet<>(tableSize, size, maxProbingDistance, table, indices, flat);
                }
            }

            @Override
            int size() {
                return size;
            }

            @Override
            public Iterator<E> iterator() {
                if (size == 0) {
                    return Collections.emptyIterator();
                }

                return new Iterator<E>() {
                    int pos = 0;

                    @Override
                    public boolean hasNext() {
                        if (pos < size) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    @Override
                    public E next() {
                        if (pos < size) {
                            E result = flat[pos];
                            pos++;
                            return result;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }
                };
            }

            IndexedImmutableSetImpl.InternalBuilder<E> probingOverheadFactor(short probingOverheadFactor) {
                this.probingOverheadFactor = probingOverheadFactor;
                return this;
            }

            private int hashPosition(Object e) {
                return Hashing.hashPosition(tableSize, e);
            }

            private void extendFlat() {
                if (size >= flat.length) {
                    this.flat =
                            GenericArrays.extend(flat, Math.min(flat.length + flat.length / 2 + 8, this.table.length));
                }
            }

            int checkTable(Object e, int hashPosition) {
                int max = hashPosition + this.maxProbingDistance;

                for (int i = hashPosition + 1; i <= max; i++) {
                    if (table[i] == null) {
                        return i;
                    } else if (table[i].equals(e)) {
                        return -1 - i;
                    }
                }

                return Hashing.NO_SPACE;
            }

            @Override
            boolean contains(Object o) {
                if (table == null) {
                    return false;
                } else {
                    int hashPosition = hashPosition(o);
                    int max = hashPosition + this.maxProbingDistance;

                    for (int i = hashPosition; i <= max; i++) {
                        if (table[i] == null) {
                            return false;
                        } else if (table[i].equals(o)) {
                            return true;
                        }
                    }

                    return false;
                }
            }
        }
    }

    static final class SetBackedSet<E> extends IndexedImmutableSetImpl<E> {

        private final Map<E, Integer> elements;
        private final E[] flat;

        SetBackedSet(Map<E, Integer> elements, E[] flat) {
            super(elements.size());
            this.elements = elements;
            this.flat = flat;
        }

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public boolean isEmpty() {
            return elements.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return elements.containsKey(o);
        }

        @Override
        public Iterator<E> iterator() {
            return elements.keySet().iterator();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return elements.keySet().toArray(a);
        }

        @Override
        public Object[] toArray() {
            return elements.keySet().toArray();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return elements.keySet().containsAll(c);
        }

        @Override
        int elementToIndex(Object element) {
            Integer pos = this.elements.get(element);

            if (pos != null) {
                return pos.intValue();
            } else {
                return -1;
            }
        }

        @Override
        E indexToElement(int i) {
            if (i >= 0 && i < flat.length) {
                return flat[i];
            } else {
                return null;
            }
        }

        static class Builder<E> extends IndexedImmutableSetImpl.InternalBuilder<E> {
            private final HashMap<E, Integer> delegate;
            private E[] flat;
            private final int expectedCapacity;

            Builder(int expectedCapacity) {
                this.delegate = new HashMap<>(expectedCapacity);
                this.expectedCapacity = expectedCapacity;
            }

            Builder(Collection<E> set) {
                int size = set.size();
                this.delegate = new HashMap<>(size);
                this.flat = GenericArrays.create(size);
                this.expectedCapacity = size;

                int i = 0;

                for (E e : set) {
                    this.delegate.put(e, i);
                    this.flat[i] = e;
                    i++;
                }
            }

            @Override
            public SetBackedSet.Builder<E> with(E e) {
                int pos = this.delegate.size();
                this.delegate.put(e, pos);
                extendFlat();
                this.flat[pos] = e;
                return this;
            }

            @Override
            IndexedImmutableSetImpl.InternalBuilder<E> with(E[] flat, int size) {
                if (this.flat == null) {
                    this.flat = flat;

                    for (int i = 0; i < size; i++) {
                        this.delegate.put(flat[i], i);
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        with(flat[i]);
                    }
                }

                return this;
            }

            @Override
            IndexedImmutableSetImpl<E> build() {
                if (delegate.isEmpty()) {
                    return IndexedImmutableSetImpl.empty();
                } else {
                    return new SetBackedSet<>(this.delegate, this.flat);
                }
            }

            @Override
            int size() {
                return delegate.size();
            }

            @Override
            public Iterator<E> iterator() {
                return delegate.keySet().iterator();
            }

            @Override
            public String toString() {
                return delegate.keySet().toString();
            }

            @Override
            boolean contains(Object o) {
                return delegate.containsKey(o);
            }

            private void extendFlat() {
                if (flat == null) {
                    this.flat = GenericArrays.create(this.expectedCapacity);
                } else if (delegate.size() >= flat.length) {
                    this.flat = GenericArrays.extend(flat, flat.length + flat.length / 2 + 8);
                }
            }
        }
    }
}
