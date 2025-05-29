// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting;

import com.azure.communication.phonenumbers.siprouting.implementation.SipRoutingAdminClientImpl;
import com.azure.communication.phonenumbers.siprouting.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunk;
import com.azure.communication.phonenumbers.siprouting.models.SipTrunkRoute;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipDomainConverter.convertFromApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkConverter.convertFromApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipDomainConverter.convertToApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkConverter.convertToApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkRouteConverter.convertFromApi;
import static com.azure.communication.phonenumbers.siprouting.implementation.converters.SipTrunkRouteConverter.convertToApi;

import com.azure.communication.phonenumbers.siprouting.implementation.models.ExpandEnum;
import com.azure.communication.phonenumbers.siprouting.implementation.models.SipConfiguration;
import com.azure.communication.phonenumbers.siprouting.models.SipDomain;

/**
 * Asynchronous SIP Routing Client.
 * This client contains all the operations for {@link SipTrunk SipTrunk} and {@link SipTrunkRoute SipTrunkRoute}.
 *
 * <p><strong>Instantiating an asynchronous SIP Routing Client using connection string</strong></p>
 *
 * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.instantiation -->
 * <pre>
 * SipRoutingAsyncClient sipRoutingAsyncClient = new SipRoutingClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.instantiation -->
 *
 * @see SipRoutingClientBuilder
 */
@ServiceClient(builder = SipRoutingClientBuilder.class, isAsync = true)
public final class SipRoutingAsyncClient {
    private final SipRoutingAdminClientImpl client;

    SipRoutingAsyncClient(SipRoutingAdminClientImpl client) {
        this.client = client;
    }

    /**
    * Gets SIP Trunk by FQDN.
    *
    * <p><strong>Code Samples</strong></p>
    *
    * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.getTrunk -->
    * <pre>
    * sipRoutingAsyncClient.getTrunk&#40;&quot;&lt;trunk fqdn&gt;&quot;&#41;.subscribe&#40;trunk -&gt;
    *     System.out.println&#40;&quot;Trunk &quot; + trunk.getFqdn&#40;&#41; + &quot;:&quot; + trunk.getSipSignalingPort&#40;&#41;&#41;&#41;;
    * </pre>
    * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.getTrunk -->
    *
    * @param fqdn SIP Trunk FQDN.
    * @return SIP Trunk if exists, null otherwise.
    */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SipTrunk> getTrunk(String fqdn) {
        return getSipConfiguration().flatMap(config -> {
            SipTrunk trunk = convertFromApi(config.getTrunks()).stream()
                .filter(sipTrunk -> fqdn.equals(sipTrunk.getFqdn()))
                .findAny()
                .orElse(null);
            return trunk != null ? Mono.just(trunk) : Mono.empty();
        });
    }

    /**
    * Gets SIP Trunk by FQDN.
    *
    * <p><strong>Code Samples</strong></p>
    *
    * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.getTrunkWithResponse -->
    * <pre>
    * sipRoutingAsyncClient.getTrunkWithResponse&#40;&quot;&lt;trunk fqdn&gt;&quot;&#41;
    *     .subscribe&#40;response -&gt; &#123;
    *         SipTrunk trunk = response.getValue&#40;&#41;;
    *         System.out.println&#40;&quot;Trunk &quot; + trunk.getFqdn&#40;&#41; + &quot;:&quot; + trunk.getSipSignalingPort&#40;&#41;&#41;;
    *     &#125;&#41;;
    * </pre>
    * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.getTrunkWithResponse -->
    *
    * @param fqdn SIP Trunk FQDN.
    * @return Response object with the SIP Trunk if exists, with null otherwise.
    */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SipTrunk>> getTrunkWithResponse(String fqdn) {
        return getSipConfigurationWithResponse()
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new SimpleResponse<>(result,
                convertFromApi(result.getValue().getTrunks()).stream()
                    .filter(sipTrunk -> fqdn.equals(sipTrunk.getFqdn()))
                    .findAny()
                    .orElse(null)));
    }

    /**
     * Lists SIP Trunks.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.listTrunks -->
     * <pre>
     * sipRoutingAsyncClient.listTrunks&#40;&#41;
     *     .subscribe&#40;trunk -&gt;
     *         System.out.println&#40;&quot;Trunk &quot; + trunk.getFqdn&#40;&#41; + &quot;:&quot; + trunk.getSipSignalingPort&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.listTrunks -->
     *
     * @return SIP Trunks.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SipTrunk> listTrunks() {
        return new PagedFlux<SipTrunk>(() -> getOnePageTrunks());
    }

    private Mono<PagedResponse<SipTrunk>> getOnePageTrunks() {
        return client.getSipRoutings()
            .getWithResponseAsync(ExpandEnum.TRUNKS_HEALTH)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new PagedResponseBase<>(result.getRequest(), result.getStatusCode(), result.getHeaders(),
                convertFromApi(result.getValue().getTrunks()), null, null));
    }

    /**
     * Lists SIP Trunk Routes.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.listRoutes -->
     * <pre>
     * sipRoutingAsyncClient.listRoutes&#40;&#41;.subscribe&#40;route -&gt; &#123;
     *     System.out.println&#40;&quot;Route name: &quot; + route.getName&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Route description: &quot; + route.getDescription&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Route number pattern: &quot; + route.getNumberPattern&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Route trunks: &quot; + String.join&#40;&quot;,&quot;, route.getTrunks&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.listRoutes -->
     *
     * @return SIP Trunk Routes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SipTrunkRoute> listRoutes() {
        return new PagedFlux<SipTrunkRoute>(() -> getOnePageRoutes());
    }

    private Mono<PagedResponse<SipTrunkRoute>> getOnePageRoutes() {
        return client.getSipRoutings()
            .getWithResponseAsync(null)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new PagedResponseBase<>(result.getRequest(), result.getStatusCode(), result.getHeaders(),
                convertFromApi(result.getValue().getRoutes()), null, null));
    }

    /**
     * Sets SIP Trunk.
     * If a trunk with specified FQDN already exists, it will be replaced, otherwise a new trunk will be added.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunk -->
     * <pre>
     * sipRoutingAsyncClient.setTrunk&#40;new SipTrunk&#40;&quot;&lt;trunk fqdn&gt;&quot;, 12345&#41;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunk -->
     *
     * @param trunk SIP Trunk.
     * @return void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setTrunk(SipTrunk trunk) {
        Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> trunks
            = new HashMap<>();
        trunks.put(trunk.getFqdn(), convertToApi(trunk));
        return setSipConfiguration(new SipConfiguration().setTrunks(trunks)).then();
    }

    /**
     * Sets SIP Trunks.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunks -->
     * <pre>
     * sipRoutingAsyncClient.setTrunks&#40;asList&#40;
     *     new SipTrunk&#40;&quot;&lt;first trunk fqdn&gt;&quot;, 12345&#41;,
     *     new SipTrunk&#40;&quot;&lt;second trunk fqdn&gt;&quot;, 23456&#41;
     * &#41;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunks -->
     *
     * @param trunks SIP Trunks.
     * @return void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setTrunks(List<SipTrunk> trunks) {
        SipConfiguration update = new SipConfiguration().setTrunks(convertToApi(trunks));
        List<SipTrunk> currentTrunks = getTrunksInternal().block();
        if (currentTrunks != null) {
            List<String> storedFqdns = currentTrunks.stream().map(SipTrunk::getFqdn).collect(Collectors.toList());
            Set<String> updatedFqdns = trunks.stream().map(SipTrunk::getFqdn).collect(Collectors.toSet());
            for (String storedFqdn : storedFqdns) {
                if (!updatedFqdns.contains(storedFqdn)) {
                    update.getTrunks().put(storedFqdn, null);
                }
            }
        }

        if (!update.getTrunks().isEmpty()) {
            return setSipConfiguration(update).then();
        }

        return Mono.empty();
    }

    /**
     * Sets SIP Trunks.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunksWithResponse -->
     * <pre>
     * sipRoutingAsyncClient.setTrunksWithResponse&#40;asList&#40;
     *     new SipTrunk&#40;&quot;&lt;first trunk fqdn&gt;&quot;, 12345&#41;,
     *     new SipTrunk&#40;&quot;&lt;second trunk fqdn&gt;&quot;, 23456&#41;
     * &#41;&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.println&#40;&quot;Response status &quot; + response.getStatusCode&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.setTrunksWithResponse -->
     *
     * @param trunks SIP Trunks.
     * @return Response object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setTrunksWithResponse(List<SipTrunk> trunks) {
        SipConfiguration update = new SipConfiguration().setTrunks(convertToApi(trunks));
        List<SipTrunk> currentTrunks = getTrunksInternal().block();
        if (currentTrunks != null) {
            List<String> storedFqdns = currentTrunks.stream().map(SipTrunk::getFqdn).collect(Collectors.toList());
            Set<String> updatedFqdns = trunks.stream().map(SipTrunk::getFqdn).collect(Collectors.toSet());
            for (String storedFqdn : storedFqdns) {
                if (!updatedFqdns.contains(storedFqdn)) {
                    update.getTrunks().put(storedFqdn, null);
                }
            }
        }

        if (!update.getTrunks().isEmpty()) {
            return setSipConfigurationWithResponse(update).map(result -> new SimpleResponse<>(result, null));
        }

        return Mono.just(new SimpleResponse<>(null, 200, null, null));
    }

    /**
     * Sets SIP Trunk Routes.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.setRoutes -->
     * <pre>
     * sipRoutingAsyncClient.setRoutes&#40;asList&#40;
     *     new SipTrunkRoute&#40;&quot;route name1&quot;, &quot;.*9&quot;&#41;.setTrunks&#40;asList&#40;&quot;&lt;first trunk fqdn&gt;&quot;, &quot;&lt;second trunk fqdn&gt;&quot;&#41;&#41;,
     *     new SipTrunkRoute&#40;&quot;route name2&quot;, &quot;.*&quot;&#41;.setTrunks&#40;asList&#40;&quot;&lt;second trunk fqdn&gt;&quot;&#41;&#41;
     * &#41;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.setRoutes -->
     *
     * @param routes SIP Trunk Routes.
     * @return void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setRoutes(List<SipTrunkRoute> routes) {
        return setSipConfiguration(new SipConfiguration().setRoutes(convertToApi(routes))).then();
    }

    /**
     * Sets SIP Trunk Routes.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.setRoutesWithResponse -->
     * <pre>
     * sipRoutingAsyncClient.setRoutesWithResponse&#40;asList&#40;
     *     new SipTrunkRoute&#40;&quot;route name1&quot;, &quot;.*9&quot;&#41;.setTrunks&#40;asList&#40;&quot;&lt;first trunk fqdn&gt;&quot;, &quot;&lt;second trunk fqdn&gt;&quot;&#41;&#41;,
     *     new SipTrunkRoute&#40;&quot;route name2&quot;, &quot;.*&quot;&#41;.setTrunks&#40;asList&#40;&quot;&lt;second trunk fqdn&gt;&quot;&#41;&#41;
     * &#41;&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.println&#40;&quot;Response status &quot; + response.getStatusCode&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.setRoutesWithResponse -->
     *
     * @param routes SIP Trunk Routes.
     * @return Response object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setRoutesWithResponse(List<SipTrunkRoute> routes) {
        return setSipConfigurationWithResponse(new SipConfiguration().setRoutes(convertToApi(routes)))
            .map(result -> new SimpleResponse<>(result, null));
    }

    /**
     * Deletes SIP Trunk.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.deleteTrunk -->
     * <pre>
     * sipRoutingAsyncClient.deleteTrunk&#40;&quot;&lt;trunk fqdn&gt;&quot;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.deleteTrunk -->
     *
     * @param fqdn SIP Trunk FQDN.
     * @return void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteTrunk(String fqdn) {
        List<SipTrunk> trunks = getTrunksInternal().block();
        if (trunks == null || trunks.isEmpty()) {
            return Mono.empty();
        }

        List<SipTrunk> deletedTrunks
            = trunks.stream().filter(trunk -> fqdn.equals(trunk.getFqdn())).collect(Collectors.toList());

        if (!deletedTrunks.isEmpty()) {
            Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> trunksUpdate
                = new HashMap<>();
            trunksUpdate.put(fqdn, null);
            return setSipConfiguration(new SipConfiguration().setTrunks(trunksUpdate)).then();
        }
        return Mono.empty();
    }

    /**
     * Deletes SIP Trunk.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.deleteTrunkWithResponse -->
     * <pre>
     * sipRoutingAsyncClient.deleteTrunkWithResponse&#40;&quot;&lt;trunk fqdn&gt;&quot;&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.println&#40;&quot;Response status &quot; + response.getStatusCode&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.deleteTrunkWithResponse -->
     *
     * @param fqdn SIP Trunk FQDN.
     * @return Response object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteTrunkWithResponse(String fqdn) {
        List<SipTrunk> trunks = getTrunksInternal().block();
        if (trunks == null || trunks.isEmpty()) {
            return Mono.just(new SimpleResponse<>(null, 200, null, null));
        }

        List<SipTrunk> deletedTrunks
            = trunks.stream().filter(trunk -> fqdn.equals(trunk.getFqdn())).collect(Collectors.toList());

        if (!deletedTrunks.isEmpty()) {
            Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipTrunk> trunksUpdate
                = new HashMap<>();
            trunksUpdate.put(fqdn, null);
            return setSipConfigurationWithResponse(new SipConfiguration().setTrunks(trunksUpdate))
                .map(result -> new SimpleResponse<>(result, null));
        }
        return Mono.just(new SimpleResponse<>(null, 200, null, null));
    }

    /**
    * Gets the list of routes matching the target phone number, ordered by priority.
    * 
    * @param targetPhoneNumber Phone number to test routing patterns against.
    * @param routes List of routes to test with targetPhoneNumber.
    * @return the list of routes matching the target phone number, ordered by priority along with {@link Response} on
    * successful completion of {@link Mono}.
    */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<List<SipTrunkRoute>>> getRoutesForNumberWithResponse(String targetPhoneNumber,
        List<SipTrunkRoute> routes) {
        SipConfiguration sipConfiguration = new SipConfiguration().setRoutes(convertToApi(routes));

        return client.getSipRoutings()
            .testRoutesWithNumberWithResponseAsync(targetPhoneNumber, sipConfiguration)
            .map(result -> new SimpleResponse<List<SipTrunkRoute>>(result,
                convertFromApi(result.getValue().getMatchingRoutes())));
    }

    /**
    * Gets the list of routes matching the target phone number, ordered by priority.
    * 
    * @param targetPhoneNumber Phone number to test routing patterns against.
    * @param routes List of routes to test with targetPhoneNumber.
    * @return the list of routes matching the target phone number, ordered by priority along with {@link Response} on
    * successful completion of {@link Mono}.
    */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<SipTrunkRoute>> getRoutesForNumber(String targetPhoneNumber, List<SipTrunkRoute> routes) {
        SipConfiguration sipConfiguration = new SipConfiguration().setRoutes(convertToApi(routes));

        return client.getSipRoutings()
            .testRoutesWithNumberWithResponseAsync(targetPhoneNumber, sipConfiguration)
            .map(result -> convertFromApi(result.getValue().getMatchingRoutes()));

    }

    /**
    * Gets SIP Domain by domain name.
    *
    * <p><strong>Code Samples</strong></p>
    *
    * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.getDomain -->
    * <pre>
    * sipRoutingAsyncClient.getDomain&#40;&quot;&lt;domain name&gt;&quot;&#41;.subscribe&#40;domain -&gt;
    *     System.out.println&#40;&quot;Domain &quot; + domain.isEnabled&#40;&#41;&#41;&#41;;
    * </pre>
    * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.getDomain -->
    *
    * @param domainName SIP domain name.
    * @return SIP Domain if exists, null otherwise.
    */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SipDomain> getDomain(String domainName) {
        return getSipConfiguration().flatMap(config -> {
            SipDomain domain = convertFromApi(config.getDomains()).stream()
                .filter(sipDomain -> domainName.equals(sipDomain.getFqdn()))
                .findAny()
                .orElse(null);
            return domain != null ? Mono.just(domain) : Mono.empty();
        });
    }

    /**
     * Gets SIP Domain by domainName.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.getDomainWithResponse -->
     * <pre>
     * sipRoutingAsyncClient.getDomainWithResponse&#40;&quot;&lt;domain domainName&gt;&quot;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         SipDomain domain = response.getValue&#40;&#41;;
     *         System.out.println&#40;&quot;Domain &quot; + domain.isEnabled&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.getDomainWithResponse -->
     *
     * @param domainName SIP Domain nmae.
     * @return Response object with the SIP Domain if exists, with null otherwise.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SipDomain>> getDomainWithResponse(String domainName) {
        return getSipConfigurationWithResponse()
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new SimpleResponse<>(result,
                convertFromApi(result.getValue().getDomains()).stream()
                    .filter(sipDomain -> domainName.equals(sipDomain.getFqdn()))
                    .findAny()
                    .orElse(null)));
    }

    /**
     * Lists SIP Domains.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.listDomains -->
     * <pre>
     * sipRoutingAsyncClient.listDomains&#40;&#41;
     *     .subscribe&#40;domain -&gt;
     *         System.out.println&#40;&quot;Domain &quot; + domain.isEnabled&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.listDomains -->
     *
     * @return SIP Domains.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SipDomain> listDomains() {
        return new PagedFlux<SipDomain>(() -> getOnePageDomains());
    }

    private Mono<PagedResponse<SipDomain>> getOnePageDomains() {
        return client.getSipRoutings()
            .getWithResponseAsync(null)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException)
            .map(result -> new PagedResponseBase<>(result.getRequest(), result.getStatusCode(), result.getHeaders(),
                convertFromApi(result.getValue().getDomains()), null, null));
    }

    /**
     * Sets SIP Domain.
     * If a domain with specified domain name already exists, it will be replaced, otherwise a new trunk will be added.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.setDomain -->
     * <pre>
     * sipRoutingAsyncClient.setDomain&#40;new SipDomain&#40;&quot;&lt;first trunk fqdn&gt;&quot;, false&#41;
     * &#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.setDomain -->
     *
     * @param domain SIP Domain.
     * @return void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setDomain(SipDomain domain) {
        Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain> domains
            = new HashMap<>();
        domains.put(domain.getFqdn(), convertToApi(domain));
        return setSipConfiguration(new SipConfiguration().setDomains(domains)).then();
    }

    /**
     * Sets SIP Domains.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.setDomains -->
     * <pre>
     * sipRoutingAsyncClient.setDomains&#40;asList&#40;
     *     new SipDomain&#40;&quot;&lt;first trunk fqdn&gt;&quot;, false&#41;,
     *     new SipDomain&#40;&quot;&lt;first trunk fqdn&gt;&quot;, false&#41;
     * &#41;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.setDomains -->
     *
     * @param domains SIP Domains.
     * @return void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setDomains(List<SipDomain> domains) {
        SipConfiguration update = new SipConfiguration().setDomains(convertToApi(domains));
        List<SipDomain> currentDomains = getDomainsInternal().block();
        if (currentDomains != null) {
            List<String> storedDomains = currentDomains.stream().map(SipDomain::getFqdn).collect(Collectors.toList());
            Set<String> updatedDomains = domains.stream().map(SipDomain::getFqdn).collect(Collectors.toSet());
            for (String storedDomain : storedDomains) {
                if (!updatedDomains.contains(storedDomain)) {
                    update.getDomains().put(storedDomain, null);
                }
            }
        }

        if (!update.getDomains().isEmpty()) {
            return setSipConfiguration(update).then();
        }

        return Mono.empty();
    }

    /**
     * Sets SIP Domains.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.setDomainsWithResponse -->
     * <pre>
     * sipRoutingAsyncClient.setDomainsWithResponse&#40;asList&#40;
     *     new SipDomain&#40;&quot;&lt;first trunk fqdn&gt;&quot;, false&#41;,
     *     new SipDomain&#40;&quot;&lt;first trunk fqdn&gt;&quot;, false&#41;
     * &#41;&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.println&#40;&quot;Response status &quot; + response.getStatusCode&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.setDomainsWithResponse -->
     *
     * @param domains SIP Trunks.
     * @return Response object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setDomainsWithResponse(List<SipDomain> domains) {
        SipConfiguration update = new SipConfiguration().setDomains(convertToApi(domains));
        List<SipDomain> currentDomainss = getDomainsInternal().block();
        if (currentDomainss != null) {
            List<String> storedDomains = currentDomainss.stream().map(SipDomain::getFqdn).collect(Collectors.toList());
            Set<String> updatedDomains = domains.stream().map(SipDomain::getFqdn).collect(Collectors.toSet());
            for (String storedDomain : storedDomains) {
                if (!updatedDomains.contains(storedDomain)) {
                    update.getDomains().put(storedDomain, null);
                }
            }
        }

        if (!update.getDomains().isEmpty()) {
            return setSipConfigurationWithResponse(update).map(result -> new SimpleResponse<>(result, null));
        }

        return Mono.just(new SimpleResponse<>(null, 200, null, null));
    }

    /**
     * Deletes SIP Domains.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.deleteDomains -->
     * <pre>
     * sipRoutingAsyncClient.deleteDomain&#40;&quot;&lt;domain name&gt;&quot;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.deleteDomain -->
     *
     * @param domainName SIP Domain name.
     * @return void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDomain(String domainName) {
        List<SipDomain> domains = getDomainsInternal().block();
        if (domains == null || domains.isEmpty()) {
            return Mono.empty();
        }

        List<SipDomain> deletedDomains
            = domains.stream().filter(domain -> domainName.equals(domain.getFqdn())).collect(Collectors.toList());

        if (!deletedDomains.isEmpty()) {
            Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain> domainsUpdate
                = new HashMap<>();
            domainsUpdate.put(domainName, null);
            return setSipConfiguration(new SipConfiguration().setDomains(domainsUpdate)).then();
        }
        return Mono.empty();
    }

    /**
     * Deletes SIP Domain.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.siprouting.asyncclient.deleteDomainWithResponse -->
     * <pre>
     * sipRoutingAsyncClient.deleteDomainWithResponse&#40;&quot;&lt;domain name&gt;&quot;&#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.println&#40;&quot;Response status &quot; + response.getStatusCode&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.communication.phonenumbers.siprouting.asyncclient.deleteDomainWithResponse -->
     *
     * @param domainName SIP Domain name.
     * @return Response object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDomainWithResponse(String domainName) {
        List<SipDomain> domains = getDomainsInternal().block();
        if (domains == null || domains.isEmpty()) {
            return Mono.just(new SimpleResponse<>(null, 200, null, null));
        }

        List<SipDomain> deletedDomains
            = domains.stream().filter(domain -> domainName.equals(domain.getFqdn())).collect(Collectors.toList());

        if (!deletedDomains.isEmpty()) {
            Map<String, com.azure.communication.phonenumbers.siprouting.implementation.models.SipDomain> domainsUpdate
                = new HashMap<>();
            domainsUpdate.put(domainName, null);
            return setSipConfigurationWithResponse(new SipConfiguration().setDomains(domainsUpdate))
                .map(result -> new SimpleResponse<>(result, null));
        }
        return Mono.just(new SimpleResponse<>(null, 200, null, null));
    }

    private Mono<SipConfiguration> getSipConfiguration() {
        return client.getSipRoutings()
            .getAsync(ExpandEnum.TRUNKS_HEALTH)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException);
    }

    private Mono<Response<SipConfiguration>> getSipConfigurationWithResponse() {
        return client.getSipRoutings()
            .getWithResponseAsync(ExpandEnum.TRUNKS_HEALTH)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException);
    }

    private Mono<SipConfiguration> setSipConfiguration(SipConfiguration update) {
        return client.getSipRoutings()
            .updateAsync(update)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException);
    }

    private Mono<Response<SipConfiguration>> setSipConfigurationWithResponse(SipConfiguration update) {
        return client.getSipRoutings()
            .updateWithResponseAsync(update)
            .onErrorMap(CommunicationErrorResponseException.class, this::translateException);
    }

    private HttpResponseException translateException(CommunicationErrorResponseException exception) {
        return new HttpResponseException(exception.getMessage(), exception.getResponse());
    }

    private Mono<List<SipTrunk>> getTrunksInternal() {
        return getSipConfiguration().map(config -> convertFromApi(config.getTrunks()));
    }

    private Mono<List<SipDomain>> getDomainsInternal() {
        return getSipConfiguration().map(config -> convertFromApi(config.getDomains()));
    }
}
