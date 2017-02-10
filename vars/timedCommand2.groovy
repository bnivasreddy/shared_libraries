// vars/timedCommand2.groovy
class timedCommand2 implements Serializable {

    private String cmd
    private String cmdOut

    def setCommand(commandToRun) {
       cmd = commandToRun
    }

    def getCommand() {
       cmd
    }

   def runCommand() {
        
       timestamps {
          cmdOut = sh (script:"sleep 5", returnStdout:true).trim()
       }
    }

    def getOutput() {
       cmdOut
    }
}
