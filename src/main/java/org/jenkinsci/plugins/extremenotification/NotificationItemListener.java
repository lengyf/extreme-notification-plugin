package org.jenkinsci.plugins.extremenotification;

import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_ITEM_COPIED;
import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_ITEM_CREATED;
import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_ITEM_DELETED;
import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_ITEM_RENAMED;
import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_ITEM_UPDATED;
import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_LOADED;
import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_SHUTDOWN;
import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_COMPLETED;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import jenkins.YesNoMaybe;

@Extension(dynamicLoadable=YesNoMaybe.YES)
public class NotificationItemListener extends ItemListener {

	public void onBeforeShutdown() {
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_SHUTDOWN));
	}
	
	@Override
	public void onCopied(Item src, Item item) {
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_ITEM_COPIED,
				"item", item
		));
	}
	
	public void onCreated(Item item) {
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_ITEM_CREATED,
				"item", item
		));
	}
	
	@Override
	public void onDeleted(Item item) {
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_ITEM_DELETED,
				"item", item
		));
	}

	@Override
	public void onLoaded() {
		// We process COMPLETED event here since we cannot hook on COMPLETED due to JENKINS-37759.
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_COMPLETED));
		// And then invoke common notification for this event
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_LOADED));
	}
	
	public void onRenamed(Item item, String oldName, String newName) {
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_ITEM_RENAMED,
				"item", item, 
				"oldName", oldName, 
				"newName", newName
		));
	}
	
	@Override
	public void onUpdated(Item item) {
		ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_ITEM_UPDATED,
				"item", item
		));
	}
	
}
