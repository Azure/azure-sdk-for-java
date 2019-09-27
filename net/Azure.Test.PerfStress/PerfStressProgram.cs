using CommandLine;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Reflection.Emit;

namespace Azure.Test.PerfStress
{
    public static class PerfStressProgram
    {
        public static void Main(Assembly assembly, string[] args)
        {
            var testTypes = assembly.ExportedTypes.Where(t => typeof(IPerfStressTest).IsAssignableFrom(t) && !t.IsAbstract);

            var optionTypes = GetOptionTypes(testTypes);

            var result = Parser.Default.ParseArguments(args, optionTypes).WithParsed<PerfStressOptions>(o =>
            {
                var verbName = o.GetType().GetCustomAttribute<VerbAttribute>().Name;
                var testType = testTypes.Where(t => GetVerbName(t.Name) == verbName).Single();
                Run(testType, o);
            });
        }

        private static void Run(Type testType, PerfStressOptions options)
        {
            Console.WriteLine(testType);
            Console.WriteLine(options);

            //if (!GCSettings.IsServerGC)
            //{
            //    throw new InvalidOperationException("Requires server GC");
            //}
        }

        // Dynamically create option types with a "Verb" attribute
        private static Type[] GetOptionTypes(IEnumerable<Type> testTypes)
        {
            var optionTypes = new List<Type>();

            var ab = AssemblyBuilder.DefineDynamicAssembly(new AssemblyName("Options"), AssemblyBuilderAccess.Run);
            var mb = ab.DefineDynamicModule("Options");

            foreach (var t in testTypes)
            {
                var baseOptionsType = t.GetConstructors().First().GetParameters().First().ParameterType;
                var tb = mb.DefineType(t.Name + "Options", TypeAttributes.Public, baseOptionsType);

                var attrCtor = typeof(VerbAttribute).GetConstructor(new Type[] { typeof(string) });
                var verbName = GetVerbName(t.Name);
                tb.SetCustomAttribute(new CustomAttributeBuilder(attrCtor, new object[] { verbName }));

                optionTypes.Add(tb.CreateType());
            }

            return optionTypes.ToArray();
        }

        private static string GetVerbName(string testName)
        {
            var lower = testName.ToLowerInvariant();
            return lower.EndsWith("test") ? lower.Substring(0, lower.Length - 4) : lower;
        }
    }
}
