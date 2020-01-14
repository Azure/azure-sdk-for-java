using Azure.Core;
using Azure.Core.Pipeline;
using System.Net.Http;
using System.Threading.Tasks;

namespace Azure.Test.PerfStress
{
    public static class PerfStressTransport
    {
        public static HttpPipelineTransport Create(PerfStressOptions options)
        {
            HttpClient httpClient;
            if (options.Insecure)
            {
                var httpClientHandler = new HttpClientHandler();
                httpClientHandler.ServerCertificateCustomValidationCallback = (message, cert, chain, errors) => true;
                httpClient = new HttpClient(httpClientHandler);
            }
            else
            {
                httpClient = new HttpClient();
            }

            var httpClientTransport = new HttpClientTransport(httpClient);

            if (!string.IsNullOrEmpty(options.Host))
            {
                return new ChangeUriTransport(httpClientTransport, options.Host, options.Port);
            }
            else
            {
                return httpClientTransport;
            }
        }

        private class ChangeUriTransport : HttpPipelineTransport
        {
            private readonly HttpPipelineTransport _transport;
            private readonly string _host;
            private readonly int? _port;

            public ChangeUriTransport(HttpPipelineTransport transport, string host, int? port)
            {
                _transport = transport;
                _host = host;
                _port = port;
            }

            public override Request CreateRequest()
            {
                return _transport.CreateRequest();
            }

            public override void Process(HttpMessage message)
            {
                ChangeUri(message);
                _transport.Process(message);
            }

            public override ValueTask ProcessAsync(HttpMessage message)
            {
                ChangeUri(message);
                return _transport.ProcessAsync(message);
            }

            private void ChangeUri(HttpMessage message)
            {
                message.Request.Headers.Add("Host", message.Request.Uri.Host);

                message.Request.Uri.Host = _host;
                if (_port.HasValue)
                {
                    message.Request.Uri.Port = _port.Value;
                }
            }
        }
    }
}
