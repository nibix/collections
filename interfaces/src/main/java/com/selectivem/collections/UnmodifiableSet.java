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
import java.util.Set;
import java.util.function.Predicate;

/**
 * Represents a set that does not allow modification via its public methods.
 * <p>
 * Possibly, such a set is also immutable, i.e., guaranteed to never change.
 * However, it is also possible that an UnmodifiableSet changes "behind" the scenes.
 * This can be the case for views on other collections.
 */
public interface UnmodifiableSet<E> extends UnmodifiableCollection<E>, Set<E> {
    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    default boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    default boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    default boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    default boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    default void clear() {
        throw new UnsupportedOperationException();
    }
}
