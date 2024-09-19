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
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class ImmutableSetImplTest {
    public static class BasicTest {

        @Test
        public void empty() {
            ImmutableSetImpl<String> set1 = ImmutableSetImpl.empty();
            Assert.assertEquals(0, set1.size());
            Assert.assertFalse(set1.contains("a"));
            Assert.assertFalse(set1.iterator().hasNext());
            Assert.assertEquals(0, set1.toArray().length);
            Assert.assertEquals(0, set1.toArray(new String[0]).length);
        }

        @Test
        public void of1() {
            ImmutableSetImpl<String> set1 = ImmutableSetImpl.of("a");
            Assert.assertEquals(1, set1.size());
            Assert.assertTrue(set1.contains("a"));
            Assert.assertFalse(set1.contains("b"));
            Assert.assertEquals("a", set1.iterator().next());
            Assert.assertEquals(1, set1.toArray().length);
            Assert.assertEquals(1, set1.toArray(new String[0]).length);
        }

        @Test(expected = NoSuchElementException.class)
        public void of1_iterator_exhausted() {
            ImmutableSetImpl<String> set1 = ImmutableSetImpl.of("a");
            Iterator<String> iter = set1.iterator();
            iter.next();
            iter.next();
        }

        @Test(expected = IllegalArgumentException.class)
        public void of1_null() {
            ImmutableSetImpl.of(null);
        }

        @Test
        public void of2() {
            ImmutableSetImpl<String> set1 = ImmutableSetImpl.of("a", "b");
            Assert.assertEquals(2, set1.size());
            Assert.assertTrue(set1.contains("a"));
            Assert.assertTrue(set1.contains("b"));
            Assert.assertFalse(set1.contains("c"));
            Iterator<String> iter = set1.iterator();
            Assert.assertEquals("a", iter.next());
            Assert.assertEquals("b", iter.next());
            Assert.assertFalse(iter.hasNext());
            Assert.assertEquals(2, set1.toArray().length);
            Assert.assertEquals(2, set1.toArray(new String[0]).length);
        }

        @Test
        public void of2_same() {
            ImmutableSetImpl<String> set1 = ImmutableSetImpl.of("a", "a");
            Assert.assertEquals(1, set1.size());
            Assert.assertEquals(ImmutableSetImpl.of("a"), set1);
        }

        @Test(expected = IllegalArgumentException.class)
        public void of2_null() {
            ImmutableSetImpl.of(null, null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void of2_null2() {
            ImmutableSetImpl.of("a", null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void of2_null3() {
            ImmutableSetImpl.of(null, "a");
        }

        @Test(expected = NoSuchElementException.class)
        public void of2_iterator_exhausted() {
            ImmutableSetImpl<String> set1 = ImmutableSetImpl.of("a", "b");
            Iterator<String> iter = set1.iterator();
            iter.next();
            iter.next();
            iter.next();
        }
    }
}
