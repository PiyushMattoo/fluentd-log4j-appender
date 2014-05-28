package net.spantree.log4j.fluentd;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Created by cedric on 5/27/14.
 */
public class FluentdLog4jAppender extends AppenderSkeleton {
    private FluentdLog4jDaemonAppender appender;

    protected String tag = "log4j-appender";
    protected String remoteHost = "localhost";
    protected int port = 24224;
    protected int maxQueueSize = 20;
    protected String label = "label";
    protected String localHost = null;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    @Override
    public void activateOptions() {
        try {
            appender = new FluentdLog4jDaemonAppender(tag, label, remoteHost, port, maxQueueSize, localHost);
        } catch (RuntimeException e) {
            getErrorHandler().error("Cannot create FluentLogger.", e, 0);
        }

        super.activateOptions();
    }

    @Override
    protected void append(LoggingEvent event) {
        appender.log(event);
    }

    @Override
    public void close() {
        try {
            appender.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}