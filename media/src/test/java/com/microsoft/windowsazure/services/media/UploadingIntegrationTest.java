/**
 * Copyright Microsoft Corporation
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
import java.util.EnumSet;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.core.pipeline.jersey.ExponentialRetryPolicy;
import com.microsoft.windowsazure.core.pipeline.jersey.RetryPolicyFilter;
import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;

/**
 * Testing uploading in various permutations.
 * 
 */
public class UploadingIntegrationTest extends IntegrationTestBase {
    private static WritableBlobContainerContract blobWriter;
    private static byte[] firstPrimes = new byte[] { 2, 3, 5, 7, 11, 13, 17,
            19, 23, 29 };

    @BeforeClass
    public static void setup() throws Exception {
        IntegrationTestBase.setup();

        AssetInfo asset = service.create(Asset.create().setName(
                testAssetPrefix + "uploadBlockBlobAsset"));

        AccessPolicyInfo policy = service.create(AccessPolicy.create(
                testPolicyPrefix + "uploadWritePolicy", 10,
                EnumSet.of(AccessPolicyPermission.WRITE)));

        LocatorInfo locator = createLocator(policy, asset, 5);

        blobWriter = service.createBlobWriter(locator);

        ExponentialRetryPolicy retryPolicy = new ExponentialRetryPolicy(5000,
                5, new int[] { 400, 404 });
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
        CreateBlobOptions options = new CreateBlobOptions().addMetadata(
                "testMetadataKey", "testMetadataValue");
        blobWriter.createBlockBlob("canUploadBlockBlobWithOptions",
                blobContent, options);
    }

    @Test
    public void canCreateBlobBlock() throws Exception {
        InputStream blobContent = new ByteArrayInputStream(firstPrimes);
        blobWriter.createBlobBlock("canCreateBlobBlock", "123", blobContent);
    }

    @Test
    public void canCreateBlobBlockWithOptions() throws Exception {
        InputStream blobContent = new ByteArrayInputStream(firstPrimes);
        CreateBlobBlockOptions options = new CreateBlobBlockOptions()
                .setTimeout(100);
        blobWriter.createBlobBlock("canCreateBlobBlockWithOptions", "123",
                blobContent, options);
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
        blobWriter.commitBlobBlocks("canCommitBlobBlocksWithOptions",
                blockList, options);
    }

    @Test
    public void canUploadBlockBlobWithExplicitRetry() throws Exception {
        InputStream blobContent = new ByteArrayInputStream(firstPrimes);
        blobWriter.createBlockBlob("canUploadBlockBlobWithExplicitRetry1",
                blobContent);

        ExponentialRetryPolicy forceRetryPolicy = new ExponentialRetryPolicy(1,
                1, new int[] { 201 });
        WritableBlobContainerContract forceRetryBlobWriter = blobWriter
                .withFilter(new RetryPolicyFilter(forceRetryPolicy));

        blobContent.reset();
        forceRetryBlobWriter.createBlockBlob(
                "canUploadBlockBlobWithExplicitRetry2", blobContent);
    }
}
