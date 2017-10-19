package org.jenkinsci.plugins.extremenotification;

import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_SAVEABLE_CHANGE;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import jenkins.YesNoMaybe;

@Extension(dynamicLoadable=YesNoMaybe.YES)
public class NotificationSaveableListener extends SaveableListener {

	@Override
	public void onChange(Saveable saveable, XmlFile file) {
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_SAVEABLE_CHANGE,
				"saveable", saveable, 
				"file", file
		));
	}
	
}
