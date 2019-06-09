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

package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.directconnectivity.Protocol;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsingEnvTest {

    @Test(groups = "unit")
    public void parseDesiredConsistencies() {
        assertThat(TestSuiteBase.parseDesiredConsistencies("[ \"SESSION\" , \"STRONG\" ]")).containsExactly(
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
        assertThat(TestSuiteBase.parseProtocols("[ \"TCP\" , \"HTTPS\" ]")).containsExactly(Protocol.TCP, Protocol.HTTPS);
    }
}
