/*
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

package com.microsoft.azure.storage;

import com.microsoft.azure.storage.blob.*;
import com.microsoft.rest.v2.RestException;
import com.microsoft.azure.storage.blob.models.*;
import com.microsoft.rest.v2.http.*;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.Flowable;
import io.reactivex.functions.BiConsumer;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.MalformedURLException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class BlobStorageAPITests {

    @Test
    public void TestPutBlobBasic() throws IOException, InvalidKeyException, InterruptedException {
        /**
         * This library uses the Azure Rest Pipeline to make its requests. Details on this pipeline can be found here:
         * https://github.com/Azure/azure-pipeline-go/blob/master/pipeline/doc.go All references to HttpPipeline and
         * the like refer to this structure.
         * This library uses Microsoft AutoRest to generate the protocol layer off of the Swagger API spec of the
         * blob service. All files in the implementation and models folders as well as the Interfaces in the root
         * directory are auto-generated using this tool.
         * This library's paradigm is centered around the URL object. A URL is constructed to a resource, such as
         * BlobURL. This is solely a reference to a location; the existence of a BlobURL does not indicate the existence
         * of a blob or hold any state related to the blob. The URL objects define methods for all operations related
         * to that resource (or will eventually; some are not supported in the library yet).
         * Several structures are defined on top of the auto-generated protocol layer to logically group items or
         * concepts relevant to a given operation or resource. This both reduces the length of the parameter list
         * and provides some coherency and relationship of ideas to aid the developer, improving efficiency and
         * discoverability.
         * In this sample test, we demonstrate the use of all APIs that are currently implemented. They have been tested
         * to work in these cases, but they have not been thoroughly tested. More advanced operations performed by
         * specifying or modifying calls in this test are not guaranteed to work. APIs not shown here are not guaranteed
         * to work. Any reports on bugs found will be welcomed and addressed.
         */


        // Creating a pipeline requires a credentials object and a structure of pipeline options to customize the behavior.
        // Set your system environment variables of ACCOUNT_NAME and ACCOUNT_KEY to pull the appropriate credentials.
        // Credentials may be SharedKey as shown here or Anonymous as shown below.
        SharedKeyCredentials creds = new SharedKeyCredentials(System.getenv().get("ACCOUNT_NAME"),
                System.getenv().get("ACCOUNT_KEY"));

        // Currently only the default PipelineOptions are supported.
        PipelineOptions po = new PipelineOptions();
        HttpClientConfiguration configuration = new HttpClientConfiguration(
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)));
        po.client = HttpClient.createDefault();//configuration);
        HttpPipeline pipeline = StorageURL.createPipeline(creds, po);

        // Create a reference to the service.
        ServiceURL su = new ServiceURL(
                new URL("http://" + System.getenv().get("ACCOUNT_NAME") + ".blob.core.windows.net"), pipeline);

        // Create a reference to a container. Using the ServiceURL to create the ContainerURL appends
        // the container name to the ServiceURL. A ContainerURL may also be created by calling its
        // constructor with a full path to the container and a pipeline.
        String containerName = "javatestcontainer" + System.currentTimeMillis();
        ContainerURL cu = su.createContainerURL(containerName);

        // Create a reference to a blob. Same pattern as containers.
        BlockBlobURL bu = cu.createBlockBlobURL("javatestblob");
        try {
            // Calls to blockingGet force the call to be synchronous. This whole test is synchronous.
            // APIs will typically return a RestResponse<*HeadersType*, *BodyType*>. It is therefore possible to
            // retrieve the headers and the deserialized body of every request. If there is no body in the request,
            // the body type will be Void.
            // Errors are thrown as exceptions in the synchronous (blockingGet) case.

            // Create the container. NOTE: Metadata is not currently supported on any resource.

            cu.create(null, PublicAccessType.BLOB).blockingGet();

            // Create the blob with a single put. See below for the stageBlock(List) scenario.
            bu.upload(Flowable.just(ByteBuffer.wrap(new byte[]{0, 0, 0})), 3, null,
                    null, null).blockingGet();

            // Download the blob contents.
            Flowable<ByteBuffer> data;
            byte[] dataByte;


            // SAS -----------------------------
            // Parses a URL into its constituent components. This structure's URL fields may be modified.
            BlobURLParts parts = URLParser.parse(bu.toURL());

            // Construct the AccountSASSignatureValues values object. This encapsulates all the values needed to create an AccountSASSignatureValues.
            AccountSASSignatureValues sas = new AccountSASSignatureValues();
            AccountSASPermission perms = new AccountSASPermission();
            perms.read = true;
            perms.write = true;
            AccountSASService service = new AccountSASService();
            service.blob = true;
            AccountSASResourceType resourceType = new AccountSASResourceType();
            resourceType.object = true;
            sas.version = "2016-05-31";
            sas.protocol = SASProtocol.HTTPS_HTTP;
            sas.startTime = null;
            sas.expiryTime = OffsetDateTime.now().plusDays(1);
            sas.permissions = perms.toString();
            sas.ipRange = null;
            sas.services = service.toString();
            sas.resourceTypes = resourceType.toString();

            // Construct a ServiceSASSignatureValues in a pattern similar to that of the AccountSASSignatureValues.
            // Comment out the AccountSASSignatureValues creation and uncomment this to run with ServiceSASSignatureValues.
            /*ServiceSASSignatureValues sas = new ServiceSASSignatureValues("2016-05-31", SASProtocol.HTTPS_HTTP,
                    DateTime.now().minusDays(1).toDate(), DateTime.now().plusDays(1).toDate(),
                    EnumSet.of(ContainerSASPermission.READ, ContainerSASPermission.WRITE),
                    null, containerName, null, null,
                    null, null, null, null);*/


            // generateSASQueryParameters hashes the sas using your account's credentials and then associates the
            // sasQueryParameters with the blobURLParts.
            parts.sasQueryParameters = sas.generateSASQueryParameters(creds);

            // Using a SAS requires AnonymousCredentials on the pipeline.
            pipeline = StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions());

            // Call toURL on the parts to get a string representation of the URL. This, along with the pipeline,
            // is used to create a new BlockBlobURL object.
            BlockBlobURL sasBlob = new BlockBlobURL(parts.toURL(), pipeline);

            // Download the blob using the SAS. To perform other operations, ensure the appropriate permissions are
            // specified above.
            data = sasBlob.download(new BlobRange(0L, 3L), null, false).blockingGet().body();
            dataByte = FlowableUtil.collectBytesInArray(data).blockingGet();
            assertArrayEquals(dataByte, new byte[]{0, 0, 0});


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            // Delete the blob and container. Deleting a container does not require deleting the blobs first.
            // This is just for demonstration purposes.
            try {
                bu.delete(DeleteSnapshotsOptionType.INCLUDE, null).blockingGet();
            } finally {
                cu.delete(null).blockingGet();
            }
        }
    }
}