package com.mentormatch.tests;

import com.mentormatch.pages.LoginPage;
import com.mentormatch.pages.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AuthSmokeTest extends BaseTest {

    @Test(description = "Guest user lands on the login page")
    public void guestLandsOnLoginPage() {
        LoginPage loginPage = new LoginPage(driver);

        loginPage.open();

        Assert.assertTrue(loginPage.isDisplayed(), "Login page was not displayed");
    }

    @Test(description = "Login form shows required field validation")
    public void emptyLoginFormShowsRequiredValidation() {
        LoginPage loginPage = new LoginPage(driver);

        loginPage.open();
        loginPage.submitEmptyForm();

        Assert.assertTrue(
                loginPage.hasRequiredValidationMessages(),
                "Required validation messages were not displayed"
        );
    }

    @Test(description = "User can navigate from login to registration")
    public void userCanNavigateFromLoginToRegistration() {
        LoginPage loginPage = new LoginPage(driver);
        RegisterPage registerPage = new RegisterPage(driver);

        loginPage.open();
        loginPage.openRegistrationPage();

        Assert.assertTrue(registerPage.isDisplayed(), "Registration page was not displayed");
    }

    @Test(
            description = "Protected routes redirect guest users to login",
            dataProvider = "protectedRoutes"
    )
    public void protectedRoutesRedirectGuestUsersToLogin(String route) {
        LoginPage loginPage = new LoginPage(driver);

        loginPage.openProtectedRouteAsGuest(route);

        Assert.assertTrue(loginPage.isDisplayed(), "Guest user was not redirected to login");
    }

    @DataProvider
    public Object[][] protectedRoutes() {
        return new Object[][] {
                {"/student/dashboard"},
                {"/mentor/dashboard"},
                {"/admin/dashboard"}
        };
    }
}
