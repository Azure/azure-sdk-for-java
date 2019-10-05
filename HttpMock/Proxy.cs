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

        public static async Task<UpstreamResponse> SendUpstreamRequest(HttpRequest request)
        {
            var upstreamUriBuilder = new UriBuilder()
            {
                Scheme = request.Scheme,
                Host = request.Host.Host,
                Path = request.Path.Value,
                Query = request.QueryString.Value,
            };

            if (request.Host.Port.HasValue)
            {
                upstreamUriBuilder.Port = request.Host.Port.Value;
            }

            var upstreamUri = upstreamUriBuilder.Uri;

            using (var upstreamRequest = new HttpRequestMessage(new HttpMethod(request.Method), upstreamUri))
            {
                foreach (var header in request.Headers.Where(h => h.Key != "Proxy-Connection"))
                {
                    upstreamRequest.Headers.Add(header.Key, values: header.Value);
                }

                if (request.ContentLength > 0)
                {
                    upstreamRequest.Content = new StreamContent(request.Body);
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