// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus;

import com.azure.resourcemanager.servicebus.implementation.TimeSpan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class TimeSpanTests {
    @Test
    public void testTimeSpanFromPeriod() {
        Duration duration = Duration.ofDays(366)
            .plus(Duration.ofHours(25))
            .plus(Duration.ofMinutes(10))
            .plus(Duration.ofSeconds(70))
            .plus(Duration.ofMillis(1001));

        TimeSpan timeSpan = new TimeSpan()
                .withDays(366)
                .withHours(25)
                .withMinutes(10)
                .withSeconds(70)
                .withMilliseconds(1001);
        Assertions.assertEquals(duration.toMillis(), timeSpan.toDuration().toMillis());

        duration = Duration.ofDays(12 * 7 + 366)
            .plus(Duration.ofHours(25))
            .plus(Duration.ofMinutes(10))
            .plus(Duration.ofSeconds(70))
            .plus(Duration.ofMillis(1001));
        // Days -> 12 * 7 + 366 + 1
        Assertions.assertEquals("451.01:11:11.0010000", TimeSpan.fromDuration(duration).toString());
    }

    @Test
    public void testTimeSpanStringParse() {
        TimeSpan timeSpan1 = TimeSpan.parse("366.01:02:00.12345");
        Assertions.assertEquals(366, timeSpan1.days());
        Assertions.assertEquals(1, timeSpan1.hours());
        Assertions.assertEquals(2, timeSpan1.minutes());
        Assertions.assertEquals(0, timeSpan1.seconds());
        Assertions.assertEquals(123, timeSpan1.milliseconds());
        Assertions.assertEquals("366.01:02:00.1230000", timeSpan1.toString());

        TimeSpan timeSpan2 = TimeSpan.parse("366");
        Assertions.assertEquals(366, timeSpan2.days());
        Assertions.assertEquals(0, timeSpan2.hours());
        Assertions.assertEquals(0, timeSpan2.minutes());
        Assertions.assertEquals(0, timeSpan2.seconds());
        Assertions.assertEquals(0, timeSpan2.milliseconds());
        Assertions.assertEquals("366.00:00:00", timeSpan2.toString());

        TimeSpan timeSpan3 = TimeSpan.parse("01:02");
        Assertions.assertEquals(0, timeSpan3.days());
        Assertions.assertEquals(1, timeSpan3.hours());
        Assertions.assertEquals(2, timeSpan3.minutes());
        Assertions.assertEquals(0, timeSpan3.seconds());
        Assertions.assertEquals(0, timeSpan3.milliseconds());
        Assertions.assertEquals("01:02:00", timeSpan3.toString());

        TimeSpan timeSpan4 = TimeSpan.parse("01:02:34");
        Assertions.assertEquals(0, timeSpan4.days());
        Assertions.assertEquals(1, timeSpan4.hours());
        Assertions.assertEquals(2, timeSpan4.minutes());
        Assertions.assertEquals(34, timeSpan4.seconds());
        Assertions.assertEquals(0, timeSpan4.milliseconds());
        Assertions.assertEquals("01:02:34", timeSpan4.toString());

        TimeSpan timeSpan5 = TimeSpan.parse("01:02:34.001");
        Assertions.assertEquals(0, timeSpan5.days());
        Assertions.assertEquals(1, timeSpan5.hours());
        Assertions.assertEquals(2, timeSpan5.minutes());
        Assertions.assertEquals(34, timeSpan5.seconds());
        Assertions.assertEquals(1, timeSpan5.milliseconds());
        Assertions.assertEquals("01:02:34.0010000", timeSpan5.toString());

        TimeSpan timeSpan6 = TimeSpan.parse("01:02:34.00011");
        Assertions.assertEquals(0, timeSpan6.days());
        Assertions.assertEquals(1, timeSpan6.hours());
        Assertions.assertEquals(2, timeSpan6.minutes());
        Assertions.assertEquals(34, timeSpan6.seconds());
        Assertions.assertEquals(0, timeSpan6.milliseconds());
        Assertions.assertEquals("01:02:34", timeSpan6.toString());

        TimeSpan timeSpan7 = TimeSpan.parse("-366.23:1:1.100000");
        Assertions.assertEquals(-366, timeSpan7.days());
        Assertions.assertEquals(-23, timeSpan7.hours());
        Assertions.assertEquals(-1, timeSpan7.minutes());
        Assertions.assertEquals(-1, timeSpan7.seconds());
        Assertions.assertEquals(-100, timeSpan7.milliseconds());
        Assertions.assertEquals("-366.23:01:01.1000000", timeSpan7.toString());

        Exception exception = null;
        try {
            TimeSpan.parse("366.24:02:00.12345");
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:66:00.12345");
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:02:77.12345");
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:02:00.12345678");
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.");
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01");
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:");
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:02:");
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:02:00.");
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:02:00.89.00");
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("");
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse(null);
        } catch (Exception ex) {
            exception = ex;
        }
        Assertions.assertNotNull(exception);

    }

    @Test
    public void testTimeSpanStringConversion() {
        TimeSpan timeSpan = new TimeSpan();
        timeSpan.withDays(0)
                .withHours(0)
                .withMinutes(0)
                .withSeconds(0)
                .withMilliseconds(0);
        Assertions.assertEquals(timeSpan.toString(), "00:00:00");

        timeSpan.withDays(1)
                .withHours(1)
                .withMinutes(1)
                .withSeconds(1)
                .withMilliseconds(1);
        Assertions.assertEquals(timeSpan.toString(), "1.01:01:01.0010000");

        timeSpan.withDays(1)
                .withHours(48)
                .withMinutes(0)
                .withSeconds(0)
                .withMilliseconds(0);
        Assertions.assertEquals(timeSpan.toString(), "3.00:00:00");

        timeSpan.withDays(1)
                .withHours(0)
                .withMinutes(120)
                .withSeconds(0)
                .withMilliseconds(0);
        Assertions.assertEquals(timeSpan.toString(), "1.02:00:00");

        timeSpan.withDays(1)
                .withHours(0)
                .withMinutes(121)
                .withSeconds(0)
                .withMilliseconds(0);
        Assertions.assertEquals(timeSpan.toString(), "1.02:01:00");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(0)
                .withMilliseconds(0);
        Assertions.assertEquals(timeSpan.toString(), "3.03:01:00");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(59)
                .withMilliseconds(0);
        Assertions.assertEquals(timeSpan.toString(), "3.03:01:59");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(0);
        Assertions.assertEquals(timeSpan.toString(), "3.03:02:00");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(100);
        Assertions.assertEquals(timeSpan.toString(), "3.03:02:00.1000000");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(999);
        Assertions.assertEquals(timeSpan.toString(), "3.03:02:00.9990000");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(1000);
        Assertions.assertEquals(timeSpan.toString(), "3.03:02:01");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(1500);
        Assertions.assertEquals(timeSpan.toString(), "3.03:02:01.5000000");

        timeSpan.withDays(368)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(1500);
        Assertions.assertEquals(timeSpan.toString(), "370.03:02:01.5000000");

        timeSpan.withDays(368)
                .withHours(-49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(1500);
        Assertions.assertEquals(timeSpan.toString(), "366.01:02:01.5000000");

        timeSpan.withDays(-368)
                .withHours(-49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(1500);
        Assertions.assertEquals(timeSpan.toString(), "-369.22:57:58.5000000");

        timeSpan.withDays(-368)
                .withHours(-49)
                .withMinutes(-121)
                .withSeconds(60)
                .withMilliseconds(1500);
        Assertions.assertEquals(timeSpan.toString(), "-370.02:59:58.5000000");

        timeSpan.withDays(-368)
                .withHours(-49)
                .withMinutes(-121)
                .withSeconds(-60)
                .withMilliseconds(1500);
        Assertions.assertEquals(timeSpan.toString(), "-370.03:01:58.5000000");

        timeSpan.withDays(-368)
                .withHours(-49)
                .withMinutes(-121)
                .withSeconds(-60)
                .withMilliseconds(-1500);
        Assertions.assertEquals(timeSpan.toString(), "-370.03:02:01.5000000");
    }
}
