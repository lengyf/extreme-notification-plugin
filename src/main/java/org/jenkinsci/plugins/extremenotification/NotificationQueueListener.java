package org.jenkinsci.plugins.extremenotification;

import hudson.Extension;
import hudson.model.Queue;
import hudson.model.queue.QueueListener;
import jenkins.YesNoMaybe;

import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.*;

@Extension(dynamicLoadable = YesNoMaybe.YES, optional = true)
public class NotificationQueueListener extends QueueListener {
    private static final String ITEM = "item";

    @Override
    public void onEnterWaiting(Queue.WaitingItem wi) {
        notify(JENKINS_QUEUE_ENTER_WAITING, wi);
    }

    @Override
    public void onLeaveWaiting(Queue.WaitingItem wi) {
        notify(JENKINS_QUEUE_LEAVE_WAITING, wi);
    }

    @Override
    public void onEnterBlocked(Queue.BlockedItem bi) {
        notify(JENKINS_QUEUE_ENTER_BLOCKED, bi);
    }

    @Override
    public void onLeaveBlocked(Queue.BlockedItem bi) {
        notify(JENKINS_QUEUE_LEAVE_BLOCKED, bi);
    }

    @Override
    public void onEnterBuildable(Queue.BuildableItem bi) {
        notify(JENKINS_QUEUE_ENTER_BUILDABLE, bi);
    }

    @Override
    public void onLeaveBuildable(Queue.BuildableItem bi) {
        notify(JENKINS_QUEUE_LEAVE_BUILDABLE, bi);
    }

    @Override
    public void onLeft(Queue.LeftItem li) {
        notify(JENKINS_QUEUE_ONLEFT, li);
    }

    private void notify(String eventKey, Queue.Item item) {
        ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(eventKey,
                ITEM, item
        ));
    }
}
