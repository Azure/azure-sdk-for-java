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

import com.microsoft.windowsazure.core.utils.DateFactory;
import com.microsoft.windowsazure.exception.ServiceException;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.management.timer.Timer;

public class WrapTokenManager {

    private WrapContract contract;
    private final DateFactory dateFactory;
    private final String uri;
    private final String name;
    private final String password;

    private final Map<String, ActiveToken> activeTokens;

    @Inject
    public WrapTokenManager(WrapContract contract, DateFactory dateFactory,
            ServiceBusConnectionSettings connectionSettings) {
        this.contract = contract;
        this.dateFactory = dateFactory;
        this.uri = connectionSettings.getWrapUri();
        this.name = connectionSettings.getWrapName();
        this.password = connectionSettings.getWrapPassword();
        activeTokens = new ConcurrentHashMap<String, ActiveToken>();
    }

    /**
     * @return the contract
     */
    public WrapContract getContract() {
        return contract;
    }

    /**
     * @param contract
     *            the contract to set
     */
    public void setContract(WrapContract contract) {
        this.contract = contract;
    }

    public String getAccessToken(URI targetUri) throws ServiceException,
            URISyntaxException {
        Date now = dateFactory.getDate();

        String scope = extractCacheScope(targetUri);
        ActiveToken active = this.activeTokens.get(scope);

        if (active != null && now.before(active.getExpiresUtc())) {
            return active.getWrapResponse().getAccessToken();
        }

        // sweep expired tokens out of collection
        Iterator<Entry<String, ActiveToken>> iterator = activeTokens.entrySet()
                .iterator();
        while (iterator.hasNext()) {
            Entry<String, ActiveToken> entry = iterator.next();
            if (!now.before(entry.getValue().getExpiresUtc())) {
                iterator.remove();
            }
        }

        WrapAccessTokenResult wrapResponse = getContract().wrapAccessToken(uri,
                name, password, scope);
        Date expiresUtc = new Date(now.getTime() + wrapResponse.getExpiresIn()
                * Timer.ONE_SECOND / 2);

        ActiveToken acquired = new ActiveToken();
        acquired.setWrapResponse(wrapResponse);
        acquired.setExpiresUtc(expiresUtc);
        this.activeTokens.put(scope, acquired);

        return wrapResponse.getAccessToken();
    }

    /**
     * Extracts the cache scope for a service bus entity
     * For topics and queues:
     * http://{serviceNamespace}.servicebus.windows.net/{queuePath|topicPath}
     * For subscriptions:
     * http://{serviceNamespace}.servicebus.windows.net/{topicPath}/subscriptions/{subscriptionName}
     * @param targetUri the uri the extract from
     * @return cache scope uri
     * @throws URISyntaxException
     */
    static String extractCacheScope(URI targetUri) throws URISyntaxException {
        String targetPath = targetUri.getPath();
        if (StringUtils.isNotBlank(targetPath)) {
            String[] pathParts = StringUtils.split(targetPath, "/", 4);
            if (pathParts.length > 0) {
                // Subscription paths will be in the format /{topicPath}/subscriptions/{subscriptionName}/..
                if (pathParts.length > 1 && pathParts[1].equalsIgnoreCase("subscriptions")) {
                    // Subscription path
                    targetPath = String.format("/%1$s/%2$s/%3$s", pathParts[0],
                            pathParts[1], pathParts[2]);
                } else if (StringUtils.isNotBlank(pathParts[0])) {
                    // Topic or queue path
                    targetPath = "/" + pathParts[0];
                }
            }
        }

        URI scopeUri = new URI("http", targetUri.getAuthority(),  targetPath, null, null);
        return scopeUri.toString();
    }

    class ActiveToken {
        private Date expiresUtc;
        private WrapAccessTokenResult wrapResponse;

        /**
         * @return the expiresUtc
         */
        public Date getExpiresUtc() {
            return expiresUtc;
        }

        /**
         * @param expiresUtc
         *            the expiresUtc to set
         */
        public void setExpiresUtc(Date expiresUtc) {
            this.expiresUtc = expiresUtc;
        }

        /**
         * @return the wrapResponse
         */
        public WrapAccessTokenResult getWrapResponse() {
            return wrapResponse;
        }

        /**
         * @param wrapResponse
         *            the wrapResponse to set
         */
        public void setWrapResponse(WrapAccessTokenResult wrapResponse) {
            this.wrapResponse = wrapResponse;
        }
    }

}