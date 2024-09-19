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

import java.util.Set;

/**
 * An IndexImmutableSet is a hash set implementation which assigns ordinal numbers to its member
 * elements. It exposes the methods elementToIndex() and indexToElement() which provide fast O(1)
 * means to convert an element to its index and vice-versa.
 * <p>
 * The indices are dense, i.e., in the range from 0 to size-1.
 * <p>
 * IndexedImmutableSet instances cannot contain null elements.
 *
 * @author Nils Bandener
 */
public interface IndexedImmutableSet<E> extends UnmodifiableSet<E> {

    /**
     * Creates an IndexedImmutableSet with the given element.
     * The element will have the index 0.
     */
    static <E> IndexedImmutableSet<E> of(E e1) {
        return IndexedImmutableSetImpl.of(e1);
    }

    /**
     * Creates an IndexedImmutableSet with the given elements.
     * The element e1 will have the index 0, e2 will have the index 1.
     */
    static <E> IndexedImmutableSet<E> of(E e1, E e2) {
        return IndexedImmutableSetImpl.of(e1, e2);
    }

    /**
     * Returns an empty IndexedImmutableSet instance.
     */
    static <E> IndexedImmutableSet<E> empty() {
        return IndexedImmutableSetImpl.empty();
    }

    /**
     * Creates an IndexedImmutableSet instance containing the elements
     * from the given set. The elements will be indexed according to the iteration order
     * from the given set.
     */
    static <E> IndexedImmutableSet<E> of(Set<E> set) {
        return IndexedImmutableSetImpl.of(set);
    }

    /**
     * Returns the index of the given element. The index will be in the range 0..size-1.
     * If this set does not contain the element, -1 will be returned.
     * <p>
     * This is a fast operation with O(1) complexity.
     */
    int elementToIndex(E element);

    /**
     * Returns the element associated with the given index. Will return null if the index is out
     * of range.
     * <p>
     * This is a fast operation with O(1) complexity.
     */
    E indexToElement(int index);
}
