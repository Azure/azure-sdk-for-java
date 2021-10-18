// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation

import com.azure.core.util.Context
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.SasIpRange
import com.azure.storage.common.sas.SasProtocol
import spock.lang.Specification
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.ZoneOffset

class SasModelsTest extends Specification {

    @Unroll
    def "AccountSasSignatureValues min"() {
        setup:
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def p = AccountSasPermission.parse("l")
        def s = AccountSasService.parse("b")
        def rt = AccountSasResourceType.parse("o")

        def st = OffsetDateTime.of(2015, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def ip = SasIpRange.parse("a-b")
        def prot = SasProtocol.HTTPS_ONLY

        when:
        def v = new AccountSasSignatureValues(e, p, s, rt)
            .setStartTime(st)
            .setSasIpRange(ip)
            .setProtocol(prot)

        then:
        v.getExpiryTime() == e
        v.getPermissions() == p.toString()
        v.getServices() == s.toString()
        v.getResourceTypes() == rt.toString()
        v.getStartTime() == st
        v.getSasIpRange() == ip
        v.getProtocol() == prot
    }


    @Unroll
    def "AccountSasSignatureValues null"() {
        setup:
        def e = expiryTime ? null : OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def p = permissions ? null : AccountSasPermission.parse("l")
        def s = services ? null : AccountSasService.parse("b")
        def rt = resourceTypes ? null : AccountSasResourceType.parse("o")

        when:
        new AccountSasSignatureValues(e, p, s, rt)

        then:
        def ex = thrown(NullPointerException)
        ex.getMessage().contains(variable)

        where:
        expiryTime | permissions | services | resourceTypes || variable
        true       | false       | false    | false         || "expiryTime"
        false      | true        | false    | false         || "permissions"
        false      | false       | true     | false         || "services"
        false      | false       | false    | true          || "resourceTypes"
    }

    @Unroll
    def "AccountSASPermissions toString"() {
        setup:
        def perms = new AccountSasPermission()
        perms.setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setListPermission(list)
            .setAddPermission(add)
            .setCreatePermission(create)
            .setUpdatePermission(update)
            .setProcessMessages(process)
            .setDeleteVersionPermission(deleteVersion)
            .setTagsPermission(tags)
            .setFilterTagsPermission(filterTags)
            .setImmutabilityPolicyPermission(setImmutabilityPolicy)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | list  | add   | create | update | process | deleteVersion | tags  | filterTags | setImmutabilityPolicy || expectedString
        true  | false | false  | false | false | false  | false  | false   | false         | false | false      | false                 || "r"
        false | true  | false  | false | false | false  | false  | false   | false         | false | false      | false                 || "w"
        false | false | true   | false | false | false  | false  | false   | false         | false | false      | false                 || "d"
        false | false | false  | true  | false | false  | false  | false   | false         | false | false      | false                 || "l"
        false | false | false  | false | true  | false  | false  | false   | false         | false | false      | false                 || "a"
        false | false | false  | false | false | true   | false  | false   | false         | false | false      | false                 || "c"
        false | false | false  | false | false | false  | true   | false   | false         | false | false      | false                 || "u"
        false | false | false  | false | false | false  | false  | true    | false         | false | false      | false                 || "p"
        false | false | false  | false | false | false  | false  | false   | true          | false | false      | false                 || "x"
        false | false | false  | false | false | false  | false  | false   | false         | true  | false      | false                 || "t"
        false | false | false  | false | false | false  | false  | false   | false         | false | true       | false                 || "f"
        false | false | false  | false | false | false  | false  | false   | false         | false | false      | true                  || "i"
        true  | true  | true   | true  | true  | true   | true   | true    | true          | true  | true       | true                  || "rwdxlacuptfi"
    }

    @Unroll
    def "AccountSASPermissions parse"() {
        when:
        def perms = AccountSasPermission.parse(permString)

        then:
        perms.hasReadPermission() == read
        perms.hasWritePermission() == write
        perms.hasDeletePermission() == delete
        perms.hasListPermission() == list
        perms.hasAddPermission() == add
        perms.hasCreatePermission() == create
        perms.hasUpdatePermission() == update
        perms.hasProcessMessages() == process
        perms.hasDeleteVersionPermission() == deleteVersion
        perms.hasTagsPermission() == tags
        perms.hasFilterTagsPermission() == filterTags
        perms.hasImmutabilityPolicyPermission() == immutabilityPolicy

        where:
        permString     || read  | write | delete | list  | add   | create | update | process | deleteVersion | tags  | filterTags | immutabilityPolicy
        "r"            || true  | false | false  | false | false | false  | false  | false   | false         | false | false      | false
        "w"            || false | true  | false  | false | false | false  | false  | false   | false         | false | false      | false
        "d"            || false | false | true   | false | false | false  | false  | false   | false         | false | false      | false
        "l"            || false | false | false  | true  | false | false  | false  | false   | false         | false | false      | false
        "a"            || false | false | false  | false | true  | false  | false  | false   | false         | false | false      | false
        "c"            || false | false | false  | false | false | true   | false  | false   | false         | false | false      | false
        "u"            || false | false | false  | false | false | false  | true   | false   | false         | false | false      | false
        "p"            || false | false | false  | false | false | false  | false  | true    | false         | false | false      | false
        "x"            || false | false | false  | false | false | false  | false  | false   | true          | false | false      | false
        "t"            || false | false | false  | false | false | false  | false  | false   | false         | true  | false      | false
        "f"            || false | false | false  | false | false | false  | false  | false   | false         | false | true       | false
        "i"            || false | false | false  | false | false | false  | false  | false   | false         | false | false      | true
        "rwdxlacuptfi" || true  | true  | true   | true  | true  | true   | true   | true    | true          | true  | true       | true
        "lwfriutpcaxd" || true  | true  | true   | true  | true  | true   | true   | true    | true          | true  | true       | true
    }

    def "AccountSASPermissions parse IA"() {
        when:
        AccountSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "AccountSASResourceType toString"() {
        setup:
        def resourceTypes = new AccountSasResourceType()
            .setService(service)
            .setContainer(container)
            .setObject(object)

        expect:
        resourceTypes.toString() == expectedString

        where:
        service | container | object || expectedString
        true    | false     | false  || "s"
        false   | true      | false  || "c"
        false   | false     | true   || "o"
        true    | true      | true   || "sco"
    }

    @Unroll
    def "AccountSASResourceType parse"() {
        when:
        def resourceTypes = AccountSasResourceType.parse(resourceTypeString)

        then:
        resourceTypes.isService() == service
        resourceTypes.isContainer() == container
        resourceTypes.isObject() == object

        where:
        resourceTypeString || service | container | object
        "s"                || true    | false     | false
        "c"                || false   | true      | false
        "o"                || false   | false     | true
        "sco"              || true    | true      | true
    }

    @Unroll
    def "AccountSASResourceType IA"() {
        when:
        AccountSasResourceType.parse("scq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "IPRange toString"() {
        setup:
        def ip = new SasIpRange()
            .setIpMin(min)
            .setIpMax(max)

        expect:
        ip.toString() == expectedString

        where:
        min  | max  || expectedString
        "a"  | "b"  || "a-b"
        "a"  | null || "a"
        null | "b"  || ""
    }

    @Unroll
    def "IPRange parse"() {
        when:
        def ip = SasIpRange.parse(rangeStr)

        then:
        ip.getIpMin() == min
        ip.getIpMax() == max

        where:
        rangeStr || min | max
        "a-b"    || "a" | "b"
        "a"      || "a" | null
        ""       || ""  | null
    }

    @Unroll
    def "SASProtocol parse"() {
        expect:
        SasProtocol.parse(protocolStr) == protocol

        where:
        protocolStr  || protocol
        "https"      || SasProtocol.HTTPS_ONLY
        "https,http" || SasProtocol.HTTPS_HTTP
    }

    def "account sas impl util null"() {
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def p = AccountSasPermission.parse("l")
        def s = AccountSasService.parse("b")
        def rt = AccountSasResourceType.parse("o")
        def v = new AccountSasSignatureValues(e, p, s, rt)
        def implUtil = new AccountSasImplUtil(v, null)

        when:
        implUtil.generateSas(null, Context.NONE)

        then:
        def ex = thrown(NullPointerException)
        ex.getMessage().contains("storageSharedKeyCredential")
    }
}
