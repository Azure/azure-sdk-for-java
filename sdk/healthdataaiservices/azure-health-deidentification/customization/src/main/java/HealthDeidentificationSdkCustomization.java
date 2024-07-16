// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.*;
import org.slf4j.Logger;

public class HealthDeidentificationSdkCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // Do it manually by editing DeidServicesAsyncClient class
        // https://github.com/Azure/autorest.java/pull/2854
        // Change the String continuationToken to String continuationTokenParam in the following functions
        // listJobDocuments(String name, String continuationToken)
        // listJobs(String continuationToken)
    }

}
