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

/**
 * Allows the creation of compact sub-sets of a given super-set. It also provides space and compute efficient
 * deduplication of sub-set instances. Creating several sub-set builders which end up to have the same elements,
 * will result in the same ImmutableCompactSubSet instances to be produced.
 *
 * In order to provide this functionality efficiently, users of this class need to follow a certain protocol:
 *
 * - Iterate through the super-set in its natural iteration order.
 * - Call DeduplicatingCompactSubSetBuilder.SubSetBuilder.add() for the current element on any
 *   DeduplicatingCompactSubSetBuilder.SubSetBuilder instance you want.
 * - Call finished() on the current element.
 * - Only then advance to the next element.
 * - After all sets have been added, call build(). This will return a DeduplicatingCompactSubSetBuilder.Completed
 *   instance which allows you to call the build() methods on the SubSetBuilder instances.
 *
 * @author Nils Bandener
 */
public class DeduplicatingCompactSubSetBuilder<E> {
    private final IndexedImmutableSetImpl<E> candidateElements;
    private final int bitArraySize;
    private E currentElement;
    private BackingBitSetBuilder<E> backingCollectionWithCurrentElementOnly = null;
    private List<SubSetBuilder<E>> subSetBuilders = new ArrayList<>();
    private List<BackingBitSetBuilder<E>> backingBitSets = new ArrayList<>();
    private int estimatedBackingArraySize = 0;
    private int estimatedObjectOverheadSize = 0;

    /**
     * Creates a new builder instance for the given superSet. The superSet will be copied.
     */
    public DeduplicatingCompactSubSetBuilder(Set<E> superSet) {
        this.candidateElements = IndexedImmutableSetImpl.of(superSet);
        this.bitArraySize = BitBackedSetImpl.bitArraySize(superSet.size());
    }

    /**
     * Creates a new SubSetBuilder instance. Can be called at any time before build() is called.
     */
    public SubSetBuilder<E> createSubSetBuilder() {
        SubSetBuilder<E> result = new SubSetBuilder<>(this);
        this.subSetBuilders.add(result);
        return result;
    }

    public void finished(E candidateElement) {
        if (this.currentElement != null) {
            if (!candidateElement.equals(this.currentElement)) {
                throw new IllegalStateException("Element was not finished: " + this.currentElement);
            }
        }

        for (SubSetBuilder<E> subSetBuilder : this.subSetBuilders) {
            subSetBuilder.finish(candidateElement);
        }

        for (BackingBitSetBuilder<E> backingBitSetBuilder : this.backingBitSets) {
            backingBitSetBuilder.finish(candidateElement);
        }

        this.currentElement = null;
        this.backingCollectionWithCurrentElementOnly = null;
    }

    /**
     * Finalizes this build process. This method will return a Completed instance which
     * can be used to finalize building individual sub-sets.
     */
    public Completed<E> build() {
        return new Completed<>(this);
    }

    /**
     * Returns the estimated byte size of all instances created by this instance. This number
     * might be higher than the finally created instances, because some compaction might also occur during
     * the final build stage.
     */
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

    public static class SubSetBuilder<E> {

        private final DeduplicatingCompactSubSetBuilder<E> root;
        private int size = 0;
        private BackingBitSetBuilder<E> backingCollection;
        private E lastAddedElement;

        SubSetBuilder(DeduplicatingCompactSubSetBuilder<E> root) {
            this.root = root;
        }

        /**
         * Adds an element to this builder instance.
         *
         * Note: You need to observe the protocol requirements imposed by DeduplicatingCompactSubSetBuilder:
         * - You can only call add() for elements that are member of the super-set given when the DeduplicatingCompactSubSetBuilder was created. Otherwise, a IllegalArgumentException will be thrown.
         * - You must stick to the iteration order for the super-set.
         */
        public void add(E element) {
            if (root.candidateElements.elementToIndex(element) == -1) {
                throw new IllegalArgumentException(
                        "Element " + element + " is not part of super set " + root.candidateElements);
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
