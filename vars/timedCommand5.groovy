// vars/timedCommand5

@Grab('org.apache.commons:commons-lang3:3.4+')
import org.apache.commons.lang.time.StopWatch

@NonCPS
def call(Closure commands) { 
   node('worker') {
      def sw = new StopWatch()
      echo "sw.getTime()"
      commands()
      echo "sw.getTime()"
      delete sw
   }
}
