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

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.WritableBlobContainerContract;
import com.microsoft.windowsazure.services.media.implementation.content.AssetFileType;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
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
    private final String MEDIA_PROCESSOR_MP4_TO_SMOOTH_STREAMS = "MP4 to Smooth Streams Task";
    private final String MEDIA_PROCESSOR_SMOOTH_STREAMS_TO_HLS = "Smooth Streams to HLS Task";

    // From http://msdn.microsoft.com/en-us/library/windowsazure/hh973635.aspx
    private final String configMp4ToSmoothStreams = "<taskDefinition xmlns='http://schemas.microsoft.com/iis/media/v4/TM/TaskDefinition#'>"
            + "  <name>MP4 to Smooth Streams</name>"
            + "  <id>5e1e1a1c-bba6-11df-8991-0019d1916af0</id>"
            + "  <description xml:lang='en'>Converts MP4 files encoded with H.264 (AVC) video and AAC-LC audio codecs to Smooth Streams.</description>"
            + "  <inputFolder />"
            + "  <properties namespace='http://schemas.microsoft.com/iis/media/V4/TM/MP4ToSmooth#' prefix='mp4'>"
            + "    <property name='keepSourceNames' required='false' value='true' helpText='This property tells the MP4 to Smooth task to keep the original file name rather than add the bitrate bitrate information.' />"
            + "  </properties>"
            + "  <taskCode>"
            + "    <type>Microsoft.Web.Media.TransformManager.MP4toSmooth.MP4toSmooth_Task, Microsoft.Web.Media.TransformManager.MP4toSmooth, Version=1.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35</type>"
            + "  </taskCode>" + "</taskDefinition>";

    // From http://msdn.microsoft.com/en-us/library/windowsazure/hh973636.aspx
    private final String configSmoothStreamsToAppleHttpLiveStreams = "<taskDefinition xmlns='http://schemas.microsoft.com/iis/media/v4/TM/TaskDefinition#'>"
            + "    <name>Smooth Streams to Apple HTTP Live Streams</name>"
            + "  <id>A72D7A5D-3022-45f2-89B4-1DDC5457C111</id>"
            + "    <description xml:lang='en'>Converts on-demand Smooth Streams encoded with H.264 (AVC) video and AAC-LC audio codecs to Apple HTTP Live Streams (MPEG-2 TS) and creates an Apple HTTP Live Streaming playlist (.m3u8) file for the converted presentation.</description>"
            + "    <inputDirectory></inputDirectory>"
            + "    <outputFolder>TS_Out</outputFolder>"
            + "    <properties namespace='http://schemas.microsoft.com/iis/media/AppleHTTP#' prefix='hls'>"
            + "        <property name='maxbitrate' required='true' value='8500000' helpText='The maximum bit rate, in bits per second (bps), to be converted to MPEG-2 TS. On-demand Smooth Streams at or below this value are converted to MPEG-2 TS segments. Smooth Streams above this value are not converted. Most Apple devices can play media encoded at bit rates up to 8,500 Kbps.'/>"
            + "        <property name='manifest' required='false' value='' helpText='The file name to use for the converted Apple HTTP Live Streaming playlist file (a file with an .m3u8 file name extension). If no value is specified, the following default value is used: &lt;ISM_file_name&gt;-m3u8-aapl.m3u8'/>"
            + "        <property name='segment' required='false' value='10' helpText='The duration of each MPEG-2 TS segment, in seconds. 10 seconds is the Apple-recommended setting for most Apple mobile digital devices.'/>"
            + "        <property name='log'  required='false' value='' helpText='The file name to use for a log file (with a .log file name extension) that records the conversion activity. If you specify a log file name, the file is stored in the task output folder.' /> "
            + "        <property name='encrypt'  required='false' value='false' helpText='Enables encryption of MPEG-2 TS segments by using the Advanced Encryption Standard (AES) with a 128-bit key (AES-128).' />"
            + "        <property name='pid'  required='false' value='' helpText='The program ID of the MPEG-2 TS presentation. Different encodings of MPEG-2 TS streams in the same presentation use the same program ID so that clients can easily switch between bit rates.' />"
            + "        <property name='codecs'  required='false' value='false' helpText='Enables codec format identifiers, as defined by RFC 4281, to be included in the Apple HTTP Live Streaming playlist (.m3u8) file.' />"
            + "        <property name='backwardcompatible'  required='false' value='false' helpText='Enables playback of the MPEG-2 TS presentation on devices that use the Apple iOS 3.0 mobile operating system.' />"
            + "        <property name='allowcaching'  required='false' value='true' helpText='Enables the MPEG-2 TS segments to be cached on Apple devices for later playback.' />"
            + "        <property name='passphrase'  required='false' value='' helpText='A passphrase that is used to generate the content key identifier.' />"
            + "        <property name='key'  required='false' value='' helpText='The hexadecimal representation of the 16-octet content key value that is used for encryption.' />"
            + "        <property name='keyuri'  required='false' value='' helpText='An alternate URI to be used by clients for downloading the key file. If no value is specified, it is assumed that the Live Smooth Streaming publishing point provides the key file.' />"
            + "        <property name='overwrite'  required='false' value='true' helpText='Enables existing files in the output folder to be overwritten if converted output files have identical file names.' />"
            + "    </properties>"
            + "    <taskCode>"
            + "        <type>Microsoft.Web.Media.TransformManager.SmoothToHLS.SmoothToHLSTask, Microsoft.Web.Media.TransformManager.SmoothToHLS, Version=1.0.0.0, Culture=neutral, PublicKeyToken=31bf3856ad364e35</type>"
            + "    </taskCode>" + "</taskDefinition>";

    public static enum EncoderType {
        WindowsAzureMediaEncoder, Mp4ToSmoothStream, SmoothStreamsToHls, StorageDecryption
    }

    public MediaServiceWrapper(MediaContract service) {
        this.service = service;
    }

    // Manage
    public AssetInfo createAsset(String name, AssetOption encryption) throws ServiceException {
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

        String contentKeyId = null;
        if (aesKey != null) {
            String protectionKeyId = (String) service.action(ProtectionKey
                    .getProtectionKeyId(ContentKeyType.StorageEncryption));
            String protectionKey = (String) service.action(ProtectionKey.getProtectionKey(protectionKeyId));

            String contentKeyIdUuid = UUID.randomUUID().toString();
            contentKeyId = "nb:kid:UUID:" + contentKeyIdUuid;

            byte[] encryptedContentKey = EncryptionHelper.encryptSymmetricKey(protectionKey, aesKey);
            String encryptedContentKeyString = Base64.encode(encryptedContentKey);
            String checksum = EncryptionHelper.calculateContentKeyChecksum(contentKeyIdUuid, aesKey);

            service.create(ContentKey.create(contentKeyId, ContentKeyType.StorageEncryption, encryptedContentKeyString)
                    .setChecksum(checksum).setProtectionKeyId(protectionKeyId));
            service.action(Asset.linkContentKey(asset.getId(), contentKeyId));
        }

        WritableBlobContainerContract uploader = service.createBlobWriter(locator);

        Hashtable<String, AssetFileInfo> infoToUpload = new Hashtable<String, AssetFileInfo>();

        boolean isFirst = true;
        for (String fileName : inputFiles.keySet()) {

            MessageDigest digest = MessageDigest.getInstance("MD5");

            InputStream inputStream = inputFiles.get(fileName);

            String initializationVector = null;
            if (aesKey != null) {
                // Media Services requires 128-bit (16-byte) initialization vectors (IV)
                // for AES encryption, but also that only the first 8 bytes are filled.
                Random random = new Random();
                byte[] effectiveIv = new byte[8];
                random.nextBytes(effectiveIv);
                byte[] iv = new byte[16];
                System.arraycopy(effectiveIv, 0, iv, 0, effectiveIv.length);

                byte[] sub = new byte[9];
                // Offset the bytes to ensure that the sign-bit is not set.
                // Media Services expects unsigned Int64 values.
                System.arraycopy(iv, 0, sub, 1, 8);
                BigInteger longIv = new BigInteger(sub);
                initializationVector = longIv.toString();

                inputStream = EncryptionHelper.encryptFile(inputStream, aesKey, iv);
            }

            InputStream digestStream = new DigestInputStream(inputStream, digest);
            CountingStream countingStream = new CountingStream(digestStream);

            uploader.createBlockBlob(fileName, countingStream);

            inputStream.close();
            byte[] md5hash = digest.digest();
            String md5 = Base64.encode(md5hash);

            AssetFileInfo fi = new AssetFileInfo(null, new AssetFileType().setContentChecksum(md5)
                    .setContentFileSize(new Long(countingStream.getCount())).setIsPrimary(isFirst).setName(fileName)
                    .setParentAssetId(asset.getAlternateId()).setInitializationVector(initializationVector));
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

    private static class CountingStream extends InputStream {
        private final InputStream wrappedStream;
        private int count;

        public CountingStream(InputStream wrapped) {
            wrappedStream = wrapped;
            count = 0;
        }

        @Override
        public int read() throws IOException {
            count++;
            return wrappedStream.read();
        }

        public int getCount() {
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
            case Mp4ToSmoothStream:
                processor = getMediaProcessorIdByName(MEDIA_PROCESSOR_MP4_TO_SMOOTH_STREAMS);
                configuration = configMp4ToSmoothStreams;
                break;
            case SmoothStreamsToHls:
                processor = getMediaProcessorIdByName(MEDIA_PROCESSOR_SMOOTH_STREAMS_TO_HLS);
                configuration = configSmoothStreamsToAppleHttpLiveStreams;
                break;
            case WindowsAzureMediaEncoder:
                processor = getMediaProcessorIdByName(MEDIA_PROCESSOR_WINDOWS_AZURE_MEDIA_ENCODER);
                configuration = "H.264 256k DSL CBR";
                break;
            case StorageDecryption:
                processor = getMediaProcessorIdByName(MEDIA_PROCESSOR_STORAGE_DECRYPTION);
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

    private String getMediaProcessorIdByName(String processorName) throws ServiceException {
        EntityListOperation<MediaProcessorInfo> operation = MediaProcessor.list();
        operation.getQueryParameters().putSingle("$filter", "Name eq '" + processorName + "'");
        MediaProcessorInfo processor = service.list(operation).get(0);
        return processor.getId();
    }

    // Process
    public boolean isJobFinished(JobInfo initialJobInfo) throws ServiceException {
        JobInfo currentJob = service.get(Job.get(initialJobInfo.getId()));
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
    public List<URL> createFileURLsFromAsset(AssetInfo asset, int availabilityWindowInMinutes) throws ServiceException,
            MalformedURLException {
        List<URL> ret = new ArrayList<URL>();

        AccessPolicyInfo readAP = service.create(AccessPolicy.create(accessPolicyPrefix + "tempAccessPolicy",
                availabilityWindowInMinutes, EnumSet.of(AccessPolicyPermission.READ)));
        LocatorInfo readLocator = service.create(Locator.create(readAP.getId(), asset.getId(), LocatorType.SAS));

        List<AssetFileInfo> publishedFiles = service.list(AssetFile.list(asset.getAssetFilesLink()));
        for (AssetFileInfo fi : publishedFiles) {
            URL file = constructUrlFromLocatorAndFileName(readLocator, fi.getName());
            ret.add(file);
        }

        return ret;
    }

    private URL constructUrlFromLocatorAndFileName(LocatorInfo locator, String fileName) throws MalformedURLException {
        String locatorPath = locator.getPath();
        int startOfSas = locatorPath.indexOf("?");
        String blobPath = locatorPath + fileName;
        if (startOfSas >= 0) {
            blobPath = locatorPath.substring(0, startOfSas) + "/" + fileName + locatorPath.substring(startOfSas);
        }

        return new URL(blobPath);
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
