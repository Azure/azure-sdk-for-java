// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared

import com.azure.storage.common.implementation.Constants
import spock.lang.IgnoreIf
import spock.lang.Specification

abstract class ServiceVersionSpec extends Specification {

    protected abstract Class getServiceVersionClass();

    @IgnoreIf({ DevopsPipeline.getInstance().map { !it.releasesToMavenCentral() }.orElse(true) })
    def "getLatest points to latest"() {
        when:
        Class clazz = getServiceVersionClass()
        def lastVersion = clazz.getEnumConstants().last()
        def latestVersion = clazz.getLatest()

        then:
        latestVersion == lastVersion
    }

    @IgnoreIf({ DevopsPipeline.getInstance().map { !it.releasesToMavenCentral() }.orElse(true) })
    def "Sas version should match last when we release"() {
        when:
        Class clazz = getServiceVersionClass()
        def latestVersion = clazz.getLatest()

        then:
        Constants.SAS_SERVICE_VERSION == latestVersion.getVersion()
    }

    @IgnoreIf({ DevopsPipeline.getInstance().map { !it.releasesToMavenCentral() }.orElse(true) })
    def "Header version should match last when we release"() {
        when:
        Class clazz = getServiceVersionClass()
        def latestVersion = clazz.getLatest()

        then:
        Constants.HeaderConstants.TARGET_STORAGE_VERSION == latestVersion.getVersion()
    }
}
