// vars/timedCommand.groovy

def setCommand(value) { commandToRun = value }

def getCommand () { command }

def runCommand () {

      timestamps {

            commandOutput = sh( script: "${command}", returnStdout: true).trim()

       }

}

def getCommandOutput () { commandOutput }
