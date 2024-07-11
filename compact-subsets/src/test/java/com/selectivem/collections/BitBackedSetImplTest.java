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
}
