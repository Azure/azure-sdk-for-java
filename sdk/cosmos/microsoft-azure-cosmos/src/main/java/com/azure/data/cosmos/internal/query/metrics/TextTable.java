// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query.metrics;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

class TextTable {
    private static final char CellLeftTop = '┌';
    private static final char CellRightTop = '┐';
    private static final char CellLeftBottom = '└';
    private static final char CellRightBottom = '┘';
    private static final char CellHorizontalJointTop = '┬';
    private static final char CellHorizontalJointBottom = '┴';
    private static final char CellVerticalJointLeft = '├';
    private static final char CellTJoint = '┼';
    private static final char CellVerticalJointRight = '┤';
    private static final char CellHorizontalLine = '-';
    private static final char CellVerticalLine = '│';

    private List<Column> columns;

    private String header;
    private String topLine;
    private String middleLine;
    private String bottomLine;

    private String rowFormatString;

    /**
     * Initializes a new instance of the TextTable class.
     *
     * @param columns The columns of the table
     */
    public TextTable(List<Column> columns) {
        this.columns = new ArrayList<>(columns);

        // Building the table header
        String headerFormatString = TextTable.buildLineFormatString(columns);
        this.header = String.format(headerFormatString, columns.stream().map(textTableColumn -> textTableColumn.columnName).toArray());

        // building the different lines
        this.topLine = TextTable.buildLine(CellLeftTop, CellRightTop, CellHorizontalJointTop, columns);
        this.middleLine = TextTable.buildLine(CellVerticalJointLeft, CellVerticalJointRight, CellTJoint, columns);
        this.bottomLine = TextTable.buildLine(CellLeftBottom, CellRightBottom, CellHorizontalJointBottom, columns);

        // building the row format string
        this.rowFormatString = TextTable.buildLineFormatString(columns);
    }

    public String getRow(List<Object> cells) {
        if (cells.size() != this.columns.size()) {
            throw new IllegalArgumentException("Cells in a row needs to have exactly 1 element per column");
        }
        return String.format(this.rowFormatString, cells.toArray());
    }

    private static String buildLine(char firstChar, char lastChar, char seperator, List<Column> columns) {
        StringBuilder lineStringBuilder = new StringBuilder();
        lineStringBuilder.append(firstChar);
        for (Column column : columns.subList(0, columns.size() - 1)) {
            lineStringBuilder.append(StringUtils.repeat(CellHorizontalLine, column.columnWidth));
            lineStringBuilder.append(seperator);
        }

        lineStringBuilder.append(StringUtils.repeat(CellHorizontalLine, columns.get(columns.size() - 1).columnWidth));
        lineStringBuilder.append(lastChar);

        return lineStringBuilder.toString();
    }

    private static String buildLineFormatString(List<Column> columns) {
        StringBuilder lineFormatStringBuilder = new StringBuilder();
        lineFormatStringBuilder.append(CellVerticalLine);
        for (Column column : columns) {
            lineFormatStringBuilder.append("%" + column.columnWidth + "s");
            lineFormatStringBuilder.append(CellVerticalLine);
        }

        return lineFormatStringBuilder.toString();
    }


    static class Column {
        String columnName;
        int columnWidth;

        public Column(String columnName, int columnWidth) {
            this.columnName = columnName;
            this.columnWidth = columnWidth;
        }
    }

    public String getHeader() {
        return header;
    }

    public String getTopLine() {
        return topLine;
    }

    public String getMiddleLine() {
        return middleLine;
    }

    public String getBottomLine() {
        return bottomLine;
    }
}
