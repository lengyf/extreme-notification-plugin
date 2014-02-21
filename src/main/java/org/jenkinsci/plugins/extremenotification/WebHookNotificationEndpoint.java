package org.jenkinsci.plugins.extremenotification;

import static org.apache.commons.httpclient.util.URIUtil.encodeQuery;
import hudson.Extension;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.URIException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.jenkinsci.plugins.extremenotification.MyPlugin.Event;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.collect.Maps;

@Extension
public class WebHookNotificationEndpoint extends NotificationEndpoint {
	
	private static final Logger LOGGER = Logger.getLogger(WebHookNotificationEndpoint.class.getName());
	
	private String url;
	
	public WebHookNotificationEndpoint() {
		
	}
	
	@DataBoundConstructor
	public WebHookNotificationEndpoint(String url) {
		setUrl(url);
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
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
			
			final HttpClient client = new DefaultHttpClient();
			HttpConnectionParams.setStaleCheckingEnabled(client.getParams(), true);
			final HttpGet method = new HttpGet(localUrl);
			Executors.newScheduledThreadPool(1).schedule(new Runnable() {
				public void run() {
					method.abort();
				}
			}, 5, TimeUnit.SECONDS);
			try {
				final HttpResponse response = client.execute(method);
				LOGGER.log(Level.FINE, "{0} status {1}", new Object[] {url, response});
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "communication failure: {0}", e.getMessage());
			} finally {
				method.releaseConnection();
			}
		} catch (URIException e) {
			LOGGER.log(Level.SEVERE, "malformed URL: {}", url);
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
