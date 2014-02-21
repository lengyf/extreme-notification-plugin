package org.jenkinsci.plugins.extremenotification;

import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_BUILD_STEP_FINISH;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_BUILD_STEP_START;
import hudson.Extension;
import hudson.model.BuildListener;
import hudson.model.BuildStepListener;
import hudson.model.AbstractBuild;
import hudson.tasks.BuildStep;
import jenkins.YesNoMaybe;

@Extension(dynamicLoadable=YesNoMaybe.YES)
public class MyBuildStepListener extends BuildStepListener {

	@Override
	public void started(AbstractBuild build, BuildStep bs, BuildListener listener) {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_BUILD_STEP_START, 
				"build", build, 
				"buildStep", bs, 
				"listener", listener
		));
	}

	@Override
	public void finished(AbstractBuild build, BuildStep bs, BuildListener listener, boolean canContinue) {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_BUILD_STEP_FINISH, 
				"build", build, 
				"buildStep", bs, 
				"listener", listener, 
				"canContinue", canContinue
		));
	}

}
