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

import java.util.function.Function;

class GenericArrays {

    @SuppressWarnings("unchecked")
    static <E> E[] create(int size) {
        return (E[]) new Object[size];
    }

    static <E> E[] extend(E[] old, int newSize) {
        E[] result = create(newSize);
        System.arraycopy(old, 0, result, 0, old.length);
        return result;
    }

    static <E> Object[] copyAsObjectArray(E[] array) {
        Object[] result = new Object[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    static <E> Object[] copyAsObjectArray(E[] array, int newSize) {
        Object[] result = new Object[newSize];
        System.arraycopy(array, 0, result, 0, Math.min(array.length, newSize));
        return result;
    }

    static <E, T> T[] copyAsTypedArray(E[] source, T[] target) {
        T[] result = target.length >= source.length
                ? target
                : (T[]) java.lang.reflect.Array.newInstance(target.getClass().getComponentType(), source.length);

        System.arraycopy(source, 0, result, 0, source.length);
        return result;
    }

    static <E, T> T[] copyAsTypedArray(E[] source, T[] target, int size) {
        T[] result = target.length >= size
                ? target
                : (T[]) java.lang.reflect.Array.newInstance(target.getClass().getComponentType(), size);

        System.arraycopy(source, 0, result, 0, size);
        return result;
    }

    static int indexOfNextNonNull(Object[] array, int start) {
        for (int i = start; i < array.length; i++) {
            if (array[i] != null) {
                return i;
            }
        }

        return -1;
    }

    static <V, V2> V2[] mapInPlace(V[] array, Function<V, V2> mappingFunction) {
        if (array == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        V2[] result = (V2[]) array;

        if (mappingFunction != null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    result[i] = mappingFunction.apply(array[i]);
                }
            }
        }

        return result;
    }
}
