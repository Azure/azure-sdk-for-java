// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common

import com.azure.core.credential.AzureNamedKeyCredential
import com.azure.storage.common.implementation.StorageImplUtils
import spock.lang.Specification

class StorageSharedKeyCredentialTest extends Specification {

    def "Can map from AzureNamedKeyCredential"() {
        setup:
        def name = "foo"
        def key = "bar"
        def randomString = UUID.randomUUID().toString()
        def namedKey = new AzureNamedKeyCredential(name, key)

        when:
        def storageKey = StorageSharedKeyCredential.fromAzureNamedKeyCredential(namedKey)
        def expectedSignature = StorageImplUtils.computeHMac256(key, randomString)
        def signature = storageKey.computeHmac256(randomString)

        then:
        storageKey.accountName == namedKey.azureNamedKey.name
        signature == expectedSignature
    }

    def "Can rotate key"() {
        setup:
        def name = "foo"
        def key1 = "bar1"
        def key2 = "bar2"
        def randomString = UUID.randomUUID().toString()
        def namedKey = new AzureNamedKeyCredential(name, key1)

        when:
        def storageKey = StorageSharedKeyCredential.fromAzureNamedKeyCredential(namedKey)
        def signature1 = storageKey.computeHmac256(randomString)
        namedKey.update(name, key2)
        def signature2 = storageKey.computeHmac256(randomString)
        def expectedSignature = StorageImplUtils.computeHMac256(key2, randomString)


        then:
        storageKey.accountName == namedKey.azureNamedKey.name
        signature1 != signature2
        signature2 == expectedSignature
    }

    def "Can parse valid connection string"() {
        when:
        def storageSharedKeyCredential = StorageSharedKeyCredential.fromConnectionString(connectionString)

        then:
        storageSharedKeyCredential.getAccountName() == "teststorage"

        where:
        connectionString || _
        "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net" || _
        "DefaultEndpointsProtocol=https;accountName=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net" || _
        "DefaultEndpointsProtocol=https;AccountName=teststorage;accountKey=atestaccountkey;EndpointSuffix=core.windows.net" || _
        "DefaultEndpointsProtocol=https;Accountname=teststorage;accountKey=atestaccountkey;EndpointSuffix=core.windows.net" || _
        "DefaultEndpointsProtocol=https;AccountName=teststorage;accountkey=atestaccountkey;EndpointSuffix=core.windows.net" || _
        "DefaultEndpointsProtocol=https;accountname=teststorage;accountkey=atestaccountkey;EndpointSuffix=core.windows.net" || _
    }

    def "Cannot parse invalid connection string"() {
        when:
        StorageSharedKeyCredential.fromConnectionString(connectionString)

        then:
        thrown(IllegalArgumentException)

        where:
        connectionString                                                                                                     || _
        "DefaultEndpointsProtocol=https;EndpointSuffix=core.windows.net"                                                     || _
        "DefaultEndpointsProtocol=https;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net"                          || _
        "DefaultEndpointsProtocol=https;AccountName=teststorage;EndpointSuffix=core.windows.net"                             || _
        "DefaultEndpointsProtocol=https;AccountName =teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net" || _
        "DefaultEndpointsProtocol=https;AccountName=teststorage;AccountKey =atestaccountkey;EndpointSuffix=core.windows.net" || _
        "DefaultEndpointsProtocol=https;Account Name=teststorage;AccountKey=atestaccountkey;EndpointSuffix=core.windows.net" || _
        "DefaultEndpointsProtocol=https;AccountName=teststorage;Account Key=atestaccountkey;EndpointSuffix=core.windows.net" || _
    }
}
