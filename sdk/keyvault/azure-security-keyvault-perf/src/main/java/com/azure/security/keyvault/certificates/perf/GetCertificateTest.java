package com.azure.security.keyvault.certificates.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.perf.core.CertificatesTest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class GetCertificateTest extends CertificatesTest<PerfStressOptions> {
    private final String certificateName;

    public GetCertificateTest(PerfStressOptions options) {
        super(options);

        certificateName = "getCertificatePerfTest-" + UUID.randomUUID();
    }

    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(certificateAsyncClient.beginCreateCertificate(certificateName, CertificatePolicy.getDefault()).then())
            .then();
    }

    @Override
    public void run() {
        certificateClient.getCertificate(certificateName);
    }

    @Override
    public Mono<Void> runAsync() {
        return certificateAsyncClient.getCertificate(certificateName).then();
    }
}
