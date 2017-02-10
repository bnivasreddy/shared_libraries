// vars/timedCommand.groovy

def setCommand(value) { commandToRun = value }

def getCommand() { commandToRun }

def runCommand() {

      def cmdValue = getCommand()
      timestamps {

            commandOutput = sh( script: "${cmdValue}", returnStdout: true).trim()

       }

}

def getCommandOutput() { commandOutput }
