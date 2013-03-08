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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetFile;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ContentKey;
import com.microsoft.windowsazure.services.media.models.ContentKeyInfo;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.JobState;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Locator;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;

public abstract class IntegrationTestBase {
    protected static MediaContract service;
    protected static Configuration config;

    protected static final String testAssetPrefix = "testAsset";
    protected static final String testPolicyPrefix = "testPolicy";
    protected static final String testContentKeyPrefix = "testContentKey";
    protected static final String testJobPrefix = "testJobPrefix";

    protected static final String validButNonexistAssetId = "nb:cid:UUID:0239f11f-2d36-4e5f-aa35-44d58ccc0973";
    protected static final String validButNonexistAccessPolicyId = "nb:pid:UUID:38dcb3a0-ef64-4ad0-bbb5-67a14c6df2f7";
    protected static final String validButNonexistLocatorId = "nb:lid:UUID:92a70402-fca9-4aa3-80d7-d4de3792a27a";

    protected static final String MEDIA_ENCODER_MEDIA_PROCESSOR_2_2_0_0_ID = "nb:mpid:UUID:70bdc2c3-ebf4-42a9-8542-5afc1e55d217";

    protected static final String invalidId = "notAValidId";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setup() throws Exception {
        config = Configuration.getInstance();
        overrideWithEnv(config, MediaConfiguration.URI);
        overrideWithEnv(config, MediaConfiguration.OAUTH_URI);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_ID);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_SECRET);
        overrideWithEnv(config, MediaConfiguration.OAUTH_SCOPE);

        service = MediaService.create(config);

        cleanupEnvironment();
    }

    protected static void overrideWithEnv(Configuration config, String key) {
        String value = System.getenv(key);
        if (value == null)
            return;

        config.setProperty(key, value);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        cleanupEnvironment();
    }

    protected static void cleanupEnvironment() {
        removeAllTestLocators();
        removeAllTestAssets();
        removeAllTestAccessPolicies();
        removeAllTestJobs();
        removeAllTestContentKeys();
    }

    private static void removeAllTestContentKeys() {
        try {
            List<ContentKeyInfo> contentKeyInfos = service.list(ContentKey.list());

            for (ContentKeyInfo contentKeyInfo : contentKeyInfos) {
                try {
                    service.delete(ContentKey.delete(contentKeyInfo.getId()));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeAllTestAccessPolicies() {
        try {
            List<AccessPolicyInfo> policies = service.list(AccessPolicy.list());

            for (AccessPolicyInfo policy : policies) {
                if (policy.getName().startsWith(testPolicyPrefix)) {
                    service.delete(AccessPolicy.delete(policy.getId()));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeAllTestAssets() {
        try {
            List<AssetInfo> listAssetsResult = service.list(Asset.list());
            for (AssetInfo assetInfo : listAssetsResult) {
                try {
                    if (assetInfo.getName().startsWith(testAssetPrefix)) {
                        service.delete(Asset.delete(assetInfo.getId()));
                    }
                    else if (assetInfo.getName().startsWith("JobOutputAsset(")
                            && assetInfo.getName().contains(testJobPrefix)) {
                        // Delete the temp assets associated with Job results.
                        service.delete(Asset.delete(assetInfo.getId()));
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeAllTestLocators() {
        try {
            ListResult<LocatorInfo> listLocatorsResult = service.list(Locator.list());
            for (LocatorInfo locatorInfo : listLocatorsResult) {
                AssetInfo ai = service.get(Asset.get(locatorInfo.getAssetId()));
                if (ai.getName().startsWith(testAssetPrefix)) {
                    service.delete(Locator.delete(locatorInfo.getId()));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeAllTestJobs() {
        try {
            ListResult<JobInfo> jobs = service.list(Job.list());
            for (JobInfo job : jobs) {
                if (job.getName().startsWith(testJobPrefix)) {
                    // Job can't be deleted when it's state is
                    // canceling, scheduled,queued or processing
                    try {
                        if (isJobBusy(job.getState())) {
                            service.action(Job.cancel(job.getId()));
                            job = service.get(Job.get(job.getId()));
                        }

                        int retryCounter = 0;
                        while (isJobBusy(job.getState()) && retryCounter < 10) {
                            Thread.sleep(2000);
                            job = service.get(Job.get(job.getId()));
                            retryCounter++;
                        }

                        if (!isJobBusy(job.getState())) {
                            service.delete(Job.delete(job.getId()));
                        }
                        else {
                            // Not much to do so except wait.
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isJobBusy(JobState state) {
        return state == JobState.Canceling || state == JobState.Scheduled || state == JobState.Queued
                || state == JobState.Processing;
    }

    interface ComponentDelegate {
        void verifyEquals(String message, Object expected, Object actual);
    }

    protected static AssetInfo setupAssetWithFile() throws ServiceException {
        String name = UUID.randomUUID().toString();
        String testBlobName = "test" + name + ".bin";
        AssetInfo assetInfo = service.create(Asset.create().setName(testAssetPrefix + name));

        AccessPolicyInfo accessPolicyInfo = service.create(AccessPolicy.create(testPolicyPrefix + name, 10,
                EnumSet.of(AccessPolicyPermission.WRITE)));
        LocatorInfo locator = createLocator(accessPolicyInfo, assetInfo, 5);
        WritableBlobContainerContract blobWriter = service.createBlobWriter(locator);
        InputStream blobContent = new ByteArrayInputStream(new byte[] { 4, 8, 15, 16, 23, 42 });
        blobWriter.createBlockBlob(testBlobName, blobContent);

        service.action(AssetFile.createFileInfos(assetInfo.getId()));

        return assetInfo;
    }

    protected static LocatorInfo createLocator(AccessPolicyInfo accessPolicy, AssetInfo asset, int startDeltaMinutes)
            throws ServiceException {

        Date now = new Date();
        Date start = new Date(now.getTime() - (startDeltaMinutes * 60 * 1000));

        return service.create(Locator.create(accessPolicy.getId(), asset.getId(), LocatorType.SAS).setStartDateTime(
                start));
    }

    protected <T> void verifyListResultContains(List<T> expectedInfos, Collection<T> actualInfos,
            ComponentDelegate delegate) {
        verifyListResultContains("", expectedInfos, actualInfos, delegate);
    }

    protected <T> void verifyListResultContains(String message, List<T> expectedInfos, Collection<T> actualInfos,
            ComponentDelegate delegate) {
        assertNotNull(message + ": actualInfos", actualInfos);
        assertTrue(message + ": actual size should be same size or larger than expected size",
                actualInfos.size() >= expectedInfos.size());

        List<T> orderedAndFilteredActualInfo = new ArrayList<T>();
        try {
            for (T expectedInfo : expectedInfos) {
                Method getId = expectedInfo.getClass().getMethod("getId");
                String expectedId = (String) getId.invoke(expectedInfo);
                for (T actualInfo : actualInfos) {
                    if (((String) getId.invoke(actualInfo)).equals(expectedId)) {
                        orderedAndFilteredActualInfo.add(actualInfo);
                        break;
                    }
                }
            }
        }
        catch (Exception e) {
            // Don't worry about problems here.
            e.printStackTrace();
        }

        assertEquals(message + ": actual filtered size should be same as expected size", expectedInfos.size(),
                orderedAndFilteredActualInfo.size());

        if (delegate != null) {
            for (int i = 0; i < expectedInfos.size(); i++) {
                delegate.verifyEquals(message + ": orderedAndFilteredActualInfo " + i, expectedInfos.get(i),
                        orderedAndFilteredActualInfo.get(i));
            }
        }
    }

    protected void assertEqualsNullEmpty(String message, String expected, String actual) {
        if ((expected == null || expected.length() == 0) && (actual == null || actual.length() == 0)) {
            // both nullOrEmpty, so match.
        }
        else {
            assertEquals(message, expected, actual);
        }
    }

    protected void assertDateApproxEquals(Date expected, Date actual) {
        assertDateApproxEquals("", expected, actual);
    }

    protected void assertDateApproxEquals(String message, Date expected, Date actual) {
        // Default allows for a 30 seconds difference in dates, for clock skew, network delays, etc.
        long deltaInMilliseconds = 30000;

        if (expected == null || actual == null) {
            assertEquals(message, expected, actual);
        }
        else {
            long diffInMilliseconds = Math.abs(expected.getTime() - actual.getTime());

            if (diffInMilliseconds > deltaInMilliseconds) {
                assertEquals(message, expected, actual);
            }
        }
    }
}
