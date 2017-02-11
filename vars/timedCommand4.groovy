// vars/timedCommand4.groovy

def call(body) {
    // collect assignments passed in into our mapping
    def settings = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = settings
    body()

    // now, time the commands
   timestamps {
      cmdOutput = echo sh (script:"${settings.cmd}", returnStdout:true).trim()
   }
   echo cmdOutput
   writeFile file: '${settings.logfilePath}', text: '${cmdOutput}'
}
