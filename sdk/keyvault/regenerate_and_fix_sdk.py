import os, sys, subprocess, re
from distutils.dir_util import copy_tree, remove_tree

# Need python 3.6 for new subprocess API
MIN_PYTHON = (3, 6)
if sys.version_info < MIN_PYTHON:
    sys.exit("Python %s.%s or later is required.\n" % MIN_PYTHON)

# Configuration
verbose = True
autorest_target_dir = os.path.join(os.path.dirname(os.path.realpath(__file__)), "tmp")
swagger_source_branch = "keyvault_preview" # Source branch in swagger repo to generate from
autorest_tag = "package-7.0-preview" # Autorest tag to use, if any
run_autorest = True # If false, assumes that we just need to repair generated code that is already in tree!
fix_generated_code = True # if false, assumes that we just need to run autorest
source_rest_spec = "https://raw.githubusercontent.com/Azure/azure-rest-api-specs/%s/specification/keyvault/data-plane/readme.md" % swagger_source_branch # Builds the URL to Swagger readme from branch
autorest_cmd = "autorest --java %s --azure-libraries-for-java-folder=%s" % (source_rest_spec, autorest_target_dir)

if autorest_tag is not None:
    autorest_cmd += " --tag=%s" % autorest_tag

# Constant values
CODE_TARGET_DIRECTORY = os.path.join(os.path.dirname(os.path.realpath(__file__)), "azure-keyvault", "src", "main", "java", "com", "microsoft", "azure", "keyvault")
CUSTOM_MODEL_DIR = os.path.join(CODE_TARGET_DIRECTORY, "models", "custom")
WEBKEY_REPLACE_MODELS = ['JsonWebKey', 'JsonWebKeyEncryptionAlgorithm', 'JsonWebKeyOperation', 'JsonWebKeySignatureAlgorithm', 'JsonWebKeyType']
WEBKEY_NAMESPACE = 'com.microsoft.azure.keyvault.webkey'
CUSTOM_MODEL_NAMESPACE = 'com.microsoft.azure.keyvault.models.custom'
MODEL_NAMESPACE = 'com.microsoft.azure.keyvault.models'
FILES_TO_REMOVE = [
    'azure-keyvault/models/%s.java' % model for model in WEBKEY_REPLACE_MODELS
]
FILES_TO_REMOVE.append('azure-keyvault/implementation/package-info.java')

# Method definitions
def move_generated_code(autorest_target_dir, verbose):
    # Remove JWK models, since those are in a separate non-generated package for now
    print("Removing generated JWK models/package info")
    for fn in FILES_TO_REMOVE:
    
        path = os.path.join(autorest_target_dir, fn)
        if os.path.exists(path):
            if verbose:
                print("Removing %s" % path)
            os.remove(path)

    # move (gen_directory)\azure-keyvault\* --> ./azure-keyvault\src\main\java\com\microsoft\azure\keyvault
    print("Replacing old generated code")
    copy_tree(os.path.join(autorest_target_dir, "azure-keyvault"), CODE_TARGET_DIRECTORY)

    print("Removing temporary generated files")
    remove_tree(autorest_target_dir)
    print("Done with generation process")

# Fixes an individual source file
def correct_file(file, verbose):
    if not file.endswith(".java"):
        return
    
    with open(file) as f:
        code = f.read()
    
    # Fix webkey models
    for model in WEBKEY_REPLACE_MODELS:
        code = code.replace("import %s.%s;" % (MODEL_NAMESPACE, model), "import %s.%s;" % (WEBKEY_NAMESPACE, model))
    
    # Fix broken javadoc
    jdocre = re.compile(r'([a-z|A-Z|0-9]*\<.*?\>)( \* \@param)')
    code = jdocre.sub(r'\2', code)
    
    
    classre = re.compile(r'public class ([a-z|A-Z|0-9]*?)[ \n\r\t]*{')
    
    # If this is a model class and not a custom model class, extend our custom code if necessary - additionally add back potentially removed imports
    dir, fn = os.path.split(file)
    if os.path.split(dir)[1] == "models":
        classes = classre.search(code)
        if classes is not None:
            classname = classes.group(1)
            # check for corresponding custom class
            if os.path.exists(os.path.join(CUSTOM_MODEL_DIR, classname + ".java")):
                if verbose:
                    print("Updating generated '%s' to extend custom '%s'" % (classname, classname))
                code = classre.sub(r'public class \1 extends %s.\1 {' % CUSTOM_MODEL_NAMESPACE, code)
    
        # Add back webkey imports which may have been removed
        class_usage_re = re.compile(r'[ \t<](' + '|'.join(WEBKEY_REPLACE_MODELS) + ')[ \t>]')
        needed_classes = list(set(class_usage_re.findall(code)))
        if len(needed_classes) > 0:
            if verbose:
                print("Adding webkey imports for %s to %s" % ( ",".join(needed_classes), fn ))
            potential_imports = ["import %s.%s;" % (WEBKEY_NAMESPACE, needed) for needed in needed_classes]
            imports = []
            
            for i in potential_imports:
                if i not in code:
                    imports.append(i)
                    
            if len(imports) > 0:
                # find package statement at beginning of code, splice and add the imports in between
                package_re = re.compile(r'package (.+?);\n\n')
                package = package_re.search(code).group(0)
                before, after = code.split(package, 2)
                code = before + package + "\n".join(imports) + "\n" + after
            
            
    # Write back to disk
    with open(file, 'w') as f:
        f.write(code)
            
        
if run_autorest:
    print("Running autorest..")
    if verbose:
        print(autorest_cmd)
    subprocess.run(autorest_cmd, check=True, shell=True)
    move_generated_code(autorest_target_dir, verbose)

if fix_generated_code:
    print("Fixing generated code..")
    for path, dirs, files in os.walk(CODE_TARGET_DIRECTORY):
        if path == CUSTOM_MODEL_DIR: # skip custom code
            continue
        for file in files:
            correct_file(os.path.join(path, file), verbose)