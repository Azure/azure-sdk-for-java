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
package com.microsoft.windowsazure.services.core.utils.pipeline;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.jersey.core.util.Base64;

/*
 * JAXB adapter for a Base64 encoded string element
 */
public class Base64StringAdapter extends XmlAdapter<String, String> {

    @Override
    public String marshal(String arg0) throws Exception {
        return new String(Base64.encode(arg0), "UTF-8");
    }

    @Override
    public String unmarshal(String arg0) throws Exception {
        return Base64.base64Decode(arg0);
    }
}
