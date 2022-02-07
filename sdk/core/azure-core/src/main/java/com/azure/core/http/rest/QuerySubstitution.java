// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

/**
 * A Query Substitution is a value that can be used to replace placeholder values in a URL
 * in the query.
 * Placeholders look like: "http://host.com/filename.html?{queryParameter1}&amp;{queryParameter2}",
 * where "{queryParameter1}" and "{queryParameter2}" are the placeholders.
 */
class QuerySubstitution extends Substitution {
    private final boolean multipleParams;

    /**
     * Create a new QuerySubstitution.
     * @param urlParameterName The name that is used between curly quotes as a placeholder in the
     *                         target URL.
     * @param methodParameterIndex The index of the parameter in the original interface method where
     *                             the value for the placeholder is.
     * @param shouldEncode Whether or not the value from the method's argument should be encoded
     *                     when the substitution is taking place.
     * @param multipleParams Get whether or not the value from the method argument needs to be sent
     *                       as string in case the values are a list or as multiple query parameters.
     */
    QuerySubstitution(String urlParameterName, int methodParameterIndex, boolean shouldEncode, boolean multipleParams) {
        super(urlParameterName, methodParameterIndex, shouldEncode);
        this.multipleParams = multipleParams;
    }

    /**
     * Get whether or not the values from the same query parameter name are sent as a list or multiple
     * different parameters.
     * @return Whether or not this query parameter list values should be sent as a list or as
     * individual query params.
     */
    public boolean mergeParameters() {
        return multipleParams;
    }
}
