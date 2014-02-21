package org.jenkinsci.plugins.extremenotification;

import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_JOB_COMPLETED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_JOB_DELETED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_JOB_FINALIZED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_JOB_STARTED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_MATRIX_CONFIG_COMPLETED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_MATRIX_CONFIG_DELETED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_MATRIX_CONFIG_FINALIZED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_MATRIX_CONFIG_STARTED;
import hudson.Extension;
import hudson.matrix.MatrixRun;
import hudson.model.TaskListener;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import jenkins.YesNoMaybe;

@Extension(dynamicLoadable=YesNoMaybe.YES)
public class MyRunListener extends RunListener<Run<?, ?>> {

	@Override
	public void onStarted(Run<?, ?> run, TaskListener listener) {
		MyPlugin.notify(new MyPlugin.Event(run instanceof MatrixRun ? JENKINS_MATRIX_CONFIG_STARTED : JENKINS_JOB_STARTED, 
				"run", run, 
				"listener", listener
		));
	}
	
	@Override
	public void onCompleted(Run<?, ?> run, TaskListener listener) {
		MyPlugin.notify(new MyPlugin.Event(run instanceof MatrixRun ? JENKINS_MATRIX_CONFIG_COMPLETED : JENKINS_JOB_COMPLETED, 
				"run", run, 
				"listener", listener
		));
	}
	
	@Override
	public void onFinalized(Run<?, ?> run) {
		MyPlugin.notify(new MyPlugin.Event(run instanceof MatrixRun ? JENKINS_MATRIX_CONFIG_FINALIZED : JENKINS_JOB_FINALIZED, 
				"run", run
		));
	}
	
	@Override
	public void onDeleted(Run<?, ?> run) {
		MyPlugin.notify(new MyPlugin.Event(run instanceof MatrixRun ? JENKINS_MATRIX_CONFIG_DELETED : JENKINS_JOB_DELETED, 
				"run", run
		));
	}
	
}
