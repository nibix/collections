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

import java.util.AbstractSet;
import java.util.Collection;

abstract class UnmodifiableSetImpl<E> extends AbstractSet<E> implements UnmodifiableSet<E> {

    @SuppressWarnings("deprecation")
    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean addAll(Collection<? extends E> e) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean remove(Object arg0) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean removeAll(Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean retainAll(Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object o : collection) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }
}
