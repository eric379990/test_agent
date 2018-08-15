package com.csc.test_agent.infrastructure;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csc.test_agent.exception.TestAgentRuntimeException;
import com.csc.test_agent.util.NetworkUtils;
import com.csc.test_agent.util.PropertiesKeyMapper;
import com.csc.test_agent.util.PropertiesWrapper;
import com.csc.test_agent.constants.TestAgentConstants;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.classic.joran.JoranConfigurator;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static com.csc.test_agent.util.Preconditions.checkNotNull;
import static com.csc.test_agent.util.NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS;

public class TestAgentConfig implements TestAgentConstants {

    private static final String AGENT_DEFAULT_FOLDER = ".test_agent";
    private static final Logger LOGGER = LoggerFactory.getLogger("agent config");

    protected TestAgentHome home = null;
    private PropertiesWrapper agentProperties;
    private PropertiesKeyMapper agentPropertyMapper = PropertiesKeyMapper.create("agent-properties.map");

    /**
     * Initialize.
     *
     * @return initialized TestAgentConfig
     */
    public TestAgentConfig init() {
        home = resolveHome();
        loadProperties();
        configureLogging();
        return this;
    }

    protected TestAgentHome resolveHome() {
        String userHomeFromEnv = trimToEmpty(System.getenv("NGRINDER_AGENT_HOME"));
        String userHomeFromProperty = trimToEmpty(System.getProperty("ngrinder.agent.home"));
        if (StringUtils.isNotEmpty(userHomeFromEnv) && !StringUtils.equals(userHomeFromEnv, userHomeFromProperty)) {
            LOGGER.info("The path to ngrinder agent home is ambiguous:");
            LOGGER.info("    '{}' is accepted.", userHomeFromProperty);
        }

        String userHome = StringUtils.defaultIfEmpty(userHomeFromProperty, userHomeFromEnv);
        if (StringUtils.isEmpty(userHome)) {
            userHome = System.getProperty("user.home") + File.separator + AGENT_DEFAULT_FOLDER;
        } else if (StringUtils.startsWith(userHome, "~" + File.separator)) {
            userHome = System.getProperty("user.home") + File.separator + userHome.substring(2);
        } else if (StringUtils.startsWith(userHome, "." + File.separator)) {
            userHome = System.getProperty("user.dir") + File.separator + userHome.substring(2);
        }

        userHome = FilenameUtils.normalize(userHome);
        LOGGER.info("TEST_AGENT_HOME : {}", userHome);
        File homeDirectory = new File(userHome);
        try {
            if (homeDirectory.mkdirs()) {
                LOGGER.info("home directory created : {}", userHome);;
            }
            if (!homeDirectory.canWrite()) {
                throw new TestAgentRuntimeException("home directory " + userHome + " is not writable.");
            }
        } catch (Exception e) {
            throw new TestAgentRuntimeException("Error while resolve the home directory.", e);
        }
        return new TestAgentHome(homeDirectory);
    }

    protected void loadProperties() {
        Objects.nonNull(home);
        Properties properties = home.getProperties("agent.conf");
        properties.put("TEST_AGENT_HOME", home.getDirectory().getAbsolutePath());
        properties.putAll(System.getProperties());
        agentProperties = new PropertiesWrapper(properties, agentPropertyMapper);
    }

    private void configureLogging() {
        File logDirectory = getHome().getLogDirectory();
        String level = "INFO";
        
        final Context context = (Context) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.putProperty("LOG_LEVEL", level);
        context.putProperty("LOG_DIRECTORY", logDirectory.getAbsolutePath());
        try {
            configurator.doConfigure(getClass().getResource("/logback-agent.xml"));
        } catch (JoranException e) {
            LOGGER.error("Error while configuring logger", e);
        }
    }

    /**
     * Get the agent pid in the form of string.
     *
     * @return pid
     */
    public String getAgentPidProperties() {
        checkNotNull(home);
        Properties properties = home.getProperties("pid");
        return (String) properties.get("agent.pid");
    }

    /**
     * Update agent pid file.
     *
     */
    public void updateAgentPidProperties() {
        checkNotNull(home);
        Properties properties = home.getProperties("pid");
        Set<String> names = properties.stringPropertyNames();
        if (names.size() > 1) {
            properties.remove("agent.pid");
            home.saveProperties("pid", properties);
        } else if (names.contains("agent.pid")) {
            removeAgentPidProperties();
        }
    }

    /**
     * Remove agent pid properties.
     */
    public void removeAgentPidProperties() {
        checkNotNull(home);
        File file = home.getFile("pid");
        FileUtils.deleteQuietly(file);
    }

    public TestAgentHome getHome() {
        return this.home;
    }

    /**
     * Get agent properties.
     *
     * @return agent properties
     */
    public PropertiesWrapper getAgentProperties() {
        return checkNotNull(agentProperties);
    }

    public File getCurrentDirectory() {
        return new File(System.getProperty("user.dir"));
    }

    public String getControllerIP() {
        return getAgentProperties().getProperty(PROP_AGENT_CONTROLLER_HOST, DEFAULT_LOCAL_HOST_ADDRESS);
    }

    public void setControllerHost(String host) {
        getAgentProperties().addProperty(PROP_AGENT_CONTROLLER_HOST, host);
    }

    public int getControllerPort() {
        return getAgentProperties().getPropertyInt(PROP_AGENT_CONTROLLER_PORT);
    }

    public String getRegion() {
        return getAgentProperties().getProperty(PROP_AGENT_REGION);
    }

    public String getAgentHostID() {
        return getAgentProperties().getProperty(PROP_AGENT_HOST_ID, NetworkUtils.DEFAULT_LOCAL_HOST_NAME);
    }

    public String getAgentLogLevel() {
        return getAgentProperties().getProperty(PROP_AGENT_LOG_LEVEL);
    }

}
