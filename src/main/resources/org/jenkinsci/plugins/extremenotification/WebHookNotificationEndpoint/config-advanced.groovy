package org.jenkinsci.plugins.extremenotification.LoggingNotificationEndpoint

def f=namespace(lib.FormTagLib)

f.entry(title:_("URL"), field:"url") {
	f.textbox()
}
