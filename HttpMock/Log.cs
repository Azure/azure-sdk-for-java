using Microsoft.AspNetCore.Http;
using System;
using System.Net.Http;

namespace HttpMock
{
    public static class Log
    {
        public static void LogRequest(HttpRequest request)
        {
#if DEBUG
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
#endif
        }

        public static void LogUpstreamRequest(HttpRequestMessage requestMessage)
        {
#if DEBUG
            Console.WriteLine(requestMessage);
            Console.WriteLine();
#endif
        }

        public static void LogUpstreamResponse(HttpResponseMessage responseMessage, bool cached)
        {
            if (cached)
            {
                Console.ForegroundColor = ConsoleColor.Green;
            }

#if DEBUG
            Console.WriteLine(responseMessage);
            Console.WriteLine();
#else
            Console.Write(".");
#endif

            if (cached)
            {
                Console.ResetColor();
            }
        }
    }
}
