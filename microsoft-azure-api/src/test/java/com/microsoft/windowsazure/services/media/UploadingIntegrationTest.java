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
import java.util.EnumSet;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.core.ExponentialRetryPolicy;
import com.microsoft.windowsazure.services.core.RetryPolicyFilter;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.CreateLocatorOptions;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;

/**
 * Testing uploading in various permutations.
 * 
 */
public class UploadingIntegrationTest extends IntegrationTestBase {
    private static WritableBlobContainerContract blobWriter;
    private static byte[] firstPrimes = new byte[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29 };

    @BeforeClass
    public static void setup() throws Exception {
        IntegrationTestBase.setup();

        AssetInfo asset = service.createAsset(new CreateAssetOptions()
                .setName(testAssetPrefix + "uploadBlockBlobAsset"));

        AccessPolicyInfo policy = service.createAccessPolicy(testPolicyPrefix + "uploadWritePolicy", 10,
                EnumSet.of(AccessPolicyPermission.WRITE));

        Date now = new Date();
        Date fiveMinutesAgo = new Date(now.getTime() - (5 * 60 * 1000));
        Date tenMinutesFromNow = new Date(now.getTime() + (10 * 60 * 1000));

        LocatorInfo locator = service.createLocator(policy.getId(), asset.getId(), LocatorType.SAS,
                new CreateLocatorOptions().setStartTime(fiveMinutesAgo).setExpirationDateTime(tenMinutesFromNow));

        blobWriter = MediaService.createBlobWriter(locator);

        ExponentialRetryPolicy retryPolicy = new ExponentialRetryPolicy(5000, 5, new int[] { 400, 404 });
        blobWriter = blobWriter.withFilter(new RetryPolicyFilter(retryPolicy));
    }

    @Test
    public void canUploadBlockBlob() throws Exception {
        InputStream blobContent = new ByteArrayInputStream(firstPrimes);
        blobWriter.createBlockBlob("uploadBlockBlobTest", blobContent);
    }

    @Test
    public void canUploadBlockBlobWithOptions() throws Exception {
        InputStream blobContent = new ByteArrayInputStream(firstPrimes);
        CreateBlobOptions options = new CreateBlobOptions().addMetadata("testMetadataKey", "testMetadataValue");
        blobWriter.createBlockBlob("canUploadBlockBlobWithOptions", blobContent, options);
    }

    @Test
    public void canCreateBlobBlock() throws Exception {
        InputStream blobContent = new ByteArrayInputStream(firstPrimes);
        blobWriter.createBlobBlock("canCreateBlobBlock", "123", blobContent);
    }

    @Test
    public void canCreateBlobBlockWithOptions() throws Exception {
        InputStream blobContent = new ByteArrayInputStream(firstPrimes);
        CreateBlobBlockOptions options = new CreateBlobBlockOptions().setTimeout(100);
        blobWriter.createBlobBlock("canCreateBlobBlockWithOptions", "123", blobContent, options);
    }

    @Test
    public void canCommitBlobBlocks() throws Exception {
        BlockList blockList = new BlockList();
        blobWriter.commitBlobBlocks("canCommitBlobBlocks", blockList);
    }

    @Test
    public void canCommitBlobBlocksWithOptions() throws Exception {
        BlockList blockList = new BlockList();
        CommitBlobBlocksOptions options = new CommitBlobBlocksOptions()
                .setBlobContentType("text/html;charset=ISO-8859-1");
        blobWriter.commitBlobBlocks("canCommitBlobBlocksWithOptions", blockList, options);
    }

}
