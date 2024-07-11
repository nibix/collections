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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class ImmutableCompactSubSetImplTest {

    @Test
    public void of_sameInstance() {
        ImmutableCompactSubSet<String> instance1 =
                new CompactSubSetBuilder<String>(setOf("a", "b", "c", "d")).of(setOf("a", "b", "c"));
        ImmutableCompactSubSet<String> instance2 = ImmutableCompactSubSetImpl.of(instance1);

        Assert.assertTrue(instance1 == instance2);
    }

    @Test
    public void of_wrappedInstance() {
        IndexedImmutableSetImpl<String> instance1 = IndexedImmutableSetImpl.of(setOf("a", "b", "c", "d"));
        ImmutableCompactSubSet<String> instance2 = ImmutableCompactSubSetImpl.of(instance1);

        Assert.assertTrue(instance2.equals(instance1));
        Assert.assertEquals(instance1.hashCode(), instance2.hashCode());
        Assert.assertEquals(instance1.toString(), instance2.toString());
        Assert.assertEquals(instance1.isEmpty(), instance2.isEmpty());
        Assert.assertArrayEquals(instance1.toArray(), instance2.toArray());
    }

    static Set<String> setOf(String... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }
}
