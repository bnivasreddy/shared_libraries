// vars/timedCommand2

def call (String cmd) {
   timestamps {
      cmdOutput = echo sh (script:"${cmd}", returnStdout:true).trim()
   }
   echo cmdOutput
}
