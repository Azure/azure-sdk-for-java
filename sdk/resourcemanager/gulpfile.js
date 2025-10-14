var path = require("path");
var gulp = require("gulp");
var args = require("yargs").argv;
var colors = require("colors");
var execa = require("execa");
var pAll = require("p-all");
var os = require("os");
var fs = require("fs");
var shell = require("gulp-shell");
var ghPages = require("gulp-gh-pages");
var argv = require("yargs").argv;
var gulpif = require("gulp-if");
var exec = require("child_process").exec;

const mappings = require("./api-specs.json");
const defaultSpecRoot = "https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main";
const repoRoot = path.resolve(__dirname, "../..");

// Get current version from eng/versioning/version_client.txt normal entry
function getCurrentVersionForArtifact(artifactId) {
  try {
    const versionClientPath = path.resolve(repoRoot, "eng/versioning/version_client.txt");
    if (!fs.existsSync(versionClientPath)) {
      return undefined;
    }
    const content = fs.readFileSync(versionClientPath, "utf8");
    const lines = content.split(/\r?\n/);
    const normalPrefix = `com.azure.resourcemanager:${artifactId};`;
    const normalLine = lines.find((l) => l.startsWith(normalPrefix));
    if (!normalLine) return undefined;
    const parts = normalLine.split(";");
    return parts.length >= 3 ? parts[2].trim() : undefined;
  } catch (_) {
    return undefined;
  }
}

// Determine whether project has been split to sdk/{service}
function isSplitProject(project) {
  const dir = mappings[project] && mappings[project].dir;
  if (!dir) return false;
  // Split projects use ../{service}/azure-resourcemanager-xxx
  // Exclude ../resourcemanagerhybrid
  return /^\.\.\/(?!resourcemanagerhybrid\/).+/.test(dir);
}

// ensure unreleased entry exists in eng/versioning/version_client.txt
function ensureUnreleasedVersionClientEntry(artifactId) {
  try {
    const versionClientPath = path.resolve(repoRoot, "eng/versioning/version_client.txt");
    if (!fs.existsSync(versionClientPath)) {
      console.warn(`version_client.txt not found at ${versionClientPath}`);
      return;
    }
    const content = fs.readFileSync(versionClientPath, "utf8");
    const lines = content.split(/\r?\n/);

    const unreleasedPrefix = `unreleased_com.azure.resourcemanager:${artifactId};`;
    const normalPrefix = `com.azure.resourcemanager:${artifactId};`;

    // If unreleased entry already exists, nothing to do
    if (lines.some((l) => l.startsWith(unreleasedPrefix))) {
      return;
    }

    // Find normal line to get current version (the 3rd semicolon-delimited token)
    const normalLine = lines.find((l) => l.startsWith(normalPrefix));
    if (!normalLine) {
      console.warn(`Normal version entry for ${artifactId} not found in version_client.txt`);
      return;
    }
    const parts = normalLine.split(";");
    // Expected: group:artifact;released;current
    let currentVersion = parts.length >= 3 ? parts[2].trim() : "";
    if (!currentVersion) {
      console.warn(`Unable to parse current version for ${artifactId} from: ${normalLine}`);
      return;
    }

    const newLine = `${unreleasedPrefix}${currentVersion}`;

    // Insert within the Unreleased dependencies section using the blank line after the last unreleased_ entry
    const unreleasedHeaderIndex = lines.findIndex((l) => l.startsWith("# Unreleased dependencies"));
    if (unreleasedHeaderIndex !== -1) {
      // Scan for the unreleased block and blank line after it
      let lastUnreleasedIdx = -1;
      let endOfSectionIdx = -1;
      let seenUnreleased = false;
      for (let i = unreleasedHeaderIndex + 1; i < lines.length; i++) {
        const line = lines[i];
        if (line.startsWith("unreleased_")) {
          seenUnreleased = true;
          lastUnreleasedIdx = i;
          continue;
        }
        if (seenUnreleased) {
          // First blank line after we started seeing unreleased entries marks end of section
          if (line.trim() === "") {
            endOfSectionIdx = i; // insert before this blank line
            break;
          }
          // If not blank, but a new header starts, still treat as end of section
          if (line.startsWith("# ")) {
            endOfSectionIdx = i;
            break;
          }
        }
      }

      let insertIndex;
      if (lastUnreleasedIdx !== -1) {
        // There are existing unreleased entries
        insertIndex = endOfSectionIdx !== -1 ? endOfSectionIdx : lastUnreleasedIdx + 1;
      } else {
        // No existing entries, insert after header comments and the following blank line if present
        insertIndex = unreleasedHeaderIndex + 1;
        // Skip comment lines immediately following the header
        while (insertIndex < lines.length && lines[insertIndex].startsWith("#")) insertIndex++;
        // If next is a blank line, insert after it to keep formatting clean
        if (insertIndex < lines.length && lines[insertIndex].trim() === "") insertIndex++;
      }

      lines.splice(insertIndex, 0, newLine);
      const updated = lines.join("\n");
      fs.writeFileSync(versionClientPath, updated.endsWith("\n") ? updated : updated + "\n", "utf8");
    } else {
      // Fallback: append at end of file
      const updated = content.endsWith("\n") ? content + newLine + "\n" : content + "\n" + newLine + "\n";
      fs.writeFileSync(versionClientPath, updated, "utf8");
    }
    console.log(`Added unreleased entry to version_client.txt: ${newLine}`);
  } catch (e) {
    console.warn(`Failed to update version_client.txt for ${artifactId}: ${e && e.message ? e.message : e}`);
  }
}

// Update sdk/resourcemanager/azure-resourcemanager/pom.xml to refer to the package by unreleased version
function updateAggregatorPomUnreleased(artifactId) {
  try {
    const pomPath = path.resolve(__dirname, "azure-resourcemanager/pom.xml");
    if (!fs.existsSync(pomPath)) {
      console.warn(`azure-resourcemanager/pom.xml not found at ${pomPath}`);
      return;
    }
    let pom = fs.readFileSync(pomPath, "utf8");

    // Only update if this artifact is referenced
    const artifactRef = `<artifactId>${artifactId}</artifactId>`;
    if (pom.indexOf(artifactRef) === -1) {
      return; // dependency not present in aggregator pom
    }

    // Update the x-version-update comment target to unreleased_ for this artifact
    const currentVersion = getCurrentVersionForArtifact(artifactId);
    if (currentVersion) {
      const groupId = "com.azure.resourcemanager";
      const project = `${groupId}:${artifactId}`;
      // Match either existing or already-unreleased marker in the version update comment
      const dependencyPattern = new RegExp(
        `(<groupId>com.azure.resourcemanager</groupId>\\s*<artifactId>${artifactId}</artifactId>\\s*<version>)[^<]+(</version>\\s*<!-- \\{x-version-update;)(?:unreleased_)?${project}(;dependency\\} -->)`,
        'gs'
      );

      newPom = pom.replace(
        dependencyPattern,
        (_, g1, g2, g3) => `${g1}${currentVersion}${g2}unreleased_${project}${g3}`
      );
    }

    if (newPom !== pom) {
      fs.writeFileSync(pomPath, newPom, "utf8");
      console.log(`Updated azure-resourcemanager/pom.xml to use unreleased reference for ${artifactId} with version ${currentVersion || "(unchanged)"}`);
    }
  } catch (e) {
    console.warn(`Failed to update azure-resourcemanager/pom.xml for ${artifactId}: ${e && e.message ? e.message : e}`);
  }
}

async function defaultInfo() {
    console.log(
        "Usage: gulp codegen " +
            "[--spec-root <swagger specs root>] " +
            "[--projects <project names>] " +
            "[--autorest <autorest info>] " +
            "[--autorest-java <autorest.java info>] " +
            "[--debugger] " +
            "[--parallel <number>] " +
            "[--autorest-args <AutoRest arguments>]\n"
    );

    console.log("--spec-root");
    console.log(`\tRoot location of Swagger API specs, default value is "${defaultSpecRoot}"`);

    console.log("--projects");
    console.log("\tComma separated projects to regenerate, default is all. List of available project names:");
    Object.keys(mappings).forEach(function (i) {
        console.log("\t" + i.magenta);
    });

    console.log("--autorest");
    console.log(
        "\tThe version of AutoRest Core. E.g. 3.9.6, or the location of AutoRest repo, e.g. E:\\repo\\autorest"
    );

    console.log("--autorest-java");
    console.log("\tPath to an autorest.java generator to pass as a --use argument to AutoRest.");
    console.log(
        "\tUsually you'll only need to provide this and not a --autorest argument in order to work on Java code generation."
    );
    console.log("\tSee https://github.com/Azure/autorest/blob/main/docs/developer/writing-an-extension.md");

    console.log("--debug");
    console.log("\tFlag that allows you to attach a debugger to the autorest.java generator.");

    console.log("--parallel");
    console.log("\tSpecifies the maximum number of projects to generate in parallel.");
    console.log("\tDefaults to the number of logical CPUs on the system. (On this system, " + os.cpus().length + ")");

    console.log("--autorest-args");
    console.log("\tPasses additional argument to AutoRest generator");
}

const maxParallelism = parseInt(args["parallel"], 10) || os.cpus().length;
var projects = args["projects"];
var autoRestVersion = "3.9.7"; // default
if (args["autorest"] !== undefined) {
    autoRestVersion = args["autorest"];
}
var autoRestJava = "@autorest/java@latest"; // default
if (args["autorest-java"] !== undefined) {
    autoRestJava = args["autorest-java"];
}
var debug = args["debugger"];
var autoRestArgs = args["autorest-args"] || "";
var autoRestExe;

async function generate(cb) {
    if (autoRestVersion.match(/[0-9]+\.[0-9]+\.[0-9]+.*/) || autoRestVersion == "latest") {
        autoRestExe = "autorest --version=" + autoRestVersion;
        handleInput(projects, cb);
    } else {
        autoRestExe = "node " + path.join(autoRestVersion, "src/autorest-core/dist/app.js");
        handleInput(projects, cb);
    }
}

function handleInput(projects, cb) {
    console.info(`Generating up to ${maxParallelism} projects in parallel..`);
    if (projects === undefined) {
        const actions = Object.keys(mappings).map((proj) => {
            return () => codegen(proj, cb);
        });
        pAll(actions, { concurrency: maxParallelism });
    } else {
        const actions = projects.split(",").map((proj) => {
            return () => {
                proj = proj.replace(/\ /g, "");
                if (mappings[proj] === undefined) {
                    console.error('Invalid project name "' + proj + '"!');
                    process.exit(1);
                }
                return codegen(proj, cb);
            };
        });
        pAll(actions, { concurrency: maxParallelism });
    }
}

function codegen(project, cb) {
    packagePath = mappings[project].package.replace(/\./g, "/");

    if (!args["preserve"]) {
        const sourcesToDelete = path.join(mappings[project].dir, "/src/main/java/", packagePath);

        deleteFolderRecursive(sourcesToDelete);

        generatedSamplesTarget = path.join("azure-resourcemanager/src/samples/java/", packagePath, "generated");
        deleteFolderRecursive(generatedSamplesTarget);
    }

    // path.join won't work if specRoot is a URL
    let projectSpecRoot = args["spec-root"] || mappings[project].spec || defaultSpecRoot;
    const readmeFile = projectSpecRoot + "/" + mappings[project].source;

    console.log('Generating "' + project + '" from spec file ' + readmeFile);
    var generator = "--fluent";
    if (mappings[project].fluent !== null && mappings[project].fluent === false) {
        generator = "";
    }

    const generatorPath = autoRestJava
        ? autoRestJava.startsWith("@autorest/java")
            ? `--use=${autoRestJava} `
            : `--use=${path.resolve(autoRestJava)} `
        : "";

    const regenManager = args["regenerate-manager"] ? " --regenerate-manager " : "";

    const outDir = path.resolve(mappings[project].dir);
    cmd =
        autoRestExe +
        " " +
        readmeFile +
        " --java " +
        " --azure-arm " +
        " --modelerfour.additional-checks=false " +
        " --generate-samples " +
        generator +
        ` --java.namespace=${mappings[project].package} ` +
        ` --java.output-folder=${outDir} ` +
        ` --java.license-header=MICROSOFT_MIT_SMALL ` +
        generatorPath +
        regenManager +
        autoRestArgs;

    if (mappings[project].args !== undefined) {
        cmd += " " + mappings[project].args;
    }

    if (debug) {
        cmd += " --java.debugger";
    }

    console.log("Command: " + cmd);
    autorest_result = execa.sync(cmd, [], { shell: true, stdio: "inherit" });

    // move generated samples to azure-resourcemanager
    generatedSamplesSource = path.join(mappings[project].dir, "/src/samples/java/", packagePath, "generated");
    generatedSamplesTarget = path.join("azure-resourcemanager/src/samples/java/", packagePath);

    copyFolderRecursiveSync(generatedSamplesSource, generatedSamplesTarget);
    deleteFolderRecursive(generatedSamplesSource);

    // If already split, ensure version_client and pom updates
    if (isSplitProject(project)) {
        const artifactId = path.basename(path.resolve(mappings[project].dir)); // e.g., azure-resourcemanager-search
        ensureUnreleasedVersionClientEntry(artifactId);
        updateAggregatorPomUnreleased(artifactId);
    }

    return autorest_result;
}

function deleteFolderRecursive(path) {
    var header = "Code generated by Microsoft (R) AutoRest Code Generator";
    if (fs.existsSync(path)) {
        fs.readdirSync(path).forEach(function (file, index) {
            var curPath = path + "/" + file;
            if (fs.lstatSync(curPath).isDirectory()) {
                // recurse
                deleteFolderRecursive(curPath);
            } else {
                // delete file
                var content = fs.readFileSync(curPath).toString("utf8");
                if (content.indexOf(header) > -1) {
                    fs.unlinkSync(curPath);
                }
            }
        });
    }
}

function copyFileSync(source, target) {
    var targetFile = target;

    // If target is a directory, a new file with the same name will be created
    if (fs.existsSync(target)) {
        if (fs.lstatSync(target).isDirectory()) {
            targetFile = path.join(target, path.basename(source));
        }
    }

    fs.writeFileSync(targetFile, fs.readFileSync(source));
}

function copyFolderRecursiveSync(source, target) {
    if (fs.existsSync(source)) {
        var files = [];

        // Check if folder needs to be created or integrated
        var targetFolder = path.join(target, path.basename(source));
        if (!fs.existsSync(targetFolder)) {
            fs.mkdirSync(targetFolder, { recursive: true });
        }

        // Copy
        if (fs.lstatSync(source).isDirectory()) {
            files = fs.readdirSync(source);
            files.forEach(function (file) {
                var curSource = path.join(source, file);
                if (fs.lstatSync(curSource).isDirectory()) {
                    copyFolderRecursiveSync(curSource, targetFolder);
                } else {
                    copyFileSync(curSource, targetFolder);
                }
            });
        }
    }
}

async function prepareBuild() {
    return shell.task("mvn package javadoc:aggregate -DskipTests -q");
}

async function prepareStage() {
    return gulp.src("./target/site/apidocs/**/*").pipe(gulp.dest("./dist"));
}

async function preparePublish() {
    var options = {};
    if (process.env.GH_TOKEN) {
        options.remoteUrl = "https://" + process.env.GH_TOKEN + "@github.com/azure/azure-libraries-for-java.git";
    }
    return gulp.src("./dist/**/*").pipe(gulpif(!argv.dryrun, ghPages(options)));
}

const build = prepareBuild;
const stage = gulp.series(build, prepareStage);
const publish = gulp.series(stage, preparePublish);

//exports task
exports.default = defaultInfo;
exports.codegen = generate;
exports.build = build;
exports.stage = stage;
exports.publish = publish;
