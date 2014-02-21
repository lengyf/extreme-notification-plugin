package org.jenkinsci.plugins.extremenotification.LoggingNotificationEndpoint

def f=namespace(lib.FormTagLib)

f.entry(title:_("Logger"), field:"loggerName") {
    f.textbox()
}

f.entry(title:_("Level"), field:"levelName") {
	f.select()
}
