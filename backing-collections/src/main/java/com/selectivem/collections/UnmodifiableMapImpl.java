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
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

abstract class UnmodifiableMapImpl<K, V> extends AbstractMap<K, V> implements UnmodifiableMap<K, V> {
    @Override
    @Deprecated
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public V replace(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }
}
