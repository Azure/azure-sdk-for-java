using Microsoft.AspNetCore.Http;
using System;
using System.Net.Http;

namespace HttpMock
{
    public static class Log
    {
        public static void LogRequest(HttpRequest request)
        {
            if (Program.Options.Debug)
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
        }

        public static void LogUpstreamRequest(HttpRequestMessage requestMessage)
        {
            if (Program.Options.Debug)
            {
                Console.WriteLine(requestMessage);
                Console.WriteLine();
            }
        }

        public static void LogUpstreamResponse(HttpResponseMessage responseMessage, bool cached)
        {
            if (Program.Options.Debug)
            {
                Console.WriteLine(responseMessage);
                Console.WriteLine();
            }
            else if (!Program.Options.Trace && !Program.Options.Silent)
            {
                if (cached)
                {
                    Console.Write(".");
                }
                else
                {
                    Console.Write("*");
                }
            }
        }
    }
}
