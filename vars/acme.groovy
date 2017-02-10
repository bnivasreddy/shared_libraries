// vars/acme.groovy
def setCommand(commandToRun) {
    cmd = commandToRun
}
def getCommand() {
    cmd
}

def runCommand() {
    sh "${cmd}"
}
