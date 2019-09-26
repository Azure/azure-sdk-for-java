using System;
using System.Linq;
using System.Reflection;

namespace Azure.Test.PerfStress
{
    public static class PerfStressProgram
    {
        public static void Main(Assembly assembly, string[] args)
        {
            var testTypes = assembly.ExportedTypes.Where(t => typeof(IPerfStressTest).IsAssignableFrom(t) && !t.IsAbstract);

            foreach (var t in testTypes)
            {
                Console.WriteLine(t);
            }

            //if (!GCSettings.IsServerGC)
            //{
            //    throw new InvalidOperationException("Requires server GC");
            //}

            // var result = Parser.Default.ParseArguments(args, new Type[] { typeof(AddOptions), typeof(DeleteOptions) });
            // Console.WriteLine(result);
        }
    }
}
