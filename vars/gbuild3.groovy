static def call (args) {
   sh "${tool 'gradle3'}/bin/gradle ${args}"
}
