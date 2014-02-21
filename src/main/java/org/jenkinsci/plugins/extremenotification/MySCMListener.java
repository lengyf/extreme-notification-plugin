package org.jenkinsci.plugins.extremenotification;

import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_SCM_CHANGELOG_PARSED;
import hudson.Extension;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.listeners.SCMListener;
import hudson.scm.ChangeLogSet;
import jenkins.YesNoMaybe;

@Extension(dynamicLoadable=YesNoMaybe.YES)
public class MySCMListener extends SCMListener {

	@Override
	public void onChangeLogParsed(AbstractBuild<?, ?> build, BuildListener listener, ChangeLogSet<?> changelog) throws Exception {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_SCM_CHANGELOG_PARSED, 
				"build", build, 
				"listener", listener, 
				"changelog", changelog
		));
	}
	
}
