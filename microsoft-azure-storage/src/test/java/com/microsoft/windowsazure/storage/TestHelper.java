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
package com.microsoft.windowsazure.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.Assert;

public class TestHelper {

    protected static void enableFiddler() {
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8888");
    }

    public static void verifyServiceStats(ServiceStats stats) {
        Assert.assertNotNull(stats);
        if (stats.getGeoReplication().getLastSyncTime() != null) {
            Assert.assertEquals(GeoReplicationStatus.LIVE, stats.getGeoReplication().getStatus());
        }
        else {
            Assert.assertTrue(stats.getGeoReplication().getStatus() == GeoReplicationStatus.BOOTSTRAP
                    || stats.getGeoReplication().getStatus() == GeoReplicationStatus.UNAVAILABLE);
        }
    }

    public static void assertStreamsAreEqual(ByteArrayInputStream src, ByteArrayInputStream dst) {
        dst.reset();
        src.reset();
        Assert.assertEquals(src.available(), dst.available());

        while (src.available() > 0) {
            Assert.assertEquals(src.read(), dst.read());
        }
    }

    public static void assertStreamsAreEqualAtIndex(ByteArrayInputStream src, ByteArrayInputStream dst, int srcIndex,
            int dstIndex, int length, int bufferSize) throws IOException {
        dst.reset();
        src.reset();

        dst.skip(dstIndex);
        src.skip(srcIndex);
        byte[] origBuffer = new byte[bufferSize];
        byte[] retrBuffer = new byte[bufferSize];
        src.read(origBuffer);
        dst.read(retrBuffer);

        for (int i = 0; i < length; i++) {
            Assert.assertEquals(src.read(), dst.read());
        }

    }
}
