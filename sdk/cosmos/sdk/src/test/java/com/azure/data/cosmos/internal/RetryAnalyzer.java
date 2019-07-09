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


package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.TestConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.util.RetryAnalyzerCount;

import java.util.concurrent.TimeUnit;

public class RetryAnalyzer extends RetryAnalyzerCount {
    private final Logger logger = LoggerFactory.getLogger(RetryAnalyzer.class);
    private final int waitBetweenRetriesInSeconds = 120;

    public RetryAnalyzer() {
        this.setCount(Integer.parseInt(TestConfigurations.MAX_RETRY_LIMIT));
    }

    @Override
    public boolean retryMethod(ITestResult result) {
        try {
            TimeUnit.SECONDS.sleep(waitBetweenRetriesInSeconds);
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }
}
