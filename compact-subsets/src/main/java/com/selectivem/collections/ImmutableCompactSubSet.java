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

import java.util.Collection;

/**
 * Instances of this interface are produced by the classes CompactSubSetBuilder and DeduplicatingCompactSubSetBuilder.
 * <p>
 * Instances are supposed to be space-efficient. For this, bit-fields can be used to specify the member elements.
 * <p>
 * Additionally, the containsAny() and containsAll() methods might use bit operations to provide computation
 * efficient functionality.
 */
public interface ImmutableCompactSubSet<E> extends UnmodifiableSet<E> {
    boolean containsAny(Collection<E> elements);
}
