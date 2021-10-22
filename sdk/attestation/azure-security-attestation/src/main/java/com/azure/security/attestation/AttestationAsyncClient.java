// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.attestation.implementation.AttestationClientImpl;
import com.azure.security.attestation.implementation.AttestationsImpl;
import com.azure.security.attestation.implementation.MetadataConfigurationsImpl;
import com.azure.security.attestation.implementation.SigningCertificatesImpl;
import com.azure.security.attestation.implementation.models.AttestationOpenIdMetadataImpl;
import com.azure.security.attestation.implementation.models.AttestationOptionsImpl;
import com.azure.security.attestation.implementation.models.AttestationResultImpl;
import com.azure.security.attestation.implementation.models.AttestationSignerImpl;
import com.azure.security.attestation.implementation.models.AttestationTokenImpl;
import com.azure.security.attestation.models.AttestationOpenIdMetadata;
import com.azure.security.attestation.models.AttestationOptions;
import com.azure.security.attestation.models.AttestationResult;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationToken;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The AttestationAsyncClient implements the functionality required by the "Attest" family of APIs.
 * <p>
 * An enclave (or Trusted Execution Environment) is a chunk of code that is isolated from the host
 * (think: "encrypted VM" or "encrypted container"). But there's one key attribute of the enclave:
 * It is encrypted.That means that
 * if data is sent from the enclave, there is no way of knowing that the data came from the enclave.
 * </p>
 * <p>
 *     And even worse, there is no way of securely communicating with the enclave (since the enclave is
 *     fully isolated from the host, all information passed into the enclave has to go through its host
 *     first).
 * </p>
 * <p>To solve the communication problem, the Attest API can be used to facilitate what is
 * known as the "Secure Key Release" (SKR) protocol.</p>
 * <p>
 * There are 4 parties involved in an attestation operation:
 * </p>
 * <ul>
 *     <li>The host (which hosts the enclave)</li>
 *     <li>The enclave (which is the enclave :)  encrypted, nobody can see what goes on inside it),</li>
 *     <li>The "verifier" which verifies the evidence from the enclave (this is the attestation service) and
 *     generates a token which can be received by a relying party, and </li>
 *     <li>The "relying party" which will interpret the token from the service. For the Secure Key Release Protocol,
 *     this is the entity which wishes to communicate with the enclave.</li>
 * </ul>
 * <p>
 *   It's possible that all these parties are on the same computer, it's possible theyre on multiple computers.<br>
 *   It's possible that the host is also the relying party. It's possible that the relying party is a component
 *   like Azure Managed HSM.
 * </p>
 *
 * <p>
 * There are three primary pieces of data received by the service for the Attest family of APIs. All of them
 * are arrays of bytes, and all of them originate from code running in the enclave (thus they need to be
 * treated as opaque arrays of bytes by the SDK):
 * </p>
 *
 * <ol>
 * <li>Evidence. For Intel SGX enclaves, this has two forms, either an SGX 'Quote' or an
 * OpenEnclave 'Report'. It is required for attestation operations.</li>
 * <li>InitTimeData  This is data which is specified at Initialization Time. It is optional
 * (and not currently supported on all enclave types in Azure)</li>
 * <li>RunTimeData  this is data which is specified at the time the quote is generated (at runtime).
 * It is optional, but required for the Secure Key Release protocol.</li>
 * </ol>
 * <p>
 * The Evidence is cryptographically signed by a known authority (for Intel SGX Quotes or OpenEnclave reports, this
 * is a key owned by Intel which represents that the SGX enclave is valid and can be trusted).<br>
 * The core idea for all attestation operations is to take advantage of a region within the Evidence which
 * is controlled by enclave. For SGX Enclaves, this is the 64 bytes of "user data" contained within SGX quote.
 * </p>
 * <p>
 *     For the Secure Key Release protocol, code inside the enclave generates an asymmetric key and serializes the public
 *     key into a byte buffer. It then calculates the SHA256 hash of the serialized key and creates a quote
 *     containing that SHA256 hash. We now have a cryptographically validated indication that the contents
 *     of the byte buffer was known inside the enclave.
 *</p>
 * <p>
 * The enclave then hands the byte buffer and the quote to its host. The host sends the quote and byte
 * buffer as the "RunTime Data" to the via the {@link AttestationAsyncClient#attestSgxEnclave(BinaryData)}  or
 * {@link AttestationAsyncClient#attestOpenEnclave} API. Assuming the byte buffer and quote are valid,
 * and the quote contains the hash of the byte buffer, the attestation service responds with an {@link AttestationToken}
 * signed by the attestation service, whose body is an {@link AttestationResult}.
 * </p>
 * <p>
 *     The token generated also includes the contents of the InitTimeData and/or RunTimeData if it was
 *     provided in the Attest API call.
 * </p>
 * <p>
 * The host then sends the token to the relying party.  The relying party verifies the token
 * and verifies the claims within the token indicate that the enclave is the correct enclave.
 * It then takes the key from the token and uses it to encrypt the data to be sent to the
 * enclave and sends that back to the host, which passes it into the enclave.
 *
 *</p>
 * <p>
 *     That completes the secure key release protocol.
 * </p><br>
 * <p>When the Attestation Token is generated by the attestation service, as mentioned, it contains the
 * InitTime and RunTime data.</p>
 * <p>There are two possible representations for RunTime Data in the attestation token, depending on the requirements of the relying party:<br>
 * The first is as JSON formatted data. That can be convenient if the relying party expects to receive its
 * public key as a JSON Web Key <br>
 * The second is as a binary blob of data. That is needed if either the data sent by the enclave isn't
 * a JSON object - for instance, if the RunTime data contained an asymmetric key which is formatted as a
 * PEM encoded key, it should be interpreted as a binary blob</p>
 *
 * If you ask for the RunTime data to be included in the token as binary, then it will be base64url
  * encoded in the "x-ms-maa-enclavehelddata" claim in the output token (the
 * {@link AttestationResult#getEnclaveHeldData()} property).
 * <br>
 * If you ask for the RunTime data to be included in the token as JSON, then it will be included in the
 * "x-ms-maa-runtimeClaims" claim in the output token (the {@link AttestationResult#getRuntimeClaims()} property).
 * <p>
 *     In addition to the Attest APIs, the {@link AttestationClient} object also contains helper APIs
 *     which can be used to retrieve the OpenId Metadata document and signing keys from the service.
 *
 * </p>
 * <p>
 *     The OpenId Metadata document contains properties which describe the attestation service.
 *     </p>
 * <p>
 *     The Attestation Signing Keys describe the keys which will be used to sign tokens generated by
 *     the attestation service. All tokens emitted by the attestation service will be signed by one
 *     of the certificates listed in the attestation signing keys.
 * </p>
 */
@ServiceClient(builder = AttestationClientBuilder.class, isAsync = true)
public final class AttestationAsyncClient {
    private final AttestationsImpl attestImpl;
    private final MetadataConfigurationsImpl metadataImpl;
    private final SigningCertificatesImpl signerImpl;
    private final ClientLogger logger;
    private final AttestationTokenValidationOptions tokenValidationOptions;
    private final AtomicReference<List<AttestationSigner>> cachedSigners;

    /**
     * Initializes an instance of Attestations client.
     *
     * @param clientImpl the service client implementation.
     */
    AttestationAsyncClient(AttestationClientImpl clientImpl, AttestationTokenValidationOptions tokenValidationOptions) {
        this.attestImpl = clientImpl.getAttestations();
        this.metadataImpl = clientImpl.getMetadataConfigurations();
        this.signerImpl = clientImpl.getSigningCertificates();
        this.tokenValidationOptions = tokenValidationOptions;
        this.logger = new ClientLogger(AttestationAsyncClient.class);
        this.cachedSigners = new AtomicReference<>(null);
    }

    /**
     * Retrieves metadata about the attestation signing keys in use by the attestation service.
     *
     * <p><strong>Retrieve the OpenID metadata for this async client.</strong></p>
     * {@codesnippet com.azure.security.attestation.AttestationAsyncClient.getOpenIdMetadataWithResponse}
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AttestationOpenIdMetadata>> getOpenIdMetadataWithResponse() {
        return withContext(context -> getOpenIdMetadataWithResponse(context));
    }

    Mono<Response<AttestationOpenIdMetadata>> getOpenIdMetadataWithResponse(Context context) {
        return this.metadataImpl.getWithResponseAsync(context)
            .map(generated -> Utilities.generateResponseFromModelType(generated, AttestationOpenIdMetadataImpl.fromGenerated(generated.getValue())));
    }

    /**
     * Retrieves metadata about the attestation signing keys in use by the attestation service.

     * <p><strong>Retrieve the OpenID metadata for this async client.</strong></p>
     * {@codesnippet com.azure.security.attestation.AttestationAsyncClient.getOpenIdMetadata}
     *
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return any object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AttestationOpenIdMetadata> getOpenIdMetadata() {
        // Forward the getOpenIdMetadata to the getOpenIdMetadataWithResponse API implementation.
        return this.getOpenIdMetadataWithResponse()
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the list of {@link AttestationSigner} objects associated with this attestation instance.
     * <p>
     *  An {@link AttestationSigner} represents an X.509 certificate chain and KeyId which can be used
     *  to validate an attestation token returned by the service.
     * </p>
     * <p><strong>Retrieve Attestation Signers for this async client.</strong></p>
     * {@codesnippet com.azure.security.attestation.AttestationAsyncClient.getAttestationSigners}
     *
     * @return Returns an array of {@link AttestationSigner} objects.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<AttestationSigner>> listAttestationSigners() {
        return this.listAttestationSignersWithResponse()
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the list of {@link AttestationSigner} objects associated with this attestation instance.
     *<p>
     * An {@link AttestationSigner} represents an X.509 certificate chain and KeyId which can be used
     * to validate an attestation token returned by the service.
     * </p>
     * <p><strong>Retrieve Attestation Signers for this async client.</strong></p>
     * {@codesnippet com.azure.security.attestation.AttestationAsyncClient.getAttestationSignersWithResponse}
     *
     * @return Returns an array of {@link AttestationSigner} objects.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<List<AttestationSigner>>> listAttestationSignersWithResponse() {
        return withContext(context -> listAttestationSignersWithResponse(context));
    }

    Mono<Response<List<AttestationSigner>>> listAttestationSignersWithResponse(Context context) {
        return  this.signerImpl.getWithResponseAsync(context)
            .map(response -> Utilities.generateResponseFromModelType(response, AttestationSignerImpl.attestationSignersFromJwks(response.getValue())));
    }


    /**
     * Return cached attestation signers, fetching from the internet if needed.
     * @return cached signers.
     */
    Mono<List<AttestationSigner>> getCachedAttestationSigners() {
        if (this.cachedSigners.get() != null) {
            return Mono.just(this.cachedSigners.get());
        } else {
            return this.signerImpl.getAsync()
                .map(AttestationSignerImpl::attestationSignersFromJwks)
                .map(signers -> {
                    this.cachedSigners.compareAndSet(null, signers);
                    return this.cachedSigners.get();
                });
        }
    }


    /**
     * Processes an OpenEnclave report, producing an artifact. The type of artifact produced is dependent upon
     * attestation policy.
     *
     * @param report - OpenEnclave report to attest.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of an attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AttestationResult>> attestOpenEnclaveWithResponse(BinaryData report) {
        return withContext(context -> this.attestOpenEnclaveWithResponse(report, context));
    }

    Mono<Response<AttestationResult>> attestOpenEnclaveWithResponse(BinaryData report, Context context) {
        return this.attestOpenEnclaveWithResponse(new AttestationOptions(report), context);
    }
    /**
     * Processes an OpenEnclave report, producing an artifact. The type of artifact produced is dependent upon
     * attestation policy.
     *
     * @param options Attestation options for attesting SGX enclaves.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of an attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AttestationResult>> attestOpenEnclaveWithResponse(AttestationOptions options) {
        return withContext(context -> attestOpenEnclaveWithResponse(options, context));
    }

    /**
     * Actually perform the OpenEnclave attestation.
     * @param options - Options for the attestation.
     * @param context - context for the operation.
     * @return The result of the attestation operation.
     */
    Mono<Response<AttestationResult>> attestOpenEnclaveWithResponse(AttestationOptions options, Context context) {
        AttestationOptionsImpl optionsImpl = new AttestationOptionsImpl(options);

        AttestationTokenValidationOptions validationOptions = options.getValidationOptions();
        if (validationOptions == null) {
            validationOptions = this.tokenValidationOptions;
        }

        AttestationTokenValidationOptions finalValidationOptions = validationOptions;
        return this.attestImpl.attestOpenEnclaveWithResponseAsync(optionsImpl.getInternalAttestOpenEnclaveRequest(), context)
            .map(response -> Utilities.generateResponseFromModelType(response, new AttestationTokenImpl(response.getValue().getToken())))
            .flatMap(response -> {
                if (finalValidationOptions.getValidateToken()) {
                    return getCachedAttestationSigners()
                        .map(signers -> {
                            response.getValue().validate(signers, finalValidationOptions);
                            return response;
                        });
                } else {
                    return Mono.just(response);
                }
            })
            .map(response -> {
                com.azure.security.attestation.implementation.models.AttestationResult generatedResult = response.getValue().getBody(com.azure.security.attestation.implementation.models.AttestationResult.class);
                return Utilities.generateAttestationResponseFromModelType(response, response.getValue(), AttestationResultImpl.fromGeneratedAttestationResult(generatedResult));
            });
    }


    /**
     * Processes an OpenEnclave report, producing an artifact. The type of artifact produced is dependent upon
     * attestation policy.
     *
     * @param report OpenEnclave generated report.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of an attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AttestationResult> attestOpenEnclave(BinaryData report) {
        return attestOpenEnclaveWithResponse(new AttestationOptions(report))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Processes an OpenEnclave report , producing an artifact. The type of artifact produced is dependent upon
     * attestation policy.
     *
     * @param options Attestation options for Intel SGX enclaves.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of an attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AttestationResult> attestOpenEnclave(AttestationOptions options) {
        return attestOpenEnclaveWithResponse(options)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Processes an SGX enclave quote, producing an artifact. The type of artifact produced is dependent upon
     * attestation policy.
     *
     * @param quote Attestation options for Intel SGX enclaves.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of an attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AttestationResult>> attestSgxEnclaveWithResponse(BinaryData quote) {
        return withContext(context -> this.attestSgxEnclaveWithResponse(quote, context));
    }

    /**
     * Processes an SGX enclave quote, producing an artifact. The type of artifact produced is dependent upon
     * attestation policy.
     *
     * @param quote Attestation options for Intel SGX enclaves.
     * @param context Context for the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of an attestation operation.
     */
    Mono<Response<AttestationResult>> attestSgxEnclaveWithResponse(BinaryData quote, Context context) {
        return attestSgxEnclaveWithResponse(new AttestationOptions(quote), context);
    }

    /**
     * Processes an SGX enclave quote, producing an artifact. The type of artifact produced is dependent upon
     * attestation policy.
     *
     * @param options Attestation options for Intel SGX enclaves.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of an attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AttestationResult>> attestSgxEnclaveWithResponse(AttestationOptions options) {
        return withContext(context -> attestSgxEnclaveWithResponse(options, context));
    }

    Mono<Response<AttestationResult>> attestSgxEnclaveWithResponse(AttestationOptions options, Context context) {
        // Ensure that the incoming request makes sense.
        AttestationOptionsImpl optionsImpl = new AttestationOptionsImpl(options);

        AttestationTokenValidationOptions validationOptions = options.getValidationOptions();
        if (validationOptions == null) {
            validationOptions = this.tokenValidationOptions;
        }

        AttestationTokenValidationOptions finalValidationOptions = validationOptions;
        return this.attestImpl.attestSgxEnclaveWithResponseAsync(optionsImpl.getInternalAttestSgxRequest(), context)
            .map(response -> Utilities.generateResponseFromModelType(response, new AttestationTokenImpl(response.getValue().getToken())))
            .flatMap(response -> {
                if (finalValidationOptions.getValidateToken()) {
                    return getCachedAttestationSigners()
                        .map(signers -> {
                            response.getValue().validate(signers, finalValidationOptions);
                            return response;
                        });
                } else {
                    return Mono.just(response);
                }
            })
            .map(response -> {
                com.azure.security.attestation.implementation.models.AttestationResult generatedResult = response.getValue().getBody(com.azure.security.attestation.implementation.models.AttestationResult.class);
                return Utilities.generateAttestationResponseFromModelType(response, response.getValue(), AttestationResultImpl.fromGeneratedAttestationResult(generatedResult));
            });
    }

    /**
     * Processes an SGX enclave quote, producing an artifact. The type of artifact produced is dependent upon
     * attestation policy.
     *
     * @param options Attestation options for Intel SGX enclaves.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of an attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AttestationResult> attestSgxEnclave(AttestationOptions options) {
        return attestSgxEnclaveWithResponse(options)
            .flatMap(FluxUtil::toMono);
    }
    /**
     * Processes an SGX enclave quote, producing an artifact. The type of artifact produced is dependent upon
     * attestation policy.
     *
     * @param quote SGX Quote to attest.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the result of an attestation operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AttestationResult> attestSgxEnclave(BinaryData quote) {
        return attestSgxEnclaveWithResponse(quote)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Processes attestation evidence from a VBS enclave, producing an attestation result. The attestation result
     * produced is dependent upon the attestation policy.
     *
     * @param request Attestation request for Trusted Platform Module (TPM) attestation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return attestation response for Trusted Platform Module (TPM) attestation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> attestTpmWithResponse(String request) {
        return withContext(context -> this.attestTpmWithResponse(request, context));
    }

    Mono<Response<String>> attestTpmWithResponse(String request, Context context) {
        Objects.requireNonNull(request);
        return this.attestImpl.attestTpmWithResponseAsync(new com.azure.security.attestation.implementation.models.TpmAttestationRequest().setData(request.getBytes(StandardCharsets.UTF_8)), context)
            .map(response -> Utilities.generateResponseFromModelType(response, new String(Objects.requireNonNull(response.getValue().getData()), StandardCharsets.UTF_8)));
    }

    /**
     * Processes attestation evidence from a VBS enclave, producing an attestation result. The attestation result
     * produced is dependent upon the attestation policy.
     *
     * @param request Attestation request for Trusted Platform Module (TPM) attestation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return attestation response for Trusted Platform Module (TPM) attestation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> attestTpm(String request) {
        return attestTpmWithResponse(request)
            .flatMap(FluxUtil::toMono);
    }
}
