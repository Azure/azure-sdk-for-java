const fs = require("fs");
const path = require("path");
const process = require("process");
const util = require("util");

const parseXml = require("@rgrove/parse-xml");

const readFile = util.promisify(fs.readFile);
const writeFile = util.promisify(fs.writeFile);

const parseArgs = () => {
  const scriptName = path.basename(process.argv[1]);
  if (process.argv.length !== 4 || ["-h", "--help"].includes(process.argv[2].toLowerCase())) {
    console.error(`Usage: ${scriptName} <dependencies-file> <output-file>`);
    console.error(`Example: ${scriptName} ../../output/dependencies.json data.js`);
    process.exit(1);
  }
  return process.argv.slice(2);
};

const convertDependencyFile = async (inputFile) => {
  const data = await readFile(inputFile, "utf8");
  const deps = JSON.parse(data);
  const dumpData = {};
  const internal = [];

  for (const dep of deps) {
    const depName = `${dep.groupId}:${dep.artifactId}`;
    for (const [depVer, libs] of Object.entries(dep.dependenciesOnVersion)) {
      for (const [libPath, depChains] of Object.entries(libs)) {
        const libName = `com.azure:${path.basename(libPath)}`;
        const pomPath = path.join("..", "..", ...libPath.split(path.sep).slice(1), "pom.xml");
        const libVer = await getVersion(pomPath);
        const libId = `${libName}:${libVer}`;

        if (!internal.includes(libName)) {
          internal.push(libName);
        }

        if (!dumpData[libId]) {
          dumpData[libId] = {
            name: libName,
            version: libVer,
            type: "internal",
            deps: []
          };
        }

        if (depChains.some(chain => !chain["dependencyChain"])) {
          // dep is a direct dependency of lib
          dumpData[libId].deps.push({
            name: depName,
            version: depVer
          });
        }
      }
    }
  }
  return [dumpData, internal];
};

const versions = {};
const getVersion = async (pomPath) => {
  versions[pomPath] = versions[pomPath] || await parseVersion(pomPath);
  return versions[pomPath];
}

const parseVersion = async (pomPath) => {
  const contents = parseXml(await readFile(pomPath, "utf8"));
  const projectNode = contents.children[0];
  const versionNode = projectNode.children.find(c => c.type === "element" && c.name === "version");
  const version = versionNode.children[0].text;
  return version;
};

const resolveLibDeps = (libs, internalLibs, libId) => {
  for (const dep of libs[libId].deps) {
    // Add the dependency to the top level of the packages list
    const depId = `${dep.name}:${dep.version}`;
    if (!libs[depId]) {
      libs[depId] = {
        name: dep.name,
        version: dep.version,
        type: internalLibs.includes(dep.name) ? "internalbinary" : "external",
        deps: []
      };
    }
  }
};

const main = async () => {
  const [inputFile, outputFile] = parseArgs();
  console.log(`Converting ${path.basename(inputFile)}...`)
  const [dumpData, internalLibs] = await convertDependencyFile(inputFile);
  for (const libId of Object.keys(dumpData)) {
    resolveLibDeps(dumpData, internalLibs, libId);
  }
  await writeFile(outputFile, "const data = " + JSON.stringify(dumpData) + ";");
  console.log(`Dependency data saved to ${path.resolve(outputFile)}`)
}

main();
