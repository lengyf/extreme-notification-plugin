package org.jenkinsci.plugins.extremenotification;

import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_SCM_POLL_BEFORE;
import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_SCM_POLL_FAILED;
import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_SCM_POLL_SUCCESS;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.AbstractProject;
import hudson.model.listeners.SCMPollListener;
import hudson.scm.PollingResult;
import jenkins.YesNoMaybe;

@Extension(dynamicLoadable=YesNoMaybe.YES)
public class NotificationSCMPollListener extends SCMPollListener {

	@Override
	public void onBeforePolling(hudson.model.AbstractProject<?,?> project, TaskListener listener) {
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_SCM_POLL_BEFORE,
				"project", project, 
				"listener", listener
		));
	}
	
	@Override
	public void onPollingSuccess(AbstractProject<?, ?> project, TaskListener listener, PollingResult result) {
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_SCM_POLL_SUCCESS,
				"project", project, 
				"listener", listener, 
				"result", result
		));
	}
	
	@Override
	public void onPollingFailed(hudson.model.AbstractProject<?,?> project, TaskListener listener, Throwable exception) {
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_SCM_POLL_FAILED,
				"project", project, 
				"listener", listener, 
				"exception", exception
		));
	}
	
}
