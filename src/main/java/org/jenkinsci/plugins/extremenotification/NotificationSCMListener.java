package org.jenkinsci.plugins.extremenotification;

import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_SCM_CHANGELOG_PARSED;
import hudson.Extension;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.listeners.SCMListener;
import hudson.scm.ChangeLogSet;
import jenkins.YesNoMaybe;

@Extension(dynamicLoadable=YesNoMaybe.YES)
public class NotificationSCMListener extends SCMListener {

	@Override
	public void onChangeLogParsed(AbstractBuild<?, ?> build, BuildListener listener, ChangeLogSet<?> changelog) throws Exception {
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_SCM_CHANGELOG_PARSED,
				"build", build, 
				"listener", listener, 
				"changelog", changelog
		));
	}
	
}
