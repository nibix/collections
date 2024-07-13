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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A BitBackedSet is a view on a IndexedImmutableSetImpl, where bitfields determine whether a
 * member of the IndexedImmutableSetImpl is also a member of the BitBackedSet. Thus, a BitBackedSet
 * is always a strict sub-set of a IndexedImmutableSetImpl.
 */
abstract class BitBackedSetImpl<E> extends UnmodifiableSetImpl<E> implements ImmutableCompactSubSet<E> {

    static final class LongArrayBacked<E> extends BitBackedSetImpl<E> {
        private final long[] bits;
        private final int size;
        private final IndexedImmutableSetImpl<E> elementToIndexMap;
        private final int bitArrayOffset;

        public LongArrayBacked(
                long[] bits, int size, IndexedImmutableSetImpl<E> elementToIndexMap, int bitArrayOffset) {
            this.bits = bits;
            this.size = size;
            this.elementToIndexMap = elementToIndexMap;
            this.bitArrayOffset = bitArrayOffset;
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {

                int currentPosition = findNext(bitArrayOffset, 0);

                @Override
                public boolean hasNext() {
                    return currentPosition != -1;
                }

                @Override
                public E next() {
                    if (currentPosition == -1) {
                        throw new NoSuchElementException();
                    }

                    E result = elementToIndexMap.indexToElement(currentPosition);
                    this.currentPosition = findNext();
                    return result;
                }

                int findNext(int arrayIndex, int bitIndex) {
                    int actualArrayIndex = arrayIndex - bitArrayOffset;

                    if (bitIndex >= 64) {
                        bitIndex = 0;
                        arrayIndex++;
                        actualArrayIndex++;

                        if (actualArrayIndex >= bits.length) {
                            return -1;
                        }

                        while (bits[actualArrayIndex] == 0) {
                            arrayIndex++;
                            actualArrayIndex++;
                            if (actualArrayIndex >= bits.length) {
                                return -1;
                            }
                        }
                    }


                    for (; ; ) {
                        long bit = 1l << bitIndex;

                        if ((bits[actualArrayIndex] & bit) != 0) {
                            return arrayIndex << 6 | bitIndex;
                        }

                        bitIndex++;
                        if (bitIndex >= 64) {
                            bitIndex = 0;
                            arrayIndex++;
                            actualArrayIndex++;

                            if (actualArrayIndex >= bits.length) {
                                return -1;
                            }

                            while (bits[actualArrayIndex] == 0) {
                                arrayIndex++;
                                actualArrayIndex++;
                                if (actualArrayIndex >= bits.length) {
                                    return -1;
                                }
                            }
                        }
                    }
                }

                int findNext() {
                    int bitIndex = this.currentPosition & 0x3f;
                    int arrayIndex = this.currentPosition >> 6;

                    return findNext(arrayIndex, bitIndex + 1);
                }
            };
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public boolean isEmpty() {
            return this.size == 0;
        }

        @Override
        public boolean contains(Object o) {
            int index = this.elementToIndexMap.elementToIndex(o);

            if (index == -1) {
                return false;
            }

            long bit = 1l << (index & 0x3f);
            int arrayIndex = (index >> 6) - this.bitArrayOffset;

            if (arrayIndex >= this.bits.length || arrayIndex < 0) {
                return false;
            }

            return (this.bits[arrayIndex] & bit) != 0;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof BitBackedSetImpl.LongArrayBacked) {
                BitBackedSetImpl.LongArrayBacked<?> other = (BitBackedSetImpl.LongArrayBacked<?>) o;

                if (other.size != this.size) {
                    return false;
                }

                if (other.elementToIndexMap == this.elementToIndexMap) {
                    if (this.bitArrayOffset != other.bitArrayOffset) {
                        return false;
                    }

                    if (this.bits.length != other.bits.length) {
                        return false;
                    }

                    for (int i = 0; i < this.bits.length; i++) {
                        if (this.bits[i] != other.bits[i]) {
                            return false;
                        }
                    }

                    return true;
                } else {
                    return super.equals(o);
                }

            } else {
                return super.equals(o);
            }
        }
    }

    static final class LongBacked<E> extends BitBackedSetImpl<E> {
        private final long bits;
        private final int size;
        private final IndexedImmutableSetImpl<E> elementToIndexMap;
        private final int bitArrayOffset;

        public LongBacked(long bits, int size, IndexedImmutableSetImpl<E> elementToIndexMap, int bitArrayOffset) {
            this.bits = bits;
            this.size = size;
            this.elementToIndexMap = elementToIndexMap;
            this.bitArrayOffset = bitArrayOffset;
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {

                int currentPosition = findNext(bitArrayOffset, 0);

                @Override
                public boolean hasNext() {
                    return currentPosition != -1;
                }

                @Override
                public E next() {
                    if (currentPosition == -1) {
                        throw new NoSuchElementException();
                    }

                    E result = elementToIndexMap.indexToElement(currentPosition);
                    this.currentPosition = findNext();
                    return result;
                }

                int findNext(int arrayIndex, int bitIndex) {
                    if (bitIndex >= 64) {
                        bitIndex = 0;
                        arrayIndex++;
                    }

                    if (arrayIndex - bitArrayOffset > 0) {
                        return -1;
                    }

                    for (; ; ) {
                        long bit = 1l << bitIndex;

                        if ((bits & bit) != 0) {
                            return arrayIndex << 6 | bitIndex;
                        }

                        bitIndex++;
                        if (bitIndex >= 64) {
                            bitIndex = 0;
                            arrayIndex++;
                        }

                        if (arrayIndex - bitArrayOffset > 0) {
                            return -1;
                        }
                    }
                }

                int findNext() {
                    int bitIndex = this.currentPosition & 0x3f;
                    int arrayIndex = this.currentPosition >> 6;

                    return findNext(arrayIndex, bitIndex + 1);
                }
            };
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public boolean isEmpty() {
            return this.size == 0;
        }

        @Override
        public boolean contains(Object o) {
            int index = this.elementToIndexMap.elementToIndex(o);

            if (index == -1) {
                return false;
            }

            long bit = 1l << (index & 0x3f);
            int arrayIndex = (index >> 6) - this.bitArrayOffset;

            if (arrayIndex != 0) {
                return false;
            }

            return (this.bits & bit) != 0;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof BitBackedSetImpl.LongBacked) {
                BitBackedSetImpl.LongBacked<?> other = (BitBackedSetImpl.LongBacked<?>) o;

                if (other.size != this.size) {
                    return false;
                }

                if (other.elementToIndexMap == this.elementToIndexMap) {
                    if (this.bitArrayOffset != other.bitArrayOffset) {
                        return false;
                    }

                    if (this.bits != other.bits) {
                        return false;
                    }

                    return true;
                } else {
                    return super.equals(o);
                }

            } else {
                return super.equals(o);
            }
        }
    }

    static int bitArraySize(int size) {
        if (size <= 64) {
            return 1;
        } else if (size <= 128) {
            return 2;
        } else if (size <= 192) {
            return 3;
        } else if (size <= 256) {
            return 4;
        } else {
            if ((size & (64 - 1)) == 0) {
                // Exact multiple of 64
                return size >> 6;
            } else {
                return size / 64 + 1;
            }
        }
    }

    static boolean setBit(long[] bits, int index, int bitArrayOffset) {
        if (index == -1) {
            return false;
        }

        long bit = 1l << (index & 0x3f);
        int arrayIndex = (index >> 6) - bitArrayOffset;

        if ((bits[arrayIndex] & bit) != 0) {
            return false;
        }

        bits[arrayIndex] |= bit;

        return true;
    }

    static int lastNonZeroIndex(long[] array) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] != 0) {
                return i;
            }
        }

        return -1;
    }

    static int firstNonZeroIndex(long[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] != 0) {
                return i;
            }
        }

        return -1;
    }
}
