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

import com.microsoft.windowsazure.core.UserAgentFilter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterRequestAdapter;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.exception.ServiceExceptionFactory;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.representation.Form;

public class WrapRestProxy implements WrapContract {
    private Client channel;

    private static Log log = LogFactory.getLog(WrapContract.class);

    @Inject
    public WrapRestProxy(Client channel, UserAgentFilter userAgentFilter) {
        this.channel = channel;
        this.channel.addFilter(new ClientFilterRequestAdapter(userAgentFilter));
    }

    @Override
    public WrapAccessTokenResult wrapAccessToken(String uri, String name,
            String password, String scope) throws ServiceException {
        Form requestForm = new Form();
        requestForm.add("wrap_name", name);
        requestForm.add("wrap_password", password);
        requestForm.add("wrap_scope", scope);

        Form responseForm;
        try {
            responseForm = channel.resource(uri)
                    .accept(MediaType.APPLICATION_FORM_URLENCODED)
                    .type(MediaType.APPLICATION_FORM_URLENCODED)
                    .post(Form.class, requestForm);
        } catch (UniformInterfaceException e) {
            log.warn("WRAP server returned error acquiring access_token", e);
            throw ServiceExceptionFactory.process("WRAP", new ServiceException(
                    "WRAP server returned error acquiring access_token", e));
        }

        WrapAccessTokenResult response = new WrapAccessTokenResult();

        response.setAccessToken(responseForm.getFirst("wrap_access_token"));

        String expiresIn = responseForm
                .getFirst("wrap_access_token_expires_in");
        response.setExpiresIn(Long.parseLong(expiresIn));

        return response;
    }
}
