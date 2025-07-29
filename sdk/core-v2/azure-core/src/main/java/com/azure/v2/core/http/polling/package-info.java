// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * This package contains utility classes and interfaces for handling long-running operations in the
 * Azure client libraries.
 *
 * <p>Long-running operations are operations such as the creation or deletion of a resource, which take a significant
 * amount of time to complete. These operations are typically handled asynchronously, with the client initiating the
 * operation and then polling the service at intervals to determine whether the operation has completed.</p>
 *
 * <p>This package provides a standard mechanism for initiating, tracking, and retrieving the results of long-running
 * operations</p>
 *
 * @see com.azure.v2.core.http.polling.Poller
 */
package com.azure.v2.core.http.polling;
