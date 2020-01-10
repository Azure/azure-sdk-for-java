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

package com.azure.storage.blob.nio

import reactor.core.publisher.Flux
import spock.lang.Unroll

class AzureFileSystemSpec extends APISpec {

    @Unroll
    def "FileSystem create"() {
        setup:
        def containerNames = Flux.range(0, numContainers).map { i -> generateContainerName() }.cache()
            .toIterable()
        def config = [:]
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = String.join(",", containerNames)
        config[AzureFileSystem.AZURE_STORAGE_ACCOUNT_KEY] = getAccountKey(PRIMARY_STORAGE)
        config[AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT] = getHttpClient()

        when:
        def fileSystem = new AzureFileSystem(new AzureFileSystemProvider(), primaryCredential.getAccountName(),
            config)

        then:
        Flux.fromIterable(fileSystem.getFileStores()).count().block().intValue() == containerNames.size()
        for (def containerName : containerNames) {
            assert Flux.fromIterable(fileSystem.getFileStores()).map { store -> store.name() }
                .hasElement(containerName)
            assert primaryBlobServiceClient.getBlobContainerClient(containerName).exists()
        }

        where:
        numContainers | createContainers
        1             | false
        3             | false
        3             | true
//        numContainers | sameAccount
//        1             | true
//        3             | true
//        1             | false
//        4             | false
    }

//    def "FileStore create fail"(){
//        setup:
//        def storeName = generateContainerName()
//        def parentFileSystem = mock(AzureFileSystem)
//        given(parentFileSystem.getBlobServiceClient()).willReturn(primaryBlobServiceClient)
//        def containerClient = primaryBlobServiceClient.getBlobContainerClient(storeName)
//        containerClient.create()
//        containerClient.delete()
//
//        when:
//        new AzureFileStore(parentFileSystem, storeName)
//
//        then:
//        thrown(IOException)
//    }

    def "FileSystem create fail"() {
        // Add a hidden option to pass in an HttpClient for debugging. Also make the map accept the actual type instead of Strings so its easier
        // to parse and to make it easier to pass a client. Update doc specifying the types.
    }

    // "FileSystem create configurations"()
}
