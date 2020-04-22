package org.jenkinsci.plugins.extremenotification;

import hudson.Extension;
import hudson.model.Run;
import jenkins.YesNoMaybe;
import org.jenkinsci.plugins.workflow.cps.CpsStepContext;
import org.jenkinsci.plugins.workflow.flow.StepListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.JENKINS_STEP_STARTED;

@Extension(dynamicLoadable= YesNoMaybe.YES, optional = true)
public class NotificationStepListener implements StepListener {

    @Override
    public void notifyOfNewStep(@Nonnull Step s, @Nonnull StepContext context) {
        if ( context instanceof CpsStepContext){
            CpsStepContext cpsContext = (CpsStepContext)context;
            try {
                Run<?,?> run   = cpsContext.get(Run.class);
                ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event( JENKINS_STEP_STARTED,
                        "run", run
                ));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

//        ExtremeNotificationPlugin.notify(new ExtremeNotificationPlugin.Event(JENKINS_BUILD_STEP_START,
//                "step", step
//        ));
    }
}
