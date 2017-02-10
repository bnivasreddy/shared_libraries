// vars/timedCommand.groovy

def setCommand(value) { commandToRun = value }

def getCommand() { commandToRun }

def runCommand() {

      timestamps {

            commandOutput = sh( script: "${commandToRun}", returnStdout: true).trim()

       }

}

def getCommandOutput() { commandOutput }
