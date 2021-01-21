package org.jenkinsci.plugins.extremenotification;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.util.Timer;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.URIException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.httpclient.util.URIUtil.encodeQuery;
import static org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.*;

@Extension
public class WebHookNotificationEndpoint extends NotificationEndpoint {
	
	private static final Logger LOGGER = Logger.getLogger(WebHookNotificationEndpoint.class.getName());
	
	private String url;
	
	private long timeout;
	
	public WebHookNotificationEndpoint() {
		
	}
	
	@DataBoundConstructor
	public WebHookNotificationEndpoint(String url, long timeout) {
		this.url = url;
		this.timeout = timeout;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public long getTimeout() {
		return timeout;
	}
	
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	@Override
	public void notify(Event event) {
		requestURL(event, url);
	}
	
	@Override
	public void notify(Event event, EndpointEvent endpointEvent) {
		final WebHookEndpointEventCustom custom = (WebHookEndpointEventCustom) endpointEvent.getCustom();
		requestURL(event, custom == null ? this.url : custom.getURL());
	}
	
	private void requestURL(Event event, String url) {
		final HashMap<String, Object> extra = Maps.newHashMap();
		extra.put("url", interpolate(this.url, event));
		try {
			final String localUrl = encodeQuery(interpolate(url, event, extra));
			
			final HttpClient httpClient = new DefaultHttpClient();
			final HttpPost post = new HttpPost(localUrl);
			post.setHeader("Content-type", "application/json; charset=utf-8");

			HookRequest hookRequest = buildHookRequest(event);
			Gson gson = new Gson();
			String hookRequestJsonStr = gson.toJson(hookRequest);
			StringEntity entity = new StringEntity(hookRequestJsonStr);
			post.setEntity(entity);
			Timer.get().schedule(new Runnable() {
				@Override
				public void run() {
					post.abort();
				}
			}, this.timeout, TimeUnit.SECONDS);
			try {
				final HttpResponse response = httpClient.execute(post);
				LOGGER.log(Level.FINE, "{0} status {1}", new Object[] {localUrl, response});
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "communication failure: {0}", e.getMessage());
			} finally {
				post.releaseConnection();
			}
		} catch (URIException e) {
			LOGGER.log(Level.SEVERE, "malformed URL: {}", url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private HookRequest buildHookRequest(Event event) {
		HookRequest hook = new HookRequest();
		HookJobRequest jobHook;
		HookRunRequest runHook;
		Run<?, ?> run;
		Item item;

		Map<String, Object> args = event.getArgs();

		switch (event.getName()) {
			case JENKINS_JOB_STARTED:
			case JENKINS_JOB_COMPLETED:
			case JENKINS_JOB_FINALIZED:
			case JENKINS_STEP_STARTED:
			case JENKINS_BUILD_STEP_FINISH:
				run = (Run<?, ?>) args.get("run");
				jobHook = new HookJobRequest();
				jobHook.setBuildable(run.getParent().isBuildable());
				jobHook.setBuilding(run.getParent().isBuilding());
				jobHook.setDescription(run.getParent().getDescription());
				jobHook.setDisplayName(run.getParent().getDisplayName());
				jobHook.setFullName(run.getParent().getFullName());
				jobHook.setFullDisplayName(run.getParent().getFullDisplayName());
				jobHook.setId(run.getParent().getName());
				jobHook.setName(run.getParent().getName());
				jobHook.setInQueue(run.getParent().isInQueue());
				jobHook.setNameEditable(run.getParent().isNameEditable());
				jobHook.setUrl(run.getParent().getUrl());
				jobHook.setAbsoluteUrl(run.getParent().getAbsoluteUrl());
				runHook = new HookRunRequest();
				runHook.setNumber(run.getNumber());
                runHook.setBuilding(run.isBuilding());
                runHook.setDescription(run.getDescription());
                runHook.setDisplayName(run.getDisplayName());
                runHook.setFullDisplayName(run.getFullDisplayName());
                runHook.setId(run.getId());
                runHook.setExternalizableId(run.getExternalizableId());
                runHook.setCreateTime(run.getTimeInMillis());
                runHook.setStartTime(run.getStartTimeInMillis());
                runHook.setDuration(run.getDuration());
                runHook.setBuildStatusUrl(run.getBuildStatusUrl());
                runHook.setLogUpdated(run.isLogUpdated());
                runHook.setNotStarted(run.hasntStartedYet());
                runHook.setResult(run.getResult());
                runHook.setUrl(run.getUrl());
				hook.setJob(jobHook);
				hook.setRun(runHook);
				break;
			case JENKINS_ITEM_CREATED:
			case JENKINS_ITEM_UPDATED:
			case JENKINS_ITEM_DELETED:
				item = (Item) args.get("item");
				jobHook = new HookJobRequest();
                jobHook.setDisplayName(item.getDisplayName());
                jobHook.setFullName(item.getFullName());
                jobHook.setFullDisplayName(item.getFullDisplayName());
                jobHook.setId(item.getName());
                jobHook.setName(item.getName());
                jobHook.setUrl(item.getUrl());
                jobHook.setAbsoluteUrl(item.getAbsoluteUrl());
				hook.setJob(jobHook);
				break;
			case JENKINS_ITEM_RENAMED:
				String oldName = (String) args.get("oldName");
				String newName = (String) args.get("newName");
				HookRenameRequest rename = new HookRenameRequest(oldName, newName);
				hook.setRename(rename);
				break;
		}
		return hook;
	}

	private class HookRequest implements Serializable {
		private HookRequest() {
		}

		public HookRunRequest getRun() {
			return run;
		}

		public void setRun(HookRunRequest run) {
			this.run = run;
		}

		private HookRunRequest run;
		private HookJobRequest job;
		private HookRenameRequest rename;

		public HookStepRequest getStep() {
			return step;
		}

		public void setStep(HookStepRequest step) {
			this.step = step;
		}

		private HookStepRequest step;

		public String getDriver() {
			return driver;
		}

		public void setDriver(String driver) {
			this.driver = driver;
		}

		private String driver = "jenkins";

		public HookJobRequest getJob() {
			return job;
		}

		public void setJob(HookJobRequest job) {
			this.job = job;
		}

		public HookRenameRequest getRename() {
			return rename;
		}

		public void setRename(HookRenameRequest rename) {
			this.rename = rename;
		}
	}

	private class HookRunRequest implements Serializable {

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getExternalizableId() {
            return externalizableId;
        }

        public void setExternalizableId(String externalizableId) {
            this.externalizableId = externalizableId;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public Result getResult() {
            return result;
        }

        public void setResult(Result result) {
            this.result = result;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getFullDisplayName() {
            return fullDisplayName;
        }

        public void setFullDisplayName(String fullDisplayName) {
            this.fullDisplayName = fullDisplayName;
        }

        public String getBuildStatusUrl() {
            return buildStatusUrl;
        }

        public void setBuildStatusUrl(String buildStatusUrl) {
            this.buildStatusUrl = buildStatusUrl;
        }

        public boolean isNotStarted() {
            return notStarted;
        }

        public void setNotStarted(boolean notStarted) {
            this.notStarted = notStarted;
        }

        public boolean isBuilding() {
            return building;
        }

        public void setBuilding(boolean building) {
            this.building = building;
        }

        public boolean isLogUpdated() {
            return logUpdated;
        }

        public void setLogUpdated(boolean logUpdated) {
            this.logUpdated = logUpdated;
        }

        private String id;

        private String externalizableId;

        private int number;

        private long createTime;

        private long startTime;

        private long duration;

        private Result result;

        private String url;

        private String displayName;

        private String description;

        private String fullDisplayName;

        private String buildStatusUrl;

        private boolean notStarted;

        private boolean building;

        private boolean logUpdated;
	}

	private class HookStepRequest implements Serializable {

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		private String id;

		private HookStepRequest(String id) {
			this.id = id;
		}
	}


	private class HookJobRequest implements Serializable {
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getFullDisplayName() {
            return fullDisplayName;
        }

        public void setFullDisplayName(String fullDisplayName) {
            this.fullDisplayName = fullDisplayName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAbsoluteUrl() {
            return absoluteUrl;
        }

        public void setAbsoluteUrl(String absoluteUrl) {
            this.absoluteUrl = absoluteUrl;
        }

        public boolean isNameEditable() {
            return nameEditable;
        }

        public void setNameEditable(boolean nameEditable) {
            this.nameEditable = nameEditable;
        }

        public boolean isBuildable() {
            return buildable;
        }

        public void setBuildable(boolean buildable) {
            this.buildable = buildable;
        }

        public boolean isBuilding() {
            return building;
        }

        public void setBuilding(boolean building) {
            this.building = building;
        }

        public boolean isInQueue() {
            return inQueue;
        }

        public void setInQueue(boolean inQueue) {
            this.inQueue = inQueue;
        }

        private String id;

		private String name;

		private String displayName;

		private String fullName;

		private String fullDisplayName;

		private String description;

		private String url;

		private String absoluteUrl;

		private boolean nameEditable;

		private boolean buildable;

		private boolean building;

		private boolean inQueue;
	}

	private class HookRenameRequest implements Serializable {
		private String oldName;

		public String getOldName() {
			return oldName;
		}

		public void setOldName(String oldName) {
			this.oldName = oldName;
		}

		public String getNewName() {
			return newName;
		}

		public void setNewName(String newName) {
			this.newName = newName;
		}

		private String newName;

		private HookRenameRequest(String oldName, String newName) {
			this.oldName = oldName;
			this.newName = newName;
		}
	}
	
	private Object readResolve() {
		setUrl(url);
		return this;
	}

	
	@Extension
    public static final class DescriptorImpl extends NotificationEndpoint.DescriptorImpl {

		@Override
        public String getDisplayName() {
            return Messages.WebHookNotificationEndpoint_DisplayName();
        }

        @Override
		protected EndpointEventCustom parseCustom(JSONObject event) {
        	final JSONObject customJSON = ((JSONObject)event).getJSONObject("custom");
			if (!customJSON.isNullObject()) {
				return new WebHookEndpointEventCustom(customJSON.getString("url"));
			}
			
			return null;
		}
        
    }
	
	public static class WebHookEndpointEventCustom implements EndpointEventCustom {
		private final String url;
		public WebHookEndpointEventCustom(String url) {
			this.url = url;
		}
		public String getURL() {
			return url;
		}
	}
}
