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
package com.microsoft.azure.storage.analytics;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestHelper;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Analytics Test Base
 */
public class AnalyticsTestHelper extends TestHelper {

    protected static String generateRandomContainerName() {
        String containerName = "container" + UUID.randomUUID().toString();
        return containerName.replace("-", "");
    }

    public static CloudBlobContainer getRandomContainerReference() throws URISyntaxException, StorageException {
        String containerName = generateRandomContainerName();
        CloudBlobClient blobClient = TestHelper.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference(containerName);

        return container;
    }

    public static ByteArrayInputStream getRandomDataStream(int length) {
        final Random randGenerator = new Random();
        final byte[] buff = new byte[length];
        randGenerator.nextBytes(buff);
        return new ByteArrayInputStream(buff);
    }

    public static List<String> CreateLogs(CloudBlobContainer container, StorageService service, int count,
            Calendar start, Granularity granularity) throws URISyntaxException, StorageException, IOException {
        String name;
        List<String> blobs = new ArrayList<String>();
        DateFormat hourFormat = new SimpleDateFormat("yyyy/MM/dd/HH");
        hourFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        CloudBlockBlob blockBlob;
        name = service.toString().toLowerCase(Locale.US) + "/" + hourFormat.format(start.getTime()) + "00/000001.log";
        blockBlob = container.getBlockBlobReference(name);
        blockBlob.upload(getRandomDataStream(1), 1, null, null, null);
        blobs.add(name);

        for (int i = 1; i < count; i++) {
            if (granularity.equals(Granularity.HOUR)) {
                start.add(GregorianCalendar.HOUR_OF_DAY, 1);
            }
            else if (granularity.equals(Granularity.DAY)) {
                start.add(GregorianCalendar.DAY_OF_MONTH, 1);
            }
            else if (granularity.equals(Granularity.MONTH)) {
                start.add(GregorianCalendar.MONTH, 1);
            }
            else {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "CreateLogs granularity of '{0}' is invalid.", granularity));
            }

            name = service.toString().toLowerCase(Locale.US) + "/" + hourFormat.format(start.getTime())
                    + "00/000001.log";
            blockBlob = container.getBlockBlobReference(name);
            blockBlob.upload(getRandomDataStream(1), 1, null, null, null);
            blobs.add(name);
            //System.out.println("BLOB ADDED: " + name);
        }

        return blobs;
    }
}
