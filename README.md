[![codecov](https://codecov.io/gh/nibix/collections/graph/badge.svg?token=IE4URKDA1A)](https://codecov.io/gh/nibix/collections)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e480bef562a14553a3ef60921e547699)](https://app.codacy.com/gh/nibix/collections?utm_source=github.com&utm_medium=referral&utm_content=nibix/collections&utm_campaign=Badge_Grade)

# com.selectiveminimalism.collections

Specialized efficient data structures for Java

## checklists

Dedicated check list and check table data structures.

Check lists and check tables take a set of items as input; initially, all items will be marked as unchecked.
You can then use the various methods to mark individual items as checked. Further methods allow you to check
whether your check list or check table is complete - and when not, what is missing.

A check list is a one-dimensional list, a check table is a two-dimensional matrix.

One typical use case for these data structures are complex access control rules. You can use
a check table to tick off what privileges are present and which are not present. The tabular
`toString()` representation helps quickly identifying what privileges are missing:

```
          | indices:data/read/search |
 index_a11| ok                       |
 index_a12| MISSING                  |
 index_a13| MISSING                  |
 index_a14| MISSING                  |
```

### Maven dependency

```
<dependency>
    <groupId>com.selectivem.collections</groupId>
    <artifactId>checklists</artifactId>
    <version>1.4.0</version>
</dependency>
```

## compact-subsets

Builder classes that allow you to create sub-sets of existing `Set<>` objects in Java. The sub-sets are represented by 
bitfields, thus they can be extremely space efficient. Efficient deduplication of equal sub-sets is also possible.

### Maven dependency

```
<dependency>
    <groupId>com.selectivem.collections</groupId>
    <artifactId>compact-subsets</artifactId>
    <version>1.4.0</version>
</dependency>
```

## compact-maps

Builder classes that allow you to create groups of maps that share a common super-set of keys. The produced maps may
share their key hash tables and only explicitly specify the value tables. Thus, these maps take about 50% less heap than normal maps.

### Maven dependency

```
<dependency>
    <groupId>com.selectivem.collections</groupId>
    <artifactId>compact-maps</artifactId>
    <version>1.4.0</version>
</dependency>
```

## indexed-set

An immutable set implementation which assigns ordinal numbers to its member elements. It exposes the
methods `elementToIndex()` and `indexToElement()` which provide fast O(1) means to convert an element to its index and 
vice-versa. This implementation is used internally by `checklists`, `compact-subsets` and `compact-maps`.

### Maven dependency

```
<dependency>
    <groupId>com.selectivem.collections</groupId>
    <artifactId>indexed-set</artifactId>
    <version>1.4.0</version>
</dependency>
```

## interfaces

Bare specialized Java interfaces for collections. Includes `UnmodifiableCollection`, `UnmodifiableSet`, `UnmodifiableList`.

### Maven dependency

```
<dependency>
    <groupId>com.selectivem.collections</groupId>
    <artifactId>interfaces</artifactId>
    <version>1.4.0</version>
</dependency>
```

## License

This code is licensed under the Apache 2.0 License.

## Copyright

Copyright 2024 Nils Bandener <code@selectiveminimalism.com>

Partially based on fluent collections which is Copyright 2022 by floragunn GmbH: https://git.floragunn.com/util/fluent-collections
