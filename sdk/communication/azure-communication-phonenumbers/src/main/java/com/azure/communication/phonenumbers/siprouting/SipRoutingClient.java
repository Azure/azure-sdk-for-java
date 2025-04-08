// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers.siprouting;

import com.azure.communication.phonenumbers.siprouting.implementation.SipRoutingAdminClientImpl;
import com.azure.communication.phonenumbers.siprouting.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.phonenumbers.siprouting.implementation.models.SipConfiguration;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunk;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.communication.phonenumbers.siprouting.implementation.converters.SipConfigurationConverter;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkConverter.convertFromApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkConverter.convertToApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkRouteConverter.convertFromApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkRouteConverter.convertToApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.RoutesForNumberConverter.convertRoutesForNumber;
import com.azure.communication.phonenumbers.siprouting.implementation.models.ExpandEnum;
import com.azure.communication.phonenumbers.siprouting.models.RoutesForNumber;
import com.azure.communication.phonenumbers.siprouting.models.SipConfigurationModel;

/**
 * Synchronous SIP Routing Client. This client contains all the operations for
 * {@link SipTrunk SipTrunk} and {@link SipTrunkRoute SipTrunkRoute}.
 *
 * <p>
 * <strong>Instantiating a synchronous SIP Routing Client using connection
 * string</strong></p>
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
        return convertFromApi(getSipConfiguration(false).getTrunks()).stream()
            .filter(sipTrunk -> fqdn.equals(sipTrunk.getFqdn()))
            .findAny()
            .orElse(null);
    }

    /**
     * Gets SIP Trunk by FQDN.
     *
     * <p>
     * <strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.getTrunk -->
     * <pre>
     * SipTrunk trunk = sipRoutingClient.getTrunk&#40;&quot;&lt;trunk fqdn&gt;&quot;&#41;;
     * System.out.println&#40;&quot;Trunk &quot; + trunk.getFqdn&#40;&#41; + &quot;:&quot; + trunk.getSipSignalingPort&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.getTrunk -->
     *
     * @param fqdn SIP Trunk FQDN.
     * @param includeHealth Option to retrieve detailed configuration. Optional.
     * @return SIP Trunk if exists, null otherwise.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SipTrunk getTrunk(String fqdn, boolean includeHealth) {
        return convertFromApi(getSipConfiguration(includeHealth).getTrunks()).stream()
            .filter(sipTrunk -> fqdn.equals(sipTrunk.getFqdn()))
            .findAny()
            .orElse(null);
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
        return getSipConfigurationResponse(fqdn, false, context);
    }

    /**
     * Gets SIP Trunk by FQDN.
     *
     * <p>
     * <strong>Code Samples</strong></p>
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
     * @param includeHealth Option to retrieve detailed configuration. Optional
     * @param context the context of the request. Can also be null or
     * Context.NONE.
     * @return Response object with the SIP Trunk if exists, with null
     * otherwise.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SipTrunk> getTrunkWithResponse(String fqdn, boolean includeHealth, Context context) {
        return getSipConfigurationResponse(fqdn, includeHealth, context);
    }

    /**
    * Lists SIP Trunks.
    *
    * <p><strong>Code Samples</strong></p>
    *
    * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.listTrunks -->
    * <pre>
    * PagedIterable&lt;SipTrunk&gt; trunks = sipRoutingClient.listTrunks&#40;&#41;;
    * for &#40;SipTrunk trunk : trunks&#41; &#123;
    *     System.out.println&#40;&quot;Trunk &quot; + trunk.getFqdn&#40;&#41; + &quot;:&quot; + trunk.getSipSignalingPort&#40;&#41;&#41;;
    * &#125;
    * </pre>
    * <!-- end com.azure.communication.phonenumbers.siprouting.client.listTrunks -->
    *
    * @return SIP Trunks.
    */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<SipTrunk> listTrunks() {
        return new PagedIterable<SipTrunk>(() -> getOnePageTrunk(false));
    }

    /**
     * Lists SIP Trunks.
     *
     * <p>
     * <strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.listTrunks -->
     * <pre>
     * PagedIterable&lt;SipTrunk&gt; trunks = sipRoutingClient.listTrunks&#40;&#41;;
     * for &#40;SipTrunk trunk : trunks&#41; &#123;
     *     System.out.println&#40;&quot;Trunk &quot; + trunk.getFqdn&#40;&#41; + &quot;:&quot; + trunk.getSipSignalingPort&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.listTrunks -->
     *
     * @param includeHealth Option to retrieve detailed configuration. Optional
     * @return SIP Trunks.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<SipTrunk> listTrunks(boolean includeHealth) {
        return new PagedIterable<SipTrunk>(() -> getOnePageTrunk(includeHealth));
    }

    private PagedResponse<SipTrunk> getOnePageTrunk(boolean includeHealth) {
        if (includeHealth) {
            return client.getSipRoutings()
                .getWithResponseAsync(ExpandEnum.TRUNKS_HEALTH)
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .map(result -> new PagedResponseBase<>(result.getRequest(), result.getStatusCode(), result.getHeaders(),
                    convertFromApi(result.getValue().getTrunks()), null, null))
                .block();
        } else {
            return client.getSipRoutings()
                .getWithResponseAsync(null)
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .map(result -> new PagedResponseBase<>(result.getRequest(), result.getStatusCode(), result.getHeaders(),
                    convertFromApi(result.getValue().getTrunks()), null, null))
                .block();
        }
    }

    /**
     * Lists SIP Trunk Routes.
     *
     * <p>
     * <strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.listRoutes -->
     * <pre>
     * PagedIterable&lt;SipTrunkRoute&gt; routes = sipRoutingClient.listRoutes&#40;&#41;;
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
    public PagedIterable<SipTrunkRoute> listRoutes() {
        return new PagedIterable<SipTrunkRoute>(() -> getOnePageRoute());
    }

    private PagedResponse<SipTrunkRoute> getOnePageRoute() {
        return client.getSipRoutings()
            .getWithResponseAsync(null)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new PagedResponseBase<>(result.getRequest(), result.getStatusCode(), result.getHeaders(),
                convertFromApi(result.getValue().getRoutes()), null, null))
            .block();
    }

    /**
     * Sets SIP Trunk. If a trunk with specified FQDN already exists, it will be
     * replaced, otherwise a new trunk will be added.
     *
     * <p>
     * <strong>Code Samples</strong></p>
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
        Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> trunks
            = new HashMap<>();
        trunks.put(trunk.getFqdn(), convertToApi(trunk));
        client.getSipRoutings()
            .updateWithResponseAsync(new SipConfiguration().setTrunks(trunks))
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .block();
    }

    /**
     * Sets SIP Trunks.
     *
     * <p>
     * <strong>Code Samples</strong></p>
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
        List<String> storedFqdns = listTrunks(false).stream().map(SipTrunk::getFqdn).collect(Collectors.toList());
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
     * <p>
     * <strong>Code Samples</strong></p>
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
     * @param context the context of the request. Can also be null or
     * Context.NONE.
     * @return Response object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setTrunksWithResponse(List<SipTrunk> trunks, Context context) {
        SipConfiguration update = new SipConfiguration().setTrunks(convertToApi(trunks));
        List<String> storedFqdns = listTrunks(false).stream().map(SipTrunk::getFqdn).collect(Collectors.toList());
        Set<String> updatedFqdns = trunks.stream().map(SipTrunk::getFqdn).collect(Collectors.toSet());
        for (String storedFqdn : storedFqdns) {
            if (!updatedFqdns.contains(storedFqdn)) {
                update.getTrunks().put(storedFqdn, null);
            }
        }

        if (!update.getTrunks().isEmpty()) {
            return client.getSipRoutings()
                .updateWithResponseAsync(update, context)
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .map(result -> new SimpleResponse<Void>(result, null))
                .block();
        }

        return new SimpleResponse<>(null, 200, null, null);
    }

    /**
     * Sets SIP Trunk Routes.
     *
     * <p>
     * <strong>Code Samples</strong></p>
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
     * <p>
     * <strong>Code Samples</strong></p>
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
     * @param context the context of the request. Can also be null or
     * Context.NONE.
     * @return Response object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setRoutesWithResponse(List<SipTrunkRoute> routes, Context context) {
        return client.getSipRoutings()
            .updateWithResponseAsync(new SipConfiguration().setRoutes(convertToApi(routes)), context)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new SimpleResponse<Void>(result, null))
            .block();
    }

    /**
     * Deletes SIP Trunk.
     *
     * <p>
     * <strong>Code Samples</strong></p>
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
        PagedIterable<SipTrunk> trunks = listTrunks(false);
        List<SipTrunk> deletedTrunks
            = trunks.stream().filter(trunk -> fqdn.equals(trunk.getFqdn())).collect(Collectors.toList());

        if (!deletedTrunks.isEmpty()) {
            Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> trunksUpdate
                = new HashMap<>();
            trunksUpdate.put(fqdn, null);
            client.getSipRoutings()
                .updateWithResponseAsync(new SipConfiguration().setTrunks(trunksUpdate))
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .block();
        }
    }

    /**
     * Deletes SIP Trunk.
     *
     * <p>
     * <strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.client.deleteTrunkWithResponse -->
     * <pre>
     * Response&lt;Void&gt; response = sipRoutingClient.deleteTrunkWithResponse&#40;&quot;&lt;trunk fqdn&gt;&quot;, Context.NONE&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.client.deleteTrunkWithResponse -->
     *
     * @param fqdn SIP Trunk FQDN.
     * @param context the context of the request. Can also be null or
     * Context.NONE.
     * @return Response object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteTrunkWithResponse(String fqdn, Context context) {
        PagedIterable<SipTrunk> trunks = listTrunks(false);
        List<SipTrunk> deletedTrunks
            = trunks.stream().filter(trunk -> fqdn.equals(trunk.getFqdn())).collect(Collectors.toList());

        if (!deletedTrunks.isEmpty()) {
            Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> trunksUpdate
                = new HashMap<>();
            trunksUpdate.put(fqdn, null);
            return client.getSipRoutings()
                .updateWithResponseAsync(new SipConfiguration().setTrunks(trunksUpdate), context)
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .map(result -> new SimpleResponse<Void>(result, null))
                .block();
        }
        return new SimpleResponse<>(null, 200, null, null);
    }

    /**
     * Gets the list of routes matching the target phone number, ordered by
     * priority.
     *
     * @param targetPhoneNumber Phone number to test routing patterns against.
     * @param sipConfigurationModel Sip configuration object to test with
     * targetPhoneNumber.
     * @param context The context to associate with this operation.
     * @return the list of routes matching the target phone number, ordered by
     * priority along with {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RoutesForNumber> testRoutesWithNumberWithResponse(String targetPhoneNumber,
        SipConfigurationModel sipConfigurationModel, Context context) {
        SipConfiguration sipConfiguration = SipConfigurationConverter.convertSipConfiguration(sipConfigurationModel);
        return client.getSipRoutings()
            .testRoutesWithNumberWithResponseAsync(targetPhoneNumber, sipConfiguration, context)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new SimpleResponse<RoutesForNumber>(result, convertRoutesForNumber(result.getValue())))
            .block();
    }

    /**
     * Gets the list of routes matching the target phone number, ordered by
     * priority.
     *
     * @param targetPhoneNumber Phone number to test routing patterns against.
     * @param sipConfigurationModel Sip configuration object to test with
     * targetPhoneNumber.
     * @return the list of routes matching the target phone number, ordered by
     * priority.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RoutesForNumber testRoutesWithNumber(String targetPhoneNumber, SipConfigurationModel sipConfigurationModel) {
        SipConfiguration sipConfiguration = SipConfigurationConverter.convertSipConfiguration(sipConfigurationModel);
        return convertRoutesForNumber(
            client.getSipRoutings().testRoutesWithNumber(targetPhoneNumber, sipConfiguration));
    }

    private SipConfiguration getSipConfiguration(boolean includeHealth) {
        if (includeHealth) {
            return client.getSipRoutings()
                .getAsync(ExpandEnum.TRUNKS_HEALTH)
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .block();
        } else {
            return client.getSipRoutings()
                .getAsync(null)
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .block();
        }
    }

    private Response<SipTrunk> getSipConfigurationResponse(String fqdn, boolean includeHealth, Context context) {
        if (includeHealth) {
            return client.getSipRoutings()
                .getWithResponseAsync(ExpandEnum.TRUNKS_HEALTH, context)
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .map(result -> new SimpleResponse<>(result,
                    convertFromApi(result.getValue().getTrunks()).stream()
                        .filter(sipTrunk -> fqdn.equals(sipTrunk.getFqdn()))
                        .findAny()
                        .orElse(null)))
                .block();
        } else {
            return client.getSipRoutings()
                .getWithResponseAsync(null, context)
                .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
                .map(result -> new SimpleResponse<>(result,
                    convertFromApi(result.getValue().getTrunks()).stream()
                        .filter(sipTrunk -> fqdn.equals(sipTrunk.getFqdn()))
                        .findAny()
                        .orElse(null)))
                .block();
        }
    }

    private SipConfiguration setSipConfiguration(SipConfiguration update) {
        return client.getSipRoutings()
            .updateAsync(update)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .block();
    }

    private HttpResponseException translateException(CommunicationErrorResponseException exception) {
        return new HttpResponseException(exception.getMessage(), exception.getResponse());
    }
}
