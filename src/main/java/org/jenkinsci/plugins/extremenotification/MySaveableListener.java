package org.jenkinsci.plugins.extremenotification;

import static org.jenkinsci.plugins.extremenotification.MyPlugin.JENKINS_SAVEABLE_CHANGE;
import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import jenkins.YesNoMaybe;

@Extension(dynamicLoadable=YesNoMaybe.YES)
public class MySaveableListener extends SaveableListener {

	@Override
	public void onChange(Saveable saveable, XmlFile file) {
		MyPlugin.notify(new MyPlugin.Event(JENKINS_SAVEABLE_CHANGE, 
				"saveable", saveable, 
				"file", file
		));
	}
	
}
