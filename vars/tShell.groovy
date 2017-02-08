// The call(body) method in any file in workflowLibs.git/vars is exposed as a
// method with the same name as the file.
def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

	/* Run the script that is passed in */
   node {
	try {
		timestamps {
			def shellOutput = sh(script: "${config.shell_command}", returnStdout: true).trim()
		}
  
    } catch (Exception rethrow) {
        failureDetail = failureDetail(rethrow)
        sendMail(config, "FAILURE: Executing shell command ${config.shell_command} in ${env.JOB_NAME}' (${env.BUILD_NUMBER}) failed!",
                "Your job failed, please review it ${env.BUILD_URL}.\n\n${failureDetail}")
        throw rethrow
    }
 }
   
}

/**
 * Read the detail from the exception to be used in the failure message
 * https://issues.jenkins-ci.org/browse/JENKINS-28119 will give better options.
 */
def failureDetail(exception) {
    /* not allowed to access StringWriter
    def w = new StringWriter()
    exception.printStackTrace(new PrintWriter(w))
    return w.toString();
    */
    return exception.toString()
}

