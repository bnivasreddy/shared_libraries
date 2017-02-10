// vars/timedCommand.groovy

def setCommand (command-to-run) { command = command-to-run }

def getCommand () { command }

def runCommand () {

      timestamps {

            commandOutput = sh( script: "${command}", returnStdout: true).trim()

       }

}

def getCommandOutput () { commandOutput }
