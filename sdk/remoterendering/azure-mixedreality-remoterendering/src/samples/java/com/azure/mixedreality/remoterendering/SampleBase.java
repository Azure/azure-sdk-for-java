// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.util.logging.ClientLogger;

/**
 * Base class for all samples.
 */
public class SampleBase {
    final SampleEnvironment environment = new SampleEnvironment();
    final RemoteRenderingClient client = new CreateClients().createClientWithAccountKey();
    final ClientLogger logger = new ClientLogger(SampleBase.class);

    /**
     * Get the storage URL used in samples.
     *
     * @return the storage URL.
     */
    String getStorageURL() {
        return "https://" + environment.getStorageAccountName() + ".blob.core.windows.net/" + environment.getBlobContainerName();
    }
}
