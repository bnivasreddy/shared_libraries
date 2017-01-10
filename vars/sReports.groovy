def call(body) {

   MAX_THRESHOLD_TYPE = 'MAX'
   MIN_THRESHOLD_TYPE = 'MIN'

   metricDefinitions = [
					'violations':[name:'Total Violations', thresholdName:'violationsThreshold', thresholdType:MAX_THRESHOLD_TYPE],
					'blocker_violations':[name:'Blocker Violations', thresholdName:'blockerThreshold', thresholdType:MAX_THRESHOLD_TYPE],
					'critical_violations':[name:'Critical Violations', thresholdName:'criticalThreshold', thresholdType:MAX_THRESHOLD_TYPE],
					'major_violations':[name:'Major Violations', thresholdName:'majorThreshold', thresholdType:MAX_THRESHOLD_TYPE],
					]

    def jenkinsValues = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = jenkinsValues
    body()
    projectName = jenkinsValues.projectName
    emailRecipients = jenkinsValues.emailRecipients
    sonarProjectId = jenkinsValues.sonarProjectId
    sonarUrl = jenkinsValues.sonarUrl





	metricsParam = metricDefinitions.keySet().join(',')
	sonarUrl = "${sonarUrl}/api/resources?resource=${sonarProjectId}&format=xml&metrics=${metricsParam}"
	sonarXml = sonarUrl.toURL().text
        def resources = new XmlParser().parseText(sonarXml)
	println sonarXml
	resources.resource[0].msr.each { msr ->
  		println "msr key: ${msr.key.text()} msr val: ${msr.val.text()}"
		sonarMetrics[msr.key.text()] = msr.val.text()
  	}
	resources = null;

	
}
