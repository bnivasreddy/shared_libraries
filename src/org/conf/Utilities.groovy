package org.conf
class Utilities {
  static def gbuild(script, args) {
    script.sh "${script.tool 'gradle3.2'}/bin/gradle ${args}"
  }
  static def getLastBuildStatus(script)
    script.currentBuild.rawBuild.getPreviousSuccessfulBuild()
  }
}

