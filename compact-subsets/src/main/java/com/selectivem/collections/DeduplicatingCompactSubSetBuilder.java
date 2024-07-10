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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeduplicatingCompactSubSetBuilder<E> {
    private final IndexedImmutableSetImpl<E> candidateElements;
    private final int bitArraySize;
    private E currentElement;
    private BackingBitSetBuilder<E> backingCollectionWithCurrentElementOnly = null;
    private List<SetBuilder<E>> setBuilders = new ArrayList<>();
    private List<BackingBitSetBuilder<E>> backingBitSets = new ArrayList<>();
    private int estimatedBackingArraySize = 0;
    private int estimatedObjectOverheadSize = 0;

    public DeduplicatingCompactSubSetBuilder(Set<E> candidateElements) {
        this.candidateElements = IndexedImmutableSetImpl.of(candidateElements);
        this.bitArraySize = BitBackedSetImpl.bitArraySize(candidateElements.size());
    }

    public DeduplicatingCompactSubSetBuilder.SetBuilder<E> createSetBuilder() {
        SetBuilder<E> result = new SetBuilder<>(this);
        this.setBuilders.add(result);
        return result;
    }

    public void finished(E candidateElement) {
        if (this.currentElement != null) {
            if (!candidateElement.equals(this.currentElement)) {
                throw new IllegalStateException("Element was not finished: " + this.currentElement);
            }
        }

        for (SetBuilder<E> setBuilder : this.setBuilders) {
            setBuilder.finish(candidateElement);
        }

        for (BackingBitSetBuilder<E> backingBitSetBuilder : this.backingBitSets) {
            backingBitSetBuilder.finish(candidateElement);
        }

        this.currentElement = null;
        this.backingCollectionWithCurrentElementOnly = null;
    }

    public Completed<E> completelyFinished() {
        return new Completed<>(this);
    }

    public int getEstimatedByteSize() {
        return this.estimatedBackingArraySize + this.estimatedObjectOverheadSize;
    }

    void setCurrentElement(E currentElement) {
        if (this.currentElement != null) {
            if (!currentElement.equals(this.currentElement)) {
                throw new IllegalStateException("Element was not finished: " + this.currentElement);
            }
        } else {
            this.currentElement = currentElement;
        }
    }

    public static class SetBuilder<E> {

        private final DeduplicatingCompactSubSetBuilder<E> root;
        private int size = 0;
        private BackingBitSetBuilder<E> backingCollection;
        private E lastAddedElement;

        SetBuilder(DeduplicatingCompactSubSetBuilder<E> root) {
            this.root = root;
        }

        public void add(E element) {
            if (root.candidateElements.elementToIndex(element) == -1) {
                throw new IllegalArgumentException("Element " + element + " is not part of super set " + root.candidateElements);
            }

            this.root.setCurrentElement(element);

            if (size == 0) {
                if (root.backingCollectionWithCurrentElementOnly != null) {
                    this.backingCollection = root.backingCollectionWithCurrentElementOnly;
                } else {
                    this.backingCollection =
                            root.backingCollectionWithCurrentElementOnly = new BackingBitSetBuilder<>(element, root);
                }

                this.size = 1;
            } else {
                backingCollection.offeredElement = element;
                this.size++;
            }
            this.lastAddedElement = element;
        }

        public ImmutableCompactSubSet<E> build(DeduplicatingCompactSubSetBuilder.Completed<E> completed) {
            if (completed.root != this.root) {
                throw new IllegalArgumentException("Called for the wrong DeduplicatingCompactSubSetBuilder");
            }

            if (backingCollection == null) {
                return ImmutableCompactSubSetImpl.empty();
            } else {
                return backingCollection.build();
            }
        }

        void finish(E element) {
            if (this.backingCollection == null) {
                return;
            }

            if (!element.equals(this.lastAddedElement)) {
                // Nothing was added during the last round
                if (backingCollection.offeredElement != null) {
                    // However, something was added to the backingCollection. Thus, we need to branch off
                    this.backingCollection = backingCollection.branchOff();
                }
            }
        }
    }

    public static class Completed<E> {
        private final DeduplicatingCompactSubSetBuilder<E> root;

        Completed(DeduplicatingCompactSubSetBuilder<E> root) {
            this.root = root;
        }
    }

    static final class BackingBitSetBuilder<E> {

        private E offeredElement;
        private long[] bits;
        private int size;
        private final IndexedImmutableSetImpl<E> elementToIndexMap;
        private final int bitArrayOffset;
        private BackingBitSetBuilder<E> copyWithNone;
        private final DeduplicatingCompactSubSetBuilder<E> root;
        private ImmutableCompactSubSet<E> finalBuildResult;
        private E firstElement;

        BackingBitSetBuilder(E element, DeduplicatingCompactSubSetBuilder<E> root) {
            this.elementToIndexMap = root.candidateElements;
            this.firstElement = element;
            this.size = 1;
            int index = elementToIndexMap.elementToIndex(element);
            long bit = 1l << (index & 0x3f);
            int arrayIndex = index >> 6;
            this.bitArrayOffset = arrayIndex;
            this.bits = new long[root.bitArraySize - this.bitArrayOffset];
            this.bits[0] = bit;
            this.root = root;
            root.backingBitSets.add(this);
            root.estimatedBackingArraySize += this.bits.length * 8;
            root.estimatedObjectOverheadSize += 36;
        }

        BackingBitSetBuilder(BackingBitSetBuilder<E> original) {
            this.elementToIndexMap = original.elementToIndexMap;
            this.firstElement = original.firstElement;
            this.bits = new long[original.bits.length];
            this.size = original.size;
            this.bitArrayOffset = original.bitArrayOffset;
            System.arraycopy(original.bits, 0, this.bits, 0, original.bits.length);
            this.root = original.root;
            root.backingBitSets.add(this);
            root.estimatedBackingArraySize += this.bits.length * 8;
            root.estimatedObjectOverheadSize += 36;
        }

        BackingBitSetBuilder<E> branchOff() {
            if (this.copyWithNone == null) {
                this.copyWithNone = this.copy();
                return this.copyWithNone;
            } else {
                return this.copyWithNone;
            }
        }

        void finish(E element) {
            this.addOfferedElement();
            this.copyWithNone = null;
        }

        void addOfferedElement() {
            if (offeredElement != null) {
                add(offeredElement);
                offeredElement = null;
            }
        }

        void add(E element) {
            int index = elementToIndexMap.elementToIndex(element);

            if (BitBackedSetImpl.setBit(this.bits, index, this.bitArrayOffset)) {
                this.size++;

                if (this.firstElement == null) {
                    this.firstElement = element;
                }
            }
        }

        BackingBitSetBuilder<E> copy() {
            return new BackingBitSetBuilder<>(this);
        }

        ImmutableCompactSubSet<E> build() {
            if (this.finalBuildResult != null) {
                return this.finalBuildResult;
            }

            if (this.size == this.elementToIndexMap.size()) {
                this.finalBuildResult = ImmutableCompactSubSetImpl.of(this.elementToIndexMap);
            } else if (this.size == 1) {
                this.finalBuildResult = ImmutableCompactSubSetImpl.of(IndexedImmutableSetImpl.of(this.firstElement));
            } else {
                long[] bits = this.bits;
                int lastNonZeroIndex = BitBackedSetImpl.lastNonZeroIndex(bits);

                if (lastNonZeroIndex <= 0) {
                    this.finalBuildResult =
                            new BitBackedSetImpl.LongBacked<>(bits[0], size, elementToIndexMap, bitArrayOffset);
                } else {
                    if (lastNonZeroIndex != bits.length - 1) {
                        long[] shortenedBits = new long[lastNonZeroIndex + 1];
                        System.arraycopy(bits, 0, shortenedBits, 0, shortenedBits.length);
                        bits = shortenedBits;
                    }

                    this.finalBuildResult =
                            new BitBackedSetImpl.LongArrayBacked<>(bits, size, elementToIndexMap, bitArrayOffset);
                }
            }

            return this.finalBuildResult;
        }


    }
}
