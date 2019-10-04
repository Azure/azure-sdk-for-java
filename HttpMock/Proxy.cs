using Microsoft.AspNetCore.Http;
using System;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;

namespace HttpMock
{
    public static class Proxy
    {
        private const string _upstreamHost = "mikeharderperf.blob.core.windows.net";

        private static readonly HttpClient _httpClient = new HttpClient();

        public static Task<HttpResponseMessage> SendUpstreamRequest(HttpRequest request)
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

#if DEBUG
                Log.LogUpstreamRequest(upstreamRequest);
#endif

                return _httpClient.SendAsync(upstreamRequest);
            }
        }

        public static Task SendDownstreamResponse(HttpResponseMessage upstreamResponse, HttpResponse response)
        {
            //response.StatusCode = (int)upstreamResponse.StatusCode;

            //foreach (var header in upstreamResponse.Headers)
            //{
            //    // Must skip "Transfer-Encoding" header, since if it's set manually Kestrel requires you to implement
            //    // your own chunking.
            //    if (string.Equals(header.Key, "Transfer-Encoding", StringComparison.OrdinalIgnoreCase))
            //    {
            //        continue;
            //    }

            //    // PERF: Store StringValues in cache
            //    response.Headers.Add(header.Key, header.Value.ToArray());
            //}

            return upstreamResponse.Content.CopyToAsync(response.Body);
        }
    }
}