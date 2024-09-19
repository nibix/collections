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
 * <p>
 * In order to provide this functionality efficiently, users of this class need to follow a certain protocol:
 * <ul>
 * <li>Iterate through the super-set in its natural iteration order.
 * <li>Call next() for the current element.
 * <li>Call DeduplicatingCompactSubSetBuilder.SubSetBuilder.add() for the current element on any
 *   DeduplicatingCompactSubSetBuilder.SubSetBuilder instance you want.
 * <li>Only then advance to the next element, also call next() for that one.
 * <li>After all sets have been added, call build(). This will return a DeduplicatingCompactSubSetBuilder.Completed
 *   instance which allows you to call the build() methods on the SubSetBuilder instances.
 * </ul>
 *
 * @author Nils Bandener
 */
public class DeduplicatingCompactSubSetBuilder<E> {
    private final IndexedImmutableSetImpl<E> candidateElements;
    private final int bitArraySize;
    private E currentElement;
    private int currentElementIndex;
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

    /**
     * Informs the builder about the next element that is going to be added to the sub-set builders.
     * The add() method of the sub-set builders will only accept the given element until `next()` is
     * called again with another element.
     * <p>
     * Note: You need to follow the iteration order of the superSet originally supplied to this instance.
     * You can however skip elements completely.
     */
    public void next(E candidateElement) {
        if (this.currentElement != null) {
            this.finishCurrentElement();
        }

        int currentElementIndex = this.candidateElements.elementToIndex(candidateElement);
        if (currentElementIndex == -1) {
            throw new IllegalArgumentException(
                    "Element " + candidateElement + " is not part of super set " + this.candidateElements);
        }

        if (this.currentElement != null && currentElementIndex <= this.currentElementIndex) {
            throw new IllegalArgumentException(
                    "Element " + candidateElement + " comes in the iteration order of the super set before "
                            + this.currentElement + " (" + currentElementIndex + " < " + this.currentElementIndex + ");"
                            + "You must follow the iteration order of the super set specified in the constructor.");
        }

        this.currentElement = candidateElement;
        this.currentElementIndex = currentElementIndex;
    }

    /**
     * Finalizes this build process. This method will return a Completed instance which
     * can be used to finalize building individual sub-sets.
     */
    public Completed<E> build() {
        this.finishCurrentElement();
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

    int validateCurrentElement(E candidateElement) {
        if (this.currentElement != null) {
            if (!candidateElement.equals(this.currentElement)) {
                throw new IllegalStateException(
                        "Trying to add an element which is not the current element; candidateElement: "
                                + candidateElement + "; currentElement: " + currentElement);
            }

            return this.currentElementIndex;
        } else {
            throw new IllegalStateException("next() must be called before an element can be added");
        }
    }

    void finishCurrentElement() {
        if (this.currentElement == null) {
            return;
        }

        for (SubSetBuilder<E> subSetBuilder : this.subSetBuilders) {
            subSetBuilder.finish(this.currentElement);
        }

        for (BackingBitSetBuilder<E> backingBitSetBuilder : this.backingBitSets) {
            backingBitSetBuilder.finish(this.currentElement);
        }

        this.currentElement = null;
        this.backingCollectionWithCurrentElementOnly = null;
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
         * <p>
         * Note: You need to observe the protocol requirements imposed by DeduplicatingCompactSubSetBuilder:
         * <ul>
         * <li>You can only call add() for elements that are member of the super-set given when the DeduplicatingCompactSubSetBuilder was created. Otherwise, a IllegalArgumentException will be thrown.
         * <li>You must stick to the iteration order for the super-set.
         * </ul>
         */
        public void add(E element) {
            int elementIndex = this.root.validateCurrentElement(element);

            if (size == 0) {
                if (root.backingCollectionWithCurrentElementOnly != null) {
                    this.backingCollection = root.backingCollectionWithCurrentElementOnly;
                } else {
                    this.backingCollection = root.backingCollectionWithCurrentElementOnly =
                            new BackingBitSetBuilder<>(element, elementIndex, root);
                }

                this.size = 1;
            } else {
                backingCollection.offerElement(element, elementIndex);
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
        private int offeredElementIndex = -1;

        private long[] bits;
        private int size;
        private final IndexedImmutableSetImpl<E> elementToIndexMap;
        private final int bitArrayOffset;
        private BackingBitSetBuilder<E> copyWithNone;
        private final DeduplicatingCompactSubSetBuilder<E> root;
        private ImmutableCompactSubSet<E> finalBuildResult;
        private E firstElement;

        BackingBitSetBuilder(E element, int elementIndex, DeduplicatingCompactSubSetBuilder<E> root) {
            this.elementToIndexMap = root.candidateElements;
            this.firstElement = element;
            this.size = 1;
            long bit = 1l << (elementIndex & 0x3f);
            int arrayIndex = elementIndex >> 6;
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

        void offerElement(E offeredElement, int offeredElementIndex) {
            this.offeredElement = offeredElement;
            this.offeredElementIndex = offeredElementIndex;
        }

        void addOfferedElement() {
            if (offeredElement == null) {
                return;
            }

            try {
                if (BitBackedSetImpl.setBit(this.bits, this.offeredElementIndex, this.bitArrayOffset)) {
                    this.size++;

                    if (this.firstElement == null) {
                        this.firstElement = this.offeredElement;
                    }
                }
            } finally {
                this.offeredElement = null;
                this.offeredElementIndex = -1;
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
