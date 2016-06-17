/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.google.common.base.Stopwatch;
import com.microsoft.azure.management.datalake.store.models.DataLakeStoreAccount;
import com.microsoft.azure.management.datalake.store.implementation.DataLakeStoreFileSystemManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PerformanceUploadTests extends DataLakeUploaderTestBase {

    private static String rgName = generateName("javaadlsrg");
    private static String adlsAcct = generateName("javaadlsacct");

    private static  final String location = "East US 2";
    private static String destFolder = generateName("performanceTest");

    private static final String Local10GbFileName = "C:\\data\\FixedBlockPerfData.txt"; // 10GB perf test binary file.
    private static final String localLargeFileName = "C:\\data\\MicrosoftTelemetry.tsv"; // 2.5GB perf test binary file
    private static final String localFileName = "C:\\data\\smallFile.txt"; // 4mb perf test binary file

    @BeforeClass
    public static void Setup() throws Exception {
        createClients();

        ResourceGroupInner group = new ResourceGroupInner();
        String location = "eastus2";
        group.withLocation(location);
        resourceManagementClient.resourceGroups().createOrUpdate(rgName, group);

        // create storage and ADLS accounts, setting the accessKey
        DataLakeStoreAccount adlsAccount = new DataLakeStoreAccount();
        adlsAccount.withLocation(location);
        adlsAccount.withName(adlsAcct);
        dataLakeStoreAccountManagementClient.accounts().create(rgName, adlsAcct, adlsAccount);

        File smallFile = new File(localFileName);
        if (!smallFile.exists()) {
            smallFile.createNewFile();
            try (FileOutputStream stream = new FileOutputStream(smallFile)) {
                byte[] contents = new byte[4 * 1024 * 1024];
                Arrays.fill(contents, (byte) 'a');
                stream.write(contents);
            }
        }

        File largeFile = new File(localLargeFileName);
        if (!largeFile.exists()) {
            try (RandomAccessFile stream = new RandomAccessFile(largeFile, "rw")) {
                stream.setLength((long)(2.5* 1024 * 1024 * 1024) - 10); // 2.5GB minus 10 bytes
                byte[] content = new byte[10];
                Arrays.fill(content, (byte)'a');
                stream.write(content);
            }
        }

        File tenGBFile = new File(Local10GbFileName);
        if (!tenGBFile.exists()) {
            try (RandomAccessFile stream = new RandomAccessFile(tenGBFile, "rw")) {
                stream.setLength((long)(10* 1024 * 1024 * 1024) - 10); // 10GB minus 10 bytes
                byte[] content = new byte[10];
                Arrays.fill(content, (byte)'a');
                stream.write(content);
            }
        }
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            resourceManagementClient.resourceGroups().delete(rgName);
        }
        catch (Exception e) {
            // ignore failures during cleanup, as it is best effort
        }
    }

    @Test
    public void Test4mbFileUpload() throws Exception {
        String folder = "begoldsm";
        ArrayList<Long> perfMetrics = new ArrayList();

        // upload Rentrak data.
        boolean force = true;  //Set this to true if you want to overwrite existing data
        System.out.println("Uploading 4mb data...");
        for (int i = 0; i < 10; ++i) {
            String destLocation = destFolder + "/" + folder + "4mbFile.txt";
            Stopwatch watch = Stopwatch.createStarted();
            UploadFile(dataLakeStoreFileSystemManagementClient, adlsAcct, localFileName, destLocation, force);
            watch.stop();
            long elapsedMs = watch.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("File Uploaded : " + i);
            perfMetrics.add(elapsedMs);
        }

        for( long perf: perfMetrics){
            System.out.println(perf);
        }
    }

    @Test
    public void Test2_5gbFileUpload() throws Exception {
        String folder = "begoldsm";
        ArrayList<Long> perfMetrics = new ArrayList();

        // upload Rentrak data.
        boolean force = true;  //Set this to true if you want to overwrite existing data
        System.out.println("Uploading 2.5GB data...");
        for (int i = 0; i < 5; ++i) {
            String destLocation = destFolder + "/" + folder + "2_5gbFile.txt";
            Stopwatch watch = Stopwatch.createStarted();
            UploadFile(dataLakeStoreFileSystemManagementClient, adlsAcct, localLargeFileName, destLocation, force);
            watch.stop();
            long elapsedMs = watch.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("File Uploaded : " + i);
            perfMetrics.add(elapsedMs);
        }

        for( long perf: perfMetrics){
            System.out.println(perf);
        }
    }

    //@Test
    public void Test10gbFileUpload() throws Exception {
        String folder = "begoldsm";
        ArrayList<Long> perfMetrics = new ArrayList();

        // upload Rentrak data.
        boolean force = true;  //Set this to true if you want to overwrite existing data
        System.out.println("Uploading 10GB data...");
        for (int i = 0; i < 3; ++i) {
            String destLocation = destFolder + "/" + folder + "10gbFile.txt";
            Stopwatch watch = Stopwatch.createStarted();
            UploadFile(dataLakeStoreFileSystemManagementClient, adlsAcct, Local10GbFileName, destLocation, force);
            watch.stop();
            long elapsedMs = watch.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("File Uploaded : " + i);
            perfMetrics.add(elapsedMs);
        }

        for( long perf: perfMetrics){
            System.out.println(perf);
        }
    }

    public static boolean UploadFile(DataLakeStoreFileSystemManagementClientImpl dataLakeStoreFileSystemClient, String dlAccountName, String srcPath, String destPath, boolean force) throws Exception {
        UploadParameters parameters = new UploadParameters(srcPath, destPath, dlAccountName, 40, force, false);
        FrontEndAdapter frontend = new DataLakeStoreFrontEndAdapterImpl(dlAccountName, dataLakeStoreFileSystemClient);
        DataLakeStoreUploader uploader = new DataLakeStoreUploader(parameters, frontend);
        uploader.execute();
        return true;
    }
}
