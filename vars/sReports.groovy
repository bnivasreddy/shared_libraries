def getSonarData(sonarProjectId,sonarUrl) {
    def sonarData = [:]
    // construct the REST API call to get the xml
    metricsList = metricsToCheck.keySet().join(',')
    sonarUrl = "${sonarUrl}/api/resources?resource=${sonarProjectId}&format=xml&metrics=${metricsList}"
    sonarXml = sonarUrl.toURL().text

    //  parse the xml and fill in the map
    def resources = new XmlParser().parseText(sonarXml)
    println sonarXml
    resources.msr.each { node ->
  	sonarData[node.key.text()] = node.val.text()
    }

    // null this out so it doesn't cause a serialization issue back in the pipeline
    resources = null

    if (sonarData) {	
	sonarData.each { rkey, rvalue ->
	   println "$rkey, $rvalue"
	}
    }
    return sonarData
}


def call(body) {

   // define the types of limits we have

   HIGH_LIMIT_DEF = 'HIGH'
   LOW_LIMIT_DEF = 'LOW'

   // define the set of metrics that we're interested in 
   metricsToCheck = [
					'violations':[desc:'Total Violations', jenkinsRef:'violationsThreshold', limitType:HIGH_LIMIT_DEF],
					'blocker_violations':[desc:'Blocker Violations', jenkinsRef:'blockerThreshold', limitType:HIGH_LIMIT_DEF],
					'critical_violations':[desc:'Critical Violations', jenkinsRef:'criticalThreshold', limitType:HIGH_LIMIT_DEF],
					'major_violations':[desc:'Major Violations', jenkinsRef:'majorThreshold', limitType:HIGH_LIMIT_DEF],
					]

    // delcare a map to hold the values passed in from jenkins
    def jenkinsValues = [:]
    // add the code to make this easily "callable" from the pipeline code 
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = jenkinsValues
    body()

    
    // declare a map to hold the sonar metrics we'll parse out of the xml
    def sonarMetrics = [:]

    // get the sonar data
    sonarMetrics = getSonarData(jenkinsValues.sonarProjectId,jenkinsValues.sonarUrl)

  

   //  do the math to see if we are over or under a limit as appropriate
   float sonarVal
   float jenkinsVal

    def results = [:]
    // iterate through the set of parameters
    metricsToCheck.each { key, value ->
       // get the key we should be searching for in the sonar map
       jenkinsKey = value.thresholdName
       metricValueInSonar = sonarMetrics[key]
       jenkinsThreshold = jenkinsValues[jenkinsKey]
		
       // get the type of comparison we should be making
       compType = value.limitType

       diffVal = null

       // do the comparison
       if (metricValueInSonar && jenkinsThreshold) {	
	  sonarVal = Float.parseFloat(metricValueInSonar)
	  jenkinsVal = Float.parseFloat(jenkinsThreshold)
	  diffVal = sonarVal - jenkinsVal
       }

       if (jenkinsThreshold  && jenkinsThreshold != -1 && diffVal) {
	  if (diffVal > 0  && compType == HIGH_LIMIT_DEF) {  
		results[key] = "The metric ${key} has a value ${metricValueInSonar} greater than the maximum threshold of ${jenkinsThreshold}."
	  }	
	  else if (diffVal < 0 && compType == LOW_LIMIT_DEF) {
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
	error("Build did not pass sonar settings check")
	
    }
}
