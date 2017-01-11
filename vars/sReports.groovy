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

    def sonarMetrics = [:]

	metricsParam = metricDefinitions.keySet().join(',')
	sonarUrl = "${sonarUrl}/api/resources?resource=${sonarProjectId}&format=xml&metrics=${metricsParam}"
	sonarXml = sonarUrl.toURL().text
        def resources = new XmlParser().parseText(sonarXml)
	resources.resource[0].msr.each { msr ->
  		sonarMetrics[msr.key.text()] = msr.val.text()
  	}
	resources = null;

	float sonarVal
	float jenkinsVal

	def results = [:]
	// iterate through the set of parameters
	metricDefinitions.each { key, value ->
		// get the key we should be searching for in the sonar map
		jenkinsKey = value.thresholdName
		metricValueInSonar = sonarMetrics[key]
		jenkinsThreshold = jenkinsValues[jenkinsKey]
		
		// get the type of comparison we should be making
		compType = value.thresholdType

		diffVal = null

		// do the comparison
		if (metricValueInSonar && jenkinsThreshold) {	
			sonarVal = Float.parseFloat(metricValueInSonar)
			jenkinsVal = Float.parseFloat(jenkinsThreshold)
			diffVal = sonarVal - jenkinsVal
		}

		if (jenkinsThreshold  && jenkinsThreshold != -1 && diffVal) {
		     if (diffVal > 0  && compType == MAX_THRESHOLD_TYPE) {  
				results[key] = "The metric ${key} has a value ${metricValueInSonar} greater than the maximum threshold of ${jenkinsThreshold}."
			}	
			else if (diffVal < 0 && compType == MIN_THRESHOLD_TYPE) {
				results[key] = "The metric ${key} has a value ${metricValueInSonar} less than the minimum threshold of ${jenkinsThreshold}."
			}
		}
		else {
			results[key] = "An invalid configuration was detected for ${key}." 
		}
	
	}
	
	if (results) {	
		results.each { rkey, rvalue ->
			println "$rvalue"
		}
	error("Build did not pass")
	}
}

