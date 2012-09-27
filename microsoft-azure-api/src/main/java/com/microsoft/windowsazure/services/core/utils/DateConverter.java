/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.core.utils;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * The Class DateConverter.
 */
public class DateConverter {

    /** The datatype factory. */
    private static DatatypeFactory datatypeFactory = null;

    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        }
        catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Cannot create a new DatatypeFactory instance.", e);
        }
    }

    /**
     * XML gregorian calendar to date.
     * 
     * @param xmlGregorianCalendar
     *            the xml gregorian calendar
     * @return the date
     */
    public static Date XMLGregorianCalendarToDate(XMLGregorianCalendar xmlGregorianCalendar) {
        if (xmlGregorianCalendar == null) {
            return null;
        }

        return xmlGregorianCalendar.toGregorianCalendar().getTime();
    }

    /**
     * Date to xml gregorian calendar.
     * 
     * @param date
     *            the date
     * @return the xML gregorian calendar
     */
    public static XMLGregorianCalendar DateToXMLGregorianCalendar(Date date) {
        if (date == null) {
            return null;
        }

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(date.getTime());
        return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    }
}
