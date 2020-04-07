/* JUG Java Uuid Generator
 *
 * Copyright (c) 2002- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.uuid;

import com.azure.cosmos.implementation.uuid.impl.NameBasedGenerator;

import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Simple command-line interface to UUID generation functionality.
 */
public class Jug
{
    protected final static HashMap<String,String> TYPES = new HashMap<String,String>();
    static {
        TYPES.put("time-based", "t");
        TYPES.put("random-based", "r");
        TYPES.put("name-based", "n");
    }

    protected final static HashMap<String,String> OPTIONS = new HashMap<String,String>();
    static {
        OPTIONS.put("count", "c");
        OPTIONS.put("ethernet-address", "e");
        OPTIONS.put("help", "h");
        OPTIONS.put("namespace", "s");
        OPTIONS.put("name", "n");
        OPTIONS.put("performance", "p");
        OPTIONS.put("verbose", "v");
    }
    
    protected static void printUsage()
    {
        String clsName = Jug.class.getName();
        System.err.println("Usage: java "+clsName+" [options] type");
        System.err.println("Where options are:");
        System.err.println("  --count / -c <number>: will generate <number> UUIDs (default: 1)");
        System.err.println("  --ethernet-address / -e <ether-address>: defines the ethernet address");
        System.err.println("    (in xx:xx:xx:xx:xx:xx notation, usually obtained using 'ifconfig' etc)");
        System.err.println("    to use with time-based UUID generation");
        System.err.println("  --help / -h: lists the usage (ie. what you see now)");
        System.err.println("  --name / -n: specifies");
        System.err.println("     o name for name-based UUID generation");
        System.err.println("     o 'information' part of tag-URI for tag-URI UUID generation");
        System.err.println("  --namespace / -s: specifies");
        System.err.println("    o the namespace (DNS or URL) for name-based UUID generation");
        System.err.println("    o 'authority' part of tag-URI for tag-URI UUID generation;");
        System.err.println("        (fully-qualified domain name, email address)");
        System.err.println("  --performance / -p: measure time it takes to generate UUID(s).");
        System.err.println("    [note that UUIDs are not printed out unless 'verbose' is also specified]");
        System.err.println("  --verbose / -v: lists additional information about UUID generation\n    (by default only UUIDs are printed out (to make it usable in scripts)");
        System.err.println("And type is one of:");
        System.err.println("  time-based / t: generate UUID based on current time and optional\n    location information (defined with -e option)");
        System.err.println("  random-based / r: generate UUID based on the default secure random number generator");
        System.err.println("  name-based / n: generate UUID based on the na the default secure random number generator");
    }

    private static void printMap(Map<String,String> m, PrintStream out, boolean option)
    {
        int i = 0;
        int len = m.size();
        for (Map.Entry<String, String> en : m.entrySet()) {
            if (++i > 1) {
                if (i < len) {
                    out.print(", ");
                } else {
                    out.print(" and ");
                }
            }
            if (option) {
                out.print("--");
            }
            out.print(en.getKey());
            out.print(" (");
            if (option) {
                out.print("-");
            }
            out.print(en.getValue());
            out.print(")");
        }
    }

    public static void main(String[] args)
    {
        if (args.length == 0) {
            printUsage();
            return;
        }

        int count = args.length;
        String type = args[count-1];
        boolean verbose = false;
        int genCount = 1;
        String name = null;
        String nameSpace = null;
        EthernetAddress addr = null;
        boolean performance = false;

        --count;

        // Type we recognize?
        String tmp = TYPES.get(type);
        if (tmp == null) {
            if (!TYPES.containsValue(type)) {
                System.err.println("Unrecognized UUID generation type '"+
                                   type+"'; currently available ones are:");
                printMap(TYPES, System.err, false);
                System.err.println();
                System.exit(1);
            }
        } else {
            // Long names get translated to shorter ones:
            type = tmp;
        }


        NoArgGenerator noArgGenerator = null; // random- or time-based
        StringArgGenerator nameArgGenerator = null; // name-based
        
        for (int i = 0; i < count; ++i) {
            String opt = args[i];

            if (opt.length() == 0 || opt.charAt(0) != '-') {
                System.err.println("Unrecognized option '"+opt+"' (missing leading hyphen?), exiting.");
                System.exit(1);
            }

            char option = (char)0;
            if (opt.startsWith("--")) {
                String o = OPTIONS.get(opt.substring(2));
                // Let's translate longer names to simple names:
                if (o != null) {
                    option = o.charAt(0);
                }
            } else {
                if (OPTIONS.containsValue(opt.substring(1))) {
                    option = opt.charAt(1);
                }
            }

            if (option == (char) 0) {
                System.err.println("Unrecognized option '"+opt+"'; exiting.");
                System.err.print("[options currently available are: ");
                printMap(OPTIONS, System.err, true);
                System.err.println("]");
                System.exit(1);
            }

            // K. Now we have one-letter options to handle:
            try {
                String next;
                switch (option) {
                case 'c':
                    // Need a number now:
                    next = args[++i];
                    try {
                        genCount = Integer.parseInt(next);
                    } catch (NumberFormatException nex) {
                        System.err.println("Invalid number argument for option '"+opt+"', exiting.");
                        System.exit(1);
                    }
                    if (genCount < 1) {
                        System.err.println("Invalid number argument for option '"+opt+"'; negative numbers not allowed, ignoring (defaults to 1).");
                    }
                    break;
                case 'e':
                    // Need the ethernet address:
                    next = args[++i];
                    try {
                        addr = EthernetAddress.valueOf(next);
                    } catch (NumberFormatException nex) {
                        System.err.println("Invalid ethernet address for option '"+opt+"', error: "+nex.toString());
                        System.exit(1);
                    }
                    break;
                case 'h':
                    printUsage();
                    return;
                case 'n':
                    // Need the name
                    name = args[++i];
                    break;
                case 'p': // performance:
                    performance = true;
                    break;
                case 's':
                    // Need the namespace id
                    nameSpace = args[++i];
                    break;
                case 'v':
                    verbose = true;
                    break;
                }
            } catch (IndexOutOfBoundsException ie) {
                // We get here when an arg is missing...
                System.err.println("Missing argument for option '"+opt+"', exiting.");
                System.exit(1);
            }
        } // for (int i = 0....)

        /* Ok, args look ok so far. Now to the generation; some args/options
         * can't be validated without knowing the type:
         */
        char typeC = type.charAt(0);
        UUID nsUUID = null;

        boolean usesRnd = false;

        switch (typeC) {
        case 't': // time-based
            usesRnd = true;
            // No address specified? Need a dummy one...
            if (addr == null) {
                if (verbose) {
                    System.out.print("(no address specified, generating dummy address: ");
                }
                addr = EthernetAddress.constructMulticastAddress(new Random(System.currentTimeMillis()));
                if (verbose) {
                    System.out.print(addr.toString());
                    System.out.println(")");
                }
            }
            noArgGenerator = Generators.timeBasedGenerator(addr);
            break;
        case 'r': // random-based
            usesRnd = true;
            {
                SecureRandom r = new SecureRandom();
                if (verbose) {
                    System.out.print("(using secure random generator, info = '"+r.getProvider().getInfo()+"')");
                }
                noArgGenerator = Generators.randomBasedGenerator(r);
            }
            break;
        case 'n': // name-based
            if (name == null) {
                System.err.println("--name-space (-s) - argument missing when using method that requires it, exiting.");
                System.exit(1);
            }
            if (name == null) {
                System.err.println("--name (-n) - argument missing when using method that requires it, exiting.");
                System.exit(1);
            }
            if (typeC == 'n') {
                String orig = nameSpace;
                nameSpace = nameSpace.toLowerCase();
                if (nameSpace.equals("url")) {
                    nsUUID = NameBasedGenerator.NAMESPACE_URL;
                } else  if (nameSpace.equals("dns")) {
                    nsUUID = NameBasedGenerator.NAMESPACE_DNS;
                } else {
                    System.err.println("Unrecognized namespace '"+orig
                                       +"'; only DNS and URL allowed for name-based generation.");
                    System.exit(1);
                }
            }
            nameArgGenerator = Generators.nameBasedGenerator(nsUUID);
            break;
        }

        // And then let's rock:
        if (verbose) {
            System.out.println();
        }

        /* When measuring performance, make sure that the random number
         * generator is initialized prior to measurements...
         */
        long now = 0L;

        if (performance) {
            // No need to pre-initialize for name-based schemes?
            if (usesRnd) {
                if (verbose) {
                    System.out.println("(initializing random number generator before UUID generation so that performance measurements are not skewed due to one-time init costs)");
                }
                // should initialize by just calling it
                noArgGenerator.generate();
                if (verbose) {
                    System.out.println("(random number generator initialized ok)");
                }
            }
            now = System.currentTimeMillis();
        }

        for (int i = 0; i < genCount; ++i) {
            UUID uuid = (nameArgGenerator == null) ?
                    noArgGenerator.generate() : nameArgGenerator.generate(name);
            if (verbose) {
                System.out.print("UUID: ");
            }
            if (!performance || verbose) {
                System.out.println(uuid.toString());
            }
        } // for (int i = 0; ...)

        if (verbose) {
            System.out.println("Done.");
        }
        if (performance) {
            now = System.currentTimeMillis() - now;
            long avg = (now * 10 + (genCount / 2)) / genCount;
            System.out.println("Performance: took "+now+" milliseconds to generate (and print out) "+genCount+" UUIDs; average being "+(avg / 10)+"."+(avg%10)+" msec.");
        }
    }
}
