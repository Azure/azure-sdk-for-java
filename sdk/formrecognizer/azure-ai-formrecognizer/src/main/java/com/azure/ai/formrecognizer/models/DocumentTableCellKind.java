// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for DocumentTableCellKind. */
public final class DocumentTableCellKind extends ExpandableStringEnum<DocumentTableCellKind> {
    /** Static value content for DocumentTableCellKind. */
    public static final DocumentTableCellKind CONTENT = fromString("content");

    /** Static value rowHeader for DocumentTableCellKind. */
    public static final DocumentTableCellKind ROW_HEADER = fromString("rowHeader");

    /** Static value columnHeader for DocumentTableCellKind. */
    public static final DocumentTableCellKind COLUMN_HEADER = fromString("columnHeader");

    /** Static value stubHead for DocumentTableCellKind. */
    public static final DocumentTableCellKind STUB_HEAD = fromString("stubHead");

    /** Static value description for DocumentTableCellKind. */
    public static final DocumentTableCellKind DESCRIPTION = fromString("description");

    /**
     * Creates or finds a DocumentTableCellKind from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding DocumentTableCellKind.
     */
    public static DocumentTableCellKind fromString(String name) {
        return fromString(name, DocumentTableCellKind.class);
    }

    /** @return known DocumentTableCellKind values. */
    public static Collection<DocumentTableCellKind> values() {
        return values(DocumentTableCellKind.class);
    }
}
