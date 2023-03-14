package com.hoatv.fwk.common.logging.listener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * To getting environment variables and put into logback configuration XML files
 * More details: https://stackoverflow.com/questions/1975939/read-environment-variables-from-logback-configuration-file
 */
public class LoggerStartupListener extends ContextAwareBase implements LoggerContextListener, LifeCycle {

    private boolean started = false;

    @Override
    public void start() {
        if (started) {
            return;
        }
        Context context = getContext();
        String hostName = System.getenv("HOSTNAME");
        String iSODate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        context.putProperty("hostname", hostName);
        context.putProperty("iso_date", iSODate);
        started = true;
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onStart(LoggerContext context) {
    }

    @Override
    public void onReset(LoggerContext context) {
    }

    @Override
    public void onStop(LoggerContext context) {
    }

    @Override
    public void onLevelChange(Logger logger, Level level) {
    }
}
