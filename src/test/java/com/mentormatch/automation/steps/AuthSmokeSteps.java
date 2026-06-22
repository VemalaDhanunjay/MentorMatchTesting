package com.mentormatch.automation.steps;

import com.mentormatch.automation.config.ConfigReader;
import com.mentormatch.automation.driver.DriverFactory;
import com.mentormatch.automation.pages.LoginPage;
import com.mentormatch.automation.pages.RegisterPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class AuthSmokeSteps {
    private LoginPage loginPage;
    private RegisterPage registerPage;

    @Given("the user opens the MentorMatch application")
    public void theUserOpensTheMentorMatchApplication() {
        System.out.println("Opening MentorMatch application");
        loginPage = new LoginPage(DriverFactory.getDriver());
        loginPage.open();
    }

    @Then("the user should be on the login page")
    public void theUserShouldBeOnTheLoginPage() {
        System.out.println("Verifying login page is displayed");
        Assert.assertTrue(loginPage.isDisplayed(), "Login page was not displayed");
    }

    @When("the user submits the login form without credentials")
    public void theUserSubmitsTheLoginFormWithoutCredentials() {
        System.out.println("Submitting login form with empty email and password");
        loginPage.submitEmptyForm();
    }

    @Then("login validation errors should be displayed")
    public void loginValidationErrorsShouldBeDisplayed() {
        System.out.println("Verifying required field validation messages");
        Assert.assertTrue(
                loginPage.hasRequiredValidationMessages(),
                "Required validation messages were not displayed"
        );
    }

    @When("the user navigates from login to registration")
    public void theUserNavigatesFromLoginToRegistration() {
        System.out.println("Clicking Register here link");
        loginPage.openRegistrationPage();
    }

    @Then("the registration page should be displayed")
    public void theRegistrationPageShouldBeDisplayed() {
        System.out.println("Verifying registration page is displayed");
        registerPage = new RegisterPage(DriverFactory.getDriver());
        Assert.assertTrue(registerPage.isDisplayed(), "Registration page was not displayed");
    }

    @When("the user opens protected route {string}")
    public void theUserOpensProtectedRoute(String route) {
        System.out.println("Opening protected route as guest: " + route);
        DriverFactory.getDriver().get(ConfigReader.get("base.url") + route);
    }

    @Then("the user should be redirected to login")
    public void theUserShouldBeRedirectedToLogin() {
        System.out.println("Verifying guest user is redirected to login");
        loginPage = new LoginPage(DriverFactory.getDriver());
        Assert.assertTrue(loginPage.isDisplayed(), "Guest user was not redirected to login");
    }
}
