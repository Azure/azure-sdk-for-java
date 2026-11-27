// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.implementation.changefeed.common.ChangeFeedQueryFilter;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ChangeFeedQueryFilterTest {
    @Test(groups = "unit")
    public void legacyUnfilteredLeasesRemainCompatible() {
        assertThat(ChangeFeedQueryFilter.fingerprint(null)).isNull();
        assertThat(ChangeFeedQueryFilter.fingerprint("")).isNull();
        ChangeFeedQueryFilter.validateProperties(null, null);
        ChangeFeedQueryFilter.validateProperties(null, Collections.emptyMap());
        assertThat(ChangeFeedQueryFilter.withFingerprint(null, null)).isNull();
        assertThat(new ChangeFeedProcessorOptions().setQuery("").getQuery()).isNull();
    }

    @Test(groups = "unit")
    public void fingerprintIsStablePrivateAndUsesExactSql() {
        String sql = "SELECT * FROM c WHERE c.age > 5";
        String fingerprint = ChangeFeedQueryFilter.fingerprint(sql);
        assertThat(fingerprint).hasSize(64).doesNotContain(sql);
        assertThat(ChangeFeedQueryFilter.fingerprint(sql)).isEqualTo(fingerprint);
        assertThat(ChangeFeedQueryFilter.fingerprint(sql + " ")).isNotEqualTo(fingerprint);
        assertThat(ChangeFeedQueryFilter.fingerprint(sql.toLowerCase(java.util.Locale.ROOT))).isNotEqualTo(fingerprint);
        Map<String, String> original = Collections.singletonMap("custom", "value");
        Map<String, String> properties = ChangeFeedQueryFilter.withFingerprint(original, fingerprint);
        assertThat(properties).containsEntry("custom", "value")
            .containsEntry(ChangeFeedQueryFilter.PROPERTY_NAME, fingerprint);
        assertThat(original).doesNotContainKey(ChangeFeedQueryFilter.PROPERTY_NAME);
        ChangeFeedQueryFilter.validateProperties(fingerprint, properties);
    }

    @Test(groups = "unit")
    public void addingRemovingOrChangingFilterFailsClearlyWithoutQueryText() {
        String sql = "SELECT * FROM c WHERE c.secret = 'private'";
        String first = ChangeFeedQueryFilter.fingerprint(sql);
        String second = ChangeFeedQueryFilter.fingerprint("SELECT * FROM c WHERE c.a = 2");
        for (String[] pair : new String[][] { { first, null }, { null, first }, { first, second } }) {
            assertThatThrownBy(() -> ChangeFeedQueryFilter.validate(pair[0], pair[1]))
                .isInstanceOf(ChangeFeedQueryFilter.QueryFilterMismatchException.class)
                .hasMessageContaining("new lease prefix or lease container")
                .hasMessageNotContaining(sql);
        }
        assertThatThrownBy(() -> ChangeFeedQueryFilter.withFingerprint(
            Collections.singletonMap(ChangeFeedQueryFilter.PROPERTY_NAME, first), second))
            .isInstanceOf(ChangeFeedQueryFilter.QueryFilterMismatchException.class);
    }
}
