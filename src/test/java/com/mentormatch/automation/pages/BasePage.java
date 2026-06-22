package com.mentormatch.automation.pages;

import com.mentormatch.automation.config.ConfigReader;
import com.mentormatch.automation.utils.TestPause;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    private final long actionPauseMs;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(ConfigReader.getLong("explicit.wait.seconds", 12))
        );
        this.actionPauseMs = ConfigReader.getLong("action.pause.ms", 0);
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        pauseForDemo();
    }

    protected void type(By locator, String value) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(value);
        pauseForDemo();
    }

    protected boolean isVisible(By locator) {
        try {
            return visible(locator).isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    protected void waitForUrlContains(String path) {
        wait.until(ExpectedConditions.urlContains(path));
    }

    protected void pauseForDemo() {
        TestPause.forMilliseconds(actionPauseMs);
    }
}
