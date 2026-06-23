package com.mentormatch.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RegisterPage extends BasePage {
    private final By pageSubtitle = By.xpath("//*[normalize-space()='Create your account']");
    private final By fullNameInput = By.id("fullName");
    private final By roleDropdown = By.id("role");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDisplayed() {
        waitForUrlContains("/auth/register");
        return isVisible(pageSubtitle) && isVisible(fullNameInput) && isVisible(roleDropdown);
    }
}
