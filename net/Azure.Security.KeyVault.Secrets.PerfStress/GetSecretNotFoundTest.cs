using Azure.Security.KeyVault.Secrets.PerfStress.Core;
using Azure.Test.PerfStress;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Security.KeyVault.Secrets.PerfStress
{
    public class GetSecretNotFoundTest : SecretTest<PerfStressOptions>
    {
        private const string _secretName = "GetSecretNotFoundName";

        public GetSecretNotFoundTest(PerfStressOptions options) : base(options)
        {
        }

        public override void Run(CancellationToken cancellationToken)
        {
            try
            {
                var _ = SecretClient.GetSecret(_secretName).Value.Value;
                throw new InvalidOperationException("Expected RequestFailedException");
            }
            catch (RequestFailedException ex) when (ex.Status == 404)
            {
            }
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
            try
            {
                var _ = (await SecretClient.GetSecretAsync(_secretName)).Value.Value;
                throw new InvalidOperationException("Expected RequestFailedException");
            }
            catch (RequestFailedException ex) when (ex.Status == 404)
            {
            }
        }
    }
}
