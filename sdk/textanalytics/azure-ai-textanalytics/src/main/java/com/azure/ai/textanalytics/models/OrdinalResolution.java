// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.OrdinalResolutionPropertiesHelper;
import com.azure.core.annotation.Immutable;

/** A resolution for ordinal numbers entity instances. */
@Immutable
public final class OrdinalResolution extends BaseResolution {
    /*
     * The offset With respect to the reference (e.g., offset = -1 in "show me the second to last"
     */
    private String offset;

    /*
     * The reference point that the ordinal number denotes.
     */
    private RelativeTo relativeTo;

    /*
     * A simple arithmetic expression that the ordinal denotes.
     */
    private String value;

    static {
        OrdinalResolutionPropertiesHelper.setAccessor(
            new OrdinalResolutionPropertiesHelper.OrdinalResolutionAccessor() {
                @Override
                public void setOffset(OrdinalResolution ordinalResolution, String offset) {
                    ordinalResolution.setOffset(offset);
                }

                @Override
                public void setRelativeTo(OrdinalResolution ordinalResolution, RelativeTo relativeTo) {
                    ordinalResolution.setRelativeTo(relativeTo);
                }

                @Override
                public void setValue(OrdinalResolution ordinalResolution, String value) {
                    ordinalResolution.setValue(value);
                }
            });
    }

    /**
     * Get the offset property: The offset With respect to the reference (e.g., offset = -1 in "show me the second to
     * last".
     *
     * @return the offset value.
     */
    public String getOffset() {
        return this.offset;
    }

    /**
     * Get the relativeTo property: The reference point that the ordinal number denotes.
     *
     * @return the relativeTo value.
     */
    public RelativeTo getRelativeTo() {
        return this.relativeTo;
    }

    /**
     * Get the value property: A simple arithmetic expression that the ordinal denotes.
     *
     * @return the value value.
     */
    public String getValue() {
        return this.value;
    }

    private void setOffset(String offset) {
        this.offset = offset;
    }

    private void setRelativeTo(RelativeTo relativeTo) {
        this.relativeTo = relativeTo;
    }

    private void setValue(String value) {
        this.value = value;
    }
}
