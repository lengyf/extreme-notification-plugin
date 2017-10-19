package org.jenkinsci.plugins.extremenotification;

import org.jenkinsci.plugins.extremenotification.testutil.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by daniel.burgmann on 19.02.16 20:17
 */

public class NotificationEndpointTest {

    NotificationEndpoint notificationEndpoint;
    ExtremeNotificationPlugin.Event event;
    Map<String, Object> extraArgs;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws Exception {
        notificationEndpoint = new NotificationEndpoint() {
            @Override
            public void notify(ExtremeNotificationPlugin.Event event) {
            }

            @Override
            public void notify(ExtremeNotificationPlugin.Event event, EndpointEvent endpointEvent) {
            }
        };

        event = Sample.event();
        extraArgs = Sample.extraArgs();
    }

    @After
    public void tearDown() throws Exception {
        event = null;
        notificationEndpoint = null;
    }

    @Test
    public void testInterpolate() {
        String interpolated;

        interpolated = notificationEndpoint.interpolate("value", event);
        assertTrue(interpolated.length() > 0);

        interpolated = notificationEndpoint.interpolate("event ${event.name} (argFoo: ${event.args.argFoo})", event);
        assertEquals("event testEvent (argFoo: foo)", interpolated);

        interpolated = notificationEndpoint.interpolate("event ${event.name} (argBar: ${event.args.argBar}) is now ${what}", event, extraArgs);
        assertEquals("event testEvent (argBar: bar) is now a simple string", interpolated);
    }

}