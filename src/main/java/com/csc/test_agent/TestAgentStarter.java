package com.csc.test_agent;

import static net.grinder.util.NetworkUtils.getIP;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.NGrinderAgentStarter;
import org.ngrinder.NGrinderAgentStarterParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.csc.test_agent.constants.TestAgentConstants;
import com.csc.test_agent.infrastructure.TestAgentConfig;

import net.grinder.AgentControllerDaemon;

public class TestAgentStarter implements TestAgentConstants {

    private static final Logger LOG = LoggerFactory.getLogger("starter");
    private TestAgentConfig testAgentConfig;

    /**
     * Constructor.
     */
    public TestAgentStarter() {
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
        LOG.info("***************************************************");
        LOG.info("   Start nGrinder Agent ...");
        LOG.info("***************************************************");

        if (StringUtils.isEmpty(System.getenv("JAVA_HOME"))) {
            LOG.info("Hey!! JAVA_HOME env var was not provided. "
                    + "Please provide JAVA_HOME env var before running agent."
                    + "Otherwise you can not execute the agent in the security mode.");
        }

        String controllerIP = getIP(agentConfig.getControllerIP());
        int controllerPort = agentConfig.getControllerPort();
        agentConfig.setControllerHost(controllerIP);
        LOG.info("connecting to controller {}:{}", controllerIP, controllerPort);

        try {
            agentController = new AgentControllerDaemon(agentConfig);
            agentController.run();
        } catch (Exception e) {
            LOG.error("Error while connecting to : {}:{}", controllerIP, controllerPort);
            printHelpAndExit("Error while starting Agent", e);
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
            LOG.error(e.getMessage());
            return;
        }

        System.getProperties().putAll(modeParam.params);
        starter.init();

        final String startMode = modeParam.name();
        if ("stop".equalsIgnoreCase(param.command)) {
            starter.stopProcess(startMode);
            System.out.println("Stop the agent!");
            return;
        }
        starter.checkDuplicatedRun(startMode);
        if (startMode.equalsIgnoreCase("agent")) {
            starter.startAgent();
        } else {
            staticPrintHelpAndExit("Invalid agent.conf, '--mode' must be set as 'agent'.");
        }
    }










}













}
