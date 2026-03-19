// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.models.ContentRange;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ContentRange}.
 */
public class ContentRangeTest {

    @Test
    public void rawStringConstructor() {
        ContentRange range = new ContentRange("1-3,5,9-");
        assertEquals("1-3,5,9-", range.toString());
    }

    @Test
    public void rawStringConstructorRejectsNull() {
        assertThrows(NullPointerException.class, () -> new ContentRange(null));
    }

    @Test
    public void pageSinglePage() {
        assertEquals("5", ContentRange.page(5).toString());
        assertEquals("1", ContentRange.page(1).toString());
    }

    @Test
    public void pageRejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> ContentRange.page(0));
    }

    @Test
    public void pageRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> ContentRange.page(-1));
    }

    @Test
    public void pagesRange() {
        assertEquals("1-3", ContentRange.pages(1, 3).toString());
        assertEquals("5-5", ContentRange.pages(5, 5).toString());
    }

    @Test
    public void pagesRejectsStartLessThanOne() {
        assertThrows(IllegalArgumentException.class, () -> ContentRange.pages(0, 3));
    }

    @Test
    public void pagesRejectsEndBeforeStart() {
        assertThrows(IllegalArgumentException.class, () -> ContentRange.pages(5, 3));
    }

    @Test
    public void pagesFromOpenEnded() {
        assertEquals("9-", ContentRange.pagesFrom(9).toString());
        assertEquals("1-", ContentRange.pagesFrom(1).toString());
    }

    @Test
    public void pagesFromRejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> ContentRange.pagesFrom(0));
    }

    @Test
    public void timeRangeMilliseconds() {
        assertEquals("0-5000", ContentRange.timeRange(Duration.ZERO, Duration.ofMillis(5000)).toString());
        assertEquals("1000-2000", ContentRange.timeRange(Duration.ofMillis(1000), Duration.ofMillis(2000)).toString());
    }

    @Test
    public void timeRangeRejectsNegativeStart() {
        assertThrows(IllegalArgumentException.class,
            () -> ContentRange.timeRange(Duration.ofMillis(-1), Duration.ofMillis(5000)));
    }

    @Test
    public void timeRangeRejectsEndBeforeStart() {
        assertThrows(IllegalArgumentException.class,
            () -> ContentRange.timeRange(Duration.ofMillis(5000), Duration.ofMillis(3000)));
    }

    @Test
    public void timeRangeFromOpenEnded() {
        assertEquals("5000-", ContentRange.timeRangeFrom(Duration.ofMillis(5000)).toString());
        assertEquals("0-", ContentRange.timeRangeFrom(Duration.ZERO).toString());
    }

    @Test
    public void timeRangeFromRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> ContentRange.timeRangeFrom(Duration.ofMillis(-1)));
    }

    @Test
    public void combineMultipleRanges() {
        ContentRange combined
            = ContentRange.combine(ContentRange.pages(1, 3), ContentRange.page(5), ContentRange.pagesFrom(9));
        assertEquals("1-3,5,9-", combined.toString());
    }

    @Test
    public void combineSingleRange() {
        ContentRange combined = ContentRange.combine(ContentRange.page(1));
        assertEquals("1", combined.toString());
    }

    @Test
    public void combineRejectsNull() {
        assertThrows(NullPointerException.class, () -> ContentRange.combine((ContentRange[]) null));
    }

    @Test
    public void combineRejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> ContentRange.combine());
    }

    @Test
    public void equalsAndHashCodeForEqualValues() {
        ContentRange a = ContentRange.pages(1, 3);
        ContentRange b = new ContentRange("1-3");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void equalsAndHashCodeForDifferentValues() {
        ContentRange a = ContentRange.pages(1, 3);
        ContentRange b = ContentRange.pages(1, 5);
        assertNotEquals(a, b);
    }

    @Test
    public void equalsReturnsFalseForNull() {
        ContentRange range = ContentRange.page(1);
        assertNotEquals(null, range);
    }

    @Test
    public void equalsReturnsFalseForDifferentType() {
        ContentRange range = ContentRange.page(1);
        assertNotEquals("1", range);
    }

    @Test
    public void toStringReturnsValue() {
        ContentRange range = ContentRange.pages(1, 3);
        assertEquals("1-3", range.toString());
    }

    // =================== Duration overloads ===================

    @Test
    public void timeRangeDuration() {
        ContentRange range = ContentRange.timeRange(Duration.ofMillis(0), Duration.ofMillis(5000));
        assertEquals("0-5000", range.toString());
    }

    @Test
    public void timeRangeFromDuration() {
        ContentRange range = ContentRange.timeRangeFrom(Duration.ofSeconds(5));
        assertEquals("5000-", range.toString());
    }

    @Test
    public void timeRangeDurationRejectsNullStart() {
        assertThrows(NullPointerException.class, () -> ContentRange.timeRange(null, Duration.ofMillis(5000)));
    }

    @Test
    public void timeRangeDurationRejectsNullEnd() {
        assertThrows(NullPointerException.class, () -> ContentRange.timeRange(Duration.ofMillis(0), null));
    }

    @Test
    public void timeRangeFromDurationRejectsNull() {
        assertThrows(NullPointerException.class, () -> ContentRange.timeRangeFrom((Duration) null));
    }
}
