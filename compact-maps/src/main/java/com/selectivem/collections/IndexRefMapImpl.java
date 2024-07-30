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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A Map implementation which is just composed of a values array and an IndexedImmutableSetImpl map instance
 * which provides indices into this array.
 * @param <K>
 * @param <V>
 */
class IndexRefMapImpl<K, V> extends UnmodifiableMapImpl<K, V> {
    private final V[] values;
    private final int size;
    private final IndexedImmutableSetImpl<K> keyToIndexMap;
    private final int valuesArrayOffset;
    private String cachedToString;

    IndexRefMapImpl(V[] values, int size, IndexedImmutableSetImpl<K> keyToIndexMap, int valuesArrayOffset) {
        this.values = values;
        this.size = size;
        this.keyToIndexMap = keyToIndexMap;
        this.valuesArrayOffset = valuesArrayOffset;
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
    public boolean containsValue(Object value) {
        for (int i = 0; i < this.values.length; i++) {
            V present = this.values[i];

            if (present != null && present.equals(value)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public V get(Object key) {
        int i = this.keyToIndexMap.elementToIndex(key);

        if (i == -1) {
            return null;
        }

        i -= this.valuesArrayOffset;

        if (i < 0 || i >= this.values.length) {
            return null;
        }

        return this.values[i];
    }

    @Override
    public Set<K> keySet() {
        return new UnmodifiableSetImpl<K>() {
            @Override
            public int size() {
                return IndexRefMapImpl.this.size;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return IndexRefMapImpl.this.containsKey(o);
            }

            @Override
            public Iterator<K> iterator() {
                return new Iterator<K>() {
                    int current = findNext(0);

                    @Override
                    public boolean hasNext() {
                        return current != -1;
                    }

                    @Override
                    public K next() {
                        if (current == -1) {
                            throw new NoSuchElementException();
                        }

                        K key = IndexRefMapImpl.this.keyToIndexMap.indexToElement(current + valuesArrayOffset);

                        current = findNext(current + 1);

                        return key;
                    }
                };
            }
        };
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new UnmodifiableSetImpl<Entry<K, V>>() {
            @Override
            public int size() {
                return IndexRefMapImpl.this.size;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                if (o instanceof Entry) {
                    Entry<?, ?> entry = (Entry<?, ?>) o;
                    V presentValue = IndexRefMapImpl.this.get(entry.getKey());
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
                    int current = findNext(0);

                    @Override
                    public boolean hasNext() {
                        return current != -1;
                    }

                    @Override
                    public Entry<K, V> next() {
                        if (current == -1) {
                            throw new NoSuchElementException();
                        }

                        V value = IndexRefMapImpl.this.values[current];
                        K key = IndexRefMapImpl.this.keyToIndexMap.indexToElement(current + valuesArrayOffset);

                        current = findNext(current + 1);

                        return new Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return key;
                            }

                            @Override
                            public V getValue() {
                                return value;
                            }

                            @Override
                            public V setValue(V value) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                };
            }
        };
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        for (int i = 0; i < this.values.length; i++) {
            V value = this.values[i];
            if (value == null) {
                continue;
            }

            K key = this.keyToIndexMap.indexToElement(i + valuesArrayOffset);

            action.accept(key, value);
        }
    }

    @Override
    public String toString() {
        String result = this.cachedToString;

        if (result == null) {
            StringBuilder stringBuilder = new StringBuilder("[");
            for (int i = 0; i < this.values.length; i++) {
                V value = this.values[i];
                if (value == null) {
                    continue;
                }

                if (stringBuilder.length() > 1) {
                    stringBuilder.append(", ");
                }

                K key = this.keyToIndexMap.indexToElement(i + valuesArrayOffset);

                stringBuilder.append(key).append("=").append(value);
            }
            stringBuilder.append("]");
            result = stringBuilder.toString();
            this.cachedToString = result;
        }

        return result;
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
