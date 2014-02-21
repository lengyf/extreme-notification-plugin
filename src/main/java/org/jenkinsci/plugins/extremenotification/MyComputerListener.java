package org.jenkinsci.plugins.extremenotification;

import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_COMPUTER_CONFIGURATION;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_COMPUTER_FAILURE;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_COMPUTER_OFFLINE;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_COMPUTER_ONLINE;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_COMPUTER_TEMPORARILY_OFFLINE;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_COMPUTER_TEMPORARILY_ONLINE;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.Computer;
import hudson.slaves.ComputerListener;
import hudson.slaves.OfflineCause;

import java.io.IOException;

import jenkins.YesNoMaybe;

@Extension(dynamicLoadable=YesNoMaybe.YES)
public class MyComputerListener extends ComputerListener {

	@Override
	public void onConfigurationChange() {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_COMPUTER_CONFIGURATION));
	}
	
	public void onLaunchFailure(Computer computer, TaskListener listener) {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_COMPUTER_FAILURE, 
				"computer", computer, 
				"listener", listener
		));
	};
	
	@Override
	public void onOffline(Computer computer) {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_COMPUTER_OFFLINE, 
				"computer", computer
		));
	}
	
	@Override
	public void onOnline(Computer computer, TaskListener listener) throws IOException, InterruptedException {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_COMPUTER_ONLINE, 
				"computer", computer, 
				"listener", listener
		));
	}
	
	@Override
	public void onTemporarilyOffline(Computer computer, OfflineCause cause) {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_COMPUTER_TEMPORARILY_OFFLINE, 
				"computer", computer, 
				"cause", cause
		));
	}
	
	@Override
	public void onTemporarilyOnline(Computer computer) {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_COMPUTER_TEMPORARILY_ONLINE, 
				"computer", computer
		));
	}
	

}
