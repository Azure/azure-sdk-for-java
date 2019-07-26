// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeTokenTest {

    private Locale defaultLocale;

    @BeforeTest(groups = { "unit" })
    public void beforeMethod() {
        defaultLocale = Locale.getDefault();
    }

    @Test(groups = { "unit" })
    public void nonLocaleUS() {
        Locale.setDefault(Locale.ITALIAN);
        DateTimeFormatter RFC_1123_DATE_TIME = 
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        String time = Utils.nowAsRFC1123();
        Locale.setDefault(Locale.US);
        RFC_1123_DATE_TIME.parse(time);
    }

    @AfterTest(groups = { "unit" })
    public void afterMethod() {
        // set back default locale before test
        if (defaultLocale != null) {
            Locale.setDefault(defaultLocale);
        } else {
            Locale.setDefault(Locale.US);
        }
    }
}
