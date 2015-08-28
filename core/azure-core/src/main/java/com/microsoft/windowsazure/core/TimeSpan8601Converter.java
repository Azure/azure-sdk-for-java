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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public abstract class TimeSpan8601Converter {
    public static String format(Duration duration) {
        return duration.toString();
    }

    public static Duration parse(String duration) {
        try {
            DatatypeFactory factory = DatatypeFactory.newInstance();
            return factory.newDuration(duration);
        } catch (DatatypeConfigurationException e) {
            String msg = String.format(
                    "The value \"%s\" is not a valid ISO8601 duration.",
                    duration);
            throw new IllegalArgumentException(msg, e);
        }
    }
}
