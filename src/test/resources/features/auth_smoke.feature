Feature: MentorMatch authentication smoke checks

  Background:
    Given the user opens the MentorMatch application

  Scenario: Guest lands on the login page
    Then the user should be on the login page

  Scenario: Login form shows required field validation
    When the user submits the login form without credentials
    Then login validation errors should be displayed

  Scenario: User can navigate from login to registration
    When the user navigates from login to registration
    Then the registration page should be displayed

  Scenario Outline: Protected routes redirect guest users to login
    When the user opens protected route "<route>"
    Then the user should be redirected to login

    Examples:
      | route              |
      | /student/dashboard |
      | /mentor/dashboard  |
      | /admin/dashboard   |
