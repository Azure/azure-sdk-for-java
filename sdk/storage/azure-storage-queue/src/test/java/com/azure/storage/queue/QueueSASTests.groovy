// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.credential.AzureSasCredential
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.SasProtocol
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.queue.models.QueueAccessPolicy
import com.azure.storage.queue.models.QueueSignedIdentifier
import com.azure.storage.queue.models.QueueStorageException
import com.azure.storage.queue.models.SendMessageResult
import com.azure.storage.queue.sas.QueueSasPermission
import com.azure.storage.queue.sas.QueueServiceSasSignatureValues
import spock.lang.Unroll

import java.time.Duration
import java.time.temporal.ChronoUnit

class QueueSASTests extends APISpec {

    QueueClient queueClient

    def setup() {
        primaryQueueServiceClient = queueServiceBuilderHelper().buildClient()
        queueClient = primaryQueueServiceClient.getQueueClient(namer.getRandomName(60))
    }

    @Unroll
    def "QueueSASPermission parse"() {
        when:
        def perms = QueueSasPermission.parse(permString)

        then:
        perms.hasReadPermission() == read
        perms.hasAddPermission() == add
        perms.hasUpdatePermission() == update
        perms.hasProcessPermission() == process

        where:
        permString || read  | add   | update | process
        "r"        || true  | false | false  | false
        "a"        || false | true  | false  | false
        "u"        || false | false | true   | false
        "p"        || false | false | false  | true
        "raup"     || true  | true  | true   | true
        "apru"     || true  | true  | true   | true
        "rap"      || true  | true  | false  | true
        "ur"       || true  | false | true   | false
    }

    @Unroll
    def "QueueSASPermission toString"() {
        setup:
        def perms = new QueueSasPermission()
            .setReadPermission(read)
            .setAddPermission(add)
            .setUpdatePermission(update)
            .setProcessPermission(process)

        expect:
        perms.toString() == expectedString

        where:
        read  | add   | update | process || expectedString
        true  | false | false  | false   || "r"
        false | true  | false  | false   || "a"
        false | false | true   | false   || "u"
        false | false | false  | true    || "p"
        true  | false | true   | false   || "ru"
        true  | true  | true   | true    || "raup"
    }

    def "QueueSASPermission parse IA"() {
        when:
        QueueSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    def "queueServiceSAS canonicalizedResource"() {
        setup:
        def queueName = queueClient.getQueueName()

        when:
        def serviceSASSignatureValues = new QueueServiceSasSignatureValues().setQueueName(queueName)

        then:
        serviceSASSignatureValues.getQueueName() == queueName
    }

    def "QueueSAS enqueue dequeue with permissions"() {
        setup:
        queueClient.create()
        SendMessageResult resp = queueClient.sendMessage("test")

        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
        def startTime = namer.getUtcNow().minusDays(1)
        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasProtocol = SasProtocol.HTTPS_HTTP

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString)
        def sasPermissions = new QueueServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setQueueName(queueClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode()

        def clientPermissions = queueBuilderHelper()
            .endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
            .sasToken(sasPermissions)
            .buildClient()
        clientPermissions.sendMessage("sastest")
        def dequeueMsgIterPermissions = clientPermissions.receiveMessages(2).iterator()

        then:
        notThrown(QueueStorageException)
        "test" == dequeueMsgIterPermissions.next().getMessageText()
        "sastest" == dequeueMsgIterPermissions.next().getMessageText()

        when:
        clientPermissions.updateMessage(resp.getMessageId(), resp.getPopReceipt(), "testing", Duration.ofHours(1))

        then:
        thrown(QueueStorageException)
    }

    def "QueueSAS update delete with permissions"() {
        setup:
        queueClient.create()
        SendMessageResult resp = queueClient.sendMessage("test")

        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setProcessPermission(true)
            .setUpdatePermission(true)
        def startTime = namer.getUtcNow().minusDays(1)
        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasProtocol = SasProtocol.HTTPS_HTTP

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString)
        def sasPermissions = new QueueServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setQueueName(queueClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode()

        def clientPermissions = queueBuilderHelper()
            .endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
            .sasToken(sasPermissions)
            .buildClient()
        clientPermissions.updateMessage(resp.getMessageId(), resp.getPopReceipt(), "testing", Duration.ZERO)
        def dequeueMsgIterPermissions = clientPermissions.receiveMessages(1).iterator()

        then:
        notThrown(QueueStorageException)
        "testing" == dequeueMsgIterPermissions.next().getMessageText()

        when:
        clientPermissions.delete()

        then:
        thrown(QueueStorageException)
    }

    // NOTE: Serializer for set access policy keeps milliseconds
    def "QueueSAS enqueue dequeue with identifier"() {
        setup:
        queueClient.create()
        queueClient.sendMessage("test")

        def permissions = new QueueSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setUpdatePermission(true)
            .setProcessPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1).truncatedTo(ChronoUnit.SECONDS)
        def startTime = namer.getUtcNow().minusDays(1).truncatedTo(ChronoUnit.SECONDS)

        QueueSignedIdentifier identifier = new QueueSignedIdentifier()
            .setId(namer.getRandomUuid())
            .setAccessPolicy(new QueueAccessPolicy().setPermissions(permissions.toString())
                .setExpiresOn(expiryTime).setStartsOn(startTime))
        queueClient.setAccessPolicy(Arrays.asList(identifier))

        // Wait 30 seconds as it may take time for the access policy to take effect.
        sleepIfLive(30000)

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString)
        def sasIdentifier = new QueueServiceSasSignatureValues()
            .setIdentifier(identifier.getId())
            .setQueueName(queueClient.getQueueName())
            .generateSasQueryParameters(credential)
            .encode()

        def clientBuilder = queueBuilderHelper()
        def clientIdentifier = clientBuilder
            .endpoint(queueClient.getQueueUrl())
            .queueName(queueClient.getQueueName())
            .sasToken(sasIdentifier)
            .buildClient()
        clientIdentifier.sendMessage("sastest")
        def dequeueMsgIterIdentifier = clientIdentifier.receiveMessages(2).iterator()

        then:
        notThrown(QueueStorageException)
        "test" == dequeueMsgIterIdentifier.next().getMessageText()
        "sastest" == dequeueMsgIterIdentifier.next().getMessageText()
    }

    def "AccountSAS create delete queue"() {
        def service = new AccountSasService()
            .setQueueAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString)
        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(credential)
            .encode()

        def scBuilder = queueServiceBuilderHelper()
        scBuilder.endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()
        def queueName = namer.getRandomName(60)
        sc.createQueue(queueName)

        then:
        notThrown(QueueStorageException)

        when:
        sc.deleteQueue(queueName)

        then:
        notThrown(QueueStorageException)
    }

    def "AccountSAS list queues"() {
        def service = new AccountSasService()
            .setQueueAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setListPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def credential = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString)
        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(credential)
            .encode()

        def scBuilder = queueServiceBuilderHelper()
        scBuilder.endpoint(primaryQueueServiceClient.getQueueServiceUrl())
            .sasToken(sas)
        def sc = scBuilder.buildClient()

        sc.listQueues()

        then:
        notThrown(QueueStorageException)
    }

    def "AccountSAS network on endpoint"() {
        setup:
        def service = new AccountSasService()
            .setQueueAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
            .setWritePermission(true)
            .setListPermission(true)
            .setDeletePermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(environment.primaryAccount.credential)
            .encode()

        def queueName = namer.getRandomName(60)

        when:
        def sc = getServiceClientBuilder(null, primaryQueueServiceClient.getQueueServiceUrl() + "?" + sas, null).buildClient()
        sc.createQueue(queueName)

        def qc = getQueueClientBuilder(primaryQueueServiceClient.getQueueServiceUrl() + "/" + queueName + "?" + sas).buildClient()
        qc.delete()

        then:
        notThrown(Exception)
    }

    def "can use sas to authenticate"() {
        setup:
        def service = new AccountSasService()
            .setQueueAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryQueueServiceClient.generateAccountSas(sasValues)

        queueClient.create()

        when:
        instrument(new QueueClientBuilder()
            .endpoint(queueClient.getQueueUrl())
            .sasToken(sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new QueueClientBuilder()
            .endpoint(queueClient.getQueueUrl())
            .credential(new AzureSasCredential(sas)))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new QueueClientBuilder()
            .endpoint(queueClient.getQueueUrl() + "?" + sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new QueueServiceClientBuilder()
            .endpoint(queueClient.getQueueUrl())
            .sasToken(sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new QueueServiceClientBuilder()
            .endpoint(queueClient.getQueueUrl())
            .credential(new AzureSasCredential(sas)))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new QueueServiceClientBuilder()
            .endpoint(queueClient.getQueueUrl() + "?" + sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()
    }
}
