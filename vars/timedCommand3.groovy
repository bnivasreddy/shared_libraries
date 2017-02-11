// vars/timedCommand3

def call(Closure commands) { 
   node('worker') {
       timestamps {
          commands() 
       }
   }
}
