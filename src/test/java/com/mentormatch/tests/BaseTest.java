package com.mentormatch.tests;

import com.mentormatch.utils.ConfigReader;
import com.mentormatch.utils.DriverManager;
import com.mentormatch.utils.TestPause;
import java.lang.reflect.Method;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {
    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        System.out.println();
        System.out.println("========== STARTING TEST ==========");
        System.out.println("Test: " + method.getName());
        System.out.println("===================================");
        DriverManager.initializeDriver();
        driver = DriverManager.getDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        try {
            TestPause.forMilliseconds(ConfigReader.getLong("scenario.pause.ms", 0));
        } finally {
            DriverManager.quitDriver();
            System.out.println("========== ENDING TEST ============");
        }
    }
}
