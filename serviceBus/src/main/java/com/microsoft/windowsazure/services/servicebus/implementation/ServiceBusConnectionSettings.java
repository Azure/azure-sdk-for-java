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

package com.microsoft.windowsazure.services.servicebus.implementation;

import com.microsoft.windowsazure.core.utils.ConnectionStringSyntaxException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class that encapsulates all the various settings needed to connect to Service
 * Bus, provided via either a connection string or via separate configuration
 * variables.
 * <p/>
 * The connection string is looked for first, falling back to separate config
 * values if not found.
 */
class ServiceBusConnectionSettings {
    private String uri;
    private String wrapUri;
    private String wrapName;
    private String wrapPassword;
    private String sharedAccessKeyName;
    private String sharedAccessKey;

    public ServiceBusConnectionSettings(String connectionString, String uri,
            String wrapUri, String wrapName, String wrapPassword,
            String sharedAccessKeyName, String sharedAccessKey)
            throws ConnectionStringSyntaxException, URISyntaxException {
        if (connectionString != null) {
            parseConnectionString(connectionString);
        } else {
            this.uri = uri;
            this.wrapUri = wrapUri;
            this.wrapName = wrapName;
            this.wrapPassword = wrapPassword;
            this.sharedAccessKey = sharedAccessKey;
            this.sharedAccessKeyName = sharedAccessKeyName;
        }
    }

    public String getUri() {
        return uri;
    }

    public String getWrapUri() {
        return wrapUri;
    }

    public String getWrapName() {
        return wrapName;
    }

    public String getWrapPassword() {
        return wrapPassword;
    }

    public String getSharedAccessKeyName() {
        return sharedAccessKeyName;
    }

    public String getSharedAccessKey() {
        return sharedAccessKey;
    }

    public boolean isSasAuthentication() {
        return sharedAccessKeyName != null && sharedAccessKey != null;
    }

    private boolean parseConnectionString(String connectionString)
            throws URISyntaxException, ConnectionStringSyntaxException {
        ServiceBusConnectionString cs = new ServiceBusConnectionString(
                connectionString);
        setUri(cs);
        setWrapUri(cs);
        wrapName = cs.getSharedSecretIssuer();
        wrapPassword = cs.getSharedSecretValue();
        sharedAccessKeyName = cs.getSharedAccessKeyName();
        sharedAccessKey = cs.getSharedAccessKey();
        return true;
    }

    private void setUri(ServiceBusConnectionString connectionString) {
        uri = connectionString.getEndpoint().replaceFirst("^sb://", "https://");
    }

    private void setWrapUri(ServiceBusConnectionString connectionString)
            throws URISyntaxException {
        if (connectionString.getStsEndpoint() == null
                || connectionString.getStsEndpoint().isEmpty()) {
            URI hostUri = new URI(uri);
            String namespace = hostUri.getHost().split("\\.")[0];
            wrapUri = "https://" + namespace
                    + "-sb.accesscontrol.windows.net/WRAPv0.9";
        } else {
            wrapUri = connectionString.getStsEndpoint().replaceAll("\\/$", "")
                    + "/WRAPv0.9";
        }
    }
}
