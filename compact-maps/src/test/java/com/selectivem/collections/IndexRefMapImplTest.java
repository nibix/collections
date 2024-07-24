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
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.Assert;
import org.junit.Test;

public class IndexRefMapImplTest {

    @Test
    public void toStringTest() {
        IndexRefMapImpl<String, String> subject =
                new IndexRefMapImpl<>(new String[] {"1", "2"}, 2, IndexedImmutableSetImpl.of("a", "b"), 0);
        Assert.assertEquals("[a=1, b=2]", subject.toString());
        subject = new IndexRefMapImpl<>(new String[] {"1"}, 2, IndexedImmutableSetImpl.of("a", "b"), 0);
        Assert.assertEquals("[a=1]", subject.toString());
    }

    @Test
    public void containsValue() {
        IndexRefMapImpl<String, String> subject =
                new IndexRefMapImpl<>(new String[] {"1", "2"}, 2, IndexedImmutableSetImpl.of("a", "b"), 0);
        Assert.assertTrue(subject.containsValue("1"));
        Assert.assertFalse(subject.containsValue("x"));
    }

    @Test
    public void get_keySuperSetMiss() {
        IndexRefMapImpl<String, String> subject =
                new IndexRefMapImpl<>(new String[] {"1", "2"}, 2, IndexedImmutableSetImpl.of("a", "b"), 0);
        Assert.assertNull(subject.get("x"));
    }

    @Test(expected = NoSuchElementException.class)
    public void entrySet_iterator_exhausted() {
        IndexRefMapImpl<String, String> subject =
                new IndexRefMapImpl<>(new String[] {"1", "2"}, 2, IndexedImmutableSetImpl.of("a", "b"), 0);
        Iterator<Map.Entry<String, String>> iter = subject.entrySet().iterator();

        while (iter.hasNext()) {
            iter.next();
        }

        iter.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void keySet_iterator_exhausted() {
        IndexRefMapImpl<String, String> subject =
                new IndexRefMapImpl<>(new String[] {"1", "2"}, 2, IndexedImmutableSetImpl.of("a", "b"), 0);
        Iterator<String> iter = subject.keySet().iterator();

        while (iter.hasNext()) {
            iter.next();
        }

        iter.next();
    }
}
