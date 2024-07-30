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

abstract class Hashing {

    static final int NO_SPACE = Integer.MAX_VALUE;
    static final int B4 = 0b0000_00001111; // 0x0f
    static final int B5 = 0b0000_00011111; // 0x1f
    static final int B6 = 0b0000_00111111; // 0x3f
    static final int B7 = 0b0000_01111111; // 0x7f
    static final int B8 = 0b0000_11111111; // 0xff
    static final int B9 = 0b0001_11111111; // 0x1ff
    static final int B10 = 0b0011_11111111; // 0x3ff
    static final int B11 = 0b0111_11111111; // 0x7ff
    static final int B12 = 0b1111_11111111; // 0xfff
    static final int B13 = 0b11111_11111111; // 0x1fff
    static final int B14 = 0b111111_11111111; // 0x3fff
    static final int B15 = 0b1111111_11111111; // 0x7fff
    static final int B16 = 0b11111111_11111111; // 0xffff

    static int hashPosition(int tableSize, Object e) {
        if (e == null) {
            throw new IllegalArgumentException("null values are not supported");
        }

        int hash = e.hashCode();

        switch (tableSize) {
            case 0x10: // 16
                hash = scramble(hash);
                int h8 = hashTo8bit(hash);
                return (h8 & B4) ^ (h8 >> 4 & B4);
            case 0x40: // 64
                hash = scramble(hash);
                return (hash & B6)
                        ^ (hash >> 6 & B6)
                        ^ (hash >> 12 & B6)
                        ^ (hash >> 18 & B6)
                        ^ (hash >> 24 & B4)
                        ^ (hash >> 28 & B4);
            case 0x100: // 256
                hash = scramble(hash);
                return hashTo8bit(hash);
            case 0x200: // 512
                hash = scramble2(scramble(hash));
                return (hash & B9) ^ (hash >> 9 & B7) ^ (hash >> 16 & B9) ^ (hash >> 25 & B7);
            case 0x400: // 1024
                hash = scramble2(scramble(hash));
                return (hash & B10) ^ (hash >> 10 & B6) ^ (hash >> 16 & B10) ^ (hash >> 26 & B6);
            case 0x800: // 2048
                hash = scramble2(scramble(hash));
                return (hash & B11) ^ (hash >> 11 & B10) ^ (hash >> 21 & B11);
            case 0x1000: // 4096
                hash = scramble2(scramble(hash));
                return (hash & B12) ^ (hash >> 12 & B8) ^ (hash >> 20 & B12);
            case 0x2000: // 8k
                hash = scramble2(scramble(hash));
                return (hash & B13) ^ (hash >> 13 & B6) ^ (hash >> 19 & B13);
            case 0x4000: // 16k
                hash = scramble2(scramble(hash));
                return (hash & B14) ^ (hash >> 14 & B4) ^ (hash >> 18 & B14);
            case 0x8000: // 32k
                hash = scramble2(scramble(hash));
                return (hash & B15) ^ (hash >> 15 & B8) ^ (hash >> 23 & B9);
            case 0x10000: // 64k
                hash = scramble2(scramble(hash));
                return (hash & B16) ^ (hash >> 16 & B16);
            default:
                hash = scramble2(scramble(hash));
                assert (tableSize & (tableSize - 1)) == 0 : "tableSize must be a power of two";
                return hash & (tableSize - 1);
        }
    }

    static int hashTo8bit(int hash) {
        return (hash & B8) ^ (hash >> 8 & B8) ^ (hash >> 16 & B8) ^ (hash >> 24 & B8);
    }

    /**
     * From murmur32 hash; public domain
     */
    static int scramble(int n) {
        n *= 0xcc9e2d51l;
        n = Integer.rotateLeft(n, 15);
        n *= 0x1b873593l;
        return n;
    }

    /**
     * From murmur32 hash; public domain
     */
    static int scramble2(int h) {
        h = h ^ (h >> 16);
        h *= 0x85ebca6b;
        h = h ^ (h >> 13);
        h *= 0xc2b2ae35;
        h = h ^ (h >> 16);
        return h;
    }

    static int hashTableSize(int elementCount) {
        if (elementCount <= 10) {
            return 0x10;
        } else if (elementCount <= 45) {
            return 0x40;
        } else if (elementCount <= 180) {
            return 0x100;
        } else if (elementCount <= 360) {
            return 0x200;
        } else if (elementCount <= 720) {
            return 0x400;
        } else if (elementCount <= 1440) {
            return 0x800;
        } else if (elementCount <= 2880) {
            return 0x1000;
        } else if (elementCount <= 5750) {
            return 0x2000;
        } else if (elementCount <= 11500) {
            return 0x4000;
        } else if (elementCount <= 23000) {
            return 0x8000;
        } else if (elementCount <= 46000) {
            return 0x10000;
        } else if (elementCount <= 92000) {
            return 0x20000;
        } else if (elementCount <= 180_000) {
            return 0x40000;
        } else {
            return -1;
        }
    }

    static int nextSize(int tableSize) {
        switch (tableSize) {
            case 0x10:
                return 0x40;
            case 0x40:
                return 0x100;
            case 0x100:
                return 0x200;
            case 0x200:
                return 0x400;
            default:
                if (tableSize <= 0x20000) {
                    return tableSize * 2;
                } else {
                    return -1;
                }
        }
    }

    static short maxProbingDistance(int tableSize) {
        if (tableSize <= 0x10) {
            return 8;
        } else if (tableSize <= 0x200) {
            return 12;
        } else if (tableSize <= 0x1000) {
            return 16;
        } else if (tableSize <= 0x8000) {
            return 24;
        } else {
            return 32;
        }
    }

    /**
     * Checks whether the given element is contained in the table or whether it can be inserted.
     *
     * Returns NO_SPACE if it cannot be inserted. If it returns a value >= 0, it is not contained and can be inserted at
     * the given position. If it returns a value < 0, the actual position can be calculated as -returnValue - 1.
     */
    static <E> int checkTable(E[] table, Object e, int hashPosition, short maxProbingDistance) {
        if (table[hashPosition] == null) {
            return hashPosition;
        } else if (table[hashPosition].equals(e)) {
            return -1 - hashPosition;
        }

        int max = hashPosition + maxProbingDistance;

        for (int i = hashPosition + 1; i <= max; i++) {
            if (table[i] == null) {
                return i;
            } else if (table[i].equals(e)) {
                return -1 - i;
            }
        }

        return NO_SPACE;
    }
}
