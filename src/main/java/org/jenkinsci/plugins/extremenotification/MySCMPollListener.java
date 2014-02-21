package org.jenkinsci.plugins.extremenotification;

import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_SCM_POLL_BEFORE;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_SCM_POLL_FAILED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_SCM_POLL_SUCCESS;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.AbstractProject;
import hudson.model.listeners.SCMPollListener;
import hudson.scm.PollingResult;
import jenkins.YesNoMaybe;

@Extension(dynamicLoadable=YesNoMaybe.YES)
public class MySCMPollListener extends SCMPollListener {

	@Override
	public void onBeforePolling(hudson.model.AbstractProject<?,?> project, TaskListener listener) {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_SCM_POLL_BEFORE, 
				"project", project, 
				"listener", listener
		));
	}
	
	@Override
	public void onPollingSuccess(AbstractProject<?, ?> project, TaskListener listener, PollingResult result) {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_SCM_POLL_SUCCESS, 
				"project", project, 
				"listener", listener, 
				"result", result
		));
	}
	
	@Override
	public void onPollingFailed(hudson.model.AbstractProject<?,?> project, TaskListener listener, Throwable exception) {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_SCM_POLL_FAILED, 
				"project", project, 
				"listener", listener, 
				"exception", exception
		));
	}
	
}
