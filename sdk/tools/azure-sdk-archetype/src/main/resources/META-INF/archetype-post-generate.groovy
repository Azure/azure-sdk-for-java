dir = new File(new File(request.outputDirectory), request.artifactId)

def run(String cmd) {
    def process = cmd.execute(null, dir)
    process.waitForProcessOutput((Appendable)System.out, System.err)
    if (process.exitValue() != 0) {
        throw new Exception("Command '$cmd' exited with code: ${process.exitValue()}")
    }
}
def mvnFileName = System.properties['os.name'].toLowerCase().contains('windows') ? 'mvn.cmd' : 'mvn'

run("echo 'Updating to latest versions...'")
run("$mvnFileName versions:update-properties -DincludeProperties=bom.version -DgenerateBackupPoms=false")
