# Azure Attestation client library for Java

Microsoft Azure Attestation (preview) is a unified solution for remotely verifying the trustworthiness of a platform and integrity of the binaries running inside it. The service supports attestation of the platforms backed by Trusted Platform Modules (TPMs) alongside the ability to attest to the state of Trusted Execution Environments (TEEs) such as Intel® Software Guard Extensions (SGX) enclaves and Virtualization-based Security (VBS) enclaves.

Attestation is a process for demonstrating that software binaries were properly instantiated on a trusted platform. Remote relying parties can then gain confidence that only such intended software is running on trusted hardware. Azure Attestation is a unified customer-facing service and framework for attestation.

Azure Attestation enables cutting-edge security paradigms such as Azure Confidential computing and Intelligent Edge protection. Customers have been requesting the ability to independently verify the location of a machine, the posture of a virtual machine (VM) on that machine, and the environment within which enclaves are running on that VM. Azure Attestation will empower these and many additional customer requests.

Azure Attestation receives evidence from compute entities, turns them into a set of claims, validates them against configurable policies, and produces cryptographic proofs for claims-based applications (for example, relying parties and auditing authorities).

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

There are four major client types provided in this preview SDK: 
- [SGX and TPM enclave attestation.](#attestation)
- [MAA Attestation Token signing certificate discovery and validation.](#attestation-token-signing-certificate-discovery-and-validation)  
- [Attestation Policy management.](#policy-management)
- [Attestation policy management certificate management](#policy-management-certificate-management) (yes, policy management management).

Each attestation instance operates in one of two separate modes of operation:
* AAD mode.
* Isolated mode

In "AAD" mode, access to the service is controlled solely by Azure Role Based Access Control.  In "Isolated" mode, 
the client is expected to provide additional evidence to prove that the client is authorized
to modify the service.

Finally, each region in which the Microsoft Azure Attestation service is available supports a "shared" instance, which
can be used to attest SGX enclaves which only need verification against the azure baseline (there are no policies applied to the shared instance). TPM attestation is not available in the shared instance.
While the shared instance requires AAD authentication, it does not have any RBAC policies - any customer with a valid AAD bearer token can attest using the shared instance. 


### Attestation
SGX or TPM attestation is the process of validating evidence collected from 
a trusted execution environment to ensure that it meets both the Azure baseline for that environment and customer defined policies applied to that environment.

### Attestation token signing certificate discovery and validation
Most responses from the MAA service are expressed in the form of a JSON Web Token. This token will be signed by a signing certificate
issued by the MAA service for the specified instance. If the MAA service instance is running in a region where the service runs in an SGX enclave, then
the certificate issued by the server can be verified using the [oe_verify_attestation_certificate() API](https://openenclave.github.io/openenclave/api/enclave_8h_a3b75c5638360adca181a0d945b45ad86.html). 

### Policy Management
Each attestation service instance has a policy applied to it which defines additional criteria which the customer has defined.

For more information on attestation policies, see [Attestation Policy](https://docs.microsoft.com/azure/attestation/author-sign-policy)

### Policy Management certificate management.
When an attestation instance is running in "Isolated" mode, the customer who created the instance will have provided
a policy management certificate at the time the instance is created. All policy modification operations require that the customer sign
the policy data with one of the existing policy management certificates. The Policy Management Certificate Management APIs enable 
clients to "roll" the policy management certificates.


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

JWTClaimsSet claims = null;
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

sponse policyResponse = client.get(AttestationType.SGX_ENCLAVE);
testationToken(httpClient, clientUri, policyResponse.getToken())
scribe(claims -> {
if (claims != null) {

    String policyDocument = claims.getClaims().get("x-ms-policy").toString();

    JOSEObject policyJose = null;
    try {
        policyJose = JOSEObject.parse(policyDocument);
    } catch (ParseException e) {
        throw logger.logExceptionAsError(new RuntimeException(e.toString()));
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
```

### Retrieve Token Certificates

Use `SigningCertificatesClient.get` to retrieve the certificates which can be used to validate the token returned from the attestation service.

<!-- embedme src\samples\com\azure\security\attestation\ReadmeSamples.java#L89-L92 -->
```java

AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

JsonWebKeySet certs = attestationBuilder.buildSigningCertificatesClient().get();
```

## Troubleshooting

Troubleshooting information for the MAA service can be found [here](https://docs.microsoft.com/azure/attestation/troubleshoot-guide)
## Next steps
For more information about the Microsoft Azure Attestation service, please see our [documentation page](https://docs.microsoft.com/azure/attestation/). 

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.


<!-- LINKS -->
[style-guide-msft]: https://docs.microsoft.com/style-guide/capitalization

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fattestation%2Fazure-security-attestation%2FREADME.png)
