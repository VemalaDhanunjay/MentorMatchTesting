package com.mentormatch.pages;

import com.mentormatch.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    private final By brandTitle = By.xpath("//h1[normalize-space()='MentorMatch']");
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By submitButton = By.id("login-submit-btn");
    private final By registerLink = By.linkText("Register here");
    private final By emailRequiredMessage = By.xpath("//*[normalize-space()='Email is required']");
    private final By passwordRequiredMessage = By.xpath("//*[normalize-space()='Password is required']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        openUrl(ConfigReader.get("base.url"));
    }

    public void openProtectedRouteAsGuest(String route) {
        openRelativePath(route);
    }

    public boolean isDisplayed() {
        waitForUrlContains("/auth/login");
        return isVisible(brandTitle) && isVisible(emailInput) && isVisible(passwordInput);
    }

    public void submitEmptyForm() {
        click(submitButton);
    }

    public void login(String email, String password) {
        type(emailInput, email);
        type(passwordInput, password);
        click(submitButton);
    }

    public void openRegistrationPage() {
        click(registerLink);
    }

    public boolean hasRequiredValidationMessages() {
        return isVisible(emailRequiredMessage) && isVisible(passwordRequiredMessage);
    }
}
