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

public class GenericArraysTest {
    @Test
    public void indexOfNextNonNull() {
        Assert.assertEquals(0, GenericArrays.indexOfNextNonNull(new Object[] {1, 2, 3}, 0));
        Assert.assertEquals(1, GenericArrays.indexOfNextNonNull(new Object[] {null, 1, 2}, 0));
        Assert.assertEquals(-1, GenericArrays.indexOfNextNonNull(new Object[] {null, null, null}, 0));
    }

    @Test
    public void mapInPlace() {
        Object[] source = new Object[] {1, 2, 3};
        Object[] mapped = GenericArrays.mapInPlace(source, o -> String.valueOf(o));
        Assert.assertTrue(source == mapped);
        Assert.assertArrayEquals(new Object[] {"1", "2", "3"}, mapped);
    }

    @Test
    public void mapInPlace_identity() {
        Object[] source = new Object[] {1, 2, 3};
        Object[] mapped = GenericArrays.mapInPlace(source, null);
        Assert.assertTrue(source == mapped);
        Assert.assertArrayEquals(new Object[] {1, 2, 3}, mapped);
    }

    @Test
    public void mapInPlace_null() {
        Object[] mapped = GenericArrays.mapInPlace(null, null);
        Assert.assertTrue(mapped == null);
    }
}
