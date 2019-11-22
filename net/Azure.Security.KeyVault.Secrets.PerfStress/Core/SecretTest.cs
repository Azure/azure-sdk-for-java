using Azure.Identity;
using Azure.Test.PerfStress;
using System;
using System.Threading.Tasks;

namespace Azure.Security.KeyVault.Secrets.PerfStress.Core
{
    public abstract class SecretTest<TOptions> : PerfStressTest<TOptions> where TOptions: PerfStressOptions
    {
        protected SecretClient SecretClient { get; private set; }

        public SecretTest(TOptions options) : base(options)
        {
            var keyvaultUri = Environment.GetEnvironmentVariable("KEYVAULT_URI");

            if (string.IsNullOrEmpty(keyvaultUri))
            {
                throw new InvalidOperationException("Undefined environment variable KEYVAULT_URI");
            }

            var secretClientOptions = new SecretClientOptions()
            {
                Transport = PerfStressTransport.Create(options)
            };

            SecretClient = new SecretClient(new Uri(keyvaultUri), new DefaultAzureCredential(), secretClientOptions);
        }

        public override async Task GlobalSetupAsync()
        {
            await base.GlobalSetupAsync();

            try
            {
                // Perform one request in GlobalSetup to initialize the TokenCredential, so future requests can use the cached token
                await SecretClient.GetSecretAsync("DoesNotExist");
            }
            catch
            {
            }
        }
    }
}
