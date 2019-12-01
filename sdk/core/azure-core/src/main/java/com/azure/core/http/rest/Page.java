// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.paging.ContinuablePage;

/**
 * Represents a paginated REST response from the service.
 *
 * @param <T> Type of the listed objects in that response.
 */
public interface Page<T> extends ContinuablePage<String, T> {
}
