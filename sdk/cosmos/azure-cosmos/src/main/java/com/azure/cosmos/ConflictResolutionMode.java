// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Conflict;
import com.azure.cosmos.implementation.StoredProcedure;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

public enum ConflictResolutionMode {
    /**
     * Last writer wins conflict resolution mode
     *
     * Setting the ConflictResolutionMode to "LAST_WRITER_WINS" indicates that conflict resolution should be done by
     * inspecting a field in the conflicting documents
     * and picking the document which has the higher value in that path. See
     * {@link ConflictResolutionPolicy#getConflictResolutionPath()} for details on how to specify the path
     *
     */
    LAST_WRITER_WINS("LastWriterWins"),

    /**
     * CUSTOM conflict resolution mode
     * <p>
     * Setting the ConflictResolutionMode to "CUSTOM" indicates that conflict resolution is custom handled by a user.
     * The user could elect to register a user specified {@link StoredProcedure} for handling conflicting resources.
     * Should the user not register a user specified StoredProcedure, conflicts will default to being made available
     * as {@link Conflict} resources,
     * which the user can inspect and manually resolve.
     * See {@link ConflictResolutionPolicy#getConflictResolutionProcedure()} for details on how to specify the stored
     * procedure
     * to run for conflict resolution.
     */
    CUSTOM("Custom"),

    /**
     * INVALID or unknown mode.
     */
    INVALID("Invalid");

    ConflictResolutionMode(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}

