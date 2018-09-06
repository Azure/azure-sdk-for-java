/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

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
