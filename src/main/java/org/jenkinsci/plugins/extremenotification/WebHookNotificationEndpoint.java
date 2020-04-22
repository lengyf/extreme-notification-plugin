package org.jenkinsci.plugins.extremenotification;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Run;
import jenkins.util.Timer;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.URIException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.*;
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
				jobHook = new HookJobRequest(run.getParent().getName());
				runHook = new HookRunRequest(run.number);
				hook.setJob(jobHook);
				hook.setRun(runHook);
				break;
			case JENKINS_ITEM_CREATED:
			case JENKINS_ITEM_UPDATED:
			case JENKINS_ITEM_DELETED:
				item = (Item) args.get("item");
				jobHook = new HookJobRequest(item.getName());
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

		private int number;

		private HookRunRequest(int number) {
			this.number = number;
		}
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

		private String id;

		private HookJobRequest(String id) {
			this.id = id;
		}
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
