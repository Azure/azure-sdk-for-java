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
package com.microsoft.windowsazure.services.table;

import com.microsoft.windowsazure.services.core.Builder;
import com.microsoft.windowsazure.services.core.UserAgentFilter;
import com.microsoft.windowsazure.services.table.implementation.AtomReaderWriter;
import com.microsoft.windowsazure.services.table.implementation.DefaultEdmValueConverter;
import com.microsoft.windowsazure.services.table.implementation.DefaultXMLStreamFactory;
import com.microsoft.windowsazure.services.table.implementation.HttpReaderWriter;
import com.microsoft.windowsazure.services.table.implementation.MimeReaderWriter;
import com.microsoft.windowsazure.services.table.implementation.SharedKeyFilter;
import com.microsoft.windowsazure.services.table.implementation.SharedKeyLiteFilter;
import com.microsoft.windowsazure.services.table.implementation.TableExceptionProcessor;
import com.microsoft.windowsazure.services.table.implementation.TableRestProxy;
import com.microsoft.windowsazure.services.table.implementation.XMLStreamFactory;

public class Exports implements Builder.Exports {
    @Override
    public void register(Builder.Registry registry) {
        registry.add(TableContract.class, TableExceptionProcessor.class);
        registry.add(TableExceptionProcessor.class);
        registry.add(TableRestProxy.class);
        registry.add(SharedKeyLiteFilter.class);
        registry.add(SharedKeyFilter.class);
        registry.add(XMLStreamFactory.class, DefaultXMLStreamFactory.class);
        registry.add(AtomReaderWriter.class);
        registry.add(MimeReaderWriter.class);
        registry.add(HttpReaderWriter.class);
        registry.add(EdmValueConverter.class, DefaultEdmValueConverter.class);
        registry.add(UserAgentFilter.class);
    }
}
