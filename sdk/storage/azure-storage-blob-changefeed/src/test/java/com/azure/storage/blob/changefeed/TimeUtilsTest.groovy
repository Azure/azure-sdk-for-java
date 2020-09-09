package com.azure.storage.blob.changefeed

import com.azure.storage.blob.changefeed.implementation.util.TimeUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.ZoneOffset

class TimeUtilsTest extends Specification {

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
    }

    @Unroll
    def "convertPathToTime"() {
        expect:
        TimeUtils.convertPathToTime(path) == time

        where:
        path                                     || time
        null                                     || null
        "idx/segments/2019"                      || OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        "idx/segments/2019/"                     || OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        "idx/segments/2019/11"                   || OffsetDateTime.of(2019, 11, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        "idx/segments/2019/11/"                  || OffsetDateTime.of(2019, 11, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        "idx/segments/2019/11/02"                || OffsetDateTime.of(2019, 11, 2, 0, 0, 0, 0, ZoneOffset.UTC)
        "idx/segments/2019/11/02/"               || OffsetDateTime.of(2019, 11, 2, 0, 0, 0, 0, ZoneOffset.UTC)
        "idx/segments/2019/11/02/1700"           || OffsetDateTime.of(2019, 11, 2, 17, 0, 0, 0, ZoneOffset.UTC)
        "idx/segments/2019/11/02/1700/"          || OffsetDateTime.of(2019, 11, 2, 17, 0, 0, 0, ZoneOffset.UTC)
        "idx/segments/2019/11/02/1700/meta.json" || OffsetDateTime.of(2019, 11, 2, 17, 0, 0, 0, ZoneOffset.UTC)
    }

    @Unroll
    def "roundDownToNearestHour"() {
        expect:
        TimeUtils.roundDownToNearestHour(time) == roundedTime

        where:
        time                                                            || roundedTime
        null                                                            || null
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       || OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        OffsetDateTime.of(2020, 3, 17, 20, 25, 30, 0, ZoneOffset.UTC)   || OffsetDateTime.of(2020, 3, 17, 20, 0, 0, 0, ZoneOffset.UTC)
    }

    @Unroll
    def "roundUpToNearestHour"() {
        expect:
        TimeUtils.roundUpToNearestHour(time) == roundedTime

        where:
        time                                                            || roundedTime
        null                                                            || null
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       || OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        OffsetDateTime.of(2020, 3, 17, 20, 25, 30, 0, ZoneOffset.UTC)   || OffsetDateTime.of(2020, 3, 17, 21, 0, 0, 0, ZoneOffset.UTC)
        OffsetDateTime.of(2020, 3, 17, 23, 25, 30, 0, ZoneOffset.UTC)   || OffsetDateTime.of(2020, 3, 18, 0, 0, 0, 0, ZoneOffset.UTC)
    }

    @Unroll
    def "roundDownToNearestYear"() {
        expect:
        TimeUtils.roundDownToNearestYear(time) == roundedTime

        where:
        time                                                            || roundedTime
        null                                                            || null
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       || OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        OffsetDateTime.of(2020, 3, 17, 20, 25, 30, 0, ZoneOffset.UTC)   || OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
    }

    @Unroll
    def "validSegment"() {
        expect:
        TimeUtils.validSegment(segment, start, end) == valid

        where:
        start                                                           | segment                                   | end                                                        || valid
        /* Null checks. */
        null                                                            | null                                      | null                                                       || false
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       | null                                      | null                                                       || false
        null                                                            | "idx/segments/2019/11/02/1700/meta.json"  | null                                                       || false
        null                                                            | null                                      | OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)  || false
        /* All equal. Not valid since end time is exclusive. */
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       | "idx/segments/2019/01/01/0000/meta.json"  | OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)  || false
        /* Increasing. */
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       | "idx/segments/2019/01/01/0000/meta.json"  | OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)  || true
        OffsetDateTime.of(2019, 3, 17, 20, 25, 30, 0, ZoneOffset.UTC)   | "idx/segments/2019/06/01/0000/meta.json"  | OffsetDateTime.of(2019, 8, 10, 0, 0, 0, 0, ZoneOffset.UTC) || true
        /* Decreasing. */
        OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       | "idx/segments/2020/01/01/0000/meta.json"  | OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)  || false
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       | "idx/segments/2020/01/01/0000/meta.json"  | OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)  || false
    }

    @Unroll
    def "validYear"() {
        expect:
        TimeUtils.validYear(year, start, end) == valid

        where:
        start                                                           | year                  | end                                                        || valid
        /* Null checks. */
        null                                                            | null                  | null                                                       || false
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       | null                  | null                                                       || false
        null                                                            | "idx/segments/2019"   | null                                                       || false
        null                                                            | null                  | OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)  || false
        /* All equal. */
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       | "idx/segments/2019"   | OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)  || true
        /* Increasing. */
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       | "idx/segments/2020"   | OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)  || true
        OffsetDateTime.of(2019, 3, 17, 20, 25, 30, 0, ZoneOffset.UTC)   | "idx/segments/2019"   | OffsetDateTime.of(2019, 8, 10, 0, 0, 0, 0, ZoneOffset.UTC) || true
        /* Decreasing. */
        OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       | "idx/segments/2020"   | OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)  || false
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)       | "idx/segments/2020"   | OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)  || false
    }

}
