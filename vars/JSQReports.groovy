/**
* The following Groovy script collects Sonar Metrics for a Jenkins pipeline,
* sending an informative email with the results.
* The following Jenkins parameters are supported:
*
* projectName: Project name that will appear in emails sent from Jenkins.
* sonarProjectId: Internal project ID used by Sonar.
* sonarUrl: URL for the Sonar server.
* emailRecipients: email addresses for recipients of Sonar metrics summary.
* rulesComplianceThreshold: minimum percentage of rule compliance for validating a build. A value of <em>false</em> means this metric will not be enforced.
* blockerThreshold: maximum number of <em>blocker</em> violations for validating a build. A value of <em>false</em> means this metric will not be enforced.
* criticalThreshold: maximum number of <em>critical</em> violations for validating a build. A value of <em>false</em> means this metric will not be enforced.
* majorThreshold: maximum number of <em>major</em> violations for validating a build. A value of <em>false</em> means this metric will not be enforced.
* codeCoverageThreshold: minimum percentage of code coverage for unit tests for validating a build.  A value of <em>false</em> means this metric will not be enforced.
* testSuccessThreshold: minimum percentage of successful unit tests for validating a build.  A value of <em>false</em> means this metric will not be enforced.
* violationsThreshold: maximum number of violations of all type for validating a build. A value of <em>false</em> means this metric will not be enforced. 
*
**/

import javax.mail.internet.MimeMessage
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.Transport



def safeParse(value) {
	try {
		return Double.parseDouble(value)
	} catch (e) { return -1 }
}

def getNameForMetric(metricId) {
	metricDefinitions.find { it.key == metricId }.value.name
}

def getThresholdTypeForMetric(metricId) {
	metricDefinitions.find { it.key == metricId }.value.thresholdType
}	

def prettyPrintFailures(failedMetrics, isHtml) {
	newline = '\n'
	if (isHtml) newline = '<br>'
	prettyText = ''
	failedMetrics.each { metricId, error ->
		metricName = getNameForMetric(metricId)
		prettyText += "The following metric failed ${metricName}:${newline}${error}${newline}${newline}"
	}
	return prettyText
}

def prettyPrintMetrics(sonarMetrics, isHtml) {
	newline = '\n'
	if (isHtml) newline = '<br>'
	prettyText = ''
	sonarMetrics.each { metricId, value ->
		metricName = getNameForMetric(metricId)
		prettyText += "${metricName}: ${value}${newline}"
	}
	return prettyText
}
	
def addSonarMetric(metricsMap, sonarMetricId, sonarResource) {
	metric = sonarResource.msr.find { it.key == sonarMetricId }
	metricsMap[sonarMetricId] = safeParse(metric.val.text())
}

def getSonarXml(sonarUrl, sonarProjectId) {
	// get sonar metrics through REST interface
	metricsParam = metricDefinitions.keySet().join(',')
	sonarUrl = "${sonarUrl}/api/resources?resource=${sonarProjectId}&format=xml&metrics=${metricsParam}"
	return sonarUrl.toURL().text
}

def getSonarMetrics(sonarUrl, sonarProjectId) {
	sonarMetrics = [:]
	println "AAAA"
	println sonarUrl
	println "BBBB"
	println sonarProjectId
    sonarXml = getSonarXml(sonarUrl, sonarProjectId)
	// parse XML
        println "CCCC"
	println sonarXml
	println "DDDD"
	resources = new XmlSlurper().parseText(sonarXml)
	projectResource = resources.resource[0]
	metricDefinitions.each { addSonarMetric(sonarMetrics, it.key, projectResource) }
	return sonarMetrics
}

def verifyMetric(metricId, sonarMetrics, sonarThresholds, failedMetrics) {
	metricValue = sonarMetrics[metricId]
	thresholdValue = sonarThresholds[metricId]
	thresholdType = getThresholdTypeForMetric(metricId)
	if (thresholdValue != -1) {
		if (thresholdType == MAX_THRESHOLD_TYPE) {
			if (metricValue > thresholdValue)
				failedMetrics[metricId] = "The metric has a value ${metricValue} greater than the maximum threshold of ${thresholdValue}."
		}
		else if (thresholdType == MIN_THRESHOLD_TYPE) {
			if (metricValue < thresholdValue)
				failedMetrics[metricId] = "The metric has a value ${metricValue} less than the minimum threshold of ${thresholdValue}."
		}
		else {
			failedMetrics[metricId] = "An invalid configuration was detected for ${metricValue}." 
		}
	}
}

def getSonarThresholds(jenkinsValues) {
	sonarThresholds = [:]
//	metricDefinitions.each { metricId, metricDefinition ->
//		sonarThresholds[metricId] = safeParse(manager.build.getBuildVariables()[metricDefinition.thresholdName])
	metricDefinitions.each { metricId, metricDefinition ->
		sonarThresholds[metricId] = jenkinsValues[metricDefinition.thresholdName]
	}
	return sonarThresholds
}

def verifyMetrics(sonarMetrics,jenkinsThresholds) {
	failedMetrics = [:]
	sonarThresholds = getSonarThresholds(jenkinsThresholds)
	metricDefinitions.each { metricId, metricDefinition ->
		verifyMetric(metricId, sonarMetrics, sonarThresholds, failedMetrics)
	}
	return failedMetrics
}

def sendMail(session, subject, from, to, body) {
	msg = new MimeMessage(session)
	msg.setSubject(subject)
	msg.setSentDate(new Date())
	msg.setFrom(InternetAddress.parse(from, false)[0])
	msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false))
	msg.setText(body)
	Transport.send(msg) 
}

def sendSuccessMail(projectName, emailRecipients, sonarMetrics) {
	body = 
"""
The Sonar metrics for ${projectName} were processed correctly. The key metrics analyzed are summarized below:

${prettyPrintMetrics(sonarMetrics, false)}
Automatically generated email from Jenkins
"""
	
	mailSession = hudson.tasks.Mailer.descriptor().createSession()
	mailSubject = "SonarQube metrics for ${projectName} (PASS)"
	mailFrom = 'jenkins@example.com'
	sendMail(mailSession, mailSubject, mailFrom, emailRecipients, body)
}

def sendFailureMail(projectName, emailRecipients, sonarMetrics, failedMetrics) {
	body = 
"""
The Sonar metrics for ${projectName} were processed correctly. The key metrics analyzed are summarized below:

${prettyPrintMetrics(sonarMetrics, false)}

The following quality problems were detected:

${prettyPrintFailures(failedMetrics, false)}
Automatically generated email from Jenkins
"""
	
	mailSession = hudson.tasks.Mailer.descriptor().createSession()
	mailSubject = "Sonar metrics for ${projectName} (FAIL)"
	mailFrom = 'jenkins@example.com'
	sendMail(mailSession, mailSubject, mailFrom, emailRecipients, body)
}

def doSuccess(projectName, emailRecipients, sonarMetrics) {
	manager.addShortText("pass",'white','green', '1px', 'gray')
	manager.createSummary("gear2.gif").appendText("The following quality metrics were approved:<br><br>${prettyPrintMetrics(sonarMetrics, true)}", false)
	// send email summary
	sendSuccessMail(projectName, emailRecipients, sonarMetrics)
}

def doFailure(projectName, emailRecipients, sonarMetrics, failedMetrics) {
	manager.addWarningBadge("There were ${failedMetrics.size()} quality metric violations")
	manager.addShortText("fail",'white','red', '1px', 'gray')
	summary = manager.createSummary("warning.gif")
	summary.appendText("The following quality metrics were processsed:<br><br>${prettyPrintMetrics(sonarMetrics, true)}", false)	
	summary.appendText("<br>The following quality metrics failed:<br><br>${prettyPrintFailures(failedMetrics, true)}", false, false, false, "red")
	// send email summary
	sendFailureMail(projectName, emailRecipients, sonarMetrics, failedMetrics)
	manager.buildFailure()
}

// main script starts here

def call(body) {

   MAX_THRESHOLD_TYPE = 'MAX'
   MIN_THRESHOLD_TYPE = 'MIN'

   metricDefinitions = [
					'violations_density':[name:'Rules Compliance', 		thresholdName:'rulesComplianceThreshold', thresholdType:MIN_THRESHOLD_TYPE],
					'violations':[name:'Total Violations', thresholdName:'violationsThreshold', thresholdType:MAX_THRESHOLD_TYPE],
					'blocker_violations':[name:'Blocker Violations', thresholdName:'blockerThreshold', thresholdType:MAX_THRESHOLD_TYPE],
					'critical_violations':[name:'Critical Violations', thresholdName:'criticalThreshold', thresholdType:MAX_THRESHOLD_TYPE],
					'major_violations':[name:'Major Violations', thresholdName:'majorThreshold', thresholdType:MAX_THRESHOLD_TYPE],
					'coverage':[name:'Code Coverage', thresholdName:'codeCoverageThreshold', thresholdType:MIN_THRESHOLD_TYPE],
					'test_success_density':[name:'Test Success', thresholdName:'testSuccessThreshold', thresholdType:MIN_THRESHOLD_TYPE]
					]

    def jenkinsValues = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = jenkinsValues
    body()
    projectName = jenkinsValues.projectName
    emailRecipients = jenkinsValues.emailRecipients
    sonarProjectId = jenkinsValues.sonarProjectId
    sonarUrl = jenkinsValues.sonarUrl

// buildVars = manager.build.getBuildVariables()
// configuration variables
// projectName = buildVars['projectName']
// emailRecipients = buildVars['emailRecipients']
// sonarProjectId = buildVars['sonarProjectId']
// sonarUrl = buildVars['sonarUrl']

// process sonar metrics
sonarMetrics = getSonarMetrics(sonarUrl, sonarProjectId)
// verify thresholds
failedMetrics = verifyMetrics(sonarMetrics,jenkinsValues)

if (failedMetrics) {
	doFailure(projectName, emailRecipients, sonarMetrics, failedMetrics)
}
else {
	doSuccess(projectName, emailRecipients, sonarMetrics)
}
}
