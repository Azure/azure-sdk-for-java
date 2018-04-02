/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.internal.ResourceId;

public class ResourceIdTests {

    @Test(groups = { "simple" })
    public void resourceIdTryParsing() throws DocumentClientException {
        HashMap<String, Boolean> cases = new HashMap<>();
        cases.put("testDb", false);
        cases.put("db", false);
        cases.put("cosmosdb", false);
        cases.put("asosfactor", false);

        cases.put("f", false);
        cases.put("fo", false);
        cases.put("foo", false);
        cases.put("foob", true);
        cases.put("fooba", false);
        cases.put("foobar", false);
        cases.put("Zm8=", false);
        cases.put("Zm9v", true);
        cases.put("Zm9vYg==", true);
        cases.put("Zm9vYmE=", false);
        cases.put("Zm9vYmFy", false);

        // collection rid
        cases.put("1-MxAPlgMgA=", true);
        cases.put("nJRwAA==", true);
        cases.put("MaZyAA==", true);
        cases.put("-qpmAA==", true);
        cases.put("wsIRAA==", true);
        cases.put("GJwnAA==", true);

        // document rid
        cases.put("ClZUAPp9+A0=", true);

        // offer rid
        cases.put("-d8Hx", false);

        for (Map.Entry<String, Boolean> testCase : cases.entrySet()) {
            Pair<Boolean, ResourceId> resourcePair = ResourceId.tryParse(testCase.getKey());
            assertThat( resourcePair.getKey()).as(String.format("ResourceId.tryParse failed for '%s'", testCase.getKey())).isEqualTo(testCase.getValue());
        }
    }

    private static int randomNextIntForTest(Random rnd, Boolean positive) {
        return rnd.nextInt(Integer.MAX_VALUE / 2) + (positive ? Integer.MAX_VALUE / 2 : - Integer.MAX_VALUE / 2);
    }

    @Test(groups = { "simple" })
    public void resourceIdParsingRoundTrip() throws DocumentClientException {
        Random rnd = new Random(System.currentTimeMillis());

        ResourceId dbRid = ResourceId.newDatabaseId(randomNextIntForTest(rnd, true));
        ResourceId parsedDbRid = ResourceId.parse(dbRid.toString());
        assertThat(parsedDbRid.getDatabase()).isEqualTo(dbRid.getDatabase());

        ResourceId collRid = ResourceId.newDocumentCollectionId(dbRid.toString(), randomNextIntForTest(rnd, false));
        ResourceId parsedCollRid = ResourceId.parse(collRid.toString());
        assertThat(parsedCollRid.getDatabase()).isEqualTo(collRid.getDatabase());
        assertThat(parsedCollRid.getDocumentCollection()).isEqualTo(collRid.getDocumentCollection());

        ResourceId userRid = ResourceId.newUserId(dbRid.toString(), randomNextIntForTest(rnd, true));
        ResourceId parsedUserRid = ResourceId.parse(userRid.toString());
        assertThat(parsedUserRid.getDatabase()).isEqualTo(userRid.getDatabase());
        assertThat(parsedUserRid.getUser()).isEqualTo(userRid.getUser());

        ResourceId permissionRid = ResourceId.newPermissionId(userRid.toString(), randomNextIntForTest(rnd, false));
        ResourceId parsedPermissionRid = ResourceId.parse(permissionRid.toString());
        assertThat(parsedPermissionRid.getDatabase()).isEqualTo(permissionRid.getDatabase());
        assertThat(parsedPermissionRid.getUser()).isEqualTo(permissionRid.getUser());
        assertThat(parsedPermissionRid.getPermission()).isEqualTo(permissionRid.getPermission());

        ResourceId attachmentRid = ResourceId.newAttachmentId("wsIRALoBhyQ9AAAAAAAACA==", randomNextIntForTest(rnd, true));
        ResourceId parsedAttachmentRid = ResourceId.parse(attachmentRid.toString());
        assertThat(parsedAttachmentRid.getDatabase()).isEqualTo(attachmentRid.getDatabase());
        assertThat(parsedAttachmentRid.getDocumentCollection()).isEqualTo(attachmentRid.getDocumentCollection());
        assertThat(parsedAttachmentRid.getDocument()).isEqualTo(attachmentRid.getDocument());
    }
}
