// vars/timedCommand5

@Grab('org.apache.commons:commons-lang3:3.4+')
import org.apache.commons.lang.time.StopWatch

@NonCPS
def call(String cmdToRun) { 
      def sw = new StopWatch()
      sw.getTime()
      sh "${cmdToRun}"
      echo "end time"
      echo sw.getTime().toString()
      delete sw
   
}
