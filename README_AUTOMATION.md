# MentorMatch Automation Starter

This starter uses the stack from your curriculum:

- Selenium WebDriver for browser automation
- TestNG as the test runner
- Page Object Model for reusable page actions
- ExtentReports for HTML execution reports with failure screenshots
- Apache POI dependency included for later Excel-driven data

## First Tests Included

The first automated tests are safe to run against the live site:

1. Open MentorMatch and verify guest redirects to login.
2. Submit empty login form and verify required validation messages.
3. Navigate from login to registration.
4. Verify protected student, mentor, and admin routes redirect guest users to login.

Test class:

```text
src/test/java/com/mentormatch/tests/AuthSmokeTest.java
```

## Run

Install Maven, then run:

```powershell
mvn test
```

Optional overrides:

```powershell
mvn test -Dbrowser=edge
mvn test -Dheadless=true
mvn test -Dbase.url=https://mentormatch-green.vercel.app
```

Reports:

```text
target/extent-report/MentorMatch-Test-Report.html
test-output/index.html
```

## Slow Demo Mode

The tests include visible pauses so you can understand what is running in the browser.

Edit:

```text
src/test/resources/config/config.properties
```

Useful values:

```properties
action.pause.ms=1200
scenario.pause.ms=2500
```

Increase them for classroom/demo runs:

```properties
action.pause.ms=2500
scenario.pause.ms=4000
```

Set both to `0` later when you want faster execution.

The console also prints the test method name before each test starts.

## Test Values

Example login, registration, profile, mentor, and session data are listed in:

```text
TEST_VALUES.md
```

## Next Flow To Add

After this smoke flow passes, add seeded login scenarios for:

- `student@mentormatch.com` / `Student@1234`
- `mentor@mentormatch.com` / `Mentor@1234`
- `admin@mentormatch.com` / `Admin@1234`

Then continue with student session booking, mentor accept/reject, review submission, and admin dashboard checks.
