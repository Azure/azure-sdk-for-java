// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

import com.azure.core.util.Context
import com.azure.storage.file.datalake.implementation.util.DataLakeSasImplUtil
import com.azure.storage.file.datalake.models.UserDelegationKey
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues
import com.azure.storage.file.datalake.sas.FileSystemSasPermission
import com.azure.storage.file.datalake.sas.PathSasPermission;
import spock.lang.Specification
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.ZoneOffset

class DataLakeServiceSasModelsTest extends Specification {

    @Unroll
    def "PathSASPermissions toString"() {
        setup:
        def perms = new PathSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)
            .setListPermission(list)
            .setMovePermission(move)
            .setExecutePermission(execute)
            .setManageOwnershipPermission(owner)
            .setManageAccessControlPermission(permission)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | create | add   | list  | move  | execute | owner | permission  || expectedString
        true  | false | false  | false  | false | false | false | false   | false | false       || "r"
        false | true  | false  | false  | false | false | false | false   | false | false       || "w"
        false | false | true   | false  | false | false | false | false   | false | false       || "d"
        false | false | false  | true   | false | false | false | false   | false | false       || "c"
        false | false | false  | false  | true  | false | false | false   | false | false       || "a"
        false | false | false  | false  | false | true  | false | false   | false | false       || "l"
        false | false | false  | false  | false | false | true  | false   | false | false       || "m"
        false | false | false  | false  | false | false | false | true    | false | false       || "e"
        false | false | false  | false  | false | false | false | false   | true  | false       || "o"
        false | false | false  | false  | false | false | false | false   | false | true        || "p"
        true  | true  | true   | true   | true  | true  | true  | true    | true  | true        || "racwdlmeop"
    }

    @Unroll
    def "PathSASPermissions parse"() {
        when:
        def perms = PathSasPermission.parse(permString)

        then:
        perms.hasReadPermission() == read
        perms.hasWritePermission() == write
        perms.hasDeletePermission() == delete
        perms.hasCreatePermission() == create
        perms.hasAddPermission() == add
        perms.hasListPermission() == list
        perms.hasMovePermission() == move
        perms.hasExecutePermission() == execute
        perms.hasManageOwnershipPermission() == owner
        perms.hasManageAccessControlPermission() == permission

        where:
        permString   || read  | write | delete | create | add   | list  | move  | execute | owner | permission
        "r"          || true  | false | false  | false  | false | false | false | false   | false | false
        "w"          || false | true  | false  | false  | false | false | false | false   | false | false
        "d"          || false | false | true   | false  | false | false | false | false   | false | false
        "c"          || false | false | false  | true   | false | false | false | false   | false | false
        "a"          || false | false | false  | false  | true  | false | false | false   | false | false
        "l"          || false | false | false  | false  | false | true  | false | false   | false | false
        "m"          || false | false | false  | false  | false | false | true  | false   | false | false
        "e"          || false | false | false  | false  | false | false | false | true    | false | false
        "o"          || false | false | false  | false  | false | false | false | false   | true  | false
        "p"          || false | false | false  | false  | false | false | false | false   | false | true
        "racwdlmeop" || true  | true  | true   | true   | true  | true  | true  | true    | true  | true
        "malwdcrepo" || true  | true  | true   | true   | true  | true  | true  | true    | true  | true
    }

    def "PathSASPermissions parse IA"() {
        when:
        PathSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    def "PathSasPermission null"() {
        when:
        PathSasPermission.parse(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "FileSystemSASPermissions toString"() {
        setup:
        def perms = new FileSystemSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)
            .setListPermission(list)
            .setMovePermission(move)
            .setExecutePermission(execute)
            .setManageOwnershipPermission(owner)
            .setManageAccessControlPermission(permission)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | create | add   | list  | move  | execute | owner | permission  || expectedString
        true  | false | false  | false  | false | false | false | false   | false | false       || "r"
        false | true  | false  | false  | false | false | false | false   | false | false       || "w"
        false | false | true   | false  | false | false | false | false   | false | false       || "d"
        false | false | false  | true   | false | false | false | false   | false | false       || "c"
        false | false | false  | false  | true  | false | false | false   | false | false       || "a"
        false | false | false  | false  | false | true  | false | false   | false | false       || "l"
        false | false | false  | false  | false | false | true  | false   | false | false       || "m"
        false | false | false  | false  | false | false | false | true    | false | false       || "e"
        false | false | false  | false  | false | false | false | false   | true  | false       || "o"
        false | false | false  | false  | false | false | false | false   | false | true        || "p"
        true  | true  | true   | true   | true  | true  | true  | true    | true  | true        || "racwdlmeop"
    }

    @Unroll
    def "FileSystemSASPermissions parse"() {
        when:
        def perms = FileSystemSasPermission.parse(permString)

        then:
        perms.hasReadPermission() == read
        perms.hasWritePermission() == write
        perms.hasDeletePermission() == delete
        perms.hasCreatePermission() == create
        perms.hasAddPermission() == add
        perms.hasListPermission() == list
        perms.hasMovePermission() == move
        perms.hasExecutePermission() == execute
        perms.hasManageOwnershipPermission() == owner
        perms.hasManageAccessControlPermission() == permission

        where:
        permString   || read  | write | delete | create | add   | list  | move  | execute | owner | permission
        "r"          || true  | false | false  | false  | false | false | false | false   | false | false
        "w"          || false | true  | false  | false  | false | false | false | false   | false | false
        "d"          || false | false | true   | false  | false | false | false | false   | false | false
        "c"          || false | false | false  | true   | false | false | false | false   | false | false
        "a"          || false | false | false  | false  | true  | false | false | false   | false | false
        "l"          || false | false | false  | false  | false | true  | false | false   | false | false
        "m"          || false | false | false  | false  | false | false | true  | false   | false | false
        "e"          || false | false | false  | false  | false | false | false | true    | false | false
        "o"          || false | false | false  | false  | false | false | false | false   | true  | false
        "p"          || false | false | false  | false  | false | false | false | false   | false | true
        "racwdlmeop" || true  | true  | true   | true   | true  | true  | true  | true    | true  | true
        "malwdcrepo" || true  | true  | true   | true   | true  | true  | true  | true    | true  | true
    }

    def "FileSystemSASPermissions parse IA"() {
        when:
        FileSystemSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    def "FileSystemSASPermissions null"() {
        when:
        FileSystemSasPermission.parse(null)

        then:
        thrown(NullPointerException)
    }

    def "path sas impl util null"() {
        setup:
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def p = new PathSasPermission().setReadPermission(true)
        def v = new DataLakeServiceSasSignatureValues(e, p)
        def implUtil = new DataLakeSasImplUtil(v, "containerName", "blobName", false)

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

    @Unroll
    def "ensure state resource and permission"() {
        setup:
        def expiryTime = OffsetDateTime.now().plusDays(1)

        expect:
        DataLakeSasImplUtil implUtil = new DataLakeSasImplUtil(new DataLakeServiceSasSignatureValues(expiryTime, permission), container, blob, isDirectory)
        implUtil.ensureState()
        implUtil.resource == resource
        implUtil.permissions ==  permissionString
        implUtil.directoryDepth == directoryDepth

        where:
        container    | blob              | isDirectory | permission                                                                       || resource | permissionString | directoryDepth
        "container"  |  null             | false       | new FileSystemSasPermission().setReadPermission(true).setListPermission(true)    || "c"      | "rl"             | null
        "container"  | "blob"            | false       | new PathSasPermission().setReadPermission(true)                                  || "b"      | "r"              | null
        "container"  | "/"               | true        | new PathSasPermission().setReadPermission(true)                                  || "d"      | "r"              | 0
        "container"  | "blob/"           | true        | new PathSasPermission().setReadPermission(true)                                  || "d"      | "r"              | 1
        "container"  | "blob/dir1"       | true        | new PathSasPermission().setReadPermission(true)                                  || "d"      | "r"              | 2
        "container"  | "blob/dir1/dir2"  | true        | new PathSasPermission().setReadPermission(true)                                  || "d"      | "r"              | 3
    }

    def "ensure state aad id illegal state"() {
        setup:
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def p = new FileSystemSasPermission().setReadPermission(true).setListPermission(true)

        when:
        def v = new DataLakeServiceSasSignatureValues(e, p)
            .setPreauthorizedAgentObjectId("authorizedId")
            .setAgentObjectId("unauthorizedId")
        DataLakeSasImplUtil implUtil = new DataLakeSasImplUtil(v, "containerName", "blobName", true)

        implUtil.ensureState()

        then:
        thrown(IllegalStateException)
    }
}
