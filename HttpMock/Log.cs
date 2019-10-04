using Microsoft.AspNetCore.Http;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;

namespace HttpMock
{
    public static class Log
    {
        public static void LogRequest(HttpRequest request)
        {
            var uriBuilder = new UriBuilder()
            {
                Scheme = request.Scheme,
                Host = request.Host.Host,
                Path = request.Path.Value,
                Query = request.QueryString.Value,
            };

            if (request.Host.Port.HasValue)
            {
                uriBuilder.Port = request.Host.Port.Value;
            }

            var uri = uriBuilder.Uri;

            Console.WriteLine($"{request.Method} {uri.ToString()} {request.Protocol}");

            foreach (var header in request.Headers)
            {
                Console.WriteLine($"{header.Key}: {header.Value}");
            }

            Console.WriteLine();
        }

        public static void LogUpstreamRequest(HttpRequestMessage requestMessage)
        {
            Console.WriteLine(requestMessage);
            Console.WriteLine();
        }

        public static void LogUpstreamResponse(HttpResponseMessage responseMessage)
        {
            Console.WriteLine(responseMessage);
            Console.WriteLine();
        }
    }
}
