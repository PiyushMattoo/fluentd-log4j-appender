package net.spantree.log4j.fluentd;

import net.spantree.log4j.DaemonAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.fluentd.logger.FluentLogger;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.fluentd.logger.FluentLogger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2014 Cedric Hurst <cedric@spantree.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class FluentdLog4jDaemonAppender extends DaemonAppender<LoggingEvent> {
    private static final int MSG_SIZE_LIMIT = 65535;

    private FluentLogger fluentLogger;
    private final String tag;
    private final String label;
    private final String remoteHost;
    private final int port;
    private final String localHost;


    public FluentdLog4jDaemonAppender(
            final String tag, final String label, final String remoteHost, final int port, final int maxQueueSize,
            final String localHost
    ) {
        super(maxQueueSize);
        this.tag = tag;
        this.label = label;
        this.remoteHost = remoteHost;
        this.port = port;
        this.localHost = localHost;
    }

    @Override
    protected void execute() {
        this.fluentLogger = FluentLogger.getLogger(tag, remoteHost, port);
        super.execute();
    }

    @Override
    protected void close() {
        try {
            super.close();
        } finally {
            fluentLogger.close();
        }
    }

    @Override
    protected void append(LoggingEvent event) {
        final Map<String, Object> data = new HashMap<String, Object>();

        data.put("logger", event.getLoggerName());
        data.put("level", event.getLevel().toString());
        data.put("message", event.getMessage());
        data.put("thread", event.getThreadName());

        if(localHost != null) {
            data.put("hostname", localHost);
        }

        ThrowableInformation throwableInfo = event.getThrowableInformation();
        if(throwableInfo != null) {
            Throwable throwable = throwableInfo.getThrowable();
            data.put("error", throwable.getClass().getCanonicalName());
            data.put("error_message", throwable.getMessage());
            String[] throwableStrings = throwableInfo.getThrowableStrRep();
            if(throwableStrings != null && throwableStrings.length > 0) {
                StringBuffer throwableBuf = new StringBuffer();
                for(int i = 0; i<throwableStrings.length; i++) {
                    throwableBuf.append(throwableStrings[i]);
                    if(i < throwableStrings.length) {
                        throwableBuf.append("\n");
                    }
                }
                data.put("stacktrace", throwableBuf.toString());
            }
        }

        fluentLogger.log(label, data, event.getTimeStamp()/1000);
    }
}
