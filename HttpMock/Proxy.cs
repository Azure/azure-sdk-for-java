using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Primitives;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;

namespace HttpMock
{
    public static class Proxy
    {
        private static readonly HttpClient _httpClient = new HttpClient();

        private static readonly string[] _excludedRequestHeaders = new string[] {
            // Special case since value may be modified based on Options.UpstreamHost
            "Host",
            
            // Only applies to request between client and proxy
            "Proxy-Connection",
        };

        // Headers which must be set on HttpContent instead of HttpRequestMessage
        private static readonly string[] _contentRequestHeaders = new string[] {
            "Content-Length",
            "Content-Type",
        };

        public static async Task<UpstreamResponse> SendUpstreamRequest(HttpRequest request)
        {
            var host = String.IsNullOrEmpty(Program.Options.UpstreamHost) ? request.Host.Host : Program.Options.UpstreamHost;

            var upstreamUriBuilder = new UriBuilder()
            {
                Scheme = request.Scheme,
                Host = host,
                Path = request.Path.Value,
                Query = request.QueryString.Value,
            };

            if (String.IsNullOrEmpty(Program.Options.UpstreamHost) && request.Host.Port.HasValue)
            {
                upstreamUriBuilder.Port = request.Host.Port.Value;
            }

            var upstreamUri = upstreamUriBuilder.Uri;

            using (var upstreamRequest = new HttpRequestMessage(new HttpMethod(request.Method), upstreamUri))
            {
                upstreamRequest.Headers.Add("Host", host);

                if (request.ContentLength > 0)
                {
                    upstreamRequest.Content = new StreamContent(request.Body);

                    foreach (var header in request.Headers.Where(h => _contentRequestHeaders.Contains(h.Key)))
                    {
                        upstreamRequest.Content.Headers.Add(header.Key, values: header.Value);
                    }
                }

                foreach (var header in request.Headers.Where(h => !_excludedRequestHeaders.Contains(h.Key) && !_contentRequestHeaders.Contains(h.Key)))
                {
                    if (!upstreamRequest.Headers.TryAddWithoutValidation(header.Key, values: header.Value)) {
                        throw new InvalidOperationException($"Could not add header {header.Key} with value {header.Value}");
                    }
                }

                using (var upstreamResponseMessage = await _httpClient.SendAsync(upstreamRequest))
                {
                    var headers = new List<KeyValuePair<string, StringValues>>();

                    foreach (var header in upstreamResponseMessage.Headers)
                    {
                        // Must skip "Transfer-Encoding" header, since if it's set manually Kestrel requires you to implement
                        // your own chunking.
                        if (string.Equals(header.Key, "Transfer-Encoding", StringComparison.OrdinalIgnoreCase))
                        {
                            continue;
                        }

                        headers.Add(new KeyValuePair<string, StringValues>(header.Key, header.Value.ToArray()));
                    }

                    foreach (var header in upstreamResponseMessage.Content.Headers)
                    {
                        headers.Add(new KeyValuePair<string, StringValues>(header.Key, header.Value.ToArray()));
                    }

                    return new UpstreamResponse()
                    {
                        StatusCode = (int)upstreamResponseMessage.StatusCode,
                        Headers = headers.ToArray(),
                        Content = await upstreamResponseMessage.Content.ReadAsByteArrayAsync()
                    };
                }
            }
        }

        public static Task SendDownstreamResponse(UpstreamResponse upstreamResponse, HttpResponse response)
        {
            response.StatusCode = upstreamResponse.StatusCode;

            foreach (var header in upstreamResponse.Headers)
            {
                response.Headers.Add(header.Key, header.Value);
            }

            return response.Body.WriteAsync(upstreamResponse.Content, 0, upstreamResponse.Content.Length);
        }
    }
}