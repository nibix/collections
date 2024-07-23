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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    };
}
