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
import java.util.LinkedHashSet;
import java.util.Set;

class SimpleTestData {
    static Set<String> setOf(String... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    static Set<String> setOfAtoZStrings(int size) {
        HashSet<String> result = new HashSet<>(size);

        for (char c = 'a'; c <= 'z'; c++) {
            result.add(String.valueOf(c));

            if (result.size() >= size) {
                return result;
            }
        }

        for (char c1 = 'a'; c1 <= 'z'; c1++) {
            for (char c2 = 'a'; c2 <= 'z'; c2++) {
                result.add(String.valueOf(c1) + c2);

                if (result.size() >= size) {
                    return result;
                }
            }
        }

        for (char c1 = 'a'; c1 <= 'z'; c1++) {
            for (char c2 = 'a'; c2 <= 'z'; c2++) {
                for (char c3 = 'a'; c3 <= 'z'; c3++) {
                    result.add(String.valueOf(c1) + c2 + c3);

                    if (result.size() >= size) {
                        return result;
                    }
                }
            }
        }

        return result;
    }

    static Set<String> orderedSetOfNumberedStrings(String prefix, int size) {
        Set<String> superSet = new LinkedHashSet<>();

        for (int i = 0; i < size; i++) {
            superSet.add(prefix + i);
        }

        return superSet;
    }

    static Set<String> intersection(Set<String> set1, Set<String> set2) {
        HashSet<String> result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }
}
