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
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface UnmodifiableList<E> extends UnmodifiableCollection<E>, List<E> {
    @Override
    @Deprecated
    default boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    boolean addAll(int index, Collection<? extends E> c);

    @Deprecated
    @Override
    default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default void sort(Comparator<? super E> c) {

        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default void clear() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default void addFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default void addLast(E e) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default E removeFirst() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default E removeLast() {
        throw new UnsupportedOperationException();
    }
}
