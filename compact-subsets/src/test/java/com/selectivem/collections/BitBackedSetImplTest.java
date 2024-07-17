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
import org.junit.Assert;
import org.junit.Test;

public class BitBackedSetImplTest {
    @Test
    public void bitArraySize() {
        Assert.assertEquals(1, BitBackedSetImpl.bitArraySize(10));
        Assert.assertEquals(1, BitBackedSetImpl.bitArraySize(64));
        Assert.assertEquals(2, BitBackedSetImpl.bitArraySize(65));
        Assert.assertEquals(5, BitBackedSetImpl.bitArraySize(257));
        Assert.assertEquals(5, BitBackedSetImpl.bitArraySize(320));
        Assert.assertEquals(6, BitBackedSetImpl.bitArraySize(321));
    }

    @Test
    public void lastNonZeroIndex() {
        Assert.assertEquals(0, BitBackedSetImpl.lastNonZeroIndex(new long[] {1, 0, 0, 0}));
        Assert.assertEquals(1, BitBackedSetImpl.lastNonZeroIndex(new long[] {0, 1, 0, 0}));
        Assert.assertEquals(2, BitBackedSetImpl.lastNonZeroIndex(new long[] {1, 2, 3, 0}));
        Assert.assertEquals(-1, BitBackedSetImpl.lastNonZeroIndex(new long[] {0, 0, 0, 0}));
    }

    @Test
    public void firstNonZeroIndex() {
        Assert.assertEquals(0, BitBackedSetImpl.firstNonZeroIndex(new long[] {1, 0, 0, 0}));
        Assert.assertEquals(1, BitBackedSetImpl.firstNonZeroIndex(new long[] {0, 1, 0, 0}));
        Assert.assertEquals(0, BitBackedSetImpl.firstNonZeroIndex(new long[] {1, 2, 3, 0}));
        Assert.assertEquals(-1, BitBackedSetImpl.firstNonZeroIndex(new long[] {0, 0, 0, 0}));
    }

    @Test
    public void empty_arrayBacked() {
        BitBackedSetImpl.LongArrayBacked<String> subject =
                new BitBackedSetImpl.LongArrayBacked<>(new long[] {0}, 0, IndexedImmutableSetImpl.of("a"), 0);
        Assert.assertTrue(subject.isEmpty());
    }

    @Test
    public void empty_longBacked() {
        BitBackedSetImpl.LongBacked<String> subject =
                new BitBackedSetImpl.LongBacked<>(0, 0, IndexedImmutableSetImpl.of("a"), 0);
        Assert.assertTrue(subject.isEmpty());
    }

    @Test
    public void contains_arrayBacked() {
        BitBackedSetImpl.LongArrayBacked<String> subject =
                new BitBackedSetImpl.LongArrayBacked<>(new long[] {1}, 0, IndexedImmutableSetImpl.of("a", "b"), 0);
        Assert.assertTrue(subject.contains("a"));
        Assert.assertFalse(subject.contains("b"));
        Assert.assertFalse(subject.contains("x"));
    }

    @Test
    public void contains_longBacked() {
        BitBackedSetImpl.LongBacked<String> subject =
                new BitBackedSetImpl.LongBacked<>(1, 0, IndexedImmutableSetImpl.of("a", "b"), 0);
        Assert.assertTrue(subject.contains("a"));
        Assert.assertFalse(subject.contains("b"));
        Assert.assertFalse(subject.contains("x"));
    }

    @Test
    public void setBit() {
        long[] bits = new long[2];
        boolean returnValue = BitBackedSetImpl.setBit(bits, 1, 0);
        Assert.assertTrue(returnValue);
        Assert.assertEquals(2, bits[0]);
        returnValue = BitBackedSetImpl.setBit(bits, 1, 0);
        Assert.assertFalse(returnValue);
        Assert.assertEquals(2, bits[0]);
        returnValue = BitBackedSetImpl.setBit(bits, 65, 0);
        Assert.assertTrue(returnValue);
        Assert.assertEquals(2, bits[1]);
    }

    @Test(expected = NoSuchElementException.class)
    public void iterator_exhausted_arrayBacked() {
        BitBackedSetImpl.LongArrayBacked<String> subject =
                new BitBackedSetImpl.LongArrayBacked<>(new long[] {1}, 0, IndexedImmutableSetImpl.of("a", "b"), 0);
        Iterator<String> iter = subject.iterator();

        while (iter.hasNext()) {
            iter.next();
        }

        iter.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void iterator_exhausted_longBacked() {
        BitBackedSetImpl.LongBacked<String> subject =
                new BitBackedSetImpl.LongBacked<>(1, 0, IndexedImmutableSetImpl.of("a", "b"), 0);
        Iterator<String> iter = subject.iterator();

        while (iter.hasNext()) {
            iter.next();
        }

        iter.next();
    }
}
