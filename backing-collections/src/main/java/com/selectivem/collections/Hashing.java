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

class Hashing {

    static final int COLLISION_HEAD_ROOM = 10;
    static final int NO_SPACE = Integer.MAX_VALUE;

    static final int hashPosition(int tableSize, Object e) {
        if (e == null) {
            throw new IllegalArgumentException("null values are not supported");
        }

        int hash = e.hashCode();

        switch (tableSize) {
            case 16:
                int h8 = hashTo8bit(hash);
                return (h8 & 0xf) ^ (h8 >> 4 & 0xf);
            case 64:
                return (hash & 0x3f)
                        ^ (hash >> 6 & 0x3f)
                        ^ (hash >> 12 & 0x3f)
                        ^ (hash >> 18 & 0x3f)
                        ^ (hash >> 24 & 0xf)
                        ^ (hash >> 28 & 0xf);
            case 256:
                return hashTo8bit(hash);
            case 1024:
                return (hash & 0x3ff) ^ (hash >> 10 & 0x3ff) ^ (hash >> 20 & 0x3ff) ^ (hash >> 30 & 0x3);
            case 4096:
                return (hash & 0xfff) ^ (hash >> 12 & 0xfff) ^ (hash >> 24 & 0xfff);
            default:
                throw new RuntimeException("Invalid tableSize " + tableSize);
        }
    }

    static final int hashTo8bit(int hash) {
        return (hash & 0xff) ^ (hash >> 8 & 0xff) ^ (hash >> 16 & 0xff) ^ (hash >> 24 & 0xff);
    }

    static final int hashTableSize(int elementCount) {
        if (elementCount <= 10) {
            return 16;
        } else if (elementCount <= 50) {
            return 64;
        } else if (elementCount <= 200) {
            return 256;
        } else if (elementCount <= 900) {
            return 1024;
        } else if (elementCount <= 3900) {
            return 4096;
        } else {
            return -1;
        }
    }

    static final int nextSize(int tableSize) {
        switch (tableSize) {
            case 16:
                return 64;
            case 64:
                return 256;
            case 256:
                return 1024;
            case 1024:
                return 4096;
            default:
                return -1;
        }
    }

    /**
     * Checks whether the given element is contained in the table or whether it can be inserted.
     *
     * Returns NO_SPACE if it cannot be inserted. If it returns a value >= 0, it is not contained and can be inserted at
     * the given position. If it returns a value < 0, the actual position can be calculated as -returnValue - 1.
     */
    static final <E> int checkTable(E[] table, Object e, int hashPosition) {
        if (table[hashPosition] == null) {
            return hashPosition;
        } else if (table[hashPosition].equals(e)) {
            return -1 - hashPosition;
        }

        int max = hashPosition + COLLISION_HEAD_ROOM;

        for (int i = hashPosition + 1; i <= max; i++) {
            if (table[i] == null) {
                return i;
            } else if (table[i].equals(e)) {
                return -1 - i;
            }
        }

        return NO_SPACE;
    }

    static final <E> E[] reindex(E[] oldTable, int newTableSize) {
        E[] newTable = GenericArrays.create(newTableSize + COLLISION_HEAD_ROOM);

        int oldTableLength = oldTable.length;

        for (int i = 0; i < oldTableLength; i++) {
            E e = oldTable[i];

            if (e == null) {
                continue;
            }

            int newPosition = checkTable(newTable, e, hashPosition(newTableSize, e));

            if (newPosition == NO_SPACE) {
                return null;
            }

            newTable[newPosition] = e;
        }

        return newTable;
    }

    static final <E> E[] extend(E[] oldTable, int oldTableSize, int maxNewTableSize) {
        int newTableSize = nextSize(oldTableSize);

        if (newTableSize == -1) {
            return null;
        }

        for (; ; ) {
            E[] newTable = reindex(oldTable, newTableSize);

            if (newTable != null) {
                return newTable;
            }

            newTableSize = nextSize(newTableSize);

            if (newTableSize == -1 || newTableSize > maxNewTableSize) {
                return null;
            }
        }
    }
}
