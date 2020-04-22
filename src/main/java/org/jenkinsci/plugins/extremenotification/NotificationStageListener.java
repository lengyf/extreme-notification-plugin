package org.jenkinsci.plugins.extremenotification;

import hudson.Extension;
import jenkins.YesNoMaybe;
import org.jenkinsci.plugins.workflow.cps.nodes.StepEndNode;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

@Extension(dynamicLoadable= YesNoMaybe.YES, optional = true)
public class NotificationStageListener implements GraphListener {
    @Override
    public void onNewHead(FlowNode node) {
        if (node instanceof StepEndNode) {
            StepEndNode sen = (StepEndNode)node;
            String status = "completed";
            if (sen.getExecution().getCauseOfFailure() != null) {
                status = "failed with " + sen.getExecution().getCauseOfFailure().getMessage();
            }
            System.out.println("【Debug】StepEndNode :"+sen.getStartNode().getStepName());
//            if (sen.getStartNode().getStepName().equalsIgnoreCase("Stage")) {
//                try {
//                    System.out.println("【Debug】 Name1 " + sen.getExecution().getOwner().getUrl());
//                    System.out.println("【Debug】 Name2 " + sen.getExecution().getOwner().getRootDir());
//                    System.out.println("【Debug】 Name2 " + sen.getExecution().getOwner().getRootDir().getName());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("【Debug stage '" + sen.getStartNode().getDisplayName() + "' has " + status);
//            }
        }
    }
}