/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package sample.mediaservices;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.WritableBlobContainerContract;
import com.microsoft.windowsazure.services.media.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

@SpringBootApplication
public class MediaServicesSampleApplication implements CommandLineRunner {

    @Autowired
    private MediaContract mediaService;

    public static void main(String[] args) {
        SpringApplication.run(MediaServicesSampleApplication.class);
    }

    // Note: Here is the minimum sample code that demonstrates how MediaContract is
    // autowired and used.
    // More information can be found at
    // https://docs.microsoft.com/en-us/azure/media-services/media-services-java-how-to-use
    @Override
    public void run(String... args) throws Exception {
        // Upload a local file to an Asset
        final AssetInfo uploadAsset = uploadFileAndCreateAsset("video.mp4");
        System.out.println("Uploaded Asset Id: " + uploadAsset.getId());
        System.out.println("Sample completed!");
    }

    private AssetInfo uploadFileAndCreateAsset(String fileName)
            throws ServiceException, IOException {
        final WritableBlobContainerContract uploader;
        final AssetInfo resultAsset;
        final AccessPolicyInfo uploadAccessPolicy;
        LocatorInfo uploadLocator = null;

        // Create an Asset
        resultAsset = mediaService
                .create(Asset.create().setName(fileName).setAlternateId("altId"));
        System.out.println("Created Asset " + fileName);

        // Create an AccessPolicy that provides Write access for 15 minutes
        uploadAccessPolicy = mediaService.create(AccessPolicy.create("uploadAccessPolicy",
                15.0, EnumSet.of(AccessPolicyPermission.WRITE)));

        // Create a Locator using the AccessPolicy and Asset
        uploadLocator = mediaService.create(Locator.create(uploadAccessPolicy.getId(),
                resultAsset.getId(), LocatorType.SAS));

        // Create the Blob Writer using the Locator
        uploader = mediaService.createBlobWriter(uploadLocator);

        // The local file that will be uploaded to your Media Services account
        try (final InputStream input = new ClassPathResource(fileName).getInputStream()) {
            System.out.println("Uploading " + fileName);

            // Upload the local file to the asset
            uploader.createBlockBlob(fileName, input);
        }
        // Inform Media Services about the uploaded files
        mediaService.action(AssetFile.createFileInfos(resultAsset.getId()));
        System.out.println("Uploaded Asset File " + fileName);

        mediaService.delete(Locator.delete(uploadLocator.getId()));
        mediaService.delete(AccessPolicy.delete(uploadAccessPolicy.getId()));

        return resultAsset;
    }

}
