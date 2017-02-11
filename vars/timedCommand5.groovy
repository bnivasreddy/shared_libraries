// vars/timedCommand5

@Grab('org.apache.commons:commons-lang3:3.4+')
import org.apache.commons.lang.time.StopWatch

@NonCPS
def call(String cmdToRun) { 
      def sw = new StopWatch()
      echo sw.getTime().toString()
      echo "${cmdToRun}"
      cmdOutput = echo sh (script:"${cmd}", returnStdout:true).trim()
      echo "end time"
      echo sw.getTime().toString()
      // delete sw
   
}
