package me.glindholm.plugin.quickimage2.util;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import me.glindholm.plugin.quickimage2.QuickImagePlugin;

/**
 * Nodeclipse Log Util
 *
 * @author Lamb Gao, Paul Verest
 */
public class LogUtil {

    public static void info(final String message) {
        log(IStatus.INFO, IStatus.OK, message, null);
    }

    public static void error(final Throwable exception) {
        error("Unexpected Exception", exception);
    }

    public static void error(final String message) {
        error(message, null);
    }

    public static void error(final String message, final Throwable exception) {
        log(IStatus.ERROR, IStatus.ERROR, message, exception);
    }

    public static void log(final int severity, final int code, final String message, final Throwable exception) {
        log(createStatus(severity, code, message, exception));
    }

    public static IStatus createStatus(final int severity, final int code, final String message, final Throwable exception) {
        return new Status(severity, QuickImagePlugin.PLUGIN_ID, code, message, exception);
    }

    public static void log(final IStatus status) {
        final ILog log = QuickImagePlugin.getDefault().getLog();
        log.log(status);
    }
}
