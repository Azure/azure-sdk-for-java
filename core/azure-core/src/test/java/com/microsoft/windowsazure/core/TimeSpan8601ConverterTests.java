/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.core;

import org.junit.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import static org.junit.Assert.assertEquals;

public class TimeSpan8601ConverterTests {
    @Test
    public void formatShouldWork() throws Exception {
        DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();
        Duration duration = dataTypeFactory.newDurationDayTime(true, 0, 3, 10,
                2);
        String durationString = TimeSpan8601Converter.format(duration);

        assertEquals("P0DT3H10M2S", durationString);
    }

    @Test
    public void parseShouldWork() throws Exception {
        Duration duration = TimeSpan8601Converter.parse("P0DT3H10M2S");

        assertEquals(0, duration.getDays());
        assertEquals(3, duration.getHours());
        assertEquals(10, duration.getMinutes());
        assertEquals(2, duration.getSeconds());
    }
}
