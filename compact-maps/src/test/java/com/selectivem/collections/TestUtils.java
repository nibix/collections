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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class TestUtils {

    static Set<String> setOf(String... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    static Map<String, String> mapOf(String... kv) {
        HashMap<String, String> result = new HashMap<>();

        for (int i = 0; i < kv.length; i += 2) {
            result.put(kv[i], kv[i + 1]);
        }

        return result;
    }

    static Map<String, String> stringMap(int size) {
        Set<String> set = stringSet(size);
        Map<String, String> result = new HashMap<>(size);

        for (String e : set) {
            result.put(e, e + "_val");
        }

        return result;
    }

    static Set<String> stringSet(int size) {
        HashSet<String> result = new HashSet<>(size);

        for (char c = 'a'; c <= 'z'; c++) {
            result.add(String.valueOf(c));
            if (result.size() >= size) {
                return result;
            }
        }

        for (char c1 = 'a'; c1 <= 'z'; c1++) {
            for (char c2 = 'a'; c2 <= 'z'; c2++) {
                result.add(String.valueOf(c1) + String.valueOf(c2));
                if (result.size() >= size) {
                    return result;
                }
            }
        }

        for (char c1 = 'a'; c1 <= 'z'; c1++) {
            for (char c2 = 'a'; c2 <= 'z'; c2++) {
                for (char c3 = 'a'; c3 <= 'z'; c3++) {
                    result.add(String.valueOf(c1) + String.valueOf(c2) + String.valueOf(c3));
                    if (result.size() >= size) {
                        return result;
                    }
                }
            }
        }

        for (char c1 = 'a'; c1 <= 'z'; c1++) {
            for (char c2 = 'a'; c2 <= 'z'; c2++) {
                for (char c3 = 'a'; c3 <= 'z'; c3++) {
                    for (char c4 = 'a'; c4 <= 'z'; c4++) {
                        result.add(String.valueOf(c1) + String.valueOf(c2) + String.valueOf(c3) + String.valueOf(c4));
                        if (result.size() >= size) {
                            return result;
                        }
                    }
                }
            }
        }

        if (result.size() < size) {
            throw new RuntimeException("Cannot generate set with size " + size);
        }

        return result;
    }
}
