// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.siprouting.implementation.SipRoutingAdminClientImpl;
import com.azure.communication.phonenumbers.siprouting.implementation.converters.SipRoutingErrorConverter;
import com.azure.communication.phonenumbers.siprouting.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.phonenumbers.siprouting.implementation.models.SipConfiguration;
import com.azure.communication.phonenumbers.siprouting.models.SipRoutingError;
import com.azure.communication.phonenumbers.siprouting.models.SipRoutingResponseException;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunk;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkConverter.convertFromApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkConverter.convertToApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkRouteConverter.convertFromApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkRouteConverter.convertToApi;

/**
 * Synchronous SIP Routing Client.
 * This client contains all the operations for {@link SipTrunk SipTrunk} and {@link SipTrunkRoute SipTrunkRoute}.
 *
 * <p><strong>Instantiating a synchronous SIP Routing Client using connection string</strong></p>
 *
 * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.instantiation -->
 * <pre>
 * SipRoutingClient sipRoutingClient = new SipRoutingClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.communication.phonenumbers.siprouting.client.instantiation -->
 *
 * @see SipRoutingClientBuilder
 */
@ServiceClient(builder = SipRoutingClientBuilder.class)
public final class SipRoutingClient {
    private final SipRoutingAdminClientImpl client;

    SipRoutingClient(SipRoutingAdminClientImpl client) {
        this.client = client;
    }

    /**
     * Gets SIP Trunk by FQDN.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.getTrunk -->
     * <pre>
     * SipTrunk trunk = sipRoutingClient.getTrunk&#40;&quot;&lt;trunk fqdn&gt;&quot;&#41;;
     * System.out.println&#40;&quot;Trunk &quot; + trunk.getFqdn&#40;&#41; + &quot;:&quot; + trunk.getSipSignalingPort&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.getTrunk -->
     *
     * @param fqdn SIP Trunk FQDN.
     * @return SIP Trunk if exists, null otherwise.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SipTrunk getTrunk(String fqdn) {
        return convertFromApi(getSipConfiguration().getTrunks()).stream()
            .filter(sipTrunk -> fqdn.equals(sipTrunk.getFqdn())).findAny().orElse(null);
    }

    /**
     * Gets SIP Trunk by FQDN.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.getTrunkWithResponse -->
     * <pre>
     * Response&lt;SipTrunk&gt; response = sipRoutingClient.getTrunkWithResponse&#40;&quot;&lt;trunk fqdn&gt;&quot;, Context.NONE&#41;;
     * SipTrunk trunk = response.getValue&#40;&#41;;
     * System.out.println&#40;&quot;Trunk &quot; + trunk.getFqdn&#40;&#41; + &quot;:&quot; + trunk.getSipSignalingPort&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.getTrunkWithResponse -->
     *
     * @param fqdn SIP Trunk FQDN.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return Response object with the SIP Trunk if exists, with null otherwise.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SipTrunk> getTrunkWithResponse(String fqdn, Context context) {
        return client.getSipConfigurationWithResponseAsync(context)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new SimpleResponse<>(result, convertFromApi(result.getValue().getTrunks()).stream()
                .filter(sipTrunk -> fqdn.equals(sipTrunk.getFqdn())).findAny().orElse(null)))
            .block();
    }

    /**
     * Lists SIP Trunks.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.listTrunks -->
     * <pre>
     * List&lt;SipTrunk&gt; trunks = sipRoutingClient.listTrunks&#40;&#41;;
     * for &#40;SipTrunk trunk : trunks&#41; &#123;
     *     System.out.println&#40;&quot;Trunk &quot; + trunk.getFqdn&#40;&#41; + &quot;:&quot; + trunk.getSipSignalingPort&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.listTrunks -->
     *
     * @return SIP Trunks.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<SipTrunk> listTrunks() {
        return convertFromApi(getSipConfiguration().getTrunks());
    }

    /**
     * Lists SIP Trunks.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.listTrunksWithResponse -->
     * <pre>
     * Response&lt;List&lt;SipTrunk&gt;&gt; response = sipRoutingClient.listTrunksWithResponse&#40;Context.NONE&#41;;
     * List&lt;SipTrunk&gt; trunks = response.getValue&#40;&#41;;
     * for &#40;SipTrunk trunk : trunks&#41; &#123;
     *     System.out.println&#40;&quot;Trunk &quot; + trunk.getFqdn&#40;&#41; + &quot;:&quot; + trunk.getSipSignalingPort&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.listTrunksWithResponse -->
     *
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return Response object with the SIP Trunks.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<SipTrunk>> listTrunksWithResponse(Context context) {
        return client.getSipConfigurationWithResponseAsync(context)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new SimpleResponse<>(result, convertFromApi(result.getValue().getTrunks())))
            .block();
    }

    /**
     * Lists SIP Trunk Routes.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.listRoutes -->
     * <pre>
     * List&lt;SipTrunkRoute&gt; routes = sipRoutingClient.listRoutes&#40;&#41;;
     * for &#40;SipTrunkRoute route : routes&#41; &#123;
     *     System.out.println&#40;&quot;Route name: &quot; + route.getName&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Route description: &quot; + route.getDescription&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Route number pattern: &quot; + route.getNumberPattern&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Route trunks: &quot; + String.join&#40;&quot;,&quot;, route.getTrunks&#40;&#41;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.listRoutes -->
     *
     * @return SIP Trunk Routes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<SipTrunkRoute> listRoutes() {
        return convertFromApi(getSipConfiguration().getRoutes());
    }

    /**
     * Lists SIP Trunk Routes.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.listRoutesWithResponse -->
     * <pre>
     * Response&lt;List&lt;SipTrunkRoute&gt;&gt; response = sipRoutingClient.listRoutesWithResponse&#40;Context.NONE&#41;;
     * List&lt;SipTrunkRoute&gt; routes = response.getValue&#40;&#41;;
     * for &#40;SipTrunkRoute route : routes&#41; &#123;
     *     System.out.println&#40;&quot;Route name: &quot; + route.getName&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Route description: &quot; + route.getDescription&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Route number pattern: &quot; + route.getNumberPattern&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Route trunks: &quot; + String.join&#40;&quot;,&quot;, route.getTrunks&#40;&#41;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.listRoutesWithResponse -->
     *
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return Response object with the SIP Trunk Routes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<SipTrunkRoute>> listRoutesWithResponse(Context context) {
        return client.getSipConfigurationWithResponseAsync(context)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new SimpleResponse<>(result, convertFromApi(result.getValue().getRoutes())))
            .block();
    }

    /**
     * Sets SIP Trunk.
     * If a trunk with specified FQDN already exists, it will be replaced, otherwise a new trunk will be added.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.setTrunk -->
     * <pre>
     * sipRoutingClient.setTrunk&#40;new SipTrunk&#40;&quot;&lt;trunk fqdn&gt;&quot;, 12345&#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.setTrunk -->
     *
     * @param trunk SIP Trunk.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setTrunk(SipTrunk trunk) {
        Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> trunks = new HashMap<>();
        trunks.put(trunk.getFqdn(), convertToApi(trunk));
        client.patchSipConfigurationWithResponseAsync(new SipConfiguration().setTrunks(trunks))
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .block();
    }

    /**
     * Sets SIP Trunks.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.setTrunks -->
     * <pre>
     * sipRoutingClient.setTrunks&#40;asList&#40;
     *     new SipTrunk&#40;&quot;&lt;first trunk fqdn&gt;&quot;, 12345&#41;,
     *     new SipTrunk&#40;&quot;&lt;second trunk fqdn&gt;&quot;, 23456&#41;
     * &#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.setTrunks -->
     *
     * @param trunks SIP Trunks.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setTrunks(List<SipTrunk> trunks) {
        SipConfiguration update = new SipConfiguration().setTrunks(convertToApi(trunks));
        List<String> storedFqdns = listTrunks().stream().map(SipTrunk::getFqdn).collect(Collectors.toList());
        Set<String> updatedFqdns = trunks.stream().map(SipTrunk::getFqdn).collect(Collectors.toSet());
        for (String storedFqdn : storedFqdns) {
            if (!updatedFqdns.contains(storedFqdn)) {
                update.getTrunks().put(storedFqdn, null);
            }
        }

        if (!update.getTrunks().isEmpty()) {
            setSipConfiguration(update);
        }
    }

    /**
     * Sets SIP Trunks.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.setTrunksWithResponse -->
     * <pre>
     * Response&lt;Void&gt; response = sipRoutingClient.setTrunksWithResponse&#40;asList&#40;
     *     new SipTrunk&#40;&quot;&lt;first trunk fqdn&gt;&quot;, 12345&#41;,
     *     new SipTrunk&#40;&quot;&lt;second trunk fqdn&gt;&quot;, 23456&#41;
     * &#41;, Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.setTrunksWithResponse -->
     *
     * @param trunks SIP Trunks.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return Response object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setTrunksWithResponse(List<SipTrunk> trunks, Context context) {
        SipConfiguration update = new SipConfiguration().setTrunks(convertToApi(trunks));
        List<String> storedFqdns = listTrunks().stream().map(SipTrunk::getFqdn).collect(Collectors.toList());
        Set<String> updatedFqdns = trunks.stream().map(SipTrunk::getFqdn).collect(Collectors.toSet());
        for (String storedFqdn : storedFqdns) {
            if (!updatedFqdns.contains(storedFqdn)) {
                update.getTrunks().put(storedFqdn, null);
            }
        }

        if (!update.getTrunks().isEmpty()) {
            return client.patchSipConfigurationWithResponseAsync(update, context)
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .map(result -> new SimpleResponse<Void>(result, null))
                .block();
        }

        return new SimpleResponse<>(null, 200, null, null);
    }

    /**
     * Sets SIP Trunk Routes.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.setRoutes -->
     * <pre>
     * sipRoutingClient.setRoutes&#40;asList&#40;
     *     new SipTrunkRoute&#40;&quot;route name1&quot;, &quot;.*9&quot;&#41;.setTrunks&#40;asList&#40;&quot;&lt;first trunk fqdn&gt;&quot;, &quot;&lt;second trunk fqdn&gt;&quot;&#41;&#41;,
     *     new SipTrunkRoute&#40;&quot;route name2&quot;, &quot;.*&quot;&#41;.setTrunks&#40;asList&#40;&quot;&lt;second trunk fqdn&gt;&quot;&#41;&#41;
     * &#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.setRoutes -->
     *
     * @param routes SIP Trunk Routes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setRoutes(List<SipTrunkRoute> routes) {
        setSipConfiguration(new SipConfiguration().setRoutes(convertToApi(routes)));
    }

    /**
     * Sets SIP Trunk Routes.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.setRoutesWithResponse -->
     * <pre>
     * Response&lt;Void&gt; response = sipRoutingClient.setRoutesWithResponse&#40;asList&#40;
     *     new SipTrunkRoute&#40;&quot;route name1&quot;, &quot;.*9&quot;&#41;.setTrunks&#40;asList&#40;&quot;&lt;first trunk fqdn&gt;&quot;, &quot;&lt;second trunk fqdn&gt;&quot;&#41;&#41;,
     *     new SipTrunkRoute&#40;&quot;route name2&quot;, &quot;.*&quot;&#41;.setTrunks&#40;asList&#40;&quot;&lt;second trunk fqdn&gt;&quot;&#41;&#41;
     * &#41;, Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.setRoutesWithResponse -->
     *
     * @param routes SIP Trunk Routes.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return Response object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setRoutesWithResponse(List<SipTrunkRoute> routes, Context context) {
        return client.patchSipConfigurationWithResponseAsync(new SipConfiguration().setRoutes(convertToApi(routes)), context)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new SimpleResponse<Void>(result, null))
            .block();
    }

    /**
     * Deletes SIP Trunk.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.deleteTrunk -->
     * <pre>
     * sipRoutingClient.deleteTrunk&#40;&quot;&lt;trunk fqdn&gt;&quot;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.deleteTrunk -->
     *
     * @param fqdn SIP Trunk FQDN.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteTrunk(String fqdn) {
        List<SipTrunk> trunks = listTrunks();
        List<SipTrunk> deletedTrunks = trunks.stream()
            .filter(trunk -> fqdn.equals(trunk.getFqdn()))
            .collect(Collectors.toList());

        if (!deletedTrunks.isEmpty()) {
            Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> trunksUpdate = new HashMap<>();
            trunksUpdate.put(fqdn, null);
            client.patchSipConfigurationWithResponseAsync(new SipConfiguration().setTrunks(trunksUpdate))
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .block();
        }
    }

    /**
     * Deletes SIP Trunk.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.deleteTrunkWithResponse -->
     * <pre>
     * Response&lt;Void&gt; response = sipRoutingClient.deleteTrunkWithResponse&#40;&quot;&lt;trunk fqdn&gt;&quot;, Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.deleteTrunkWithResponse -->
     *
     * @param fqdn SIP Trunk FQDN.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return Response object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTrunkWithResponse(String fqdn, Context context) {
        List<SipTrunk> trunks = listTrunks();
        List<SipTrunk> deletedTrunks = trunks.stream().filter(trunk -> fqdn.equals(trunk.getFqdn()))
            .collect(Collectors.toList());

        if (!deletedTrunks.isEmpty()) {
            Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> trunksUpdate = new HashMap<>();
            trunksUpdate.put(fqdn, null);
            return client.patchSipConfigurationWithResponseAsync(new SipConfiguration().setTrunks(trunksUpdate), context)
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .map(result -> new SimpleResponse<Void>(result, null)).block();
        }
        return new SimpleResponse<>(null, 200, null, null);
    }

    private SipConfiguration getSipConfiguration() {
        return client.getSipConfigurationAsync()
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .block();
    }

    private SipConfiguration setSipConfiguration(SipConfiguration update) {
        return client.patchSipConfigurationAsync(update)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .block();
    }

    private SipRoutingResponseException translateException(CommunicationErrorResponseException exception) {
        SipRoutingError error = null;
        if (exception.getValue() != null) {
            error = SipRoutingErrorConverter.convert(exception.getValue().getError());
        }
        return new SipRoutingResponseException(exception.getMessage(), exception.getResponse(), error);
    }
}
