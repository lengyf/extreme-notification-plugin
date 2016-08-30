package org.jenkinsci.plugins.extremenotification;

import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_COMPLETED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_JOBS_LOADED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_PLUGINS_AUGMENTED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_PLUGINS_LISTED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_PLUGINS_PREPARED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_PLUGINS_STARTED;
import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_STARTED;
import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;

import java.io.IOException;

import jenkins.YesNoMaybe;

@Extension(dynamicLoadable=YesNoMaybe.YES)
public class ServerStateListener {

	@Initializer(after=InitMilestone.STARTED)
	public static void started() throws IOException {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_STARTED));
	}
	
	@Initializer(after=InitMilestone.PLUGINS_LISTED)
	public static void pluginsListed() throws IOException {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_PLUGINS_LISTED));
	}
	
	@Initializer(after=InitMilestone.PLUGINS_PREPARED)
	public static void pluginsPrepared() throws IOException {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_PLUGINS_PREPARED));
	}
	
	@Initializer(after=InitMilestone.PLUGINS_STARTED)
	public static void pluginsStarted() throws IOException {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_PLUGINS_STARTED));
	}
	
	@Initializer(after=InitMilestone.EXTENSIONS_AUGMENTED)
	public static void extensionsAugmented() throws IOException {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_PLUGINS_AUGMENTED));
	}
	
	@Initializer(after=InitMilestone.JOB_LOADED)
	public static void jobLoaded() throws IOException {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_JOBS_LOADED));
	}

	/**
	 * Notifies about initialization completion.
	 * It is not an initializer, because we cannot hook on {@link InitMilestone#COMPLETED} due to
	 * <a href="https://issues.jenkins-ci.org/browse/JENKINS-37759">JENKINS-37759</>.
	 * @deprecated The implementation has been moved to {@link MyItemListener#onLoaded()}, which is the nearest hook available.
	 * @throws IOException Notification processing error
     */
	public static void completed() throws IOException {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_COMPLETED));
	}
	
}
