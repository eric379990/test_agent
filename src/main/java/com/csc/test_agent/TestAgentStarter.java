package com.csc.test_agent;

import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.csc.test_agent.constants.TestAgentConstants;
import com.csc.test_agent.infrastructure.TestAgentConfig;
import com.csc.test_agent.util.ProcessUtils;


public class TestAgentStarter implements TestAgentConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger("starter");
    private TestAgentConfig testAgentConfig;
    public static AgentClient agent = null;
    private short retryCount = 5;

    /**
     * Constructor.
     */
    public TestAgentStarter() {
    }

    public void init() {
        this.testAgentConfig = createAgentConfig();
    }

    protected TestAgentConfig createAgentConfig() {
        TestAgentConfig testAgentConfig = new TestAgentConfig();
        testAgentConfig.init();
        return testAgentConfig;
    }

    /**
     * Start test agent.
     */
    public void startAgent() {
        LOGGER.info("***************************************************");
        LOGGER.info("   Start Test Agent ...");
        LOGGER.info("***************************************************");

        if (StringUtils.isEmpty(System.getenv("JAVA_HOME"))) {
            LOGGER.info("Hey!! JAVA_HOME env var was not provided. "
                    + "Please provide JAVA_HOME env var before running agent."
                    + "Otherwise you can not execute the agent in the security mode.");
        }

        String controllerIP = testAgentConfig.getControllerIP();
        int controllerPort = testAgentConfig.getControllerPort();
        testAgentConfig.setControllerHost(controllerIP);
        LOGGER.info("connecting to controller {}:{}", controllerIP, controllerPort);

        try {
            while(retryCount > 0) {
                try {
                    agent = new AgentClient(new URI("ws://localhost:8887"));
                    agent.connect();
                    Thread.sleep(5000);
                    retryCount--;
                } catch (InterruptedException e) {
                    LOGGER.error("Agent is crashed. {}", e.getMessage());
                }

                if(agent.isOpen()) {
                    LOGGER.info("Agent is connected to server!");
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while connecting to : {}:{}", controllerIP, controllerPort);
            staticPrintHelpAndExit("Error while starting Agent", e);
        }

    }

    public static void main(String[] args) {
        TestAgentStarter starter = new TestAgentStarter();
        final TestAgentStarterParam param = new TestAgentStarterParam();
        JCommander commander = new JCommander(param);
        commander.setProgramName("test-agent");
        commander.setAcceptUnknownOptions(true);
        try {
            commander.parse(args);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return;
        }

        starter.init();

        if ("stop".equalsIgnoreCase(param.command)) {
            starter.stopProcess();
            System.out.println("Stop the agent!");
            return;
        }

        starter.startAgent();
    }

    /**
     * Stop process.
     */
    protected void stopProcess() {
        String pid = testAgentConfig.getAgentPidProperties();
        try {
            if (StringUtils.isNotBlank(pid)) {
                ProcessUtils.killProcess(pid);;
            }
            testAgentConfig.updateAgentPidProperties();
        } catch (Exception e) {
            staticPrintHelpAndExit(String.format("Error occurred while terminating %s process.\n"
                    + "It can be already stopped or you may not have the permission.\n"
                    + "If everything is OK. Please stop it manually."), e);
        }
    }

    private static void staticPrintHelpAndExit(String message, Exception e) {
        if (e == null) {
            LOGGER.error(message);
        } else {
            LOGGER.error(message, e);
        }
        System.exit(-1);
    }


}
