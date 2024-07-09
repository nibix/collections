[![codecov](https://codecov.io/gh/nibix/checklists/graph/badge.svg?token=957EGPZ5OE)](https://codecov.io/gh/nibix/checklists)

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
    <version>1.2.0</version>
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
    <version>1.2.0</version>
</dependency>
```

## interfaces

Bare specialized Java interfaces for collections. Includes `UnmodifiableCollection`, `UnmodifiableSet`, `UnmodifiableList`.

### Maven dependency

```
<dependency>
    <groupId>com.selectivem.collections</groupId>
    <artifactId>interfaces</artifactId>
    <version>1.2.0</version>
</dependency>
```

## License

This code is licensed under the Apache 2.0 License.

## Copyright

Copyright 2024 Nils Bandener <code@selectiveminimalism.com>

Partially based on fluent collections which is Copyright 2022 by floragunn GmbH: https://git.floragunn.com/util/fluent-collections
