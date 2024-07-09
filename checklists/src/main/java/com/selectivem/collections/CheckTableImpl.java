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
 *
 * Based on code which is:
 *
 * Copyright 2022-2024 floragunn GmbH
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
 *
 */

package com.selectivem.collections;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

class CheckTableImpl {

    static <R, C> CheckTable<R, C> create(R row, Set<C> columns) {
        int columnsSize = columns.size();

        if (columnsSize == 0) {
            throw new IllegalArgumentException("Must contain at least one column");
        } else if (columnsSize == 1) {
            return new CheckTableImpl.SingleCellCheckTable<>(
                    row,
                    columns.iterator().next(),
                    IndexedImmutableSetImpl.of(row),
                    IndexedImmutableSetImpl.of(columns));
        } else {
            return new CheckTableImpl.SingleRowCheckTable<>(row, columns);
        }
    }

    static <R, C> CheckTable<R, C> create(Set<R> rows, C column) {
        int rowsSize = rows.size();

        if (rowsSize == 0) {
            throw new IllegalArgumentException("Must contain at least one row");
        } else if (rowsSize == 1) {
            return new CheckTableImpl.SingleCellCheckTable<>(
                    rows.iterator().next(),
                    column,
                    IndexedImmutableSetImpl.of(rows),
                    IndexedImmutableSetImpl.of(column));
        } else {
            return new CheckTableImpl.SingleColumnCheckTable<R, C>(rows, column);
        }
    }

    static <R, C> CheckTable<R, C> create(Set<R> rows, Set<C> columns) {
        int rowsSize = rows.size();
        int columnsSize = columns.size();

        if (rowsSize == 0 || columnsSize == 0) {
            throw new IllegalArgumentException(
                    "Must contain at least one column and at least one row (got " + rows + "/" + columns + ")");
        } else if (rowsSize == 1) {
            if (columnsSize == 1) {
                return new CheckTableImpl.SingleCellCheckTable<>(
                        rows.iterator().next(),
                        columns.iterator().next(),
                        IndexedImmutableSetImpl.of(rows),
                        IndexedImmutableSetImpl.of(columns));
            } else {
                return new CheckTableImpl.SingleRowCheckTable<>(rows.iterator().next(), columns);
            }
        } else if (columnsSize == 1) {
            return new CheckTableImpl.SingleColumnCheckTable<>(
                    rows, columns.iterator().next());
        } else {
            return new CheckTableImpl.ArrayCheckTable<>(rows, columns);
        }
    }

    static final class SingleCellCheckTable<R, C> extends AbstractCheckTable<R, C> {
        private final R row;
        private final C column;
        private final Set<R> rowSet;
        private final Set<C> columnSet;
        private boolean checked = false;

        SingleCellCheckTable(R row, C column, IndexedImmutableSetImpl<R> rowSet, IndexedImmutableSetImpl<C> columnSet) {
            this.row = row;
            this.column = column;
            this.rowSet = rowSet;
            this.columnSet = columnSet;
        }

        @Override
        public boolean check(R row, C column) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            if (!checked) {
                checked = true;
            }

            return true;
        }

        @Override
        public void uncheck(R row, C column) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            if (checked) {
                checked = false;
            }
        }

        @Override
        public boolean isComplete() {
            return checked;
        }

        @Override
        public boolean isBlank() {
            return !checked;
        }

        @Override
        public boolean checkIf(R row, Predicate<C> columnCheckPredicate) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            if (checked) {
                return true;
            }

            if (columnCheckPredicate.test(column)) {
                checked = true;
            }

            return checked;
        }

        @Override
        public boolean checkIf(Predicate<R> rowCheckPredicate, C column) {
            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            if (checked) {
                return true;
            }

            if (rowCheckPredicate.test(row)) {
                checked = true;
            }

            return checked;
        }

        @Override
        public void uncheckIf(R row, Predicate<C> columnCheckPredicate) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            if (checked && columnCheckPredicate.test(column)) {
                checked = false;
            }
        }

        @Override
        public void uncheckIf(Predicate<R> rowCheckPredicate, C column) {
            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            if (checked && rowCheckPredicate.test(row)) {
                checked = false;
            }
        }

        @Override
        public boolean isChecked(R row, C column) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            return checked;
        }

        @Override
        public boolean isRowComplete(R row) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            return checked;
        }

        @Override
        public boolean isColumnComplete(C column) {
            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            return checked;
        }

        @Override
        public String toString(String checkedIndicator, String uncheckedIndicator) {
            return row + "/" + column + ": " + (checked ? checkedIndicator : uncheckedIndicator);
        }

        @Override
        public String toTableString(String checkedIndicator, String uncheckedIndicator) {
            StringBuilder result = new StringBuilder();

            int rowHeaderWidth = row.toString().length() + 1;

            appendPadded("", rowHeaderWidth, ' ', result);

            result.append("|");

            String columnLabel = column.toString();
            if (columnLabel.length() > STRING_TABLE_HEADER_WIDTH) {
                columnLabel = columnLabel.substring(0, STRING_TABLE_HEADER_WIDTH);
            }
            int columnWidth = columnLabel.length();

            result.append(" ").append(columnLabel).append(" |");

            result.append("\n");

            result.append(row.toString());
            result.append(" |");

            String v = checked ? checkedIndicator : uncheckedIndicator;

            result.append(" ");
            appendPadded(v, columnWidth, ' ', result);
            result.append(" |");

            return result.toString();
        }

        @Override
        public Set<R> getCompleteRows() {
            if (checked) {
                return rowSet;
            } else {
                return IndexedImmutableSetImpl.empty();
            }
        }

        @Override
        public Set<C> getCompleteColumns() {
            if (checked) {
                return columnSet;
            } else {
                return IndexedImmutableSetImpl.empty();
            }
        }

        @Override
        public Set<R> getIncompleteRows() {
            if (checked) {
                return IndexedImmutableSetImpl.empty();
            } else {
                return rowSet;
            }
        }

        @Override
        public Set<C> getIncompleteColumns() {
            if (checked) {
                return IndexedImmutableSetImpl.empty();
            } else {
                return columnSet;
            }
        }

        @Override
        public Set<R> getCheckedRows(C column) {
            return getCompleteRows();
        }

        @Override
        public Set<C> getCheckedColumns(R row) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            return getCompleteColumns();
        }

        @Override
        public Set<R> getRows() {
            return rowSet;
        }

        @Override
        public Set<C> getColumns() {
            return columnSet;
        }

        @Override
        public void uncheckAll() {
            checked = false;
        }

        @Override
        public void uncheckRowIf(Predicate<R> rowCheckPredicate) {
            if (rowCheckPredicate.test(row)) {
                checked = false;
            }
        }

        @Override
        public void uncheckRow(R row) {
            if (this.row.equals(row)) {
                checked = false;
            } else {
                throw new IllegalArgumentException("Invalid row: " + row);
            }
        }

        @Override
        public void uncheckRowIfPresent(R row) {
            if (this.row.equals(row)) {
                checked = false;
            }
        }

        @Override
        public Iterable<R> iterateUncheckedRows(C column) {
            if (this.column.equals(column)) {
                if (checked) {
                    return IndexedImmutableSetImpl.empty();
                } else {
                    return rowSet;
                }
            } else {
                throw new IllegalArgumentException("Invalid column: " + column);
            }
        }

        @Override
        public Iterable<C> iterateUncheckedColumns(R row) {
            if (this.row.equals(row)) {
                if (checked) {
                    return IndexedImmutableSetImpl.empty();
                } else {
                    return columnSet;
                }
            } else {
                throw new IllegalArgumentException("Invalid row: " + row);
            }
        }

        @Override
        public Iterable<R> iterateCheckedRows(C column) {
            if (this.column.equals(column)) {
                if (checked) {
                    return rowSet;
                } else {
                    return IndexedImmutableSetImpl.empty();
                }
            } else {
                throw new IllegalArgumentException("Invalid column: " + column);
            }
        }

        @Override
        public Iterable<C> iterateCheckedColumns(R row) {
            if (this.row.equals(row)) {
                if (checked) {
                    return columnSet;
                } else {
                    return IndexedImmutableSetImpl.empty();
                }
            } else {
                throw new IllegalArgumentException("Invalid row: " + row);
            }
        }

        @Override
        public boolean containsCellFor(R row, C column) {
            return this.row.equals(row) && this.column.equals(column);
        }
    }

    static final class SingleRowCheckTable<R, C> extends AbstractCheckTable<R, C> {
        private final R row;
        private final CheckList<C> columns;

        SingleRowCheckTable(R row, Set<C> columns) {
            this.row = row;
            this.columns = CheckListImpl.create(columns, "column");
        }

        @Override
        public boolean check(R row, C column) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            return this.columns.check(column);
        }

        @Override
        public void uncheck(R row, C column) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            this.columns.uncheck(column);
        }

        @Override
        public boolean isComplete() {
            return this.columns.isComplete();
        }

        @Override
        public boolean isBlank() {
            return this.columns.isBlank();
        }

        @Override
        public boolean checkIf(R row, Predicate<C> columnCheckPredicate) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            return columns.checkIf(columnCheckPredicate);
        }

        @Override
        public boolean checkIf(Predicate<R> rowCheckPredicate, C column) {
            if (isComplete()) {
                return true;
            }

            if (rowCheckPredicate.test(row)) {
                return columns.check(column);
            } else {
                return false;
            }
        }

        @Override
        public void uncheckIf(R row, Predicate<C> columnCheckPredicate) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            columns.uncheckIf(columnCheckPredicate);
        }

        @Override
        public void uncheckIf(Predicate<R> rowCheckPredicate, C column) {
            if (rowCheckPredicate.test(row)) {
                columns.uncheck(column);
            }
        }

        @Override
        public boolean isChecked(R row, C column) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            return columns.isChecked(column);
        }

        @Override
        public boolean isRowComplete(R row) {
            if (!row.equals(this.row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            return isComplete();
        }

        @Override
        public boolean isColumnComplete(C column) {
            return this.columns.isChecked(column);
        }

        @Override
        public String toTableString(String checkedIndicator, String uncheckedIndicator) {
            StringBuilder result = new StringBuilder();

            int rowHeaderWidth = row.toString().length() + 1;

            appendPadded("", rowHeaderWidth, ' ', result);
            result.append("|");

            int[] columnWidth = new int[this.columns.size()];

            int i = 0;
            for (C column : columns.getElements()) {
                String columnLabel = column.toString();

                if (columnLabel.length() > STRING_TABLE_HEADER_WIDTH) {
                    columnLabel = columnLabel.substring(0, STRING_TABLE_HEADER_WIDTH);
                }

                columnWidth[i] = columnLabel.length();
                i++;
                result.append(" ").append(columnLabel).append(" |");
            }

            result.append("\n");

            result.append(row.toString());
            result.append(" |");

            i = 0;
            for (C column : columns.getElements()) {
                String v = columns.isChecked(column) ? checkedIndicator : uncheckedIndicator;

                result.append(" ");
                appendPadded(v, columnWidth[i], ' ', result);
                result.append(" |");
                i++;
            }

            return result.toString();
        }

        @Override
        public String toString(String checkedIndicator, String uncheckedIndicator) {
            return toTableString(checkedIndicator, uncheckedIndicator);
        }

        @Override
        public Set<R> getCompleteRows() {
            if (isComplete()) {
                return IndexedImmutableSetImpl.of(row);
            } else {
                return IndexedImmutableSetImpl.empty();
            }
        }

        @Override
        public Set<C> getCompleteColumns() {
            return columns.getCheckedElements();
        }

        @Override
        public Set<R> getIncompleteRows() {
            if (isComplete()) {
                return IndexedImmutableSetImpl.empty();
            } else {
                return IndexedImmutableSetImpl.of(row);
            }
        }

        @Override
        public Set<C> getIncompleteColumns() {
            return columns.getUncheckedElements();
        }

        @Override
        public Set<R> getRows() {
            return IndexedImmutableSetImpl.of(row);
        }

        @Override
        public Set<C> getColumns() {
            return columns.getElements();
        }

        @Override
        public void uncheckAll() {
            columns.uncheckAll();
        }

        @Override
        public void uncheckRowIf(Predicate<R> rowCheckPredicate) {
            if (rowCheckPredicate.test(row)) {
                columns.uncheckAll();
            }
        }

        @Override
        public void uncheckRow(R row) {
            if (this.row.equals(row)) {
                columns.uncheckAll();
            } else {
                throw new IllegalArgumentException("Invalid row: " + row);
            }
        }

        @Override
        public void uncheckRowIfPresent(R row) {
            if (this.row.equals(row)) {
                columns.uncheckAll();
            }
        }

        @Override
        public Iterable<R> iterateUncheckedRows(C column) {
            if (columns.isChecked(column)) {
                return IndexedImmutableSetImpl.empty();
            } else {
                return IndexedImmutableSetImpl.of(row);
            }
        }

        @Override
        public Iterable<C> iterateUncheckedColumns(R row) {
            if (this.row.equals(row)) {
                return columns.iterateUncheckedElements();
            } else {
                throw new IllegalArgumentException("Invalid row: " + row);
            }
        }

        @Override
        public Set<R> getCheckedRows(C column) {
            if (columns.isChecked(column)) {
                return IndexedImmutableSetImpl.of(row);
            } else {
                return IndexedImmutableSetImpl.empty();
            }
        }

        @Override
        public Set<C> getCheckedColumns(R row) {
            if (this.row.equals(row)) {
                return columns.getCheckedElements();
            } else {
                throw new IllegalArgumentException("Invalid row: " + row);
            }
        }

        @Override
        public Iterable<R> iterateCheckedRows(C column) {
            return getCheckedRows(column);
        }

        @Override
        public Iterable<C> iterateCheckedColumns(R row) {
            if (this.row.equals(row)) {
                return columns.iterateCheckedElements();
            } else {
                throw new IllegalArgumentException("Invalid row: " + row);
            }
        }

        @Override
        public boolean containsCellFor(R row, C column) {
            return this.row.equals(row) && this.columns.getElements().contains(column);
        }
    }

    static final class SingleColumnCheckTable<R, C> extends AbstractCheckTable<R, C> {
        private final C column;
        private final CheckList<R> rows;

        SingleColumnCheckTable(Set<R> rows, C column) {
            this.column = column;
            this.rows = CheckListImpl.create(rows, "row");
        }

        @Override
        public boolean check(R row, C column) {
            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            return rows.check(row);
        }

        @Override
        public void uncheck(R row, C column) {
            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            rows.uncheck(row);
        }

        @Override
        public boolean checkIf(R row, Predicate<C> columnCheckPredicate) {
            if (isComplete()) {
                return true;
            }

            if (columnCheckPredicate.test(column)) {
                return rows.check(row);
            } else {
                return false;
            }
        }

        @Override
        public boolean checkIf(Predicate<R> rowCheckPredicate, C column) {
            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            return rows.checkIf(rowCheckPredicate);
        }

        @Override
        public void uncheckIf(R row, Predicate<C> columnCheckPredicate) {
            if (!rows.getElements().contains(row)) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            if (rows.isChecked(row) && columnCheckPredicate.test(column)) {
                rows.uncheck(row);
            }
        }

        @Override
        public void uncheckIf(Predicate<R> rowCheckPredicate, C column) {
            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            rows.uncheckIf(rowCheckPredicate);
        }

        @Override
        public boolean isChecked(R row, C column) {
            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            return rows.isChecked(row);
        }

        @Override
        public boolean isRowComplete(R row) {
            return this.rows.isChecked(row);
        }

        @Override
        public boolean isColumnComplete(C column) {
            if (!column.equals(this.column)) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            return isComplete();
        }

        @Override
        public String toTableString(String checkedIndicator, String uncheckedIndicator) {
            StringBuilder result = new StringBuilder();

            int rowHeaderWidth = rows.getElements().stream()
                    .map((r) -> r.toString().length())
                    .max(Comparator.naturalOrder())
                    .get();

            appendPadded("", rowHeaderWidth, ' ', result);
            result.append("|");

            String columnLabel = column.toString();
            int columnWidth = columnLabel.length();
            result.append(" ").append(columnLabel).append(" |");
            result.append("\n");

            for (R row : rows.getElements()) {
                appendPadded(row.toString(), rowHeaderWidth, ' ', result);
                result.append("|");
                String v = this.rows.isChecked(row) ? checkedIndicator : uncheckedIndicator;

                result.append(" ");
                appendPadded(v, columnWidth, ' ', result);
                result.append(" |\n");
            }

            return result.toString();
        }

        @Override
        public boolean isComplete() {
            return this.rows.isComplete();
        }

        @Override
        public boolean isBlank() {
            return this.rows.isBlank();
        }

        @Override
        public Set<R> getCompleteRows() {
            return this.rows.getCheckedElements();
        }

        @Override
        public Set<C> getCompleteColumns() {
            if (isComplete()) {
                return IndexedImmutableSetImpl.of(column);
            } else {
                return IndexedImmutableSetImpl.empty();
            }
        }

        @Override
        public Set<R> getIncompleteRows() {
            return this.rows.getUncheckedElements();
        }

        @Override
        public Set<C> getIncompleteColumns() {
            if (isComplete()) {
                return IndexedImmutableSetImpl.empty();
            } else {
                return IndexedImmutableSetImpl.of(column);
            }
        }

        @Override
        public Set<R> getRows() {
            return this.rows.getElements();
        }

        @Override
        public Set<C> getColumns() {
            return IndexedImmutableSetImpl.of(column);
        }

        @Override
        public void uncheckAll() {
            rows.uncheckAll();
        }

        @Override
        public void uncheckRowIf(Predicate<R> rowCheckPredicate) {
            this.rows.uncheckIf(rowCheckPredicate);
        }

        @Override
        public void uncheckRow(R row) {
            this.rows.uncheck(row);
        }

        @Override
        public void uncheckRowIfPresent(R row) {
            this.rows.uncheckIfPresent(row);
        }

        @Override
        public Set<R> getCheckedRows(C column) {
            if (this.column.equals(column)) {
                return rows.getCheckedElements();
            } else {
                throw new IllegalArgumentException("Invalid column: " + column);
            }
        }

        @Override
        public Set<C> getCheckedColumns(R row) {
            if (rows.isChecked(row)) {
                return IndexedImmutableSetImpl.of(column);
            } else {
                return IndexedImmutableSetImpl.empty();
            }
        }

        @Override
        public Iterable<R> iterateCheckedRows(C column) {
            if (this.column.equals(column)) {
                return rows.iterateCheckedElements();
            } else {
                throw new IllegalArgumentException("Invalid column: " + column);
            }
        }

        @Override
        public Iterable<C> iterateCheckedColumns(R row) {
            return getCheckedColumns(row);
        }

        @Override
        public Iterable<R> iterateUncheckedRows(C column) {
            if (this.column.equals(column)) {
                return rows.iterateUncheckedElements();
            } else {
                throw new IllegalArgumentException("Invalid column: " + column);
            }
        }

        @Override
        public Iterable<C> iterateUncheckedColumns(R row) {
            if (rows.isChecked(row)) {
                return IndexedImmutableSetImpl.empty();
            } else {
                return IndexedImmutableSetImpl.of(column);
            }
        }

        @Override
        public boolean containsCellFor(R row, C column) {
            return this.column.equals(column) && this.rows.getElements().contains(row);
        }
    }

    static final class ArrayCheckTable<R, C> extends AbstractCheckTable<R, C> {
        private final IndexedImmutableSetImpl<R> rows;
        private final IndexedImmutableSetImpl<C> columns;

        private final boolean[] table;
        private int checkedCount = 0;
        private int uncheckedCount;
        private final int size;
        private final int rowCount;
        private final int columnCount;

        ArrayCheckTable(Set<R> rows, Set<C> columns) {
            this.rows = IndexedImmutableSetImpl.of(rows);
            this.columns = IndexedImmutableSetImpl.of(columns);
            this.size = this.rows.size() * this.columns.size();
            this.table = new boolean[this.size];
            this.rowCount = this.rows.size();
            this.columnCount = this.columns.size();
            this.uncheckedCount = size;
        }

        private int tableIndex(int rowIndex, int columnIndex) {
            return rowIndex + columnIndex * this.rowCount;
        }

        @Override
        public boolean check(R row, C column) {

            int rowIndex = rows.elementToIndex(row);

            if (rowIndex == -1) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            int columnIndex = columns.elementToIndex(column);

            if (columnIndex == -1) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            int i = tableIndex(rowIndex, columnIndex);

            if (!this.table[i]) {
                this.table[i] = true;
                this.checkedCount++;
                this.uncheckedCount--;
            }

            return this.uncheckedCount == 0;
        }

        @Override
        public void uncheck(R row, C column) {

            int rowIndex = rows.elementToIndex(row);

            if (rowIndex == -1) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            int columnIndex = columns.elementToIndex(column);

            if (columnIndex == -1) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            int i = tableIndex(rowIndex, columnIndex);

            if (this.table[i]) {
                this.table[i] = false;
                this.checkedCount--;
                this.uncheckedCount++;
            }
        }

        @Override
        public void uncheckAll() {
            this.checkedCount = 0;
            this.uncheckedCount = this.size;
            Arrays.fill(this.table, false);
        }

        @Override
        public void uncheckRowIf(Predicate<R> rowCheckPredicate) {
            if (isBlank()) {
                return;
            }

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                R row = this.rows.indexToElement(rowIndex);

                if (rowCheckPredicate.test(row)) {
                    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                        int i = tableIndex(rowIndex, columnIndex);

                        if (this.table[i]) {
                            this.table[i] = false;
                            this.checkedCount--;
                            this.uncheckedCount++;

                            if (this.checkedCount == 0) {
                                return;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void uncheckRow(R row) {
            int rowIndex = rows.elementToIndex(row);

            if (rowIndex == -1) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            if (isBlank()) {
                return;
            }

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                int i = tableIndex(rowIndex, columnIndex);

                if (this.table[i]) {
                    this.table[i] = false;
                    this.checkedCount--;
                    this.uncheckedCount++;

                    if (this.checkedCount == 0) {
                        return;
                    }
                }
            }
        }

        @Override
        public void uncheckRowIfPresent(R row) {
            int rowIndex = rows.elementToIndex(row);

            if (rowIndex == -1) {
                return;
            }

            if (isBlank()) {
                return;
            }

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                int i = tableIndex(rowIndex, columnIndex);

                if (this.table[i]) {
                    this.table[i] = false;
                    this.checkedCount--;
                    this.uncheckedCount++;

                    if (this.checkedCount == 0) {
                        return;
                    }
                }
            }
        }

        @Override
        public boolean isComplete() {
            return this.uncheckedCount == 0;
        }

        @Override
        public boolean isBlank() {
            return this.checkedCount == 0;
        }

        @Override
        public boolean isChecked(R row, C column) {
            int rowIndex = rows.elementToIndex(row);

            if (rowIndex == -1) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            int columnIndex = columns.elementToIndex(column);

            if (columnIndex == -1) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            int i = tableIndex(rowIndex, columnIndex);

            return this.table[i];
        }

        @Override
        public boolean checkIf(R row, Predicate<C> columnCheckPredicate) {
            int rowIndex = rows.elementToIndex(row);

            if (rowIndex == -1) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                int i = tableIndex(rowIndex, columnIndex);

                if (!this.table[i]) {
                    if (columnCheckPredicate.test(this.columns.indexToElement(columnIndex))) {
                        this.table[i] = true;
                        this.checkedCount++;
                        this.uncheckedCount--;

                        if (this.uncheckedCount == 0) {
                            return true;
                        }
                    }
                }
            }

            return this.uncheckedCount == 0;
        }

        @Override
        public boolean checkIf(Predicate<R> rowCheckPredicate, C column) {
            int columnIndex = columns.elementToIndex(column);

            if (columnIndex == -1) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                int i = tableIndex(rowIndex, columnIndex);

                if (!this.table[i]) {
                    if (rowCheckPredicate.test(this.rows.indexToElement(rowIndex))) {
                        this.table[i] = true;
                        this.checkedCount++;
                        this.uncheckedCount--;

                        if (this.uncheckedCount == 0) {
                            return true;
                        }
                    }
                }
            }

            return this.uncheckedCount == 0;
        }

        @Override
        public Iterable<R> iterateUncheckedRows(C column) {
            int columnIndex = columns.elementToIndex(column);

            if (columnIndex == -1) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            if (this.uncheckedCount == 0) {
                return IndexedImmutableSetImpl.empty();
            }

            return new Iterable<R>() {
                @Override
                public Iterator<R> iterator() {
                    return new Iterator<R>() {

                        int rowIndex = findNext(0);

                        @Override
                        public boolean hasNext() {
                            if (rowIndex != -1) {
                                return true;
                            } else {
                                return false;
                            }
                        }

                        @Override
                        public R next() {
                            R result = rows.indexToElement(rowIndex);
                            rowIndex = findNext(rowIndex + 1);
                            return result;
                        }

                        int findNext(int start) {
                            for (int rowIndex = start; rowIndex < rowCount; rowIndex++) {
                                int i = tableIndex(rowIndex, columnIndex);

                                if (!table[i]) {
                                    return rowIndex;
                                }
                            }

                            return -1;
                        }
                    };
                }
            };
        }

        @Override
        public Iterable<C> iterateUncheckedColumns(R row) {
            int rowIndex = rows.elementToIndex(row);

            if (rowIndex == -1) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            if (this.uncheckedCount == 0) {
                return IndexedImmutableSetImpl.empty();
            }

            return new Iterable<C>() {
                @Override
                public Iterator<C> iterator() {
                    return new Iterator<C>() {

                        int columnIndex = findNext(0);

                        @Override
                        public boolean hasNext() {
                            if (columnIndex != -1) {
                                return true;
                            } else {
                                return false;
                            }
                        }

                        @Override
                        public C next() {
                            C result = columns.indexToElement(columnIndex);
                            columnIndex = findNext(columnIndex + 1);
                            return result;
                        }

                        int findNext(int start) {
                            for (int columnIndex = start; columnIndex < columnCount; columnIndex++) {
                                int i = tableIndex(rowIndex, columnIndex);

                                if (!table[i]) {
                                    return columnIndex;
                                }
                            }

                            return -1;
                        }
                    };
                }
            };
        }

        @Override
        public Iterable<R> iterateCheckedRows(C column) {
            int columnIndex = columns.elementToIndex(column);

            if (columnIndex == -1) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            if (this.uncheckedCount == 0) {
                return rows;
            }

            return new Iterable<R>() {
                @Override
                public Iterator<R> iterator() {
                    return new Iterator<R>() {

                        int rowIndex = findNext(0);

                        @Override
                        public boolean hasNext() {
                            if (rowIndex != -1) {
                                return true;
                            } else {
                                return false;
                            }
                        }

                        @Override
                        public R next() {
                            R result = rows.indexToElement(rowIndex);
                            rowIndex = findNext(rowIndex + 1);
                            return result;
                        }

                        int findNext(int start) {
                            for (int rowIndex = start; rowIndex < rowCount; rowIndex++) {
                                int i = tableIndex(rowIndex, columnIndex);

                                if (table[i]) {
                                    return rowIndex;
                                }
                            }

                            return -1;
                        }
                    };
                }
            };
        }

        @Override
        public Iterable<C> iterateCheckedColumns(R row) {
            int rowIndex = rows.elementToIndex(row);

            if (rowIndex == -1) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            if (this.uncheckedCount == 0) {
                return columns;
            }

            return new Iterable<C>() {
                @Override
                public Iterator<C> iterator() {
                    return new Iterator<C>() {

                        int columnIndex = findNext(0);

                        @Override
                        public boolean hasNext() {
                            if (columnIndex != -1) {
                                return true;
                            } else {
                                return false;
                            }
                        }

                        @Override
                        public C next() {
                            C result = columns.indexToElement(columnIndex);
                            columnIndex = findNext(columnIndex + 1);
                            return result;
                        }

                        int findNext(int start) {
                            for (int columnIndex = start; columnIndex < columnCount; columnIndex++) {
                                int i = tableIndex(rowIndex, columnIndex);

                                if (table[i]) {
                                    return columnIndex;
                                }
                            }

                            return -1;
                        }
                    };
                }
            };
        }

        @Override
        public void uncheckIf(R row, Predicate<C> columnCheckPredicate) {
            int rowIndex = rows.elementToIndex(row);

            if (rowIndex == -1) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            if (this.checkedCount == 0) {
                return;
            }

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                int i = tableIndex(rowIndex, columnIndex);

                if (this.table[i]) {
                    if (columnCheckPredicate.test(this.columns.indexToElement(columnIndex))) {
                        this.table[i] = false;
                        this.checkedCount--;
                        this.uncheckedCount++;

                        if (this.checkedCount == 0) {
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public void uncheckIf(Predicate<R> rowCheckPredicate, C column) {
            int columnIndex = columns.elementToIndex(column);

            if (columnIndex == -1) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            if (this.checkedCount == 0) {
                return;
            }

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                int i = tableIndex(rowIndex, columnIndex);

                if (this.table[i]) {
                    if (rowCheckPredicate.test(this.rows.indexToElement(rowIndex))) {
                        this.table[i] = false;
                        this.checkedCount--;
                        this.uncheckedCount++;

                        if (this.checkedCount == 0) {
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public String toTableString(String checkedIndicator, String uncheckedIndicator) {
            StringBuilder result = new StringBuilder();

            int rowHeaderWidth = rows.stream()
                    .map((r) -> r.toString().length())
                    .max(Comparator.naturalOrder())
                    .get();

            appendPadded("", rowHeaderWidth, ' ', result);
            result.append("|");

            int[] columnWidth = new int[columns.size()];

            int i = 0;
            for (C column : columns) {
                String columnLabel = column.toString();

                if (columnLabel.length() > STRING_TABLE_HEADER_WIDTH) {
                    columnLabel = columnLabel.substring(0, STRING_TABLE_HEADER_WIDTH);
                }

                columnWidth[i] = columnLabel.length();
                i++;
                result.append(" ").append(columnLabel).append(" |");
            }

            result.append("\n");

            for (R row : rows) {
                appendPadded(row.toString(), rowHeaderWidth, ' ', result);
                result.append("|");

                i = 0;
                for (C column : columns) {
                    String v = isChecked(row, column) ? checkedIndicator : uncheckedIndicator;

                    result.append(" ");
                    appendPadded(v, columnWidth[i], ' ', result);
                    result.append(" |");
                    i++;
                }
                result.append("\n");
            }

            return result.toString();
        }

        @Override
        public Set<R> getCompleteRows() {
            if (isBlank()) {
                return IndexedImmutableSetImpl.empty();
            }

            if (isComplete()) {
                return rows;
            }

            int estimatedRows = Math.min(checkedCount / columnCount + 12, rowCount - 1);
            IndexedImmutableSetImpl.InternalBuilder<R> builder = IndexedImmutableSetImpl.builder(estimatedRows);

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                if (isRowCompleted(rowIndex)) {
                    R row = rows.indexToElement(rowIndex);
                    builder = builder.with(row);
                }
            }

            return builder.build();
        }

        @Override
        public Set<C> getCompleteColumns() {
            if (isBlank()) {
                return IndexedImmutableSetImpl.empty();
            }

            if (isComplete()) {
                return columns;
            }

            int estimatedColumns = Math.min(checkedCount / rowCount + 12, columnCount - 1);
            IndexedImmutableSetImpl.InternalBuilder<C> builder = IndexedImmutableSetImpl.builder(estimatedColumns);

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                if (isColumnCompleted(columnIndex)) {
                    C column = columns.indexToElement(columnIndex);
                    builder = builder.with(column);
                }
            }

            return builder.build();
        }

        @Override
        public Set<R> getIncompleteRows() {
            if (isComplete()) {
                return IndexedImmutableSetImpl.empty();
            }

            if (isBlank()) {
                return rows;
            }

            int estimatedRows = Math.min(uncheckedCount / columnCount + 12, rowCount - 1);
            IndexedImmutableSetImpl.InternalBuilder<R> builder = IndexedImmutableSetImpl.builder(estimatedRows);

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                if (!isRowCompleted(rowIndex)) {
                    R row = rows.indexToElement(rowIndex);
                    builder = builder.with(row);
                }
            }

            return builder.build();
        }

        @Override
        public Set<C> getIncompleteColumns() {
            if (isComplete()) {
                return IndexedImmutableSetImpl.empty();
            }

            if (isBlank()) {
                return columns;
            }

            int estimatedColumns = Math.min(uncheckedCount / rowCount + 12, columnCount - 1);
            IndexedImmutableSetImpl.InternalBuilder<C> builder = IndexedImmutableSetImpl.builder(estimatedColumns);

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                if (!isColumnCompleted(columnIndex)) {
                    C column = columns.indexToElement(columnIndex);
                    builder = builder.with(column);
                }
            }

            return builder.build();
        }

        @Override
        public boolean isRowComplete(R row) {
            int rowIndex = rows.elementToIndex(row);

            if (rowIndex == -1) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            return isRowCompleted(rowIndex);
        }

        @Override
        public boolean isColumnComplete(C column) {
            int columnIndex = columns.elementToIndex(column);

            if (columnIndex == -1) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            return isColumnCompleted(columnIndex);
        }

        @Override
        public Set<R> getCheckedRows(C column) {
            int columnIndex = columns.elementToIndex(column);

            if (columnIndex == -1) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            if (checkedCount == 0) {
                return IndexedImmutableSetImpl.empty();
            }

            IndexedImmutableSetImpl.InternalBuilder<R> builder = IndexedImmutableSetImpl.builder(rowCount);

            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                int i = tableIndex(rowIndex, columnIndex);

                if (this.table[i]) {
                    builder = builder.with(rows.indexToElement(rowIndex));
                }
            }

            return builder.build();
        }

        @Override
        public Set<C> getCheckedColumns(R row) {
            int rowIndex = rows.elementToIndex(row);

            if (rowIndex == -1) {
                throw new IllegalArgumentException("Invalid row: " + row);
            }

            if (checkedCount == 0) {
                return IndexedImmutableSetImpl.empty();
            }

            IndexedImmutableSetImpl.InternalBuilder<C> builder = IndexedImmutableSetImpl.builder(columnCount);

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                int i = tableIndex(rowIndex, columnIndex);

                if (this.table[i]) {
                    builder = builder.with(columns.indexToElement(columnIndex));
                }
            }

            return builder.build();
        }

        private boolean isRowCompleted(int rowIndex) {
            int start = tableIndex(rowIndex, 0);
            int end = tableIndex(rowIndex, columnCount - 1);
            int inc = this.rowCount;

            for (int i = start; i <= end; i += inc) {
                if (!this.table[i]) {
                    return false;
                }
            }

            return true;
        }

        private boolean isColumnCompleted(int columnIndex) {
            int start = tableIndex(0, columnIndex);
            int end = tableIndex(rowCount - 1, columnIndex);

            for (int i = start; i <= end; i++) {
                if (!this.table[i]) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public Set<R> getRows() {
            return rows;
        }

        @Override
        public Set<C> getColumns() {
            return columns;
        }

        @Override
        public boolean containsCellFor(R row, C column) {
            return this.rows.contains(row) && this.columns.contains(column);
        }
    }

    abstract static class AbstractCheckTable<R, C> implements CheckTable<R, C> {

        static final int STRING_TABLE_HEADER_WIDTH = 40;

        @Override
        public boolean checkIf(Iterable<R> rows, Predicate<C> columnCheckPredicate) {

            Iterator<R> iter = rows.iterator();

            while (iter.hasNext()) {
                R row = iter.next();

                if (checkIf(row, columnCheckPredicate)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public void uncheckIf(Iterable<R> rows, Predicate<C> columnCheckPredicate) {
            if (isBlank()) {
                return;
            }

            Iterator<R> iter = rows.iterator();

            while (iter.hasNext()) {
                R row = iter.next();

                uncheckIf(row, columnCheckPredicate);

                if (isBlank()) {
                    return;
                }
            }
        }

        @Override
        public void uncheckIf(Predicate<R> rowCheckPredicate, Iterable<C> columns) {
            if (isBlank()) {
                return;
            }

            Iterator<C> iter = columns.iterator();

            while (iter.hasNext()) {
                C column = iter.next();

                uncheckIf(rowCheckPredicate, column);

                if (isBlank()) {
                    return;
                }
            }
        }

        @Override
        public boolean isRowIncomplete(R row) {
            return !isRowComplete(row);
        }

        @Override
        public boolean isColumnIncomplete(C column) {
            return !isColumnComplete(column);
        }

        @Override
        public String toString() {
            return toString("x", "");
        }

        @Override
        public String toTableString() {
            return toTableString("x", "");
        }

        @Override
        public String toString(String checkedIndicator, String uncheckedIndicator) {
            return toTableString(checkedIndicator, uncheckedIndicator);
        }

        static void appendPadded(String string, int width, char paddingChar, StringBuilder resultBuilder) {
            resultBuilder.append(string);

            int length = string.length();

            while (length < width) {
                resultBuilder.append(paddingChar);
                length++;
            }
        }
    }
}
