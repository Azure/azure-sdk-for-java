var path = require('path');
var gulp = require('gulp');
var args = require('yargs').argv;
var colors = require('colors');
var execa = require('execa');
var pAll = require('p-all');
var os = require('os');
var fs = require('fs');
var shell = require('gulp-shell');
var ghPages = require('gulp-gh-pages');
var argv = require('yargs').argv;
var gulpif = require('gulp-if');
var exec = require('child_process').exec;

const mappings = require('./api-specs.json');
const defaultSpecRoot = "https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master";

async function defaultInfo() {
    console.log("Usage: gulp codegen " +
        "[--spec-root <swagger specs root>] " +
        "[--projects <project names>] " +
        "[--autorest <autorest info>] " +
        "[--autorest-java <autorest.java info>] " +
        "[--debugger] " +
        "[--parallel <number>] " +
        "[--autorest-args <AutoRest arguments>]\n");

    console.log("--spec-root");
    console.log(`\tRoot location of Swagger API specs, default value is "${defaultSpecRoot}"`);

    console.log("--projects");
    console.log("\tComma separated projects to regenerate, default is all. List of available project names:");
    Object.keys(mappings).forEach(function(i) {
        console.log('\t' + i.magenta);
    });

    console.log("--autorest");
    console.log("\tThe version of AutoRest. E.g. 2.0.9, or the location of AutoRest repo, e.g. E:\\repo\\autorest");

    console.log("--autorest-java");
    console.log("\tPath to an autorest.java generator to pass as a --use argument to AutoRest.");
    console.log("\tUsually you'll only need to provide this and not a --autorest argument in order to work on Java code generation.");
    console.log("\tSee https://github.com/Azure/autorest/blob/master/docs/developer/autorest-extension.md");

    console.log("--debug");
    console.log("\tFlag that allows you to attach a debugger to the autorest.java generator.");

    console.log("--parallel");
    console.log("\tSpecifies the maximum number of projects to generate in parallel.");
    console.log("\tDefaults to the number of logical CPUs on the system. (On this system, " + os.cpus().length + ")");

    console.log("--autorest-args");
    console.log("\tPasses additional argument to AutoRest generator");
}

const maxParallelism = parseInt(args['parallel'], 10) || os.cpus().length;
var specRoot = args['spec-root'] || defaultSpecRoot;
var projects = args['projects'];
var autoRestVersion = 'latest'; // default
if (args['autorest'] !== undefined) {
    autoRestVersion = args['autorest'];
}
var debug = args['debugger'];
var autoRestArgs = args['autorest-args'] || '';
var autoRestExe;

async function generate(cb) {
    if (autoRestVersion.match(/[0-9]+\.[0-9]+\.[0-9]+.*/) ||
        autoRestVersion == 'latest') {
            autoRestExe = 'autorest --version=' + autoRestVersion;
            handleInput(projects, cb);
    } else {
        autoRestExe = "node " + path.join(autoRestVersion, "src/autorest-core/dist/app.js");
        handleInput(projects, cb);
    }
}

function handleInput(projects, cb) {
    console.info(`Generating up to ${maxParallelism} projects in parallel..`);
    if (projects === undefined) {
        const actions = Object.keys(mappings).map(proj => {
            return () => codegen(proj, cb);
        });
        pAll(actions, { concurrency: maxParallelism });
    } else {
        const actions = projects.split(",").map(proj => {
            return () => {
                proj = proj.replace(/\ /g, '');
                if (mappings[proj] === undefined) {
                    console.error('Invalid project name "' + proj + '"!');
                    process.exit(1);
                }
                return codegen(proj, cb);
            }
        });
        pAll(actions, { concurrency: maxParallelism });
    }
}

function codegen(project, cb) {
    if (!args['preserve']) {
        const sourcesToDelete = path.join(
            mappings[project].dir,
            '/src/main/java/',
            mappings[project].package.replace(/\./g, '/'));

        deleteFolderRecursive(sourcesToDelete);
    }

    // path.join won't work if specRoot is a URL
    const readmeFile = specRoot + '/' + mappings[project].source;
//    const transcribedReadmeFile = readmeFile + '.temp.md';
//    const tag = findTag(mappings[project].package + ' ' + mappings[project].args);
//    transcribeReadme(readmeFile, transcribedReadmeFile, tag);

    console.log('Generating "' + project + '" from spec file ' + readmeFile);
    var generator = '--fluent=true';
    if (mappings[project].fluent !== null && mappings[project].fluent === false) {
        generator = '';
    }

    const generatorPath = args['autorest-java']
        ? `--use=${path.resolve(args['autorest-java'])} `
        : '';

    const regenManager = args['regenerate-manager'] ? ' --regenerate-manager=true ' : '';

    const outDir = path.resolve(mappings[project].dir);
    cmd = autoRestExe + ' ' + readmeFile +
                        ' --java ' +
                        ' --azure-arm=true ' +
                        ' --generate-client-as-impl=true --implementation-subpackage=models --sync-methods=all --required-parameter-client-methods=true ' +
                        generator +
                        ` --namespace=${mappings[project].package} ` +
                        ` --java.output-folder=${outDir} ` +
                        ` --license-header=MICROSOFT_MIT_SMALL ` +
                        generatorPath +
                        regenManager +
                        autoRestArgs;

    if (mappings[project].args !== undefined) {
        cmd += ' ' + mappings[project].args;
    }

    if (debug) {
        cmd += ' --java.debugger';
    }

    console.log('Command: ' + cmd);
    return execa(cmd, [], { shell: true, stdio: "inherit" });
};

function findTag(stringContainsTag) {
    const regex = new RegExp('--tag=(\\S+)');
    return stringContainsTag.match(regex)[1];
}

function transcribeReadme(inputFile, outputFile, tag) {
    const lines = fs.readFileSync(inputFile).toString('utf8').split("\n");
    const outputHeader = `# Resource
> see https://aka.ms/autorest
This is the AutoRest configuration file for Resource.
---
`;

    const outputLines = [];

    // transcribe common config
    let commonFound = false;
    const commonFields = ['title', 'description', 'openapi-type']
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        if (!commonFound && line.trim() === '``` yaml') {
            commonFound = true;
            outputLines.push('``` yaml');
        } else if (commonFound && line.trim() === '```') {
            outputLines.push('```');
            break;
        } else if (commonFound) {
            commonFields.forEach(field => {
                if (line.trim().startsWith(field + ':')) {
                    outputLines.push(line);
                }
            });
        }
    }

    // transcribe input-file config from java with specified tag
    let tagFound = false;
    let inputFileFound = false;
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        if (!tagFound && line.indexOf('yaml $(tag)') >= 0 && line.indexOf(tag) >= 0) {
            tagFound = true;
            outputLines.push('``` yaml $(java)');
        } else if (tagFound && !inputFileFound && line.indexOf('input-file:') >= 0) {
            inputFileFound = true;
            outputLines.push(line);
        } else if (inputFileFound) {
            if (line.trim().startsWith('- ')) {
                outputLines.push(line);
            } else {
                outputLines.push('```');
                break;
            }
        }
    }

    fs.writeFileSync(outputFile, outputHeader + outputLines.join('\n'));
}

function deleteFolderRecursive(path) {
    var header = "Code generated by Microsoft (R) AutoRest Code Generator";
    if(fs.existsSync(path)) {
        fs.readdirSync(path).forEach(function(file, index) {
            var curPath = path + "/" + file;
            if(fs.lstatSync(curPath).isDirectory()) { // recurse
                deleteFolderRecursive(curPath);
            } else { // delete file
                var content = fs.readFileSync(curPath).toString('utf8');
                if (content.indexOf(header) > -1) {
                    fs.unlinkSync(curPath);
                }
            }
        });
    }
};

async function prepareBuild() {
    return shell.task('mvn package javadoc:aggregate -DskipTests=true -q');
}

async function prepareStage() {
    return gulp.src('./target/site/apidocs/**/*').pipe(gulp.dest('./dist')); 
}

async function preparePublish() {
    var options = {};
    if(process.env.GH_TOKEN){
        options.remoteUrl = 'https://' + process.env.GH_TOKEN + '@github.com/azure/azure-libraries-for-java.git'  
    }
    return gulp.src('./dist/**/*').pipe(gulpif(!argv.dryrun, ghPages(options)));
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
