using Microsoft.AspNetCore.Http;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;

namespace HttpMock
{
    public static class Proxy
    {
        private const string _upstreamHost = "mikeharderperf.blob.core.windows.net";

        private static readonly HttpClient _httpClient = new HttpClient();

        public static async Task ProxyRequest(HttpRequest request, HttpResponse response)
        {
            var upstreamResponse = await SendUpstreamRequest(request);
            await SendDownstreamResponse(upstreamResponse, response);
        }

        private static Task<HttpResponseMessage> SendUpstreamRequest(HttpRequest request)
        {
#if DEBUG
            Log.LogRequest(request);
#endif
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

            var upstreamRequest = new HttpRequestMessage(new HttpMethod(request.Method), upstreamUri);

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

        private static Task SendDownstreamResponse(HttpResponseMessage upstreamResponse, HttpResponse response)
        {
#if DEBUG
            Log.LogUpstreamResponse(upstreamResponse);
#endif

            response.StatusCode = (int)upstreamResponse.StatusCode;
            
            foreach (var header in upstreamResponse.Headers)
            {
                response.Headers.Add(header.Key, header.Value.ToArray());
            }

            return upstreamResponse.Content.CopyToAsync(response.Body);
        }
    }
}