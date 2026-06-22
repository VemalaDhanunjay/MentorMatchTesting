package com.mentormatch.automation.steps;

import com.mentormatch.automation.config.ConfigReader;
import com.mentormatch.automation.driver.DriverFactory;
import com.mentormatch.automation.utils.TestPause;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Hooks {
    @Before
    public void setUp(Scenario scenario) {
        System.out.println();
        System.out.println("========== STARTING SCENARIO ==========");
        System.out.println("Scenario: " + scenario.getName());
        System.out.println("=======================================");
        DriverFactory.initializeDriver();
    }

    @After
    public void tearDown(Scenario scenario) {
        System.out.println("Scenario status: " + scenario.getStatus());
        System.out.println("========== ENDING SCENARIO ============");
        TestPause.forMilliseconds(ConfigReader.getLong("scenario.pause.ms", 0));
        DriverFactory.quitDriver();
    }
}
