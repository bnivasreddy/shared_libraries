// vars/timedCommand5

@Grab('org.apache.commons:commons-lang3:3.4+')
import org.apache.commons.lang.time.StopWatch

@NonCPS
def call(String cmdToRun) { 
      def sw = new StopWatch()
      // echo "sw.getTime()"
      sh "${cmdToRun}"
      // echo "sw.getTime()"
      //delete sw
   
}
