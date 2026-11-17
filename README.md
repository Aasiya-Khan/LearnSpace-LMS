# LearnSpace

**One login, two ways to learn.**
LearnSpace is a Java Swing desktop learning-management system (LMS) backed by MySQL. Institutions run private classroom courses, and their students also learn from an open course marketplace, all in one app.

Built using agile sprints, MoSCoW prioritisation and a functional-requirements (FRD) tracker.

---

## Roles

| Role | What they can do |
|---|---|
| **Institution admin** | Create the institution, share its join code, approve or reject instructors |
| **Instructor** | Join an institution, publish institution-only or public courses with ordered video/PDF lessons |
| **Student** | Join an institution with its code, see its courses, enrol in public courses, track progress |

## Tech stack

Java 17 · Swing + FlatLaf · MySQL 8 · JDBC (MySQL Connector/J) · jBCrypt · Maven · JUnit 5 · Figma (wireframes and prototype)

## Architecture

```
GUI (Swing)  →  Service (rules, permissions, validation)  →  DAO (JDBC, PreparedStatement)  →  MySQL 8
                     model classes passed between layers
```

| Package | Contents |
|---|---|
| `gui` | LoginFrame, RegisterFrame, BaseDashboard, AdminDashboard, InstructorDashboard, StudentDashboard, InstitutionPanel, AddLessonsDialog, ExploreFrame, CourseDetailsFrame, Theme |
| `service` | AuthService, InstitutionService, CourseService, LessonService, FileStorageService, EnrollmentService, ProgressService |
| `dao` | UserDAO, InstitutionDAO, CourseDAO, LessonDAO, EnrollmentDAO, ProgressDAO |
| `model` | User (abstract), Student, Instructor, InstitutionAdmin, Institution, Course, Lesson |
| `database` | DBConnection |

**Security:** passwords stored as salted BCrypt hashes; every query uses PreparedStatement placeholders; role and ownership checks live in the service layer.

## Database

9 tables: Users, Institutions, Institution_Members, Categories, Courses, Lessons, Enrollments, Lesson_Progress, Announcements. Primary and foreign keys, UNIQUE constraints (email, join code, one enrolment per student and course), ENUMs for roles and status, and ON DELETE CASCADE for lessons, enrolments and progress.

## Setup

**Needs:** Java 17 or newer, Maven, MySQL 8, and a computer with a screen (it's a desktop app).

1. **Create the databases** (main + a separate one for tests):
   ```
   mysql -u root -p -e "CREATE DATABASE LearnSpace; CREATE DATABASE LearnSpace_test;"
   mysql -u root -p LearnSpace      < schema.sql
   mysql -u root -p LearnSpace_test < schema.sql
   ```
2. **Add your database login:** copy `db.properties.example` to `db.properties` (same folder as `pom.xml`) and fill in your MySQL user and password. `db.properties` is git-ignored.
3. **Run the app:**
   ```
   mvn compile exec:java
   ```
   or open the folder in IntelliJ and run `Main`.
4. **Run the tests** (they use `LearnSpace_test` and wipe it before each test):
   ```
   mvn test
   ```

Uploaded lesson files are copied to an `uploads/` folder next to `pom.xml` (git-ignored).

## Progress

| Area | Result |
|---|---|
| MoSCoW Must features | 10 / 10 |
| Must requirements (FR-00 to FR-15) | 16 / 16 |
| Should requirements | 2 of 5 built (FR-16 progress, FR-22 validation) |
| JUnit tests | 39 passing (7 test classes, incl. an automated full-flow test) |
| Integration tests | IT-01 to IT-08 passed |

### Requirements

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-00 | Connect to MySQL via JDBC | Must | Done |
| FR-01 | Register with role | Must | Done |
| FR-02 | Log in with hashed password, open role dashboard | Must | Done |
| FR-03 | Admin creates institution | Must | Done |
| FR-04 | Unique 8-character join code | Must | Done |
| FR-05 | Instructor requests to join (pending) | Must | Done |
| FR-06 | Admin approves / rejects | Must | Done |
| FR-07 | Student joins with code | Must | Done |
| FR-08 | Create public or institution course | Must | Done |
| FR-09 | Only active members publish institution courses | Must | Done |
| FR-10 | Ordered lessons | Must | Done |
| FR-11 | Video / PDF upload | Must | Done |
| FR-12 | Institution courses visible only to active members | Must | Done |
| FR-13 | Public marketplace | Must | Done |
| FR-14 | Enrol, no duplicates | Must | Done |
| FR-15 | Open a lesson's video or PDF | Must | Done |
| FR-16 | Mark complete, course progress % | Should | Done |
| FR-17 | Instructor views enrolled students | Should | Skipped |
| FR-18 | Admin views members and courses | Should | Skipped |
| FR-19 | Search marketplace by title | Should | Skipped |
| FR-20 | Filter by category | Could | Deferred |
| FR-21 | Institution announcements | Could | Deferred |
| FR-22 | Clear validation and error messages | Should | Done |

## Timeline

| Phase | Planned | Actual |
|---|---|---|
| Objectives, MoSCoW, FRD | 6–7 Nov | 5 Nov |
| Database | 8 Nov | 5–6 Nov |
| Sprint 1: accounts + institutions | 9 Nov | 6–7 Nov |
| Figma wireframes + prototype | 7–8 Nov | 8 Nov |
| Sprint 2: courses + lessons | 10 Nov | 8 Nov |
| Sprint 3: marketplace, viewer, progress | 11 Nov | 8–9 Nov |
| Integration testing | 12–13 Nov | 9–10 Nov |
| Final demo | 14 Nov | 14 Nov |

Sprint milestones are tagged: `sprint-1`, `sprint-2`, `sprint-3`, `v1.0`.

> **About the commit history:** the code was uploaded to GitHub on 15 Nov 2026, after the project was finished. It was committed step by step in the order it was built, and each commit's date was set to the day that step was built, taken from the project's build log. Times of day are approximate.

## Known limitations

- Runs on one computer; lesson files are stored in a local `uploads/` folder
- Desktop only (Swing), so it can't be hosted on the web as-is
- Institution access is all-or-nothing: an active member sees all of that institution's courses
- One institution per admin and per user
- Deleting a course leaves its files in `uploads/` (database rows are removed by cascade)
- No edit/delete screens for courses or lessons, no password reset
- Videos open in the computer's default player

## Future work

- The skipped Shoulds: marketplace search, admin member list, instructor's student list
- Category filter, announcements, edit/delete courses, file clean-up on delete
- A double-click installer (jpackage)
- A web version: React front end, Spring Boot API reusing the service and DAO layers, hosted MySQL and cloud file storage
