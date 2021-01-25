# Azure Attestation client library for Java

Microsoft Azure Attestation (preview) is a unified solution for remotely verifying the trustworthiness of a platform and integrity of the binaries running inside it. The service supports attestation of the platforms backed by Trusted Platform Modules (TPMs) alongside the ability to attest to the state of Trusted Execution Environments (TEEs) such as Intel® Software Guard Extensions (SGX) enclaves and Virtualization-based Security (VBS) enclaves.

Attestation is a process for demonstrating that software binaries were properly instantiated on a trusted platform. Remote relying parties can then gain confidence that only such intended software is running on trusted hardware. Azure Attestation is a unified customer-facing service and framework for attestation.

Azure Attestation enables cutting-edge security paradigms such as Azure Confidential computing and Intelligent Edge protection. Customers have been requesting the ability to independently verify the location of a machine, the posture of a virtual machine (VM) on that machine, and the environment within which enclaves are running on that VM. Azure Attestation will empower these and many additional customer requests.

Azure Attestation receives evidence from compute entities, turns them into a set of claims, validates them against configurable policies, and produces cryptographic proofs for claims-based applications (for example, relying parties and auditing authorities).

Use the client library for attestation to perform the following operations:
- Attesting SGX or TPM protected enclaves hosted on Azure machines
- Modifying the attestation policies used in performing attestations.

> NOTE: This is a preliminary SDK for the Microsoft Azure Attestation service. It provides all the essential functionality to access the Azure Attestation service, but requires a significant amount of infrastructure to work correctly.


## Getting started
### Adding the package to your project
Maven dependency for the Azure Attestation  library. Add it to your project's POM file.

[//]: # ({x-version-update-start;com.azure:azure-security-attestation;current})
```xml
<!-- Install the Azure Attestation SDK -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-attestation</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An existing [Azure Attestation][azure_attestation]. If you need to create an attestation instance, you can use the [Azure Cloud Shell][azure_cloud_shell] to create one with this Azure CLI command. Replace `<your-resource-group-name>` and `<your-instance-name>` with your own, unique names:

    ```bash
    az attestation create --resource-group <your-resource-group-name> --name <your-key-vault-name>
    ```

### Authenticate the client
In order to interact with the Azure Attestation service, your client must present an Azure Active Directory bearer token to the service.

The simplest way of providing a bearer token is to use the  `DefaultAzureCredential` authentication method by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].


## Key concepts

The Microsoft Azure Attestation service runs in two separate modes: "Isolated" and "AAD". When the service is running in "Isolated" mode, the customer needs to 
provide additional information beyond their authentication credentials to verify that they are authorized to modify the state of an attestation instance.

There are four major components in the preview  

The *Key concepts* section should describe the functionality of the main classes. Point out the most important and useful classes in the package (with links to their reference pages) and explain how those classes work together. Feel free to use bulleted lists, tables, code blocks, or even diagrams for clarity.

## Examples

* [Attest an SGX enclave](#attest-sgx-enclave)
* [Get attestation policy](#get-attestation-policy)
* [Retrieve token validation certificates](#retrieve-token-certificates)

### Attest SGX Enclave

Use the `attestSgxEnclave` method to attest an SGX enclave.
<!-- embedme src\samples\com\azure\security\attestation\ReadmeSamples.java#L36-L44 -->
```java
AttestSgxEnclaveRequest request = new AttestSgxEnclaveRequest();
request.setQuote(decodedSgxQuote);
RuntimeData runtimeData = new RuntimeData();
runtimeData.setDataType(DataType.BINARY);
runtimeData.setData(decodedRuntimeData);
request.setRuntimeData(runtimeData);
AttestationResponse response = client.attestSgxEnclave(request);

JWTClaimsSet claims = verifyAttestationToken(httpClient, clientUri, response.getToken()).block();
```

### Get attestation policy

The `attestationPolicyClient.get` method retrieves the attestation policy from the service.
Attestation Policies are instanced on a per-attestation type basis, the `AttestationType` parameter defines the type to retrieve. 
The response to an attestation policy get is a JSON Web Token signed by the attestation service.
The token contains an `x-ms-policy` claim, which in turn contains the secured (or unsecured) attestation policy document which was 
set by the customer. 

The attestation policy document is a JSON Web Signature object, with a single field named `AttestationPolicy`, whose value is the actual policy document encoded in Base64Url.

<!-- embedme src\samples\com\azure\security\attestation\ReadmeSamples.java#L59-L84 -->
```java
cyResponse policyResponse = client.get(AttestationType.SGX_ENCLAVE);
fyAttestationToken(httpClient, clientUri, policyResponse.getToken())
.subscribe(claims -> {
    if (claims != null) {

        String policyDocument = claims.getClaims().get("x-ms-policy").toString();

        JOSEObject policyJose = null;
        try {
            policyJose = JOSEObject.parse(policyDocument);
        } catch (ParseException e) {
            logger.logExceptionAsError(new RuntimeException(e.toString()));
        }
        assert policyJose != null;
        Map<String, Object> jsonObject = policyJose.getPayload().toJSONObject();
        if (jsonObject != null) {
            assertTrue(jsonObject.containsKey("AttestationPolicy"));
            String base64urlPolicy = jsonObject.get("AttestationPolicy").toString();

            byte[] attestationPolicyUtf8 = Base64.getUrlDecoder().decode(base64urlPolicy);
            String attestationPolicy;
            attestationPolicy = new String(attestationPolicyUtf8, StandardCharsets.UTF_8);
            // Inspect the retrieved policy.
        }
    }
});
```

### Retrieve Token Certificates

Use `SigningCertificatesClient.get` to retrieve the certificates which can be used to validate the token returned from the attestation service.

<!-- embedme src\samples\com\azure\security\attestation\ReadmeSamples.java#L89-L91 -->
```java
AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

JsonWebKeySet certs = attestationBuilder.buildSigningCertificatesClient().get();
```

## Troubleshooting

Describe common errors and exceptions, how to "unpack" them if necessary, and include guidance for graceful handling and recovery.

Provide information to help developers avoid throttling or other service-enforced errors they might encounter. For example, provide guidance and examples for using retry or connection policies in the API.

If the package or a related package supports it, include tips for logging or enabling instrumentation to help them debug their code.

## Next steps

## Contributing

This project welcomes contributions and suggestions.  Most contributions require
you to agree to a Contributor License Agreement (CLA) declaring that you have
the right to, and actually do, grant us the rights to use your contribution. For
details, visit https://cla.microsoft.com.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct].
For more information see the [Code of Conduct FAQ][coc_faq]
or contact opencode@microsoft.com with any
additional questions or comments.


* Provide a link to additional code examples, ideally to those sitting alongside the README in the package's `/samples` directory.
* If appropriate, point users to other packages that might be useful.
* If you think there's a good chance that developers might stumble across your package in error (because they're searching for specific functionality and mistakenly think the package provides that functionality), point them to the packages they might be looking for.

<!-- LINKS -->
[style-guide-msft]: https://docs.microsoft.com/style-guide/capitalization

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fattestation%2Fazure-security-attestation%2FREADME.png)
