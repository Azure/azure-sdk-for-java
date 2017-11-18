/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.servicebus.implementation.TimeSpan;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;

public class TimeSpanTests {
    @Test
    public void testTimeSpanFromPeriod() {
        Period period = new Period()
                .withDays(366)
                .withHours(25)
                .withMinutes(10)
                .withSeconds(70)
                .withMillis(1001);
        TimeSpan timeSpan = new TimeSpan()
                .withDays(366)
                .withHours(25)
                .withMinutes(10)
                .withSeconds(70)
                .withMilliseconds(1001);
        Assert.assertEquals(TimeSpan.fromPeriod(period).toString(), timeSpan.toString());

        period = new Period()
                .withWeeks(12)
                .withDays(366)
                .withHours(25)
                .withMinutes(10)
                .withSeconds(70)
                .withMillis(1001);
        // Days -> 12 * 7 + 366 + 1
        Assert.assertEquals("451.01:11:11.0010000", TimeSpan.fromPeriod(period).toString());
    }

    @Test
    public void testTimeSpanStringParse() {
        TimeSpan timeSpan1 = TimeSpan.parse("366.01:02:00.12345");
        Assert.assertEquals(366, timeSpan1.days());
        Assert.assertEquals(1, timeSpan1.hours());
        Assert.assertEquals(2, timeSpan1.minutes());
        Assert.assertEquals(0, timeSpan1.seconds());
        Assert.assertEquals(123, timeSpan1.milliseconds());
        Assert.assertEquals("366.01:02:00.1230000", timeSpan1.toString());

        TimeSpan timeSpan2 = TimeSpan.parse("366");
        Assert.assertEquals(366, timeSpan2.days());
        Assert.assertEquals(0, timeSpan2.hours());
        Assert.assertEquals(0, timeSpan2.minutes());
        Assert.assertEquals(0, timeSpan2.seconds());
        Assert.assertEquals(0, timeSpan2.milliseconds());
        Assert.assertEquals("366.00:00:00", timeSpan2.toString());

        TimeSpan timeSpan3 = TimeSpan.parse("01:02");
        Assert.assertEquals(0, timeSpan3.days());
        Assert.assertEquals(1, timeSpan3.hours());
        Assert.assertEquals(2, timeSpan3.minutes());
        Assert.assertEquals(0, timeSpan3.seconds());
        Assert.assertEquals(0, timeSpan3.milliseconds());
        Assert.assertEquals("01:02:00", timeSpan3.toString());

        TimeSpan timeSpan4 = TimeSpan.parse("01:02:34");
        Assert.assertEquals(0, timeSpan4.days());
        Assert.assertEquals(1, timeSpan4.hours());
        Assert.assertEquals(2, timeSpan4.minutes());
        Assert.assertEquals(34, timeSpan4.seconds());
        Assert.assertEquals(0, timeSpan4.milliseconds());
        Assert.assertEquals("01:02:34", timeSpan4.toString());

        TimeSpan timeSpan5 = TimeSpan.parse("01:02:34.001");
        Assert.assertEquals(0, timeSpan5.days());
        Assert.assertEquals(1, timeSpan5.hours());
        Assert.assertEquals(2, timeSpan5.minutes());
        Assert.assertEquals(34, timeSpan5.seconds());
        Assert.assertEquals(1, timeSpan5.milliseconds());
        Assert.assertEquals("01:02:34.0010000", timeSpan5.toString());

        TimeSpan timeSpan6 = TimeSpan.parse("01:02:34.00011");
        Assert.assertEquals(0, timeSpan6.days());
        Assert.assertEquals(1, timeSpan6.hours());
        Assert.assertEquals(2, timeSpan6.minutes());
        Assert.assertEquals(34, timeSpan6.seconds());
        Assert.assertEquals(0, timeSpan6.milliseconds());
        Assert.assertEquals("01:02:34", timeSpan6.toString());

        TimeSpan timeSpan7 = TimeSpan.parse("-366.23:1:1.100000");
        Assert.assertEquals(-366, timeSpan7.days());
        Assert.assertEquals(-23, timeSpan7.hours());
        Assert.assertEquals(-1, timeSpan7.minutes());
        Assert.assertEquals(-1, timeSpan7.seconds());
        Assert.assertEquals(-100, timeSpan7.milliseconds());
        Assert.assertEquals("-366.23:01:01.1000000", timeSpan7.toString());

        Exception exception = null;
        try {
            TimeSpan.parse("366.24:02:00.12345");
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:66:00.12345");
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:02:77.12345");
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:02:00.12345678");
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.");
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01");
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:");
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:02:");
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:02:00.");
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("366.01:02:00.89.00");
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse("");
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            TimeSpan.parse(null);
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);

    }

    @Test
    public void testTimeSpanStringConversion() {
        TimeSpan timeSpan = new TimeSpan();
        timeSpan.withDays(0)
                .withHours(0)
                .withMinutes(0)
                .withSeconds(0)
                .withMilliseconds(0);
        Assert.assertEquals(timeSpan.toString(), "00:00:00");

        timeSpan.withDays(1)
                .withHours(1)
                .withMinutes(1)
                .withSeconds(1)
                .withMilliseconds(1);
        Assert.assertEquals(timeSpan.toString(), "1.01:01:01.0010000");

        timeSpan.withDays(1)
                .withHours(48)
                .withMinutes(0)
                .withSeconds(0)
                .withMilliseconds(0);
        Assert.assertEquals(timeSpan.toString(), "3.00:00:00");

        timeSpan.withDays(1)
                .withHours(0)
                .withMinutes(120)
                .withSeconds(0)
                .withMilliseconds(0);
        Assert.assertEquals(timeSpan.toString(), "1.02:00:00");

        timeSpan.withDays(1)
                .withHours(0)
                .withMinutes(121)
                .withSeconds(0)
                .withMilliseconds(0);
        Assert.assertEquals(timeSpan.toString(), "1.02:01:00");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(0)
                .withMilliseconds(0);
        Assert.assertEquals(timeSpan.toString(), "3.03:01:00");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(59)
                .withMilliseconds(0);
        Assert.assertEquals(timeSpan.toString(), "3.03:01:59");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(0);
        Assert.assertEquals(timeSpan.toString(), "3.03:02:00");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(100);
        Assert.assertEquals(timeSpan.toString(), "3.03:02:00.1000000");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(999);
        Assert.assertEquals(timeSpan.toString(), "3.03:02:00.9990000");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(1000);
        Assert.assertEquals(timeSpan.toString(), "3.03:02:01");

        timeSpan.withDays(1)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(1500);
        Assert.assertEquals(timeSpan.toString(),"3.03:02:01.5000000");

        timeSpan.withDays(368)
                .withHours(49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(1500);
        Assert.assertEquals(timeSpan.toString(),"370.03:02:01.5000000");

        timeSpan.withDays(368)
                .withHours(-49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(1500);
        Assert.assertEquals(timeSpan.toString(),"366.01:02:01.5000000");

        timeSpan.withDays(-368)
                .withHours(-49)
                .withMinutes(121)
                .withSeconds(60)
                .withMilliseconds(1500);
        Assert.assertEquals(timeSpan.toString(),"-369.22:57:58.5000000");

        timeSpan.withDays(-368)
                .withHours(-49)
                .withMinutes(-121)
                .withSeconds(60)
                .withMilliseconds(1500);
        Assert.assertEquals(timeSpan.toString(),"-370.02:59:58.5000000");

        timeSpan.withDays(-368)
                .withHours(-49)
                .withMinutes(-121)
                .withSeconds(-60)
                .withMilliseconds(1500);
        Assert.assertEquals(timeSpan.toString(),"-370.03:01:58.5000000");

        timeSpan.withDays(-368)
                .withHours(-49)
                .withMinutes(-121)
                .withSeconds(-60)
                .withMilliseconds(-1500);
        Assert.assertEquals(timeSpan.toString(),"-370.03:02:01.5000000");
    }
}
