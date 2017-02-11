// vars/timedCommand5

@Grab('org.apache.commons:commons-lang3:3.4+')
import org.apache.commons.lang.time.StopWatch

@NonCPS
def call(String cmdToRun) { 
      def sw = new StopWatch()
      def proc = "$cmdToRun".execute()
      echo sw.getTime().toString()
      echo "${cmdToRun}"
      proc.waitFor()
      
     
    
      
      println( "time =  ${sw.getTime().toString()} \n")
      // delete sw
   
}
