// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.util.polling.SyncPoller;
import com.azure.mixedreality.remoterendering.models.AssetConversion;
import com.azure.mixedreality.remoterendering.models.AssetConversionOptions;
import com.azure.mixedreality.remoterendering.models.AssetConversionStatus;

import java.util.UUID;

/**
 * Sample class demonstrating how to convert a simple asset.
 */
public class ConvertSimpleAsset extends SampleBase
{
    /**
     * Main method to invoke this demo about how to convert a simple asset.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        new ConvertSimpleAsset().convertSimpleAsset();
    }

    /**
     * Sample method demonstrating how to convert a simple asset.
     */
    public void convertSimpleAsset() {
        AssetConversionOptions conversionOptions = new AssetConversionOptions()
            .setInputStorageContainerUrl(getStorageURL())
            .setInputRelativeAssetPath("box.fbx")
            .setOutputStorageContainerUrl(getStorageURL());

        // A randomly generated UUID is a good choice for a conversionId.
        String conversionId = UUID.randomUUID().toString();

        SyncPoller<AssetConversion, AssetConversion> conversionOperation = client.beginConversion(conversionId, conversionOptions);

        AssetConversion conversion = conversionOperation.getFinalResult();
        if (conversion.getStatus() == AssetConversionStatus.SUCCEEDED) {
            logger.info("Conversion succeeded: Output written to {}", conversion.getOutputAssetUrl());
        } else if (conversion.getStatus() == AssetConversionStatus.FAILED) {
            logger.error("Conversion failed: {} {}", conversion.getError().getCode(), conversion.getError().getMessage());
        } else {
            logger.error("Unexpected conversion status: {}", conversion.getStatus());
        }
    }
}
