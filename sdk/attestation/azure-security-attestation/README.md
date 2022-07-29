# Azure Attestation client library for Java

Microsoft Azure Attestation (preview) is a unified solution for remotely verifying the trustworthiness of a platform and integrity of the binaries running inside it. The service supports attestation of the platforms backed by Trusted Platform Modules (TPMs) alongside the ability to attest to the state of Trusted Execution Environments (TEEs) such as Intel® Software Guard Extensions (SGX) enclaves and Virtualization-based Security (VBS) enclaves.

Attestation is a process for demonstrating that software binaries were properly instantiated on a trusted platform. Remote relying parties can then gain confidence that only such intended software is running on trusted hardware. Azure Attestation is a unified customer-facing service and framework for attestation.

Azure Attestation enables cutting-edge security paradigms such as Azure Confidential computing and Intelligent Edge protection. Customers have been requesting the ability to independently verify the location of a machine, the posture of a virtual machine (VM) on that machine, and the environment within which enclaves are running on that VM. Azure Attestation will empower these and many additional customer requests.

Azure Attestation receives evidence from compute entities, turns them into a set of claims, validates them against configurable policies, and produces cryptographic proofs for claims-based applications (for example, relying parties and auditing authorities).

> NOTE: This is a preliminary SDK for the Microsoft Azure Attestation service. It provides all the essential functionality to access the Azure Attestation service, but requires a significant amount of infrastructure to work correctly.

## Getting started

### Include the package

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag as shown below.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-attestation</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-security-attestation;current})

```xml
<!-- Install the Azure Attestation SDK -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-attestation</artifactId>
    <version>1.1.4</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An existing [Azure Attestation instance][azure_attestation]. If you need to create an attestation instance, you can use the [Azure Cloud Shell][azure_cloud_shell] to create one with this Azure CLI command. Replace `<your-resource-group-name>` and `<your-instance-name>` with your own, unique names:

```bash
az attestation create --resource-group <your-resource-group-name> --name <your-instance-name>
```

### Authenticate the client

In order to interact with the Azure Attestation service, your client must present an Azure Active Directory bearer token to the service.

The simplest way of providing a bearer token is to use the  `DefaultAzureCredential` authentication method by providing client secret credentials is being used in this getting started section, but you can find more ways to authenticate with [azure-identity][azure_identity].

## Key concepts

The Microsoft Azure Attestation service runs in two separate modes: "Isolated" and "AAD". When the service is running in "Isolated" mode, the customer needs to
provide additional information beyond their authentication credentials to verify that they are authorized to modify the state of an attestation instance.

There are four major client types provided in this preview SDK:

- [SGX and TPM enclave attestation.](#attestation)
- [MAA Attestation Token signing certificate discovery and validation.](#attestation-token-signing-certificate-discovery-and-validation)  
- [Attestation Policy management.](#policy-management)
- [Attestation policy management certificate management](#policy-management-certificate-management) (yes, policy management management).

Each attestation instance operates in one of two separate modes of operation:

- AAD mode.
- Isolated mode

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

### Policy Management certificate management

When an attestation instance is running in "Isolated" mode, the customer who created the instance will have provided
a policy management certificate at the time the instance is created. All policy modification operations require that the customer sign
the policy data with one of the existing policy management certificates. The Policy Management Certificate Management APIs enable
clients to add, remove or enumerate the policy management certificates.

## Examples

- [Instantiate a synchronous attestation client](#instantiate-a-synchronous-attestation-client)
- [Retrieve token validation certificates](#retrieve-token-validation-certificates)
- [Attest an SGX enclave](#attest-an-sgx-enclave)
- [Instantiate a synchronous administrative client](#instantiate-a-synchronous-administrative-client)
- [Get attestation policy](#get-attestation-policy)
- [Set unsigned attestation policy](#set-unsigned-attestation-policy)
- [Set signed attestation policy](#set-signed-attestation-policy)
- [List policy management certificates](#list-policy-management-certificates)
- [Add policy management certificate](#add-policy-management-certificate)
- [Remove attestation signing certificate](#remove-attestation-signing-certificate)

### Instantiate a synchronous attestation client

The `AttestationClientBuilder` class is used to create instances of the attestation client:

```java readme-sample-create-synchronous-client
AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
// Note that the "attest" calls do not require authentication.
AttestationClient client = attestationBuilder
    .endpoint(endpoint)
    .buildClient();
```

### Retrieve token validation certificates

Use `listAttestationSigners` to retrieve the set of certificates, which can be used to validate the token returned from the attestation service.
Normally, this information is not required as the attestation SDK will perform the validation as a part of the interaction with the
attestation service, however the APIs are provided for completeness and to facilitate customer's independently validating
attestation results.

```java readme-sample-getSigningCertificates
AttestationSignerCollection certs = client.listAttestationSigners();

certs.getAttestationSigners().forEach(cert -> {
    System.out.println("Found certificate.");
    if (cert.getKeyId() != null) {
        System.out.println("    Certificate Key ID: " + cert.getKeyId());
    } else {
        System.out.println("    Signer does not have a Key ID");
    }
    cert.getCertificates().forEach(chainElement -> {
        System.out.println("        Cert Subject: " + chainElement.getSubjectDN().getName());
        System.out.println("        Cert Issuer: " + chainElement.getIssuerDN().getName());
    });
});
```

### Attest an SGX Enclave

Use the `attestSgxEnclave` method to attest an SGX enclave.

```java readme-sample-attest-sgx-enclave
BinaryData decodedRuntimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
BinaryData sgxQuote = BinaryData.fromBytes(SampleCollateral.getSgxEnclaveQuote());

// Attest evidence from an OpenEnclave enclave specifying runtime data which should be
// interpreted as binary data.
AttestationResult result = client.attestSgxEnclave(new AttestationOptions(sgxQuote)
    .setRunTimeData(
        new AttestationData(decodedRuntimeData, AttestationDataInterpretation.BINARY)));

String issuer = result.getIssuer();

System.out.println("Attest Sgx Enclave completed. Issuer: " + issuer);
System.out.printf("Runtime Data Length: %d\n", result.getEnclaveHeldData().getLength());
```

### Instantiate a synchronous administrative client

All administrative clients are authenticated.

```java readme-sample-create-admin-client
AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
// Note that the "policy" calls require authentication.
AttestationAdministrationClient client = attestationBuilder
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### Get attestation policy

Use the `getAttestationPolicy` API to retrieve the current attestation policy for a given TEE.

```java readme-sample-getCurrentPolicy
String currentPolicy = client.getAttestationPolicy(AttestationType.OPEN_ENCLAVE);
System.out.printf("Current policy for OpenEnclave is: %s\n", currentPolicy);
```

### Set unsigned attestation policy

When an attestation instance is in AAD mode, the caller can use a convenience method to set an unsigned attestation
policy on the instance.

```java readme-sample-set-unsigned-policy
// Set the listed policy on an attestation instance. Please note that this particular policy will deny all
// attestation requests and should not be used in production.
PolicyResult policyResult = client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE,
    "version=1.0; authorizationrules{=> deny();}; issuancerules{};");
System.out.printf("Policy set for OpenEnclave result: %s\n", policyResult.getPolicyResolution());
```

### Set signed attestation policy

For isolated mode attestation instances, the set or reset policy request must be signed using the key that is associated
with the attestation signing certificates configured on the attestation instance.

```java readme-sample-set-signed-policy
// Set the listed policy on an attestation instance using a signed policy token.
PolicyResult policyResult = client.setAttestationPolicy(AttestationType.SGX_ENCLAVE,
    new AttestationPolicySetOptions()
        .setAttestationPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
            .setAttestationSigner(new AttestationSigningKey(certificate, privateKey)));
System.out.printf("Policy set for Sgx result: %s\n", policyResult.getPolicyResolution());
```

### List policy management certificates

When an attestation instance is in `Isolated` mode, the policy APIs need additional proof of authorization. This proof is
provided via the `AttestationSigningKey` parameter passed into the set and reset policy APIs.

Each `Isolated` mode instance has a set of certificates, which determine whether a caller has the authority to set an
attestation policy. When an attestation policy is set, the client presents a signed "token" to the service, which is signed
by the key in the `AttestationSigningKey`. The signed token, including the certificate in the `AttestationSigningKey` is
sent to the attestation service, which verifies that the token was signed with the private key corresponding to the
public key in the token. The set or reset policy operation will only succeed if the certificate in the token is one of
the policy management tokens. This interaction ensures that the client is in possession of the private key associated with
one of the policy management certificates and is thus authorized to perform the operation.

```java readme-sample-listPolicyCertificates
AttestationSignerCollection signers = client.listPolicyManagementCertificates();
System.out.printf("Instance %s contains %d signers.\n", endpoint, signers.getAttestationSigners().size());
for (AttestationSigner signer : signers.getAttestationSigners()) {
    System.out.printf("Certificate Subject: %s", signer.getCertificates().get(0).getSubjectDN().toString());
}
```

### Add policy management certificate

Adds a new certificate to the set of policy management certificates. The request to add the policy management certificate
must be signed with the private key associated with one of the existing policy management certificates (this ensures that
the caller is authorized to update the set of policy certificates).

Note: Adding the same certificate twice is not considered an error - if the certificate is already present, the addition is
ignored (this possibly surprising behavior is there because retries could cause the addition to be executed multiple times)

```java readme-sample-addPolicyManagementCertificate
System.out.printf("Adding new certificate %s\n", certificateToAdd.getSubjectDN().toString());
PolicyCertificatesModificationResult modificationResult = client.addPolicyManagementCertificate(
    new PolicyManagementCertificateOptions(certificateToAdd,
        new AttestationSigningKey(isolatedCertificate, isolatedKey)));
System.out.printf("Updated policy certificate, certificate add result: %s\n",
    modificationResult.getCertificateResolution());
System.out.printf("Added certificate thumbprint: %s\n", modificationResult.getCertificateThumbprint());
```

### Remove attestation signing certificate

Removes a certificate from the set of policy management certificates. The request to remove the policy management certificate
must be signed with the private key associated with one of the existing policy management certificates (this ensures that
the caller is authorized to update the set of policy certificates).

Note: Removing a non-existent certificate is not considered an error - if the certificate is not present, the removal is
ignored (this possibly surprising behavior is there because retries could cause the removal to be executed multiple times)

```java readme-sample-removePolicyManagementCertificate
System.out.printf("Removing existing certificate %s\n", certificateToRemove.getSubjectDN().toString());
PolicyCertificatesModificationResult modificationResult = client.deletePolicyManagementCertificate(
    new PolicyManagementCertificateOptions(certificateToRemove,
        new AttestationSigningKey(isolatedCertificate, isolatedKey)));
System.out.printf("Updated policy certificate, certificate remove result: %s\n",
    modificationResult.getCertificateResolution());
System.out.printf("Removed certificate thumbprint: %s\n", modificationResult.getCertificateThumbprint());
```

## Troubleshooting

Troubleshooting information for the MAA service can be found [here](https://docs.microsoft.com/azure/attestation/troubleshoot-guide)

## Next steps

For more information about the Microsoft Azure Attestation service, please see our [documentation page](https://docs.microsoft.com/azure/attestation/).

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information, see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[style-guide-msft]: https://docs.microsoft.com/style-guide/capitalization
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azure_attestation]: https://docs.microsoft.com/azure/attestation
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/
[azure_cli]: https://docs.microsoft.com/cli/azure
[rest_api]: https://docs.microsoft.com/rest/api/attestation/
[azure_create_application_in_portal]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_cloud_shell]: https://shell.azure.com/bash
[http_clients_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fattestation%2Fazure-security-attestation%2FREADME.png)
