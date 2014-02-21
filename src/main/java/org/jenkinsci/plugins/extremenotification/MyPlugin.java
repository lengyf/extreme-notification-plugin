package org.jenkinsci.plugins.extremenotification;

import static hudson.init.InitMilestone.PLUGINS_STARTED;
import hudson.Plugin;
import hudson.init.Initializer;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.util.DescribableList;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.extremenotification.NotificationEndpoint.EndpointEvent;
import org.kohsuke.stapler.StaplerRequest;

import com.google.common.collect.Maps;

public class MyPlugin extends Plugin {
	
	private static final Logger LOGGER = Logger.getLogger(MyPlugin.class.getName());
	
	public static final String JENKINS_BUILD_STEP_START = "jenkins.build.step.start";
	
	public static final String JENKINS_BUILD_STEP_FINISH = "jenkins.build.step.finish";

	public static final String JENKINS_COMPUTER_CONFIGURATION = "jenkins.computer.configuration";
	
	public static final String JENKINS_COMPUTER_FAILURE = "jenkins.computer.failure";
	
	public static final String JENKINS_COMPUTER_OFFLINE = "jenkins.computer.offline";

	public static final String JENKINS_COMPUTER_ONLINE = "jenkins.computer.online";
	
	public static final String JENKINS_COMPUTER_TEMPORARILY_OFFLINE = "jenkins.computer.temporarily-offline";
	
	public static final String JENKINS_COMPUTER_TEMPORARILY_ONLINE = "jenkins.computer.temporarily-online";

	public static final String JENKINS_SHUTDOWN = "jenkins.shutdown";
	
	public static final String JENKINS_ITEM_COPIED = "jenkins.item.copied";
	
	public static final String JENKINS_ITEM_CREATED = "jenkins.item.created";
	
	public static final String JENKINS_ITEM_DELETED = "jenkins.item.deleted";
	
	public static final String JENKINS_LOADED = "jenkins.loaded";
	
	public static final String JENKINS_ITEM_RENAMED = "jenkins.item.renamed";
	
	public static final String JENKINS_ITEM_UPDATED = "jenkins.item.updated";

	public static final String JENKINS_JOB_STARTED = "jenkins.job.started";
	
	public static final String JENKINS_JOB_COMPLETED = "jenkins.job.completed";
	
	public static final String JENKINS_JOB_FINALIZED = "jenkins.job.finalized";
	
	public static final String JENKINS_JOB_DELETED = "jenkins.job.deleted";
	
	public static final String JENKINS_SAVEABLE_CHANGE = "jenkins.saveable.change";
	
	public static final String JENKINS_SCM_CHANGELOG_PARSED = "jenkins.scm.changelog.parsed";

	public static final String JENKINS_SCM_POLL_BEFORE = "jenkins.scm.poll.before";
	
	public static final String JENKINS_SCM_POLL_SUCCESS = "jenkins.scm.poll.success";
	
	public static final String JENKINS_SCM_POLL_FAILED = "jenkins.scm.poll.failed";

	public static final String JENKINS_STARTED = "jenkins.started";
	
	public static final String JENKINS_PLUGINS_LISTED = "jenkins.plugins.listed";
	
	public static final String JENKINS_PLUGINS_PREPARED = "jenkins.plugins.prepared";
	
	public static final String JENKINS_PLUGINS_STARTED = "jenkins.plugins.started";
	
	public static final String JENKINS_PLUGINS_AUGMENTED = "jenkins.plugins.augmented";
	
	public static final String JENKINS_JOBS_LOADED = "jenkins.jobs.loaded";
	
	public static final String JENKINS_COMPLETED = "jenkins.completed";
	
	public static final String[] ENDPOINTS = new String[] {
		JENKINS_BUILD_STEP_START,
		JENKINS_BUILD_STEP_FINISH,
		JENKINS_COMPUTER_CONFIGURATION,
		JENKINS_COMPUTER_FAILURE,
		JENKINS_COMPUTER_OFFLINE,
		JENKINS_COMPUTER_ONLINE,
		JENKINS_COMPUTER_TEMPORARILY_OFFLINE,
		JENKINS_COMPUTER_TEMPORARILY_ONLINE,
		JENKINS_SHUTDOWN,
		JENKINS_ITEM_COPIED,
		JENKINS_ITEM_CREATED,
		JENKINS_ITEM_DELETED,
		JENKINS_LOADED,
		JENKINS_ITEM_RENAMED,
		JENKINS_ITEM_UPDATED,
		JENKINS_JOB_STARTED,
		JENKINS_JOB_COMPLETED,
		JENKINS_JOB_FINALIZED,
		JENKINS_JOB_DELETED,
		JENKINS_SAVEABLE_CHANGE,
		JENKINS_SCM_CHANGELOG_PARSED,
		JENKINS_SCM_POLL_BEFORE,
		JENKINS_SCM_POLL_SUCCESS,
		JENKINS_SCM_POLL_FAILED,
		JENKINS_STARTED,
		JENKINS_PLUGINS_LISTED,
		JENKINS_PLUGINS_PREPARED,
		JENKINS_PLUGINS_STARTED,
		JENKINS_PLUGINS_AUGMENTED,
		JENKINS_JOBS_LOADED,
		JENKINS_COMPLETED
	};
	
	private static MyPlugin instance;

	public static void notify(Event event) {
		if (instance != null) {
			instance._notify(event);
		}
	}
	
	@Initializer(after = PLUGINS_STARTED)
	public static void init() {
		instance = Jenkins.getInstance().getPlugin(MyPlugin.class);
	}
	
	private DescribableList<NotificationEndpoint, Descriptor<NotificationEndpoint>> endpoints = new DescribableList<NotificationEndpoint, Descriptor<NotificationEndpoint>>(this);
	
	@Override
	public void start() throws Exception {
		try {
			load();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to load", e);
		}
	}
	
	public DescribableList<NotificationEndpoint, Descriptor<NotificationEndpoint>> getEndpoints() {
		return endpoints;
	}
	
	@Override
	public void configure(StaplerRequest req, JSONObject formData) throws IOException, ServletException, FormException {
		try {
			endpoints.rebuildHetero(req, formData, NotificationEndpoint.all(), "endpoints");
			save();
	    } catch (IOException e) {
	        throw new FormException(e, "endpoints");
	    }
	}
	
	private void _notify(final Event event) {
		for (final NotificationEndpoint endpoint : endpoints) {
			if (endpoint.getEvents().isEmpty()) {
				start(new Runnable() {
					public void run() {
						endpoint.notify(event);
					}
				});
			} else if (endpoint.getEvents().containsKey(event.getName())) {
				final EndpointEvent endpointEvent = endpoint.getEvents().get(event.getName());
				start(new Runnable() {
					public void run() {
						endpoint.notify(event, endpointEvent);
					}
				});
			}
		}
	}

	private void start(Runnable runnable) {
		Executors.newSingleThreadExecutor().submit(runnable);
	}
	
	public static final class Event {
		
		private final Long timestamp;
		
		private final String name;
		
		private final Map<String, Object> args = Maps.newHashMap();
		
		public Event(String name, Object... args) {
			this.timestamp = System.currentTimeMillis();
			this.name = name;
			this.args.put("event", this);
			for (int i = 0; i < args.length; i+=2) {
				this.args.put((String) args[i], args[i+1]);
			}
		}
		
		public Long getTimestamp() {
			return timestamp;
		}
		
		public String getName() {
			return name;
		}
		
		public Map<String, Object> getArgs() {
			return args;
		}
		
	}

}
