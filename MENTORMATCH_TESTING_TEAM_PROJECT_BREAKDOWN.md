# MentorMatch Testing Team Project Breakdown

Prepared from the current source code on 2026-06-21.

This document is intended for manual testers, automation testers, API testers, database testers, and anyone creating regression or smoke suites for MentorMatch. It focuses on what the application does, which frontend URLs and backend APIs exist, what data changes should be verified, and which edge cases matter most.

## 1. Project Summary

MentorMatch is a mentor-student connection platform.

The main business flow is:

1. A user registers or logs in with email and password.
2. Registration creates either a student profile or a mentor profile. Admin registration is blocked by the backend.
3. Students complete their profile, browse available mentors, filter mentors by industry, minimum rating, and skill, then view mentor details.
4. Students request mentoring sessions with a topic, plan type, occurrence count, scheduled date/time, and duration.
5. The backend creates a `PENDING` session and sends a notification to the mentor.
6. Mentors manage their profile, skills, hourly rate, availability, incoming session requests, reviews, and notifications.
7. Mentors accept sessions with a meeting link, reject sessions with a reason, cancel sessions with a reason, or mark sessions as completed.
8. Students view session status, cancel sessions, and submit reviews after completed sessions.
9. Reviews update the mentor's average rating and create mentor notifications.
10. Admin users view platform stats, users, sessions, reviews, top mentors, recent sessions, and can broadcast notifications, toggle users, update roles, cancel/complete/delete sessions, and delete reviews.

## 2. Current Runtime Facts

Use these facts over older README or document instructions if there is disagreement.

| Area | Current value |
| --- | --- |
| Frontend framework | Angular 17 module-based app |
| Frontend local URL | `http://localhost:4200` when using `npm start` / `ng serve` |
| Frontend production API target | `https://mentormatch-backend-8qq1.onrender.com` from `environment.ts` |
| Frontend development API target | `http://localhost:8081` from `environment.development.ts` |
| Vercel output directory | `dist/mentormatch-frontend/browser` |
| Backend framework | Spring Boot 3.2.5, Java 17, Maven |
| Backend local URL | `http://localhost:8081` |
| Backend API base | `http://localhost:8081/api` locally |
| Database | MySQL/MariaDB-compatible database through Spring Data JPA |
| DB configuration | Environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| Hibernate mode | `spring.jpa.hibernate.ddl-auto=update` |
| Auth | JWT access and refresh tokens |
| Access token storage | Browser `localStorage` key `access_token` |
| Refresh token storage | Browser `localStorage` key `refresh_token` |
| User storage | Browser `localStorage` key `user_data` |
| Access token expiry | `86400000` ms, 24 hours |
| Refresh token expiry | `604800000` ms, 7 days |
| Swagger UI | `/swagger-ui.html` |
| API docs path configured | `/api-docs` |
| WebSocket backend endpoint | `/ws` with SockJS/STOMP |
| Frontend WebSocket call | `${environment.apiUrl}/api/ws`, which does not match backend `/ws` |
| CORS allowed origins | `https://mentormatch-green.vercel.app`, `http://localhost:4200` |
| Docker | Backend-focused Dockerfiles expose port `8081` |

Important environment notes:

- `frontend/src/environments/environment.ts` is production and points directly to Render.
- `frontend/src/environments/environment.development.ts` points to local backend `http://localhost:8081`.
- Angular development serving uses the development file replacement.
- Backend requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` to be set before starting.
- `WebSocketConfig` registers `/ws`, but the student notification service connects to `/api/ws`. Real-time notifications should be tested carefully because this mismatch can break live updates.
- `springdoc.api-docs.path=/api-docs`, but security permits `/v3/api-docs/**`. Verify Swagger/API docs behavior in the target environment.

Seeded users from `DataInitializer`:

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@mentormatch.com` | `Admin@1234` |
| Student | `student@mentormatch.com` | `Student@1234` |
| Mentor | `mentor@mentormatch.com` | `Mentor@1234` |

## 3. User Roles

| Role | Backend role string | Typical landing URL | Main permissions |
| --- | --- | --- | --- |
| Unauthenticated | None | `/auth/login` | Can access login/register and public mentor/review pages only. Protected routes redirect to login. |
| Student | `STUDENT` | `/student/dashboard` or `/student/profile` | Manage own student profile, browse mentors, request sessions, view/cancel own sessions, submit completed-session reviews, view notifications. |
| Mentor | `MENTOR` | `/mentor/dashboard` | Manage mentor profile, skills, hourly rate, availability, incoming sessions, session actions, own reviews, notifications. |
| Admin | `ADMIN` | `/admin/dashboard` | View/administer users, sessions, reviews, stats, top mentors, recent sessions, broadcasts, role updates, and destructive admin actions. |

Testing focus:

- Verify frontend route guards and backend API authorization separately.
- Verify direct URL entry, browser refresh, and API calls with student, mentor, and admin tokens.
- Verify logout clears `access_token`, `refresh_token`, and `user_data`.
- Verify disabled users cannot log in.
- High-risk security note: current backend security requires authentication for `/api/admin/**` but does not restrict those endpoints to `ADMIN`. Test direct admin API calls with student and mentor tokens.

## 4. Frontend URL Matrix

Assume base frontend URL is `http://localhost:4200` for local Angular dev server. Replace the base with the deployed frontend URL for deployed testing.

### Public/Auth URLs

| URL | Component | Allowed roles | Brief working | Backend APIs | Test focus |
| --- | --- | --- | --- | --- | --- |
| `/` | Redirect | Anyone | Empty path redirects to `/auth/login`. | None | Verify first app load lands on login. |
| `/auth/login` | `LoginComponent` | Anyone | User logs in with email and password. On success, token/user data are saved and user is routed by role. Student login fetches `/api/students/me`; blank headline routes to `/student/profile`, otherwise `/student/dashboard`. | `POST /api/auth/login`, student also calls `GET /api/students/me` | Valid login, invalid email/password, disabled account, empty fields, token storage, role redirect, student profile redirect logic. Note: student profile fetch error routes to `/students/dashboard`, which is not a defined route. |
| `/auth/register` | `RegisterComponent` | Anyone | User registers with full name, email, password, and role. Frontend validates required fields, email format, password length, and role. Backend creates user plus role-specific profile. | `POST /api/auth/register` | Required fields, duplicate email, invalid role, admin registration rejection, role-specific profile creation. Backend does not use validation annotations for register fields, so API-level blank/invalid values should be tested. |
| `/**` | Redirect | Anyone | Unknown routes redirect to `/auth/login`. | None | Verify bad URLs do not show a blank page. |

### Student URLs

| URL | Component | Allowed roles | Brief working | Backend APIs | Test focus |
| --- | --- | --- | --- | --- | --- |
| `/student` | Redirect | Student | Child route redirects to `/student/dashboard`. | None | Verify parent route redirect and guard behavior. |
| `/student/dashboard` | `StudentDashboardComponent` | Student | Loads profile, student sessions, status counts, recent sessions, and profile greeting. | `GET /api/students/me`, `GET /api/sessions/my` | Profile load, no-profile fallback, recent sessions, counts for pending/accepted/completed, error states. |
| `/student/profile` | `StudentProfileComponent` | Student | Loads and edits headline, current role, goals, interest tags; can delete account. Save redirects to dashboard. | `GET /api/students/me`, `PUT /api/students/me`, `DELETE /api/students/me` | Required fields, max lengths, tag handling, save redirect, delete logs user out, database profile update. |
| `/student/mentors` | `BrowseMentorsComponent` | Student | Lists available active mentors, supports skill/industry/minRating filters and availability frontend filter, shows recommended mentors. | `GET /api/mentors?industry=&minRating=&skill=`, `GET /api/mentors/recommended` | Empty mentor list, filters, available toggle, recommended list, inactive/unavailable mentors excluded. |
| `/student/mentors/:id` | `MentorDetailComponent` | Student | Loads one mentor profile and allows booking a session with topic, message, plan type, occurrences, date, time, and duration. | `GET /api/mentors/{id}`, `POST /api/sessions` | Missing mentor, unavailable mentor behavior, booking validation, invalid date/time, invalid occurrence count, request success, notification to mentor. |
| `/student/sessions` | `MySessionsComponent` | Student | Lists student's sessions with tabs for all/pending/accepted/completed/rejected/cancelled. Allows cancelling pending/accepted sessions and reviewing completed sessions. | `GET /api/sessions/my`, `PATCH /api/sessions/{id}/cancel` | Own sessions only, status filters/counts, cancel flow, review button visibility, reload after action. |
| `/student/sessions/:id` | `MySessionsComponent` | Student | Same component as sessions list; route parameter is not used for detail loading. | `GET /api/sessions/my` | Verify direct session URL does not imply a separate detail page. |
| `/student/submit-review/:id` | `SubmitReviewComponent` | Student | Student submits 1-5 star review and optional comment for a completed session. Redirects to sessions after success. | `POST /api/reviews/sessions/{sessionId}` | Rating required, only completed session, only session student, duplicate review blocked, mentor rating recalculated. |
| `/student/notifications` | `NotificationsComponent` | Student | Loads notifications, marks one/all as read, navigates to notification link. | `GET /api/notifications`, `PATCH /api/notifications/{id}/read`, `PATCH /api/notifications/read-all` | Unread count updates, mark-one authorization, mark-all behavior, bad link handling. |

### Mentor URLs

| URL | Component | Allowed roles | Brief working | Backend APIs | Test focus |
| --- | --- | --- | --- | --- | --- |
| `/mentor` | No child default route | Mentor | Parent route loads mentor module, but the module does not define an empty redirect. | None | Verify direct `/mentor` behavior; it may render blank unless routed to `/mentor/dashboard`. |
| `/mentor/dashboard` | `MentorDashboardComponent` | Mentor | Single dashboard with tabs for dashboard, sessions, reviews, profile, and notifications. Loads profile, sessions, and unread count on init. | `GET /api/mentors/me`, `GET /api/sessions/mentor`, `GET /api/notifications/unread-count` | Dashboard load, blank profile state, tab switching, session counts, review counts, notification count. |
| `/mentor/dashboard` profile tab | `MentorDashboardComponent` | Mentor | Mentor edits industry, hourly rate, bio, skills, availability, and can delete mentor profile. | `GET /api/mentors/me`, `PUT /api/mentors/me`, `PATCH /api/mentors/me/availability?isAvailable=`, `DELETE /api/mentors/me` | Required industry/bio, hourly rate bounds, skill tags, availability toggle, delete profile and effect on student search. |
| `/mentor/dashboard` sessions tab | `MentorDashboardComponent` | Mentor | Mentor accepts with meeting link, rejects with reason, cancels with reason, or marks complete. | `GET /api/sessions/mentor`, `PATCH /api/sessions/{id}/accept`, `PATCH /api/sessions/{id}/reject`, `PATCH /api/sessions/{id}/mentor-cancel`, `PATCH /api/sessions/{id}/complete` | Meeting link required in UI, reject/cancel reason required in UI, reload after action, status updates, student notification creation. |
| `/mentor/dashboard` reviews tab | `MentorDashboardComponent` | Mentor | Loads reviews for the logged-in mentor and computes average rating display. | `GET /api/reviews/mentor/me` | Own reviews only, empty reviews, average calculation, stale rating mismatch. |
| `/mentor/dashboard` notifications tab | `MentorDashboardComponent` | Mentor | Loads notifications, marks read, and switches tabs or navigates based on link. | `GET /api/notifications`, `PATCH /api/notifications/{id}/read`, `PATCH /api/notifications/read-all` | Read/unread state, link routing, mark-all, notification ordering. |
| `/mentor/notifications` | `MentorNotificationsComponent` | Mentor | Separate notifications route in mentor module. | Notification APIs | Verify route loads and data matches dashboard notification tab. |

Frontend/API mismatch to test:

- `SessionManagementService.cancelOccurrence()` calls `PATCH /api/sessions/occurrences/{occurrenceId}/cancel`, but `SessionController` does not expose this endpoint. Test any UI control that calls occurrence cancellation; it should currently return 404 unless the backend has been changed outside this source.

### Admin URLs

| URL | Component | Allowed roles | Brief working | Backend APIs | Test focus |
| --- | --- | --- | --- | --- | --- |
| `/admin` | Redirect | Admin | Child route redirects to `/admin/dashboard`. | None | Verify parent redirect and guard behavior. |
| `/admin/dashboard` | `AdminDashboardComponent` | Admin | Admin shell/sidebar; redirects child path to overview. | Admin APIs below | Verify logout clears localStorage and route guards. |
| `/admin/dashboard/overview` | `OverviewComponent` | Admin | Shows platform stats, top mentors, and recent sessions. | `GET /api/admin/stats`, `GET /api/admin/mentors/top5`, `GET /api/admin/sessions/recent` | Counts, empty data, rating display, recent ordering. |
| `/admin/dashboard/students` | `StudentsComponent` | Admin | Student management view. | `GET /api/admin/users?role=STUDENT`, `PATCH /api/admin/users/{id}/toggle`, user detail APIs | Filter by role, active toggle, inactive login blocked, direct API access control. |
| `/admin/dashboard/mentors` | `MentorsComponent` | Admin | Mentor management view. | `GET /api/admin/users?role=MENTOR`, `GET /api/admin/users/{id}`, `PATCH /api/admin/users/{id}/toggle`, `PATCH /api/admin/users/{id}/role` | Role filtering, role updates, deactivation effect on mentor search. |
| `/admin/dashboard/sessions` | `SessionsComponent` | Admin | Lists/filter sessions and supports cancel, force-complete, and delete. | `GET /api/admin/sessions`, `GET /api/admin/sessions?status=`, `GET /api/admin/sessions/{id}`, `PATCH /api/admin/sessions/{id}/cancel`, `PATCH /api/admin/sessions/{id}/force-complete`, `DELETE /api/admin/sessions/{id}` | Status filtering, action side effects, completed cancel blocked, delete behavior. |
| `/admin/dashboard/reviews` | `ReviewsComponent` | Admin | Lists and deletes reviews. | `GET /api/admin/reviews`, `DELETE /api/admin/reviews/{id}` | Delete review, mentor average rating recalculation, empty states. |
| `/admin/dashboard/broadcast` | `BroadcastComponent` | Admin | Sends notification broadcast to student, mentor, or all non-admin users. | `POST /api/admin/notifications/broadcast` | Required fields, target audience, database notifications, WebSocket delivery. |

## 5. Navigation Behavior

App shell:

- `AppComponent` renders only `<router-outlet>`.
- There is no global bottom navigation.
- Student navigation is implemented by `StudentNavbarComponent`.
- Mentor dashboard uses internal tabs inside `MentorDashboardComponent`.
- Admin dashboard uses `AdminDashboardComponent.menuItems` for dashboard sections.

Student navigation items:

- `/student/dashboard`
- `/student/mentors`
- `/student/sessions`
- `/student/notifications`
- `/student/profile`

Admin navigation items:

- `/admin/dashboard/overview`
- `/admin/dashboard/students`
- `/admin/dashboard/mentors`
- `/admin/dashboard/sessions`
- `/admin/dashboard/reviews`
- `/admin/dashboard/broadcast`

Test focus:

- Verify nav changes after login/logout.
- Verify active item state after redirects and refresh.
- Verify direct protected URLs without token redirect to `/auth/login`.
- Verify wrong-role direct URLs redirect users to their own dashboard.
- Verify `/mentor` direct route behavior because no empty child redirect exists.

## 6. Frontend Architecture

| Area | Files / behavior |
| --- | --- |
| App shell | `app.component.ts` renders `router-outlet` only. |
| Routing | `app-routing.module.ts` defines lazy routes for `auth`, `student`, `mentor`, and `admin`. |
| Auth state | `AuthService` stores access token, refresh token, and user data in `localStorage`. |
| HTTP auth | `JwtInterceptor` attaches `Authorization: Bearer <token>` to non-auth API requests. |
| Token refresh | On `401`, interceptor calls `POST /api/auth/refresh-token`, queues concurrent requests, and logs out on refresh failure. |
| Guards | `AuthGuard` requires any access token. `RoleGuard` compares stored `user_data.role` with route `data.role`. |
| Student profile | `StudentProfileComponent` converts interest tags to a comma-separated string for backend storage. |
| Mentor profile | `MentorDashboardComponent` stores skills as tag list and saves them through `PUT /api/mentors/me`. |
| Mentor search | `BrowseMentorsComponent` sends backend filters for skill, industry, and min rating, then applies availability client-side. |
| Notifications | Student notification service uses HTTP plus STOMP over SockJS. Mentor notification service uses HTTP only in the current code path. |
| Charts | Chart.js is installed and likely used in admin/dashboard visuals. |

## 7. Backend Architecture

| Layer | Main packages | Responsibility |
| --- | --- | --- |
| Entry point | `com.mentormatch.app.MentorMatchApplication` | Starts Spring Boot app. |
| Config | `config`, `security`, `filter` | CORS, Spring Security, JWT auth filter, password encoder, WebSocket, OpenAPI, seed data. |
| Auth | `AuthController`, `AuthService` | Register, login, refresh tokens, create role-specific profiles. |
| Users/profiles | `StudentController`, `MentorController`, services | Student profile CRUD, mentor profile CRUD, mentor search and recommendations. |
| Sessions | `SessionController`, `SessionService` | Book session, list student/mentor sessions, accept/reject/cancel/complete sessions. |
| Reviews | `ReviewController`, `ReviewService` | Submit completed-session reviews, fetch mentor/session reviews, recalculate mentor rating. |
| Notifications | `NotificationController`, `NotificationService`, `WebSocketConfig` | Persist notifications, mark read, unread count, push to user queue. |
| Admin | `AdminController`, `AdminService` | User/session/review management, stats, top mentors, recent sessions, broadcasts. |
| Persistence | `entity`, `repository` | JPA entities and Spring Data queries. |

Security summary:

- `/api/auth/**` is public.
- `/api/mentors`, `/api/mentors/**`, and `/api/reviews/mentors/**` are public at the HTTP security layer.
- Mentor self-profile endpoints use method security with `hasRole('MENTOR')`.
- Student self-profile endpoints use method security with `hasAuthority('ROLE_STUDENT')`.
- `/api/reviews/mentor/me` uses method security with `ROLE_MENTOR`.
- All other endpoints require authentication only.
- Current backend does not add role restrictions to `/api/admin/**` in `SecurityConfig` or `AdminController`.
- Current session endpoints do not declare `@PreAuthorize` and several service methods do not verify that the authenticated user owns the session they are modifying.

## 8. Backend API Matrix

Use `Authorization: Bearer <access_token>` for all non-public requests.

### Auth

| Method | Endpoint | Role | Purpose | Main validations / expected behavior |
| --- | --- | --- | --- | --- |
| POST | `/api/auth/register` | Public | Create student or mentor account. | Duplicate email rejected. Role must be `STUDENT` or `MENTOR`; `ADMIN` registration is rejected. Creates `StudentProfile` or `MentorProfile`. Backend does not currently enforce email format/password length via annotations. |
| POST | `/api/auth/login` | Public | Login by email/password. | Uses Spring AuthenticationManager. Disabled accounts return forbidden. Bad credentials return unauthorized. Returns access token, refresh token, role, fullName, email. |
| POST | `/api/auth/refresh-token` | Public | Issue new access/refresh tokens. | Refresh token must validate; expired/invalid token rejected. |

### Students

| Method | Endpoint | Role | Purpose | Test focus |
| --- | --- | --- | --- | --- |
| GET | `/api/students/me` | Student | Fetch current student's profile. | Returns fullName/email and student fields. If profile is absent, service returns a temporary blank object without saving it. |
| PUT | `/api/students/me` | Student | Create/update student profile. | Headline, goals, interests, current role persistence. Check max length at UI and DB. |
| DELETE | `/api/students/me` | Student | Delete student profile and user account. | Verify user/profile removed, token becomes unusable, related sessions/reviews behavior with FK constraints. |

### Mentors

| Method | Endpoint | Role | Purpose | Test focus |
| --- | --- | --- | --- | --- |
| GET | `/api/mentors?industry=&minRating=&skill=` | Public by backend, used by student UI | List available active mentors with optional filters. | Only `isAvailable=true` and active users. Industry and skill are exact match. Rating uses greater-than-or-equal. |
| GET | `/api/mentors/recommended` | Public by backend, used by student UI | Top 5 available active mentors by rating. | Ordering, empty list, unavailable/inactive exclusion. |
| GET | `/api/mentors/{id}` | Public by backend, used by student UI | Fetch mentor profile by mentor profile ID. | 404/missing ID, profile details, public data exposure. |
| GET | `/api/mentors/me` | Mentor | Fetch logged-in mentor profile. | Requires `ROLE_MENTOR`; profile missing error. |
| PUT | `/api/mentors/me` | Mentor | Create/update own mentor profile. | Bio, industry, hourlyRate, skills. Existing profile updated or created if absent. |
| PATCH | `/api/mentors/me/availability?isAvailable=true` | Mentor | Toggle availability. | Hidden from student mentor list when false. |
| DELETE | `/api/mentors/me` | Mentor | Delete mentor profile. | Verify profile removed and mentor disappears from search; user account remains. |

### Sessions

| Method | Endpoint | Role | Purpose | Test focus |
| --- | --- | --- | --- | --- |
| POST | `/api/sessions` | Authenticated by backend, intended Student | Book a session using mentor profile ID. | Required topic, planType, totalOccurrences, mentorId, scheduledAt, duration. Invalid plan type, bad date parsing, mentor not found, notification to mentor. Test non-student token because backend does not explicitly restrict role. |
| GET | `/api/sessions/my` | Authenticated by backend, intended Student | List sessions where current user is student. | Own sessions only, ordering expectations, empty list. |
| GET | `/api/sessions/mentor` | Authenticated by backend, intended Mentor | List sessions where current user is mentor. | Own mentor sessions only, empty list. |
| PATCH | `/api/sessions/{id}/accept` body `{ "meetingLink": "..." }` | Authenticated by backend, intended Mentor | Accept session and optionally save meeting link. | Backend accepts even blank link; UI requires link. Test wrong mentor/student/admin token because service does not check owner. |
| PATCH | `/api/sessions/{id}/reject` body `{ "reason": "..." }` | Authenticated by backend, intended Mentor | Reject session and save optional cancellation reason. | Backend allows blank reason; UI requires reason. Test already completed/cancelled sessions and wrong owner. |
| PATCH | `/api/sessions/{id}/cancel` | Authenticated by backend, intended Student | Student cancels session. | Backend does not check student ownership. Verify wrong student/mentor can or cannot cancel. |
| PATCH | `/api/sessions/{id}/mentor-cancel` body `{ "reason": "..." }` | Authenticated by backend, intended Mentor | Mentor cancels session with reason. | Backend does not check mentor ownership. Test reason persistence and notifications. |
| PATCH | `/api/sessions/{id}/complete` | Authenticated by backend, intended Mentor | Mark session complete and notify student to review. | Backend does not check mentor ownership or previous status. Test completion from pending/rejected/cancelled. |

Frontend mismatch:

| Method | Endpoint | Current status | Test focus |
| --- | --- | --- | --- |
| PATCH | `/api/sessions/occurrences/{occurrenceId}/cancel` | Called by frontend mentor service, missing in backend controller | Should return 404 unless implemented elsewhere. |

### Reviews

| Method | Endpoint | Role | Purpose | Test focus |
| --- | --- | --- | --- | --- |
| POST | `/api/reviews/sessions/{sessionId}` | Authenticated, service restricts to session student | Submit review after completed session. | Rating 1-5, completed-only, student-only, duplicate prevention, mentor average rating recalculation, mentor notification. |
| GET | `/api/reviews/mentor/me` | Mentor | Get reviews for logged-in mentor. | Own reviews only, empty state. |
| GET | `/api/reviews/mentors/{mentorId}` | Public by backend | Public reviews for mentor profile ID. | Profile ID translated to mentor user ID; missing profile; ordering expectations. |
| GET | `/api/reviews/sessions/{sessionId}` | Authenticated by backend | Reviews for a session. | Access control/data exposure: current code does not enforce session ownership. |

### Notifications

| Method | Endpoint | Role | Purpose | Test focus |
| --- | --- | --- | --- | --- |
| GET | `/api/notifications` | Authenticated | List current user's notifications newest first. | Own notifications only, ordering, empty state. |
| GET | `/api/notifications/unread-count` | Authenticated | Count unread notifications. | Count updates after mark-one and mark-all. |
| PATCH | `/api/notifications/read-all` | Authenticated | Mark all current user's notifications as read. | Only current user's rows update. |
| PATCH | `/api/notifications/{id}/read` | Authenticated owner | Mark one notification as read. | Unauthorized when notification belongs to another user. |

WebSocket:

| Endpoint | Client behavior | Test focus |
| --- | --- | --- |
| `/ws` | Backend SockJS/STOMP endpoint, broker destinations `/user`, `/topic`, user queue `/user/queue/notifications` | Verify real-time delivery by user. Also verify frontend's `/api/ws` mismatch. |

### Admin

| Method | Endpoint | Role intended | Purpose | Test focus |
| --- | --- | --- | --- | --- |
| GET | `/api/admin/users?role=STUDENT` | Admin | List users, optional role filter. | Role filter, active flag, data exposure. Test with non-admin tokens. |
| GET | `/api/admin/users/{id}` | Admin | User detail. | Missing user, role, active flag. Test with non-admin tokens. |
| PATCH | `/api/admin/users/{id}/toggle` | Admin | Activate/deactivate user. | Disabled login blocked, active mentor hidden from search when disabled. Test with non-admin tokens. |
| PATCH | `/api/admin/users/{id}/role` | Admin | Change user role. | Role escalation risk; invalid role; profile consistency after role change. Test with non-admin tokens. |
| DELETE | `/api/admin/users/{id}` | Admin | Hard delete user. | FK behavior with profiles/sessions/reviews. Test with non-admin tokens. |
| GET | `/api/admin/stats` | Admin | Platform counts. | Total users, mentors, students, sessions, completed, pending. Test with non-admin tokens. |
| GET | `/api/admin/sessions?status=PENDING` | Admin | List sessions, optional status filter. | Status filter, occurrence data, lazy loading. Test with non-admin tokens. |
| GET | `/api/admin/sessions/{id}` | Admin | Session detail. | Missing session, occurrence display. |
| GET | `/api/admin/mentors/top5` | Admin | Top 5 mentors by rating. | Available active mentors only, ordering. |
| GET | `/api/admin/sessions/recent` | Admin | Recent sessions. | Last 5 sessions by createdAt. |
| PATCH | `/api/admin/sessions/{id}/cancel` | Admin | Cancel session and occurrences unless completed. | Completed session blocked, occurrences updated. |
| PATCH | `/api/admin/sessions/{id}/force-complete` | Admin | Force session and occurrences to completed. | Review now allowed, statuses updated. |
| DELETE | `/api/admin/sessions/{id}` | Admin | Delete session. | Cascade/orphan review behavior. |
| GET | `/api/admin/reviews` | Admin | List all reviews. | Review data, session/user names, empty state. |
| DELETE | `/api/admin/reviews/{id}` | Admin | Delete review and recalculate mentor rating. | Rating recalculation to 0 when no reviews. |
| POST | `/api/admin/notifications/broadcast` | Admin | Broadcast notification to student, mentor, or all non-admin users. | Target audience, DB rows, unread counts, WebSocket delivery. |

## 9. Database Tables and What to Verify

| Table/entity | Purpose | Key fields to verify |
| --- | --- | --- |
| `users` | Authenticated users. | `id`, `full_name`, `email`, `password`, `role`, `is_active`, `created_at`. Email is unique. |
| `student_profiles` | Student-specific profile. | `id`, `user_id`, `headline`, `goals`, `interests`, `current_role`, `total_sessions`. One profile per user. |
| `mentor_profiles` | Mentor-specific profile. | `id`, `user_id`, `bio`, `industry`, `hourly_rate`, `is_available`, `rating`. One profile per user. |
| `mentor_skills` | Element collection for mentor skills. | `mentor_profile_id`, `skill`. Verify add/remove/duplicates. |
| `sessions` | Session booking header. | `id`, `mentor_id`, `student_id`, `topic`, `message`, `status`, `plan_type`, `total_occurrences`, `scheduled_at`, `duration_minutes`, `meeting_link`, `cancellation_reason`, `created_at`. |
| `session_occurrences` | Occurrence rows for recurring sessions. | `session_id`, `scheduled_at`, `duration_minutes`, `meeting_link`, `status`. Current booking service does not create occurrence rows. |
| `reviews` | Student review of mentor after session. | `session_id`, `reviewer_id`, `reviewee_id`, `rating`, `comment`, `reviewer_role`, `created_at`. Unique constraint on session plus reviewer. |
| `notifications` | User notification feed. | `user_id`, `title`, `message`, `link`, `is_read`, `created_at`. |

Database verification after registration:

1. `users` row exists with encoded password and correct role.
2. Student registration creates one `student_profiles` row.
3. Mentor registration creates one `mentor_profiles` row with `is_available=true`, `rating=0.0`, and empty skills collection.
4. Admin registration is rejected and no admin row is created from public registration.

Database verification after session booking:

1. `sessions` row exists with authenticated student and selected mentor user.
2. Session status is `PENDING`.
3. `plan_type`, `total_occurrences`, `scheduled_at`, and `duration_minutes` match request.
4. A notification row is created for the mentor.
5. Current code does not create `session_occurrences` during normal booking; document as expected/requirement gap if recurring plans should create occurrences.

Database verification after mentor action:

1. Accept sets status `ACCEPTED` and persists meeting link if provided.
2. Reject sets status `REJECTED` and optional `cancellation_reason`.
3. Mentor cancel sets status `CANCELLED` and optional `cancellation_reason`.
4. Complete sets status `COMPLETED`.
5. Notifications are created for the student after each action.

Database verification after review:

1. `reviews` row exists with session, reviewer student, reviewee mentor, rating, comment, and reviewer role `STUDENT`.
2. Duplicate review for the same session and reviewer is blocked.
3. `mentor_profiles.rating` updates to average of review ratings for that mentor user.
4. Notification row is created for the mentor.

## 10. Critical End-to-End Test Flows

### E2E-01 Student Registers, Completes Profile, and Reaches Dashboard

Preconditions:

- Backend is running with database and JWT environment variables.
- Frontend is running with development API target.

Steps:

1. Open `/auth/register`.
2. Register as `STUDENT` with a unique email.
3. Login with the new email and password.
4. Confirm first login routes to `/student/profile` if headline is blank.
5. Fill headline, current role, goals, and interests.
6. Save profile.
7. Verify redirect to `/student/dashboard`.
8. Verify `users` and `student_profiles` rows.

Expected:

- Student account exists and is active.
- Tokens are stored in localStorage.
- Profile fields persist.
- Dashboard loads profile and session stats without console/network blockers.

### E2E-02 Student Browses Mentors and Requests Session

Preconditions:

- At least one active available mentor profile exists with industry, skills, and rating.

Steps:

1. Login as student.
2. Open `/student/mentors`.
3. Filter by industry, skill, and minimum rating.
4. Open mentor detail.
5. Submit a session request with topic, plan type, occurrence count, scheduled date/time, and duration.
6. Verify success message.
7. Check student session list and mentor notification.

Expected:

- Search returns only available active mentors.
- Session row is created as `PENDING`.
- Mentor receives notification.

### E2E-03 Mentor Accepts Session

Preconditions:

- A pending session exists for the mentor.

Steps:

1. Login as mentor.
2. Open `/mentor/dashboard` and switch to sessions tab.
3. Accept session with a meeting link.
4. Login as student and open `/student/sessions`.
5. Verify status and meeting link.
6. Check notification row for student.

Expected:

- Session status becomes `ACCEPTED`.
- Meeting link persists.
- Student notification is created.

### E2E-04 Mentor Rejects or Cancels Session

Preconditions:

- A pending or accepted session exists for the mentor.

Steps:

1. Login as mentor.
2. Reject a pending session with a reason, or cancel an accepted session with a reason.
3. Login as student and verify session status and reason.
4. Verify notification row.

Expected:

- Status becomes `REJECTED` or `CANCELLED`.
- Cancellation reason persists when provided.
- Student is notified.

### E2E-05 Mentor Completes Session and Student Reviews

Preconditions:

- An accepted session exists.

Steps:

1. Login as mentor.
2. Mark session complete.
3. Login as student.
4. Open `/student/sessions` and click review for the completed session.
5. Submit a 1-5 star review and optional comment.
6. Open mentor detail/reviews or admin reviews.
7. Verify mentor average rating updates.

Expected:

- Session status becomes `COMPLETED`.
- Review is saved once.
- Duplicate review is blocked.
- Mentor rating recalculates.
- Mentor receives notification.

### E2E-06 Admin Manages Platform

Preconditions:

- Admin seeded user exists.
- There are students, mentors, sessions, reviews, and notifications.

Steps:

1. Login as admin.
2. Open `/admin/dashboard/overview`.
3. Verify stats, top mentors, and recent sessions.
4. Toggle a user inactive.
5. Attempt login as inactive user.
6. Change a user's role.
7. Cancel, force-complete, or delete a session.
8. Delete a review.
9. Send broadcast notification.

Expected:

- Admin UI actions reflect in DB.
- Disabled users cannot login.
- Review delete recalculates mentor rating.
- Broadcast creates notifications for target audience.
- Direct API authorization is verified separately because backend currently lacks admin-only restrictions.

## 11. Module-Level Testing Breakdown

### Auth and Access

Functional:

- Register student.
- Register mentor.
- Reject admin registration.
- Login by email/password.
- Refresh token after access token expiration.
- Logout from student, mentor, and admin screens.
- Redirect users by role.

Negative:

- Duplicate email.
- Invalid email format through UI and direct API.
- Password below UI minimum through UI and direct API.
- Missing fullName/email/password/role through direct API.
- Invalid role value.
- Disabled user login.
- Missing/expired/tampered JWT.

Security:

- Student direct access to `/admin/dashboard`, `/mentor/dashboard`, and admin APIs.
- Mentor direct access to `/student/dashboard`, `/admin/dashboard`, and admin APIs.
- Admin direct API calls expected to be allowed only for admin, but current backend likely allows any authenticated token.
- Role changes through `/api/admin/users/{id}/role` with non-admin token.

### Mentor Search and Profile

Functional:

- List available mentors.
- Filter by exact industry.
- Filter by exact skill.
- Filter by minimum rating.
- Recommended mentors sorted by rating.
- Mentor updates bio, industry, hourly rate, skills.
- Mentor toggles availability.

Negative:

- Missing mentor ID.
- Unavailable mentor should disappear from list.
- Inactive mentor user should disappear from list.
- Negative hourly rate through direct API.
- Oversized bio/industry values.
- Duplicate skill tags.

### Student Profile

Functional:

- First-login profile completion route.
- Update headline, current role, goals, interests.
- Interest tag add/remove.
- Dashboard uses profile data.

Negative:

- Blank required UI fields.
- Max length violations.
- Delete account when user has sessions/reviews.
- Refresh after profile deletion.

### Session Lifecycle

Functional:

- Student books `SINGLE`, `WEEKLY`, and `MONTHLY` sessions.
- Mentor accepts with meeting link.
- Mentor rejects with reason.
- Student cancels own session.
- Mentor cancels with reason.
- Mentor completes session.
- Admin cancels, force-completes, and deletes sessions.

Negative/security:

- Mentor accepts another mentor's session.
- Student accepts/rejects/completes a session through API.
- Student cancels another student's session.
- Complete a pending/rejected/cancelled session through API.
- Invalid `planType` causing enum conversion error.
- Invalid `scheduledAt` parsing silently leaves schedule null in service.
- Missing backend route for occurrence cancellation.

### Reviews

Functional:

- Student submits review for completed own session.
- Mentor sees own reviews.
- Public mentor review list loads.
- Admin sees all reviews.
- Admin deletes review and rating recalculates.

Negative:

- Review before session is completed.
- Review by non-student or wrong student.
- Duplicate review.
- Rating 0 or 6.
- Long comment.
- Fetch session reviews for sessions the caller does not own.

### Notifications and WebSocket

Functional:

- Session booking notifies mentor.
- Accept/reject/cancel/complete notifies student.
- Review notifies mentor.
- Admin broadcast notifies target users.
- Unread count updates.
- Mark one/all as read.

Negative:

- Mark another user's notification as read.
- Broken notification link.
- WebSocket connection with `/api/ws` vs backend `/ws`.
- WebSocket reconnect after token refresh/logout.

### Admin

Functional:

- Dashboard stats.
- User list and role filter.
- Toggle user active/inactive.
- Change user role.
- Session list/status filter.
- Review list/delete.
- Broadcast target audience.

Negative/security:

- Non-admin token calling every `/api/admin/**` endpoint.
- Delete user with related sessions/reviews.
- Change user to `ADMIN` through API.
- Cancel completed session should fail.
- Force-complete deleted/missing session.

## 12. Automation Recommendations

Automate first:

1. Login smoke for student, mentor, and admin seeded users.
2. Student profile load and update.
3. Mentor search and mentor detail load.
4. Session booking API flow.
5. Mentor accept/reject/cancel/complete API flows.
6. Review submit and duplicate-review API tests.
7. Notification list/unread/read-all tests.
8. Admin API access negative tests with student and mentor tokens.
9. Frontend route guard tests for direct URLs.

Keep manual/exploratory:

- Real-time notification WebSocket behavior.
- Responsive layouts for dashboard/profile/mentor search.
- Admin visual dashboards and chart rendering.
- Browser localStorage/token refresh behavior across tabs.
- Destructive admin workflows with realistic data.

Suggested automation layers:

- UI: Playwright or Selenium for smoke and role navigation.
- API: Postman/Newman or REST Assured for auth, role/security, session, review, and notification flows.
- DB: SQL checks after registration, session actions, review delete/rating recalculation, and broadcasts.
- Performance: JMeter for login, mentor search, session booking, notification list, and admin sessions.
- Accessibility: axe/Lighthouse for login, student dashboard, mentor search/detail, mentor dashboard, and admin dashboard.

## 13. Smoke Test Checklist

Run this before full testing:

1. Frontend loads `/auth/login`.
2. Backend responds to `POST /api/auth/login`.
3. Student seeded login routes to `/student/dashboard` or `/student/profile` based on profile headline.
4. Mentor seeded login routes to `/mentor/dashboard`.
5. Admin seeded login routes to `/admin/dashboard`.
6. Student profile loads.
7. Mentor list loads.
8. Student can create a pending session.
9. Mentor can see the pending session.
10. Mentor can accept the session with a meeting link.
11. Student can see accepted status.
12. Mentor can complete the session.
13. Student can submit a review.
14. Admin overview stats load.
15. Protected URL without token redirects to `/auth/login`.
16. Browser console has no blocking runtime errors.

## 14. High-Risk Areas

Prioritize these because they can create important user-facing or security defects:

- `/api/admin/**` endpoints are not backend-restricted to `ADMIN`; any authenticated user may be able to call them.
- Session action endpoints do not consistently enforce role or ownership in service code.
- Frontend WebSocket URL uses `/api/ws`, while backend registers `/ws`.
- Frontend mentor service calls an occurrence-cancel endpoint that is missing in the backend controller.
- Registration relies mostly on frontend validation; direct API can bypass email/password/fullName validation.
- Student login error fallback routes to `/students/dashboard`, but defined route is `/student/dashboard`.
- Direct `/mentor` route has no empty child redirect.
- `SessionService.bookSession()` catches invalid `scheduledAt` parsing and silently leaves the date null.
- Booking recurring sessions stores `totalOccurrences` but currently does not create `session_occurrences` rows.
- Mentor search filters use exact matches for industry and skill, which can surprise testers/users expecting partial or case-insensitive search.
- Admin role update can change a user's role without creating/deleting the matching student/mentor profile.
- Deleting users/sessions/reviews may hit foreign key or cascade behavior depending on DB constraints.
- `springdoc.api-docs.path=/api-docs` may not match the security permit rule for `/v3/api-docs/**`.
- Production frontend points to Render backend directly; local testing requires development build/file replacement.

## 15. Suggested Test Data

Create or seed:

- One admin.
- One active student with complete profile.
- One active student with blank profile.
- One inactive student.
- One active available mentor with high rating.
- One active available mentor with low rating.
- One unavailable mentor.
- One inactive mentor user.
- Mentors across industries: `FinTech`, `Healthcare`, `Software / IT`, `E-commerce`, `EdTech`, `Gaming`.
- Skills across several mentors, including shared skills and unique skills.
- One pending session.
- One accepted session with meeting link.
- One rejected session with reason.
- One cancelled session with reason.
- One completed session without review.
- One completed session with review.
- Notifications for student and mentor, both read and unread.

## 16. Entry and Exit Criteria

Entry criteria:

- Backend starts with required environment variables.
- Frontend starts with development environment API target for local testing.
- Test database is available and resettable.
- Seeded users are available or equivalent test users are created.
- CORS origin matches the frontend test URL.
- Testers know whether WebSocket notifications are in scope for the current test cycle.

Exit criteria:

- Smoke suite passes.
- Critical auth, route guard, and backend authorization tests pass.
- Student profile, mentor profile, mentor search, session booking, mentor action, review, and notification flows pass.
- Admin workflows pass or known backend authorization gaps are documented as defects.
- Database side effects are verified for registration, session actions, reviews, rating recalculation, and notifications.
- No open blocker or critical defect remains.
- Known implementation gaps such as missing occurrence cancellation and WebSocket path mismatch are documented.

## 17. Useful Commands

Backend local run with required environment variables:

```powershell
cd "C:\Users\2492244\OneDrive - Cognizant\Desktop\MentorMatchTeam\mentormatch\backend"
$env:DB_URL="jdbc:mysql://localhost:3306/mentormatch_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
$env:JWT_SECRET="change-this-to-a-long-test-secret"
mvn spring-boot:run
```

Backend compile:

```powershell
cd "C:\Users\2492244\OneDrive - Cognizant\Desktop\MentorMatchTeam\mentormatch\backend"
mvn -q -DskipTests compile
```

Frontend local run:

```powershell
cd "C:\Users\2492244\OneDrive - Cognizant\Desktop\MentorMatchTeam\mentormatch\frontend"
npm start
```

Frontend development build:

```powershell
cd "C:\Users\2492244\OneDrive - Cognizant\Desktop\MentorMatchTeam\mentormatch\frontend"
npm run build -- --configuration development
```

Frontend production build:

```powershell
cd "C:\Users\2492244\OneDrive - Cognizant\Desktop\MentorMatchTeam\mentormatch\frontend"
npm run build
```

Backend Docker build from project root:

```powershell
cd "C:\Users\2492244\OneDrive - Cognizant\Desktop\MentorMatchTeam\mentormatch"
docker build -t mentormatch-backend .
```

## 18. What Testers Should Report Clearly

For each defect include:

- Frontend URL.
- Backend API request/response if visible.
- Role used.
- Token state: logged in/out, expired/tampered if relevant.
- Test data IDs: user ID, mentor profile ID, student profile ID, session ID, review ID, notification ID.
- Browser and viewport.
- Console error.
- Network status code and response body.
- DB evidence for profile/session/review/notification/rating bugs.
- Whether issue occurs local, deployed, or both.
- Whether issue is frontend-only, backend-only, or a frontend/backend contract mismatch.
