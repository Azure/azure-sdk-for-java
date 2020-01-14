using Azure.Security.KeyVault.Secrets.PerfStress.Core;
using Azure.Test.PerfStress;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Security.KeyVault.Secrets.PerfStress
{
    public class GetSecretTest : SecretTest<PerfStressOptions>
    {
        private const string _secretName = "GetSecretName";
        private const string _secretValue = "GetSecretValue";

        public GetSecretTest(PerfStressOptions options) : base(options)
        {
        }

        public override async Task GlobalSetupAsync()
        {
            await base.GlobalSetupAsync();

            await SecretClient.SetSecretAsync(_secretName, _secretValue);
        }

        public override async Task GlobalCleanupAsync()
        {
            await SecretClient.StartDeleteSecretAsync(_secretName);

            await base.GlobalCleanupAsync();
        }

        public override void Run(CancellationToken cancellationToken)
        {
            var _ = SecretClient.GetSecret(_secretName).Value.Value;
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
            var _ = (await SecretClient.GetSecretAsync(_secretName)).Value.Value;
        }
    }
}
