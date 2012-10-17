/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ExponentialRetryPolicy;
import com.microsoft.windowsazure.services.core.RetryPolicyFilter;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAccessPolicyOptions;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.CreateLocatorOptions;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;

/**
 * Testing uploading in various permutations.
 * 
 */
public class UploadingIntegrationTest extends IntegrationTestBase {

    @Test
    public void canUploadBlockBlob() throws Exception {
        MediaContract service = MediaService.create(config);

        AssetInfo asset = service.createAsset(new CreateAssetOptions().setName("uploadBlockBlobAsset"));

        AccessPolicyInfo policy = service.createAccessPolicy("uploadWritePolicy", 10,
                new CreateAccessPolicyOptions().addPermissions(AccessPolicyPermission.WRITE));

        Date now = new Date();
        Date fiveMinutesAgo = new Date();
        Date tenMinutesFromNow = new Date();

        fiveMinutesAgo.setTime(now.getTime() - (5 * 60 * 1000));
        tenMinutesFromNow.setTime(now.getTime() + (10 * 60 * 1000));

        LocatorInfo locator = service.createLocator(policy.getId(), asset.getId(), LocatorType.SAS,
                new CreateLocatorOptions().setStartTime(fiveMinutesAgo).setExpirationDateTime(tenMinutesFromNow));

        WritableBlobContainerContract blobWriter = MediaService.createBlobWriter(locator);

        ExponentialRetryPolicy retryPolicy = new ExponentialRetryPolicy(5000, 5, new int[] { 400, 404 });
        blobWriter = blobWriter.withFilter(new RetryPolicyFilter(retryPolicy));

        InputStream blobContent = new ByteArrayInputStream(new byte[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29 });

        blobWriter.createBlockBlob("uploadBlockBlobTest", blobContent);
    }
}
