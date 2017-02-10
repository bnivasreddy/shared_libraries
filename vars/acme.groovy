// vars/acme.groovy
def setCommand(value) {
    cmd = value
}
def getCommand() {
    cmd
}

def runCommand() {
    sh "${cmd}"
}
