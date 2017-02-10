// vars/acme.groovy
def setCommand(commandToRun) {
    cmd = commandToRun
}
def getCommand() {
    cmd
}

def runCommand() {
   
   cmdOut = sh (script:"${cmd}", returnStdout:true).trim()
}

def getOutput() {
   cmdOut
}
