package com.selectivem.collections;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface UnmodifiableMap<K, V> extends Map<K, V> {
    @Override
    @Deprecated
    default V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default V replace(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }
}
