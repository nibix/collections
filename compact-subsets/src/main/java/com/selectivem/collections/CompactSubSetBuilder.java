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

public class CompactSubSetBuilder<E> {
    private final IndexedImmutableSetImpl<E> elementToIndexMap;
    private final int bitArraySize;

    public CompactSubSetBuilder(Set<E> superSet) {
        this.elementToIndexMap = IndexedImmutableSetImpl.of(superSet);
        this.bitArraySize = BitBackedSetImpl.bitArraySize(elementToIndexMap.size());
    }

    public DeduplicatingCompactSubSetBuilder<E> deduplicatingBuilder() {
        return new DeduplicatingCompactSubSetBuilder<>(this.elementToIndexMap);
    }

    public ImmutableCompactSubSet<E> of(Set<E> set) {
            long[] bits = new long[bitArraySize];
            int size = 0;

            for (E e : set) {
                int index = elementToIndexMap.elementToIndex(e);

                if (BitBackedSetImpl.setBit(bits, index, 0)) {
                    size++;
                }
            }

            if (size == 0) {
                return ImmutableCompactSubSetImpl.empty();
            }

            int firstNonZero = BitBackedSetImpl.firstNonZeroIndex(bits);
            int lastNonZero = BitBackedSetImpl.lastNonZeroIndex(bits);

            if (firstNonZero == lastNonZero) {
                return new BitBackedSetImpl.LongBacked<>(bits[firstNonZero], size, elementToIndexMap, firstNonZero);
            } else {
                if (firstNonZero != 0 || lastNonZero != bits.length - 1) {
                    long [] oldBits = bits;
                    bits = new long [lastNonZero - firstNonZero + 1];
                    System.arraycopy(oldBits, firstNonZero, bits, 0, bits.length);
                }

                return new BitBackedSetImpl.LongArrayBacked<>(bits, size, elementToIndexMap, firstNonZero);
            }
    }
}
