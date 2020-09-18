// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;


import com.azure.digitaltwins.core.DigitalTwinsClient;

import java.util.List;

/**
 * Optional settings that are specific to calls to {@link DigitalTwinsClient#updateComponent(String, String, List)} and its overloads.
 */
public final class UpdateRelationshipRequestOptions extends DigitalTwinsRequestOptions {
    // This class exists to be added to later if the updateRelationship APIs get a new optional parameter in later service
    // API versions and so that we don't have to expose that new optional parameter for other APIs like updateDigitalTwin,
    // updateComponent, etc.

    // Need to override this method so that the returned value is of type UpdateRelationshipRequestOptions instead of type RequestOptions
    /**
     * Sets the ifMatch condition on on the UpdateRelationshipRequestOptions
     * @param ifMatch A string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @return The UpdateRelationshipRequestOptions itself.
     */
    @Override
    public UpdateRelationshipRequestOptions setIfMatch(String ifMatch) {
        super.setIfMatch(ifMatch);
        return this;
    }
}
