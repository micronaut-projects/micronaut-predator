package x;

import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.Level;
import java.util.logging.LogManager;

public class LoggingConfig {
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().getLogger("").setLevel(Level.FINEST);
    }
}
