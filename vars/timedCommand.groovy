// vars/timedCommand.groovy

def setCommand(value) { commandToRun = value }

def getCommand() { commandToRun }

def runCommand() {

      def cmdValue = commandToRun
      timestamps {

            commandOutput = sh( script: "${cmdValue}", returnStdout: true).trim()

       }

}

def getCommandOutput() { commandOutput }
