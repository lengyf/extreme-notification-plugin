package org.jenkinsci.plugins.extremenotification;

import groovy.lang.Binding;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.extremenotification.ExtremeNotificationPlugin.Event;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;
import org.jenkinsci.plugins.scriptsecurity.scripts.ApprovalContext;
import org.jenkinsci.plugins.scriptsecurity.scripts.ClasspathEntry;
import org.kohsuke.stapler.StaplerRequest;

import com.google.common.collect.Maps;

public abstract class NotificationEndpoint extends AbstractDescribableImpl<NotificationEndpoint> implements ExtensionPoint {

	private static final Logger LOGGER = Logger.getLogger(NotificationEndpoint.class.getName());
	
	public static DescriptorExtensionList<NotificationEndpoint, Descriptor<NotificationEndpoint>> all() {
        return Jenkins.getInstance().getDescriptorList(NotificationEndpoint.class);
    }
	
	public abstract void notify(Event event);
	
	public abstract void notify(Event event, EndpointEvent endpointEvent);
	
	private Map<String, EndpointEvent> events = Maps.newHashMap();
	
	public Map<String, EndpointEvent> getEvents() {
		return events;
	}
	
	protected String interpolate(String value, Event event) {
		return interpolate(value, event, new HashMap<String, Object>());
	}
	
	protected String interpolate(String value, Event event, Map<String, Object> extraArgs) {
		final Map<String, Object> args = Maps.newHashMap(event.getArgs());
		args.putAll(extraArgs);
		SecureGroovyScript script = new SecureGroovyScript('"' + value + '"', true, new ArrayList<ClasspathEntry>());
		try {
			script.configuring(ApprovalContext.create());
			return script.evaluate(ExtremeNotificationPlugin.class.getClassLoader(), new Binding(args)).toString();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Invalid message", e);
			return value;
		}
	}
	
	public abstract static class DescriptorImpl extends Descriptor<NotificationEndpoint> {
		
        public ListBoxModel doFillEndpointsItems() {
        	final ListBoxModel listBoxModel = new ListBoxModel();
        	for (String endpoint : ExtremeNotificationPlugin.ENDPOINTS) {
        		listBoxModel.add(endpoint);
        	}
			return listBoxModel;
        }
        
        @Override
        public NotificationEndpoint newInstance(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
        	final NotificationEndpoint instance = super.newInstance(req, formData);
        	
        	final JSONObject events = formData.getJSONObject("events");
        	if (!events.isNullObject()) {
	        	final JSONArray eventArray;
	        	if (events.get("event") instanceof JSONArray) {
	        		eventArray = events.getJSONArray("event");
	        	} else {
	        		eventArray = new JSONArray();
	        		eventArray.add(events.getJSONObject("event"));
	        	}
	        	for (Object event : eventArray) {
	        		final String endpoint = ((JSONObject)event).getString("endpoint");
	        		final EndpointEventCustom custom = parseCustom(((JSONObject)event));
	        		instance.getEvents().put(endpoint, new EndpointEvent(custom));
	        	}
        	}
			return instance;
        }
        
        protected EndpointEventCustom parseCustom(JSONObject event) {
        	return null;
        }
        
    }
	
	public interface EndpointEventCustom {
		
	}
	
	public static class EndpointEvent {
		private final EndpointEventCustom custom;
		public EndpointEvent(EndpointEventCustom custom) {
			this.custom = custom;
		}
		public EndpointEventCustom getCustom() {
			return custom;
		}
	}
	
}
