// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query.metrics;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

class TextTable {

    //  Added unicode characters using : https://www.unicode.org/charts/PDF/U2500.pdf
    private static final char CellLeftTop = '\u250C';
    private static final char CellRightTop = '\u2510';
    private static final char CellLeftBottom = '\u2514';
    private static final char CellRightBottom = '\u2518';
    private static final char CellHorizontalJointTop = '\u252C';
    private static final char CellHorizontalJointBottom = '\u2534';
    private static final char CellVerticalJointLeft = '\u251C';
    private static final char CellTJoint = '\u253C';
    private static final char CellVerticalJointRight = '\u2524';
    private static final char CellHorizontalLine = '\u2500';
    private static final char CellVerticalLine = '\u2502';

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
