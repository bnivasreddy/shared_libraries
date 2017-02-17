package org.conf
class Utilities2 {
  static def gbuild2(script, args) {
    script.sh "${script.tool 'gradle3.2'}/bin/gradle ${args}"
  }
}

