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

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Allows the creation of space efficient maps where the keys are a sub-set of the super-set specified in the constructor.
 *
 * This class copies the super-set. All maps produced by this class will be immutable.
 *
 * This class puts the priority on random access via the get() method and compactness. Map entry iteration may be slower
 * than it is for other map implementations.
 *
 * @author Nils Bandener
 */
public class CompactMapGroupBuilder<K, V> {
    private final IndexedImmutableSetImpl<K> keyToIndexMap;
    private int estimatedBackingArraySize = 0;
    private int estimatedObjectOverheadSize = 0;

    public CompactMapGroupBuilder(Set<K> keySuperSet) {
        this.keyToIndexMap = IndexedImmutableSetImpl.of(keySuperSet);
    }

    /**
     * Creates a compact copy of the given map.
     */
    public Map<K, V> of(Map<K, V> original) {
        MapBuilder<K, V> builder = createMapBuilder();
        original.forEach((k, v) -> builder.put(k, v));
        return builder.build();
    }

    /**
     * Returns a MapBuilder instance which can be used to build compact map instances.
     */
    public MapBuilder<K, V> createMapBuilder() {
        return new MapBuilder<>(this, null);
    }

    /**
     * Returns a MapBuilder instance which can be used to build compact map instances.
     *
     * @param missingValueSupplier In case MapBuilder.get() is called for a non-existing entry,
     *                             this supplier is used to create a default value. This value is then stored in the map.
     */
    public MapBuilder<K, V> createMapBuilder(Function<K, V> missingValueSupplier) {
        return new MapBuilder<>(this, missingValueSupplier);
    }

    /**
     * Returns the estimated number of bytes which are used for representing the maps which
     * are built by this instance.
     */
    public int getEstimatedByteSize() {
        return this.estimatedBackingArraySize + this.estimatedObjectOverheadSize;
    }

    public static class MapBuilder<K, V> {
        private final CompactMapGroupBuilder<K, V> root;
        private final V[] values;
        private final Function<K, V> missingValueSupplier;
        private int size = 0;
        private int minIndex = Integer.MAX_VALUE;
        private int maxIndex = Integer.MIN_VALUE;

        MapBuilder(CompactMapGroupBuilder<K, V> root, Function<K, V> missingValueSupplier) {
            this.root = root;
            this.values = GenericArrays.create(root.keyToIndexMap.size());
            this.missingValueSupplier = missingValueSupplier;
            root.estimatedObjectOverheadSize += 36;
        }

        public void put(K key, V value) {
            int i = this.root.keyToIndexMap.elementToIndex(key);

            if (i == -1) {
                throw new IllegalArgumentException("Invalid key " + key + "; not present in keySuperSet");
            }

            if (value == null) {
                throw new IllegalArgumentException("Does not support null values; key: " + key);
            }

            if (this.values[i] != null) {
                this.values[i] = value;
            } else {
                this.putNonExistent(i, value);
            }
        }

        public V get(K key) {
            int i = this.root.keyToIndexMap.elementToIndex(key);

            if (i == -1) {
                throw new IllegalArgumentException("Invalid key " + key + "; not present in keySuperSet");
            }

            V value = this.values[i];

            if (value == null && this.missingValueSupplier != null) {
                value = this.missingValueSupplier.apply(key);
                this.putNonExistent(i, value);
            }

            return value;
        }

        public int size() {
            return this.size;
        }

        public Map<K, V> build() {
            if (size == 0) {
                return ImmutableMapImpl.empty();
            } else if (size == 1) {
                int i = findNext(0);
                V value = this.values[i];
                K key = this.root.keyToIndexMap.indexToElement(i);
                return ImmutableMapImpl.of(key, value);
            } else if (size == 2) {
                int i = findNext(0);
                V v1 = this.values[i];
                K k1 = this.root.keyToIndexMap.indexToElement(i);
                i = findNext(i + 1);
                V v2 = this.values[i];
                K k2 = this.root.keyToIndexMap.indexToElement(i);
                return ImmutableMapImpl.of(k1, v1, k2, v2);
            } else {
                V[] compactedValues;
                int valuesArrayOffset;

                if (minIndex > 4 || maxIndex < values.length - 5) {
                    compactedValues = GenericArrays.create(maxIndex - minIndex + 1);
                    System.arraycopy(this.values, minIndex, compactedValues, 0, maxIndex - minIndex + 1);
                    valuesArrayOffset = minIndex;
                } else {
                    compactedValues = values;
                    valuesArrayOffset = 0;
                }

                return new IndexRefMapImpl<>(compactedValues, size, root.keyToIndexMap, valuesArrayOffset);
            }
        }

        private void putNonExistent(int keyIndex, V value) {
            this.values[keyIndex] = value;

            if (this.minIndex > keyIndex) {
                this.minIndex = keyIndex;
            }

            if (this.maxIndex < keyIndex) {
                this.maxIndex = keyIndex;
            }

            this.size++;

            if (this.size <= 2) {
                this.root.estimatedBackingArraySize += 8;
            } else if (this.size == 3) {
                this.root.estimatedBackingArraySize += (this.values.length - 2) * 8;
            }
        }

        private int findNext(int start) {
            for (int i = start; i < this.values.length; i++) {
                if (this.values[i] != null) {
                    return i;
                }
            }

            return -1;
        }
    }
}
