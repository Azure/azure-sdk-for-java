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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

/*
 * JAXB adapter for RFC 1123 date element
 */
public class RFC1123DateAdapter extends XmlAdapter<String, Date> {

    @Override
    public Date unmarshal(String arg0) throws Exception {
        return new RFC1123DateConverter().parse(arg0);
    }

    @Override
    public String marshal(Date arg0) throws Exception {
        return new RFC1123DateConverter().format(arg0);
    }
}
