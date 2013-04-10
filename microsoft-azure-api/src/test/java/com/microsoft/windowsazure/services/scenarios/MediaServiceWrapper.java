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

package com.microsoft.windowsazure.services.scenarios;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.Assert;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.WritableBlobContainerContract;
import com.microsoft.windowsazure.services.media.entityoperations.EntityListOperation;
import com.microsoft.windowsazure.services.media.implementation.content.AssetFileType;
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetFile;
import com.microsoft.windowsazure.services.media.models.AssetFile.Updater;
import com.microsoft.windowsazure.services.media.models.AssetFileInfo;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.AssetOption;
import com.microsoft.windowsazure.services.media.models.ContentKey;
import com.microsoft.windowsazure.services.media.models.ContentKeyType;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.Job.Creator;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.Locator;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.microsoft.windowsazure.services.media.models.MediaProcessor;
import com.microsoft.windowsazure.services.media.models.MediaProcessorInfo;
import com.microsoft.windowsazure.services.media.models.ProtectionKey;
import com.microsoft.windowsazure.services.media.models.Task;

class MediaServiceWrapper {
    private final MediaContract service;

    private static final String accessPolicyPrefix = "scenarioTestPrefix";

    private final String MEDIA_PROCESSOR_STORAGE_DECRYPTION = "Storage Decryption";
    private final String MEDIA_PROCESSOR_WINDOWS_AZURE_MEDIA_ENCODER = "Windows Azure Media Encoder";

    public static enum EncoderType {
        WindowsAzureMediaEncoder, StorageDecryption
    }

    public MediaServiceWrapper(MediaContract service) {
        this.service = service;
    }

    // Manage
    public AssetInfo createAsset(String name, AssetOption encryption) throws ServiceException {
        if (encryption == AssetOption.StorageEncrypted && !EncryptionHelper.canUseStrongCrypto()) {
            Assert.fail("JVM does not support the required encryption");
        }

        // Create asset. The SDK's top-level method is the simplest way to do that.
        return service.create(Asset.create().setName(name).setAlternateId("altId").setOptions(encryption));
    }

    public List<ListResult<AssetInfo>> getAssetSortedPagedResults(String rootName, int pageSize)
            throws ServiceException {
        List<ListResult<AssetInfo>> pages = new ArrayList<ListResult<AssetInfo>>();
        for (int skip = 0; true; skip += pageSize) {
            EntityListOperation<AssetInfo> listOperation = Asset.list().setTop(pageSize).setSkip(skip)
                    .set("$filter", "startswith(Name,'" + rootName + "')").set("$orderby", "Name");

            ListResult<AssetInfo> listAssetResult = service.list(listOperation);
            pages.add(listAssetResult);
            if (listAssetResult.size() == 0) {
                break;
            }
        }

        return pages;
    }

    // Ingest
    public void uploadFilesToAsset(AssetInfo asset, int uploadWindowInMinutes, Hashtable<String, InputStream> inputFiles)
            throws Exception {
        uploadFilesToAsset(asset, uploadWindowInMinutes, inputFiles, null);
    }

    public void uploadFilesToAsset(AssetInfo asset, int uploadWindowInMinutes,
            Hashtable<String, InputStream> inputFiles, byte[] aesKey) throws Exception {
        AccessPolicyInfo accessPolicy = service.create(AccessPolicy.create(accessPolicyPrefix + "tempAccessPolicy",
                uploadWindowInMinutes, EnumSet.of(AccessPolicyPermission.WRITE)));
        LocatorInfo locator = service.create(Locator.create(accessPolicy.getId(), asset.getId(), LocatorType.SAS));

        String contentKeyId = createAssetContentKey(asset, aesKey);

        WritableBlobContainerContract uploader = service.createBlobWriter(locator);

        Hashtable<String, AssetFileInfo> infoToUpload = new Hashtable<String, AssetFileInfo>();

        boolean isFirst = true;
        for (String fileName : inputFiles.keySet()) {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            InputStream inputStream = inputFiles.get(fileName);

            byte[] iv = null;
            if (aesKey != null) {
                iv = createIV();
                inputStream = EncryptionHelper.encryptFile(inputStream, aesKey, iv);
            }

            InputStream digestStream = new DigestInputStream(inputStream, digest);
            CountingStream countingStream = new CountingStream(digestStream);
            uploader.createBlockBlob(fileName, countingStream);

            inputStream.close();
            byte[] md5hash = digest.digest();
            String md5 = Base64.encode(md5hash);

            AssetFileInfo fi = new AssetFileInfo(null, new AssetFileType().setContentChecksum(md5)
                    .setContentFileSize(countingStream.getCount()).setIsPrimary(isFirst).setName(fileName)
                    .setInitializationVector(getIVString(iv)));
            infoToUpload.put(fileName, fi);

            isFirst = false;
        }

        service.action(AssetFile.createFileInfos(asset.getId()));
        for (AssetFileInfo assetFile : service.list(AssetFile.list(asset.getAssetFilesLink()))) {
            AssetFileInfo fileInfo = infoToUpload.get(assetFile.getName());
            Updater updateOp = AssetFile.update(assetFile.getId()).setContentChecksum(fileInfo.getContentChecksum())
                    .setContentFileSize(fileInfo.getContentFileSize()).setIsPrimary(fileInfo.getIsPrimary());

            if (aesKey != null) {
                updateOp.setIsEncrypted(true).setEncryptionKeyId(contentKeyId).setEncryptionScheme("StorageEncryption")
                        .setEncryptionVersion("1.0").setInitializationVector(fileInfo.getInitializationVector());
            }

            service.update(updateOp);
        }

        service.list(AssetFile.list(asset.getAssetFilesLink()));

        service.delete(Locator.delete(locator.getId()));
        service.delete(AccessPolicy.delete(accessPolicy.getId()));
    }

    private String getIVString(byte[] iv) {
        if (iv == null) {
            return null;
        }

        // Offset the bytes to ensure that the sign-bit is not set.
        // Media Services expects unsigned Int64 values.
        byte[] sub = new byte[9];
        System.arraycopy(iv, 0, sub, 1, 8);
        BigInteger longIv = new BigInteger(sub);
        return longIv.toString();
    }

    private byte[] createIV() {
        // Media Services requires 128-bit (16-byte) initialization vectors (IV)
        // for AES encryption, but also that only the first 8 bytes are filled.
        Random random = new Random();
        byte[] effectiveIv = new byte[8];
        random.nextBytes(effectiveIv);
        byte[] iv = new byte[16];
        System.arraycopy(effectiveIv, 0, iv, 0, effectiveIv.length);
        return iv;
    }

    private String createAssetContentKey(AssetInfo asset, byte[] aesKey) throws Exception {
        if (aesKey == null) {
            return null;
        }

        String protectionKeyId = service.action(ProtectionKey.getProtectionKeyId(ContentKeyType.StorageEncryption));
        String protectionKey = service.action(ProtectionKey.getProtectionKey(protectionKeyId));

        String contentKeyIdUuid = UUID.randomUUID().toString();
        String contentKeyId = "nb:kid:UUID:" + contentKeyIdUuid;

        byte[] encryptedContentKey = EncryptionHelper.encryptSymmetricKey(protectionKey, aesKey);
        String encryptedContentKeyString = Base64.encode(encryptedContentKey);
        String checksum = EncryptionHelper.calculateContentKeyChecksum(contentKeyIdUuid, aesKey);

        service.create(ContentKey.create(contentKeyId, ContentKeyType.StorageEncryption, encryptedContentKeyString)
                .setChecksum(checksum).setProtectionKeyId(protectionKeyId));
        service.action(Asset.linkContentKey(asset.getId(), contentKeyId));
        return contentKeyId;
    }

    private static class CountingStream extends InputStream {
        private final InputStream wrappedStream;
        private long count;

        public CountingStream(InputStream wrapped) {
            wrappedStream = wrapped;
            count = 0;
        }

        @Override
        public int read() throws IOException {
            count++;
            return wrappedStream.read();
        }

        public long getCount() {
            return count;
        }
    }

    // Process
    public JobInfo createJob(String jobName, AssetInfo inputAsset, Task.CreateBatchOperation task)
            throws ServiceException {
        List<Task.CreateBatchOperation> tasks = new ArrayList<Task.CreateBatchOperation>();
        tasks.add(task);
        return createJob(jobName, inputAsset, tasks);
    }

    public JobInfo createJob(String jobName, AssetInfo inputAsset, List<Task.CreateBatchOperation> tasks)
            throws ServiceException {
        Creator jobCreator = Job.create().setName(jobName).addInputMediaAsset(inputAsset.getId()).setPriority(2);

        for (Task.CreateBatchOperation task : tasks) {
            jobCreator.addTaskCreator(task);
        }

        return service.create(jobCreator);
    }

    // Process
    public Task.CreateBatchOperation createTaskOptions(String taskName, int inputAssetId, int outputAssetId,
            EncoderType encoderType) throws ServiceException {
        String taskBody = getTaskBody(inputAssetId, outputAssetId);

        String processor = null;
        String configuration = null;
        switch (encoderType) {
            case WindowsAzureMediaEncoder:
                processor = getMediaProcessorIdByName(MEDIA_PROCESSOR_WINDOWS_AZURE_MEDIA_ENCODER, "2.2.0.0");
                // Full list of configurations strings for version 2.1 is at:
                // http://msdn.microsoft.com/en-us/library/jj129582.aspx
                configuration = "VC1 Broadband SD 4x3";
                break;
            case StorageDecryption:
                processor = getMediaProcessorIdByName(MEDIA_PROCESSOR_STORAGE_DECRYPTION, "1.6");
                configuration = null;
                break;
            default:
                break;
        }

        Task.CreateBatchOperation taskCreate = Task.create(processor, taskBody).setName(taskName)
                .setConfiguration(configuration);

        return taskCreate;
    }

    private String getTaskBody(int inputAssetId, int outputAssetId) {
        return "<taskBody><inputAsset>JobInputAsset(" + inputAssetId + ")</inputAsset>"
                + "<outputAsset>JobOutputAsset(" + outputAssetId + ")</outputAsset></taskBody>";
    }

    private String getMediaProcessorIdByName(String processorName, String version) throws ServiceException {
        EntityListOperation<MediaProcessorInfo> operation = MediaProcessor.list();
        operation.getQueryParameters().putSingle("$filter",
                "(Name eq '" + processorName + "') and (Version eq '" + version + "')");
        MediaProcessorInfo processor = service.list(operation).get(0);
        return processor.getId();
    }

    // Process
    public boolean isJobFinished(JobInfo initialJobInfo) throws ServiceException {
        JobInfo currentJob = service.get(Job.get(initialJobInfo.getId()));
        System.out.println(currentJob.getState());
        switch (currentJob.getState()) {
            case Finished:
            case Canceled:
            case Error:
                return true;
            default:
                return false;
        }
    }

    public List<AssetInfo> getJobOutputMediaAssets(JobInfo job) throws ServiceException {
        return service.list(Asset.list(job.getOutputAssetsLink()));
    }

    // Process
    public void cancelJob(JobInfo job) throws ServiceException {
        // Use the service function
        service.action(Job.cancel(job.getId()));
    }

    // Deliver
    private Hashtable<String, URL> createFileURLsFromAsset(AssetInfo asset, int availabilityWindowInMinutes)
            throws ServiceException, MalformedURLException {
        Hashtable<String, URL> ret = new Hashtable<String, URL>();

        AccessPolicyInfo readAP = service.create(AccessPolicy.create(accessPolicyPrefix + "tempAccessPolicy",
                availabilityWindowInMinutes, EnumSet.of(AccessPolicyPermission.READ)));
        LocatorInfo readLocator = service.create(Locator.create(readAP.getId(), asset.getId(), LocatorType.SAS));

        List<AssetFileInfo> publishedFiles = service.list(AssetFile.list(asset.getAssetFilesLink()));
        for (AssetFileInfo fi : publishedFiles) {
            ret.put(fi.getName(),
                    new URL(readLocator.getBaseUri() + "/" + fi.getName() + readLocator.getContentAccessToken()));
        }

        return ret;
    }

    // Deliver
    public Hashtable<String, InputStream> downloadFilesFromAsset(AssetInfo asset, int downloadWindowInMinutes)
            throws Exception {
        Hashtable<String, URL> urls = createFileURLsFromAsset(asset, downloadWindowInMinutes);
        Hashtable<String, InputStream> ret = new Hashtable<String, InputStream>();

        for (String fileName : urls.keySet()) {
            URL url = urls.get(fileName);
            InputStream stream = getInputStreamWithRetry(url);
            ret.put(fileName, stream);
        }

        return ret;
    }

    // This method is needed because there can be a delay before a new read locator
    // is applied for the asset files.
    private InputStream getInputStreamWithRetry(URL file) throws IOException, InterruptedException {
        InputStream reader = null;
        for (int counter = 0; true; counter++) {
            try {
                reader = file.openConnection().getInputStream();
                break;
            }
            catch (IOException e) {
                System.out.println("Got error, wait a bit and try again");
                if (counter < 6) {
                    Thread.sleep(10000);
                }
                else {
                    // No more retries.
                    throw e;
                }
            }
        }

        return reader;
    }

    public void removeAllAssetsWithPrefix(String assetPrefix) throws ServiceException {
        ListResult<LocatorInfo> locators = service.list(Locator.list());
        EntityListOperation<AssetInfo> operation = Asset.list();
        operation.getQueryParameters().add("$filter", "startswith(Name,'" + assetPrefix + "')");
        List<AssetInfo> assets = service.list(operation);
        for (AssetInfo asset : assets) {
            if (asset.getName().length() > assetPrefix.length()
                    && asset.getName().substring(0, assetPrefix.length()).equals(assetPrefix)) {
                for (LocatorInfo locator : locators) {
                    if (locator.getAssetId().equals(asset.getId())) {
                        try {
                            service.delete(Locator.delete(locator.getId()));
                        }
                        catch (ServiceException e) {
                            // Don't worry if cannot delete now.
                            // Might be held on to by a running job
                        }
                    }
                }

                try {
                    service.delete(Asset.delete(asset.getId()));
                }
                catch (ServiceException e) {
                    // Don't worry if cannot delete now.
                    // Might be held on to by a running job
                }
            }
        }
    }

    public void removeAllAccessPoliciesWithPrefix() throws ServiceException {
        List<AccessPolicyInfo> accessPolicies = service.list(AccessPolicy.list());
        for (AccessPolicyInfo accessPolicy : accessPolicies) {
            if (accessPolicy.getName().length() > accessPolicyPrefix.length()
                    && accessPolicy.getName().substring(0, accessPolicyPrefix.length()).equals(accessPolicyPrefix)) {
                try {
                    service.delete(AccessPolicy.delete(accessPolicy.getId()));
                }
                catch (ServiceException e) {
                    // Don't worry if cannot delete now.
                    // Might be held on to by a running job
                }
            }
        }
    }

    private static class EncryptionHelper {
        public static boolean canUseStrongCrypto() {
            try {
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                SecretKeySpec secretKeySpec = new SecretKeySpec(new byte[32], "AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            }
            catch (Exception e) {
                return false;
            }
            return true;
        }

        public static byte[] encryptSymmetricKey(String protectionKey, byte[] inputData) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            byte[] protectionKeyBytes = Base64.decode(protectionKey);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(protectionKeyBytes);
            Certificate certificate = certificateFactory.generateCertificate(byteArrayInputStream);
            Key publicKey = certificate.getPublicKey();
            SecureRandom secureRandom = new SecureRandom();
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, secureRandom);
            byte[] cipherText = cipher.doFinal(inputData);
            return cipherText;
        }

        public static String calculateContentKeyChecksum(String uuid, byte[] aesKey) throws Exception {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptionResult = cipher.doFinal(uuid.getBytes("UTF8"));
            byte[] checksumByteArray = new byte[8];
            System.arraycopy(encryptionResult, 0, checksumByteArray, 0, 8);
            String checksum = Base64.encode(checksumByteArray);
            return checksum;
        }

        public static InputStream encryptFile(InputStream inputStream, byte[] key, byte[] iv) throws Exception {
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
            return cipherInputStream;
        }
    }

}
