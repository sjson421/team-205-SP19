package edu.northeastern.ccs.im;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.rules.ExternalResource;

import java.io.CharArrayWriter;

/**
 * This can help to test log4j2 if necessary
 * reference: https://www.dontpanicblog.co.uk/2018/04/29/test-log4j2-with-junit/
 */
public class LogAppenderResource extends ExternalResource {
    private static final String APPENDER_NAME = "log4jRuleAppender";

    private static final String PATTERN = "%-5level %msg";

    private Logger logger;
    private Appender appender;
    private final CharArrayWriter outContent = new CharArrayWriter();

    /**
     * Initialize appender resource
     *
     * @param logger the logger
     */
    public LogAppenderResource(org.apache.logging.log4j.Logger logger) {
        this.logger = (org.apache.logging.log4j.core.Logger) logger;
    }

    /**
     * before for this resource rule
     */
    @Override
    protected void before() {
        StringLayout layout = PatternLayout.newBuilder().withPattern(PATTERN).build();
        appender = WriterAppender.newBuilder()
                .setTarget(outContent)
                .setLayout(layout)
                .setName(APPENDER_NAME).build();
        appender.start();
        logger.addAppender(appender);
    }

    /**
     * after for this rule
     */
    @Override
    protected void after() {
        logger.removeAppender(appender);
    }

    /**
     * get output logging as String
     *
     * @return return string that been logged
     */
    public String getOutput() {
        return outContent.toString();
    }
}
