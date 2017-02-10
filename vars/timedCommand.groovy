// vars/timedCommand.groovy

def setCommand(cmd) { commandToRun = cmd }

def getCommand () { cmd }

def runCommand () {

      timestamps {

            commandOutput = sh( script: "${cmd}", returnStdout: true).trim()

       }

}

def getCommandOutput () { commandOutput }
