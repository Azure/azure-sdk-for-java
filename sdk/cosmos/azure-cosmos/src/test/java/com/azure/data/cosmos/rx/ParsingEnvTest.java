// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.internal.directconnectivity.Protocol;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsingEnvTest {

    @Test(groups = "unit")
    public void parseDesiredConsistencies() {
        assertThat(TestSuiteBase.parseDesiredConsistencies("[ \"BoundedStaleness\" ]")).containsExactly(ConsistencyLevel.BOUNDED_STALENESS);
        assertThat(TestSuiteBase.parseDesiredConsistencies("[ \"Session\" , \"Strong\" ]")).containsExactly(
                ConsistencyLevel.SESSION, ConsistencyLevel.STRONG);
    }

    @Test(groups = "unit")
    public void parseDesiredConsistencies_null() {
        assertThat(TestSuiteBase.parseDesiredConsistencies(null)).isNull();
    }

    @Test(groups = "unit")
    public void lowerConsistencies() {
        assertThat(TestSuiteBase.allEqualOrLowerConsistencies(ConsistencyLevel.SESSION))
                .containsExactly(ConsistencyLevel.SESSION, ConsistencyLevel.CONSISTENT_PREFIX, ConsistencyLevel.EVENTUAL);
    }

    @Test(groups = "unit")
    public void parseAccountConsistency() {
        assertThat(TestSuiteBase.parseConsistency("Strong")).isEqualTo(ConsistencyLevel.STRONG);
        assertThat(TestSuiteBase.parseConsistency("Session")).isEqualTo(ConsistencyLevel.SESSION);
        assertThat(TestSuiteBase.parseConsistency("BoundedStaleness")).isEqualTo(ConsistencyLevel.BOUNDED_STALENESS);
        assertThat(TestSuiteBase.parseConsistency("ConsistentPrefix")).isEqualTo(ConsistencyLevel.CONSISTENT_PREFIX);
        assertThat(TestSuiteBase.parseConsistency("Eventual")).isEqualTo(ConsistencyLevel.EVENTUAL);
    }

    @Test(groups = "unit")
    public void parsePreferredLocation() {
        assertThat(TestSuiteBase.parsePreferredLocation("[ \"central us\" , \"central us2\" ]"))
                .containsExactly("central us", "central us2");
    }

    @Test(groups = "unit")
    public void parsePreferredLocation_null() {
        assertThat(TestSuiteBase.parsePreferredLocation(null)).isNull();
    }

    @Test(groups = "unit")
    public void protocols() {
        assertThat(TestSuiteBase.parseProtocols("[ \"Tcp\" , \"Https\" ]")).containsExactly(Protocol.TCP, Protocol.HTTPS);
    }
}
