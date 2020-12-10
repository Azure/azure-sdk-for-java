// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.util.Context
import com.azure.storage.blob.implementation.util.BlobSasImplUtil
import com.azure.storage.blob.models.UserDelegationKey
import com.azure.storage.blob.sas.BlobContainerSasPermission
import com.azure.storage.blob.sas.BlobSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import spock.lang.Specification
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.ZoneOffset

class BlobServiceSasModelsTest extends Specification {

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
    }

    @Unroll
    def "BlobSASPermissions toString"() {
        setup:
        def perms = new BlobSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)
            .setDeleteVersionPermission(deleteVersion)
            .setTagsPermission(tags)
            .setListPermission(list)
            .setMovePermission(move)
            .setExecutePermission(execute)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | create | add   | deleteVersion | tags  | list  | move  | execute || expectedString
        true  | false | false  | false  | false | false         | false | false | false | false   || "r"
        false | true  | false  | false  | false | false         | false | false | false | false   || "w"
        false | false | true   | false  | false | false         | false | false | false | false   || "d"
        false | false | false  | true   | false | false         | false | false | false | false   || "c"
        false | false | false  | false  | true  | false         | false | false | false | false   || "a"
        false | false | false  | false  | false | true          | false | false | false | false   || "x"
        false | false | false  | false  | false | false         | true  | false | false | false   || "t"
        false | false | false  | false  | false | false         | false | true  | false | false   || "l"
        false | false | false  | false  | false | false         | false | false | true  | false   || "m"
        false | false | false  | false  | false | false         | false | false | false | true    || "e"
        true  | true  | true   | true   | true  | true          | true  | true  | true  | true    || "racwdxltme"
    }

    @Unroll
    def "BlobSASPermissions parse"() {
        when:
        def perms = BlobSasPermission.parse(permString)

        then:
        perms.hasReadPermission() == read
        perms.hasWritePermission() == write
        perms.hasDeletePermission() == delete
        perms.hasCreatePermission() == create
        perms.hasAddPermission() == add
        perms.hasDeleteVersionPermission() == deleteVersion
        perms.hasTagsPermission() == tags
        perms.hasListPermission() == list
        perms.hasMovePermission() == move
        perms.hasExecutePermission() == execute

        where:
        permString    || read  | write | delete | create | add   | deleteVersion | tags  | list  | move  | execute
        "r"           || true  | false | false  | false  | false | false         | false | false | false | false
        "w"           || false | true  | false  | false  | false | false         | false | false | false | false
        "d"           || false | false | true   | false  | false | false         | false | false | false | false
        "c"           || false | false | false  | true   | false | false         | false | false | false | false
        "a"           || false | false | false  | false  | true  | false         | false | false | false | false
        "x"           || false | false | false  | false  | false | true          | false | false | false | false
        "t"           || false | false | false  | false  | false | false         | true  | false | false | false
        "l"           || false | false | false  | false  | false | false         | false | true  | false | false
        "m"           || false | false | false  | false  | false | false         | false | false | true  | false
        "e"           || false | false | false  | false  | false | false         | false | false | false | true
        "racwdxltme"  || true  | true  | true   | true   | true  | true          | true  | true  | true  | true
        "dtcxewlrma"  || true  | true  | true   | true   | true  | true          | true  | true  | true  | true
    }

    def "BlobSASPermissions parse IA"() {
        when:
        BlobSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    def "BlobSasPermission null"() {
        when:
        BlobSasPermission.parse(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "ContainerSASPermissions toString"() {
        setup:
        def perms = new BlobContainerSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)
            .setListPermission(list)
            .setDeleteVersionPermission(deleteVersion)
            .setTagsPermission(tags)
            .setMovePermission(move)
            .setExecutePermission(execute)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | create | add   | deleteVersion | tags  | list  | move  | execute || expectedString
        true  | false | false  | false  | false | false         | false | false | false | false   || "r"
        false | true  | false  | false  | false | false         | false | false | false | false   || "w"
        false | false | true   | false  | false | false         | false | false | false | false   || "d"
        false | false | false  | true   | false | false         | false | false | false | false   || "c"
        false | false | false  | false  | true  | false         | false | false | false | false   || "a"
        false | false | false  | false  | false | true          | false | false | false | false   || "x"
        false | false | false  | false  | false | false         | true  | false | false | false   || "t"
        false | false | false  | false  | false | false         | false | true  | false | false   || "l"
        false | false | false  | false  | false | false         | false | false | true  | false   || "m"
        false | false | false  | false  | false | false         | false | false | false | true    || "e"
        true  | true  | true   | true   | true  | true          | true  | true  | true  | true    || "racwdxltme"
    }

    @Unroll
    def "ContainerSASPermissions parse"() {
        when:
        def perms = BlobContainerSasPermission.parse(permString)

        then:
        perms.hasReadPermission() == read
        perms.hasWritePermission() == write
        perms.hasDeletePermission() == delete
        perms.hasCreatePermission() == create
        perms.hasAddPermission() == add
        perms.hasDeleteVersionPermission() == deleteVersion
        perms.hasTagsPermission() == tags
        perms.hasListPermission() == list
        perms.hasMovePermission() == move
        perms.hasExecutePermission() == execute

        where:
        permString    || read  | write | delete | create | add   | deleteVersion | tags  | list  | move  | execute
        "r"           || true  | false | false  | false  | false | false         | false | false | false | false
        "w"           || false | true  | false  | false  | false | false         | false | false | false | false
        "d"           || false | false | true   | false  | false | false         | false | false | false | false
        "c"           || false | false | false  | true   | false | false         | false | false | false | false
        "a"           || false | false | false  | false  | true  | false         | false | false | false | false
        "x"           || false | false | false  | false  | false | true          | false | false | false | false
        "t"           || false | false | false  | false  | false | false         | true  | false | false | false
        "l"           || false | false | false  | false  | false | false         | false | true  | false | false
        "m"           || false | false | false  | false  | false | false         | false | false | true  | false
        "e"           || false | false | false  | false  | false | false         | false | false | false | true
        "racwdxltme"  || true  | true  | true   | true   | true  | true          | true  | true  | true  | true
        "dtcxewlrma"  || true  | true  | true   | true   | true  | true          | true  | true  | true  | true
    }

    def "ContainerSASPermissions parse IA"() {
        when:
        BlobContainerSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    def "ContainerSASPermissions null"() {
        when:
        BlobContainerSasPermission.parse(null)

        then:
        thrown(NullPointerException)
    }

    def "blob sas impl util null"() {
        setup:
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def p = new BlobSasPermission().setReadPermission(true)
        def v = new BlobServiceSasSignatureValues(e, p)
        def implUtil = new BlobSasImplUtil(v, "containerName", "blobName", null, null)

        when:
        implUtil.generateSas(null, Context.NONE)

        then:
        def ex = thrown(NullPointerException)
        ex.getMessage().contains("storageSharedKeyCredential")

        when:
        implUtil.generateUserDelegationSas(null, "accountName", Context.NONE)

        then:
        ex = thrown(NullPointerException)
        ex.getMessage().contains("delegationKey")

        when:
        implUtil.generateUserDelegationSas(new UserDelegationKey(), null, Context.NONE)

        then:
        ex = thrown(NullPointerException)
        ex.getMessage().contains("accountName")
    }

    def "ensure state version"() {
        when:
        BlobSasImplUtil implUtil = new BlobSasImplUtil(new BlobServiceSasSignatureValues("id"), "container")
        implUtil.version = null
        implUtil.ensureState()

        then:
        implUtil.version // Version is set
        implUtil.resource == "c" // Default resource is container
        !implUtil.permissions // Identifier was used so permissions is null
    }

    def "ensure state illegal argument"() {
        when:
        BlobSasImplUtil implUtil = new BlobSasImplUtil(new BlobServiceSasSignatureValues(), null)

        implUtil.ensureState()

        then:
        thrown(IllegalStateException)
    }

    @Unroll
    def "ensure state resource and permission"() {
        setup:
        def expiryTime = OffsetDateTime.now().plusDays(1)

        expect:
        BlobSasImplUtil implUtil = new BlobSasImplUtil(new BlobServiceSasSignatureValues(expiryTime, permission), container, blob, snapshot, versionId)
        implUtil.ensureState()
        implUtil.resource == resource
        implUtil.permissions ==  permissionString

        where:
        container    | blob    | snapshot    | versionId | permission                                                                       || resource | permissionString
        "container"  |  null   | null        | null      | new BlobContainerSasPermission().setReadPermission(true).setListPermission(true) || "c"      | "rl"
        "container"  | "blob"  | null        | null      | new BlobSasPermission().setReadPermission(true)                                  || "b"      | "r"
        "container"  | "blob"  | "snapshot"  | null      | new BlobSasPermission().setReadPermission(true)                                  || "bs"     | "r"
        "container"  | "blob"  | null        | "version" | new BlobSasPermission().setReadPermission(true)                                  || "bv"     | "r"
    }
}
