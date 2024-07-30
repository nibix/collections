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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

abstract class ImmutableMapImpl<K, V> extends UnmodifiableMapImpl<K, V> {

    @SuppressWarnings("unchecked")
    static <K, V> ImmutableMapImpl<K, V> empty() {
        return (ImmutableMapImpl<K, V>) EMPTY;
    }

    static <K, V> ImmutableMapImpl<K, V> of(K k1, V v1) {
        return new SingleElementMap<>(k1, v1);
    }

    static <K, V> ImmutableMapImpl<K, V> of(K k1, V v1, K k2, V v2) {
        return new TwoElementMap<>(k1, v1, k2, v2);
    }

    static <K, V> ImmutableMapImpl<K, V> of(Map<K, V> map) {
        int size = map.size();

        if (map instanceof ImmutableMapImpl) {
            return (ImmutableMapImpl<K, V>) map;
        } else if (size == 0) {
            return empty();
        } else if (size == 1) {
            Map.Entry<K, V> entry = map.entrySet().iterator().next();
            return new SingleElementMap<>(entry.getKey(), entry.getValue());
        } else if (size == 2) {
            Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
            Map.Entry<K, V> entry1 = iter.next();
            Map.Entry<K, V> entry2 = iter.next();
            return new TwoElementMap<>(entry1.getKey(), entry1.getValue(), entry2.getKey(), entry2.getValue());
        } else {
            int tableSize = Hashing.hashTableSize(size);

            if (tableSize != -1) {
                return new HashArrayBackedMap.Builder<K, V>(tableSize).with(map).build();
            } else {
                return new MapBackedMap<K, V>(new HashMap<K, V>(map));
            }
        }
    }

    abstract int getEstimatedByteSize();

    static class SingleElementMap<K, V> extends ImmutableMapImpl<K, V> {
        private final K key;
        private final V value;
        private Set<K> keySet;
        private Collection<V> values;
        private Set<Entry<K, V>> entrySet;

        SingleElementMap(K key, V value) {
            this.key = key;
            this.value = value;
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
        public boolean containsValue(Object value) {
            return Objects.equals(this.value, value);
        }

        @Override
        public boolean containsKey(Object key) {
            return Objects.equals(this.key, key);
        }

        @Override
        public V get(Object key) {
            if (Objects.equals(this.key, key)) {
                return this.value;
            } else {
                return null;
            }
        }

        @Override
        public Set<K> keySet() {
            if (keySet == null) {
                keySet = IndexedImmutableSetImpl.of(this.key);
            }

            return keySet;
        }

        @Override
        public Collection<V> values() {
            if (values == null) {
                values = IndexedImmutableSetImpl.of(this.value);
            }

            return values;
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            if (entrySet == null) {
                entrySet = IndexedImmutableSetImpl.of(new AbstractMap.SimpleEntry<>(key, value));
            }
            return entrySet;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map)) {
                return false;
            }

            Map<?, ?> otherMap = (Map<?, ?>) o;

            if (otherMap.size() != 1) {
                return false;
            }

            Entry<?, ?> entry = otherMap.entrySet().iterator().next();

            return Objects.equals(key, entry.getKey()) && Objects.equals(value, entry.getValue());
        }

        @Override
        int getEstimatedByteSize() {
            return 48;
        }
    }

    static class TwoElementMap<K, V> extends ImmutableMapImpl<K, V> {
        private final K key1;
        private final V value1;
        private final K key2;
        private final V value2;
        private Set<K> keySet;
        private List<V> values;
        private Set<Entry<K, V>> entrySet;

        TwoElementMap(K key1, V value1, K key2, V value2) {
            this.key1 = key1;
            this.value1 = value1;
            this.key2 = key2;
            this.value2 = value2;
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
        public boolean containsValue(Object value) {
            return Objects.equals(this.value1, value) || Objects.equals(this.value2, value);
        }

        @Override
        public boolean containsKey(Object key) {
            return Objects.equals(this.key1, key) || Objects.equals(this.key2, key);
        }

        @Override
        public V get(Object key) {
            if (Objects.equals(this.key1, key)) {
                return this.value1;
            } else if (Objects.equals(this.key2, key)) {
                return this.value2;
            } else {
                return null;
            }
        }

        @Override
        public Set<K> keySet() {
            if (keySet == null) {
                keySet = IndexedImmutableSetImpl.of(this.key1, this.key2);
            }

            return keySet;
        }

        @Override
        public Collection<V> values() {
            if (values == null) {
                values = Collections.unmodifiableList(Arrays.asList(this.value1, this.value2));
            }

            return values;
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            if (entrySet == null) {
                entrySet = IndexedImmutableSetImpl.of(
                        new AbstractMap.SimpleEntry<>(key1, value1), new AbstractMap.SimpleEntry<>(key2, value2));
            }
            return entrySet;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map)) {
                return false;
            }

            Map<?, ?> otherMap = (Map<?, ?>) o;

            if (otherMap.size() != 2) {
                return false;
            }

            return Objects.equals(value1, otherMap.get(key1)) && Objects.equals(value2, otherMap.get(key2));
        }

        @Override
        int getEstimatedByteSize() {
            return 64;
        }
    }

    static class HashArrayBackedMap<K, V> extends ImmutableMapImpl<K, V> {

        final int tableSize;
        final int size;
        final short maxProbingDistance;

        private final K[] keyTable;
        private final V[] valueTable;

        private List<V> valuesCollection;

        HashArrayBackedMap(int tableSize, int size, short maxProbingDistance, K[] keyTable, V[] valueTable) {
            this.tableSize = tableSize;
            this.size = size;
            this.maxProbingDistance = maxProbingDistance;
            this.keyTable = keyTable;
            this.valueTable = valueTable;
            assert size != 0;
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
        public boolean containsKey(Object key) {
            return containsKey(key, hashPosition(key));
        }

        boolean containsKey(Object key, int position) {
            return checkTable(key, position) < 0;
        }

        @Override
        public V get(Object key) {
            int check = checkTable(key, hashPosition(key));

            if (check < 0) {
                int actualPosition = -check - 1;
                return this.valueTable[actualPosition];
            } else {
                return null;
            }
        }

        @Override
        public Set<K> keySet() {
            return new UnmodifiableSetImpl<K>() {
                @Override
                public int size() {
                    return HashArrayBackedMap.this.size;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public boolean contains(Object o) {
                    return HashArrayBackedMap.this.containsKey(o);
                }

                @Override
                public Iterator<K> iterator() {
                    return new Iterator<K>() {
                        int current = findNextKey(0);

                        @Override
                        public boolean hasNext() {
                            return current != -1;
                        }

                        @Override
                        public K next() {
                            if (current == -1) {
                                throw new NoSuchElementException();
                            }

                            K key = HashArrayBackedMap.this.keyTable[current];
                            current = findNextKey(current + 1);
                            return key;
                        }
                    };
                }
            };
        }

        @Override
        public List<V> values() {
            List<V> result = this.valuesCollection;

            if (result == null) {
                ArrayList<V> list = new ArrayList<>(this.size);

                for (int i = 0; i < this.keyTable.length; i++) {
                    if (this.keyTable[i] != null) {
                        list.add(this.valueTable[i]);
                    }
                }

                this.valuesCollection = result = Collections.unmodifiableList(list);
            }

            return result;
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return new UnmodifiableSetImpl<Entry<K, V>>() {
                @Override
                public int size() {
                    return HashArrayBackedMap.this.size;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public boolean contains(Object o) {
                    if (o instanceof Entry) {
                        Entry<?, ?> entry = (Entry<?, ?>) o;
                        V presentValue = HashArrayBackedMap.this.get(entry.getKey());
                        if (presentValue != null && presentValue.equals(entry.getValue())) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                @Override
                public Iterator<Entry<K, V>> iterator() {
                    return new Iterator<Entry<K, V>>() {
                        int current = findNextKey(0);

                        @Override
                        public boolean hasNext() {
                            return current != -1;
                        }

                        @Override
                        public Entry<K, V> next() {
                            if (current == -1) {
                                throw new NoSuchElementException();
                            }

                            K key = HashArrayBackedMap.this.keyTable[current];
                            V value = HashArrayBackedMap.this.valueTable[current];

                            current = findNextKey(current + 1);

                            return new AbstractMap.SimpleEntry<K, V>(key, value);
                        }
                    };
                }
            };
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            for (int i = 0; i < keyTable.length; i++) {
                K key = keyTable[i];

                if (key != null) {
                    action.accept(key, valueTable[i]);
                }
            }
        }

        @Override
        int getEstimatedByteSize() {
            return 32 + this.keyTable.length * 8 * 2;
        }

        static int getEstimatedByteSize(int size) {
            return 32 + size * 8 * 2 * 2;
        }

        private int hashPosition(Object key) {
            return Hashing.hashPosition(this.tableSize, key);
        }

        private int checkTable(Object key, int hashPosition) {
            return Hashing.checkTable(this.keyTable, key, hashPosition, this.maxProbingDistance);
        }

        private int findNextKey(int start) {
            for (int i = start; i < this.keyTable.length; i++) {
                if (this.keyTable[i] != null) {
                    return i;
                }
            }

            return -1;
        }

        static class Builder<K, V> extends InternalBuilder<K, V> {
            private K[] keyTable;
            private V[] valueTable;

            private int size = 0;
            private final int tableSize;
            private int probingOverhead;
            private short probingOverheadFactor = 3;
            private final short maxProbingDistance;
            private boolean valid = true;

            Builder(int tableSize) {
                this.tableSize = tableSize;
                this.maxProbingDistance = Hashing.maxProbingDistance(tableSize);
            }

            InternalBuilder<K, V> with(K key, V value) {
                if (key == null) {
                    throw new IllegalArgumentException("Null keys are not supported");
                }

                return with(key, value, hashPosition(key));
            }

            private InternalBuilder<K, V> with(K key, V value, int pos) {
                if (!valid) {
                    throw new IllegalStateException("Builder instance is not active any more");
                }

                if (keyTable == null) {
                    keyTable = GenericArrays.create(tableSize + maxProbingDistance);
                    valueTable = GenericArrays.create(tableSize + maxProbingDistance);

                    keyTable[pos] = key;
                    valueTable[pos] = value;
                    size++;
                    return this;
                } else {
                    if (keyTable[pos] == null) {
                        keyTable[pos] = key;
                        valueTable[pos] = value;
                        size++;
                        return this;
                    } else if (keyTable[pos].equals(key)) {
                        // already contained
                        valueTable[pos] = value;
                        return this;
                    } else {
                        // collision

                        int check = Hashing.checkTable(keyTable, key, pos, maxProbingDistance);

                        if (check < 0) {
                            // contained
                            int actualPos = -check - 1;
                            valueTable[actualPos] = value;
                            return this;
                        } else if (check == Hashing.NO_SPACE) {
                            try {
                                int newTableSize = Hashing.nextSize(tableSize);
                                if (newTableSize != -1) {
                                    return new Builder<K, V>(newTableSize)
                                            .probingOverheadFactor(probingOverheadFactor)
                                            .withNonNull(keyTable, valueTable)
                                            .with(key, value);
                                } else {
                                    return new ImmutableMapImpl.MapBackedMap.Builder<K, V>(this.size)
                                            .withNonNull(keyTable, valueTable)
                                            .with(key, value);
                                }
                            } finally {
                                this.valid = false;
                            }
                        } else {
                            keyTable[check] = key;
                            valueTable[check] = value;
                            size++;

                            this.probingOverhead += check - pos;

                            if (this.size >= 12 && this.probingOverhead > this.size * this.probingOverheadFactor) {
                                // probing overhead exceeds threshold
                                try {
                                    int newTableSize = Hashing.nextSize(tableSize);
                                    if (newTableSize != -1) {
                                        return new ImmutableMapImpl.HashArrayBackedMap.Builder<K, V>(newTableSize)
                                                .probingOverheadFactor(this.probingOverheadFactor)
                                                .withNonNull(keyTable, valueTable);
                                    } else {
                                        return new ImmutableMapImpl.MapBackedMap.Builder<K, V>(this.size)
                                                .withNonNull(keyTable, valueTable);
                                    }
                                } finally {
                                    this.valid = false;
                                }
                            }

                            return this;
                        }
                    }
                }
            }

            InternalBuilder<K, V> with(Map<K, V> map) {
                InternalBuilder<K, V> builder = this;

                for (Map.Entry<K, V> entry : map.entrySet()) {
                    builder = builder.with(entry.getKey(), entry.getValue());
                }

                return builder;
            }

            @Override
            int size() {
                return size;
            }

            @Override
            boolean containsKey(Object key) {
                return get(key) != null;
            }

            @Override
            V get(Object key) {
                if (keyTable == null) {
                    return null;
                }

                int check = Hashing.checkTable(keyTable, key, hashPosition(key), maxProbingDistance);

                if (check < 0) {
                    int actualPos = -check - 1;
                    return valueTable[actualPos];
                }

                return null;
            }

            @Override
            int getEstimatedByteSize() {
                if (this.keyTable != null) {
                    return 32 + this.keyTable.length * 8 * 2;
                } else {
                    return 32;
                }
            }

            @Override
            Set<K> keySet() {
                return new UnmodifiableSetImpl<K>() {
                    @Override
                    public int size() {
                        return HashArrayBackedMap.Builder.this.size;
                    }

                    @Override
                    public boolean contains(Object o) {
                        return HashArrayBackedMap.Builder.this.containsKey(o);
                    }

                    @Override
                    public Iterator<K> iterator() {
                        return new Iterator<K>() {
                            int current = findNextKey(0);

                            @Override
                            public boolean hasNext() {
                                return current != -1;
                            }

                            @Override
                            public K next() {
                                if (current == -1) {
                                    throw new NoSuchElementException();
                                }

                                K key = HashArrayBackedMap.Builder.this.keyTable[current];
                                current = findNextKey(current + 1);
                                return key;
                            }
                        };
                    }
                };
            }

            @Override
            ImmutableMapImpl<K, V> build() {
                if (size == 0) {
                    return ImmutableMapImpl.empty();
                } else if (size == 1) {
                    int i = GenericArrays.indexOfNextNonNull(this.keyTable, 0);
                    return new SingleElementMap<>(this.keyTable[i], this.valueTable[i]);
                } else if (size == 2) {
                    int i1 = GenericArrays.indexOfNextNonNull(this.keyTable, 0);
                    K key1 = this.keyTable[i1];
                    V value1 = this.valueTable[i1];
                    int i2 = GenericArrays.indexOfNextNonNull(this.keyTable, i1 + 1);
                    K key2 = this.keyTable[i2];
                    V value2 = this.valueTable[i2];

                    return new TwoElementMap<>(key1, value1, key2, value2);
                } else {
                    this.valid = false;
                    return new HashArrayBackedMap<>(tableSize, size, maxProbingDistance, keyTable, valueTable);
                }
            }

            @Override
            <V2> ImmutableMapImpl<K, V2> build(Function<V, V2> valueMappingFunction) {
                if (size == 0) {
                    return ImmutableMapImpl.empty();
                } else if (size == 1) {
                    int i = GenericArrays.indexOfNextNonNull(this.keyTable, 0);
                    return new SingleElementMap<>(this.keyTable[i], valueMappingFunction.apply(this.valueTable[i]));
                } else if (size == 2) {
                    int i1 = GenericArrays.indexOfNextNonNull(this.keyTable, 0);
                    K key1 = this.keyTable[i1];
                    V2 value1 = valueMappingFunction.apply(this.valueTable[i1]);
                    int i2 = GenericArrays.indexOfNextNonNull(this.keyTable, i1 + 1);
                    K key2 = this.keyTable[i2];
                    V2 value2 = valueMappingFunction.apply(this.valueTable[i2]);

                    return new TwoElementMap<>(key1, value1, key2, value2);
                } else {
                    this.valid = false;
                    return new HashArrayBackedMap<>(
                            tableSize,
                            size,
                            maxProbingDistance,
                            keyTable,
                            GenericArrays.mapInPlace(valueTable, valueMappingFunction));
                }
            }

            private int hashPosition(Object e) {
                return Hashing.hashPosition(tableSize, e);
            }

            private int findNextKey(int start) {
                if (this.keyTable == null) {
                    return -1;
                }

                for (int i = start; i < this.keyTable.length; i++) {
                    if (this.keyTable[i] != null) {
                        return i;
                    }
                }

                return -1;
            }
        }
    }

    static class MapBackedMap<K, V> extends ImmutableMapImpl<K, V> {
        private final Map<K, V> delegate;

        MapBackedMap(Map<K, V> delegate) {
            this.delegate = delegate;
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
        public boolean containsValue(Object value) {
            return delegate.containsValue(value);
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public V get(Object key) {
            return delegate.get(key);
        }

        @Override
        public Set<K> keySet() {
            return Collections.unmodifiableSet(delegate.keySet());
        }

        @Override
        public Collection<V> values() {
            return Collections.unmodifiableCollection(delegate.values());
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return Collections.unmodifiableSet(delegate.entrySet());
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            delegate.forEach(action);
        }

        @Override
        int getEstimatedByteSize() {
            return (int) (40 + delegate.size() * 52 * 1.3);
        }

        static class Builder<K, V> extends InternalBuilder<K, V> {
            private final HashMap<K, V> delegate;

            Builder(int expectedCapacity) {
                this.delegate = new HashMap<>(expectedCapacity);
            }

            Builder<K, V> with(K key, V value) {
                this.delegate.put(key, value);
                return this;
            }

            @Override
            int size() {
                return delegate.size();
            }

            @Override
            boolean containsKey(Object e) {
                return delegate.containsKey(e);
            }

            @Override
            V get(K k) {
                return delegate.get(k);
            }

            @Override
            int getEstimatedByteSize() {
                return (int) (40 + delegate.size() * 52 * 1.3);
            }

            @Override
            Set<K> keySet() {
                return delegate.keySet();
            }

            @Override
            ImmutableMapImpl<K, V> build() {
                return new MapBackedMap<>(this.delegate);
            }

            @Override
            <V2> ImmutableMapImpl<K, V2> build(Function<V, V2> valueMappingFunction) {
                HashMap<K, V2> result = new HashMap<>(delegate.size());

                for (Map.Entry<K, V> entry : this.delegate.entrySet()) {
                    result.put(entry.getKey(), valueMappingFunction.apply(entry.getValue()));
                }
                return new MapBackedMap<>(result);
            }
        }
    }

    private static final ImmutableMapImpl<?, ?> EMPTY = new ImmutableMapImpl<Object, Object>() {
        @Override
        public Set<Entry<Object, Object>> entrySet() {
            return IndexedImmutableSetImpl.empty();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public Object get(Object key) {
            return null;
        }

        @Override
        public Set<Object> keySet() {
            return IndexedImmutableSetImpl.empty();
        }

        @Override
        public Collection<Object> values() {
            return IndexedImmutableSetImpl.empty();
        }

        @Override
        public String toString() {
            return "[]";
        }

        @Override
        int getEstimatedByteSize() {
            return 0;
        }
    };

    abstract static class InternalBuilder<K, V> {
        abstract InternalBuilder<K, V> with(K key, V value);

        InternalBuilder<K, V> withNonNull(K[] keyTable, V[] valueTable) {
            InternalBuilder<K, V> builder = this;

            for (int i = 0; i < keyTable.length; i++) {
                K key = keyTable[i];

                if (key != null) {
                    builder = builder.with(key, valueTable[i]);
                }
            }

            return builder;
        }

        abstract boolean containsKey(Object key);

        abstract V get(K key);

        abstract ImmutableMapImpl<K, V> build();

        abstract <V2> ImmutableMapImpl<K, V2> build(Function<V, V2> valueMappingFunction);

        abstract int size();

        abstract int getEstimatedByteSize();

        abstract Set<K> keySet();

        InternalBuilder<K, V> probingOverheadFactor(short probingOverheadFactor) {
            return this;
        }

        static <K, V> InternalBuilder<K, V> create(int size) {
            int tableSize = Hashing.hashTableSize(size);

            if (tableSize != -1) {
                return new HashArrayBackedMap.Builder<>(tableSize);
            } else {
                return new MapBackedMap.Builder<>(size);
            }
        }
    }
}
