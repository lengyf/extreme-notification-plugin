package org.jenkinsci.plugins.extremenotification;

import com.google.common.collect.Maps;
import hudson.Plugin;
import hudson.init.Initializer;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import jenkins.util.Timer;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.extremenotification.NotificationEndpoint.EndpointEvent;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hudson.init.InitMilestone.PLUGINS_STARTED;

public class ExtremeNotificationPlugin extends Plugin {
	
	private static final Logger LOGGER = Logger.getLogger(ExtremeNotificationPlugin.class.getName());

	static final String JENKINS_ITEM_CREATED = "job_created";

	static final String JENKINS_ITEM_DELETED = "job_deleted";

	static final String JENKINS_ITEM_UPDATED = "job_updated";

	static final String JENKINS_ITEM_RENAMED = "job_renamed";

	static final String JENKINS_JOB_STARTED = "job_started";

	static final String JENKINS_JOB_FINALIZED = "job_finalized";

	static final String JENKINS_JOB_COMPLETED = "job_completed";

	static final String JENKINS_STEP_STARTED = "job_step_started";

	static final String JENKINS_BUILD_STEP_START = "job_step_start";

	static final String JENKINS_BUILD_STEP_FINISH = "job_step_finish";

	static final String JENKINS_COMPUTER_CONFIGURATION = "jenkins.computer.configuration";
	
	static final String JENKINS_COMPUTER_FAILURE = "jenkins.computer.failure";
	
	static final String JENKINS_COMPUTER_OFFLINE = "jenkins.computer.offline";

	static final String JENKINS_COMPUTER_ONLINE = "jenkins.computer.online";
	
	static final String JENKINS_COMPUTER_TEMPORARILY_OFFLINE = "jenkins.computer.temporarily-offline";
	
	static final String JENKINS_COMPUTER_TEMPORARILY_ONLINE = "jenkins.computer.temporarily-online";

	static final String JENKINS_SHUTDOWN = "jenkins.shutdown";
	
	static final String JENKINS_ITEM_COPIED = "jenkins.job.copied";

	static final String JENKINS_LOADED = "jenkins.loaded";
	
	static final String JENKINS_JOB_DELETED = "jenkins.job.deleted";

	static final String JENKINS_QUEUE_ENTER_WAITING = "jenkins.queue.enter.waiting";

	static final String JENKINS_QUEUE_LEAVE_WAITING = "jenkins.queue.leave.waiting";

	static final String JENKINS_QUEUE_ENTER_BLOCKED = "jenkins.queue.enter.blocked";

	static final String JENKINS_QUEUE_LEAVE_BLOCKED = "jenkins.queue.leave.blocked";

	static final String JENKINS_QUEUE_ENTER_BUILDABLE = "jenkins.queue.enter.buildable";

	static final String JENKINS_QUEUE_LEAVE_BUILDABLE = "jenkins.queue.leave.buildable";

	static final String JENKINS_QUEUE_ONLEFT = "jenkins.queue.onleft";

	static final String JENKINS_MATRIX_CONFIG_STARTED = "jenkins.matrix-config.started";
	
	 static final String JENKINS_MATRIX_CONFIG_COMPLETED = "jenkins.matrix-config.completed";
	
	static final String JENKINS_MATRIX_CONFIG_FINALIZED = "jenkins.matrix-config.finalized";
	
	static final String JENKINS_MATRIX_CONFIG_DELETED = "jenkins.matrix-config.deleted";
	
	static final String JENKINS_SAVEABLE_CHANGE = "jenkins.saveable.change";
	
	static final String JENKINS_SCM_CHANGELOG_PARSED = "jenkins.scm.changelog.parsed";

	static final String JENKINS_SCM_POLL_BEFORE = "jenkins.scm.poll.before";
	
	static final String JENKINS_SCM_POLL_SUCCESS = "jenkins.scm.poll.success";
	
	static final String JENKINS_SCM_POLL_FAILED = "jenkins.scm.poll.failed";

	static final String JENKINS_STARTED = "jenkins.started";
	
	static final String JENKINS_PLUGINS_LISTED = "jenkins.plugins.listed";
	
	static final String JENKINS_PLUGINS_PREPARED = "jenkins.plugins.prepared";
	
	static final String JENKINS_PLUGINS_STARTED = "jenkins.plugins.started";
	
	static final String JENKINS_PLUGINS_AUGMENTED = "jenkins.plugins.augmented";
	
	static final String JENKINS_JOBS_LOADED = "jenkins.jobs.loaded";
	
	static final String JENKINS_COMPLETED = "jenkins.completed";
	
	static final String[] ENDPOINTS = new String[]{
			JENKINS_ITEM_CREATED,
			JENKINS_ITEM_DELETED,
			JENKINS_ITEM_UPDATED,
			JENKINS_ITEM_RENAMED,
			JENKINS_JOB_STARTED,
			JENKINS_JOB_FINALIZED,
			JENKINS_JOB_COMPLETED,
			JENKINS_STEP_STARTED
	};
	
	private static ExtremeNotificationPlugin instance;

	public static void notify(Event event) {
		if (instance != null) {
			instance._notify(event);
		}
	}
	
	@Initializer(after = PLUGINS_STARTED)
	public static void init() {
		instance = Jenkins.getActiveInstance().getPlugin(ExtremeNotificationPlugin.class);
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
		Timer.get().submit(runnable);
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

		@Whitelisted
		public String getName() {
			return name;
		}

		@Whitelisted
		public Map<String, Object> getArgs() {
			return args;
		}
		
	}

}
