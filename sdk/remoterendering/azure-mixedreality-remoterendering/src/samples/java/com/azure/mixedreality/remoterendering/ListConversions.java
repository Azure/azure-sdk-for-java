// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.mixedreality.remoterendering.models.AssetConversion;
import com.azure.mixedreality.remoterendering.models.AssetConversionStatus;

import java.time.OffsetDateTime;

/**
 * Sample class demonstrating how to list conversions.
 */
public class ListConversions extends SampleBase {

    /**
     * Main method to invoke this demo about how to list conversions.
     * Note: This test assume DRAM is set up, so we do not run them live.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        new ListConversions().listConversions();
    }

    /**
     * Sample method demonstrating how to list conversions.
     */
    public void listConversions() {
        logger.info("Successful conversions since yesterday:");

        for (AssetConversion conversion : client.listConversions()) {
            if ((conversion.getStatus() == AssetConversionStatus.SUCCEEDED)
                && (conversion.getCreationTime().isAfter(OffsetDateTime.now().minusDays(1)))) {
                logger.info("Output Asset URL: {}", conversion.getOutputAssetUrl());
            }
        }
    }
}
