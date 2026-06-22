# MentorMatch Example Test Values

Use these values while creating the next Selenium/Cucumber flows.

## Seeded Login Users

| Role | Email | Password | Expected landing |
| --- | --- | --- | --- |
| Student | `student@mentormatch.com` | `Student@1234` | `/student/dashboard` or `/student/profile` |
| Mentor | `mentor@mentormatch.com` | `Mentor@1234` | `/mentor/dashboard` |
| Admin | `admin@mentormatch.com` | `Admin@1234` | `/admin/dashboard` |

## Invalid Login Values

| Case | Email | Password | Expected result |
| --- | --- | --- | --- |
| Empty form | empty | empty | Email and password required messages |
| Invalid email format | `wrongemail` | `Student@1234` | Valid email message |
| Short password | `student@mentormatch.com` | `123` | Minimum 6 characters message |
| Wrong credentials | `student@mentormatch.com` | `Wrong@123` | Login failed server error |

## Sample Registration Values

Use a unique email each time to avoid duplicate-email failures.

| Field | Student example | Mentor example |
| --- | --- | --- |
| Full Name | `Test Student One` | `Test Mentor One` |
| Email | `student.test001@example.com` | `mentor.test001@example.com` |
| Password | `Password@123` | `Password@123` |
| Role | `STUDENT` | `MENTOR` |

## Sample Student Profile Values

| Field | Example |
| --- | --- |
| Headline | `Aspiring full stack developer` |
| Current Role | `Final year engineering student` |
| Goals | `Improve Java, Spring Boot, and interview preparation` |
| Interests | `Java, Selenium, Spring Boot, Testing` |

## Sample Mentor Profile Values

| Field | Example |
| --- | --- |
| Industry | `Software / IT` |
| Hourly Rate | `500` |
| Bio | `I help students prepare for software testing and backend interviews.` |
| Skills | `Java, Selenium, TestNG, API Testing` |

## Sample Session Booking Values

| Field | Example |
| --- | --- |
| Topic | `Selenium framework guidance` |
| Message | `I need help understanding Page Object Model and test data handling.` |
| Plan Type | `SINGLE` |
| Occurrences | `1` |
| Duration | `60` minutes |
| Meeting Link | `https://meet.google.com/test-session-link` |

