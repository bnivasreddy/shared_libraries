// vars/timedCommand.groovy
def setCommand(commandToRun) {
    cmd = commandToRun
}
def getCommand() {
    cmd
}

def runCommand() {
   timestamps {
      cmdOut = sh (script:"${cmd}", returnStdout:true).trim()
   }
   println "ELAPSED TIME: ${currentBuild.rawBuild.getTimestampString()}"
}

def getOutput() {
   cmdOut
}
