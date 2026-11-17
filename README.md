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
2. **Add your database login:** copy `db.properties.example` to `db.properties` in the project folder (next to `pom.xml`) and fill in your MySQL user and password. `db.properties` is git-ignored.
3. **Run the app:**
   ```
   mvn compile exec:java
   ```
   or open the folder in IntelliJ and run `Main`.
4. **Run the tests** (they use `LearnSpace_test` and wipe it before each test):
   ```
   mvn test
   ```

**Where the app keeps its files.** The app looks for its home folder (the one holding `db.properties`; uploaded lessons go into `uploads/` inside it) in this order:
1. `-Dlearnspace.home=/path/to/folder` on the Java command line
2. the `LEARNSPACE_HOME` environment variable
3. the project folder the compiled classes are in (so running from IntelliJ or `mvn` works from any folder)
4. the folder you start the app from, or any folder above it
5. `~/.learnspace` as a last resort

Older lessons saved as `uploads/...` keep working, because stored paths are read relative to this home folder.

## Progress

| Area | Result |
|---|---|
| MoSCoW Must features | 10 / 10 |
| Must requirements (FR-00 to FR-15) | 16 / 16 |
| Should requirements | 2 of 5 built (FR-16 progress, FR-22 validation) |
| JUnit tests | 39 passing at v1.0 (7 test classes, incl. an automated full-flow test); 1 category test added after v1.0 |
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
| FR-20 | Filter by category | Could | Deferred (choosing a category when creating a course was added after v1.0; filtering is not built) |
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

### Sprint milestones

Each sprint ends with its test commit:

| Milestone | Commit | Message |
|---|---|---|
| Sprint 1: accounts + institutions | `f634876` | test(sprint-1): JUnit 5 on separate LearnSpace_test database |
| Sprint 2: courses + lessons | `55455eb` | test(sprint-2): course and lesson service tests |
| Sprint 3: marketplace, viewer, progress | `8da0ae0` | test(sprint-3): enrolment, progress and full end-to-end flow |
| v1.0 (finished build + README) | `363ead9` | docs: README with setup, requirements, test results and timeline |

See the repository's **Releases** page for these milestones as tags, once added.

> **About the commit history:** the code was put on GitHub after the project was finished (first push 17 Nov 2026). It was committed step by step in the order it was built, and each commit's date was set by hand to the day that step was built, taken from the project's build log. Times of day are approximate.

## Changes after v1.0 (17 Nov 2026)

These were found while preparing the repository and are committed with their real date (17 Nov), not backdated:

- **`schema.sql` added.** The v1.0 commit could not create its database from the repository alone; the tables had only been created by hand.
- **Font fix.** v1.0 looked for font files that weren't in `resources/fonts`, so it silently fell back to a default font. `Theme` now loads the files that are there.
- **`pom.xml`.** Java version made consistent (17), JUnit 5 test plugin and `mvn compile exec:java` added.
- **Category picker.** v1.0 showed a Category column that was always "—", because no screen let you choose one. Instructors can now pick a category when creating a course (one new unit test).
- **Start from any folder.** v1.0 looked for `db.properties` and `uploads/` in whatever folder the app was launched from, so it broke when started elsewhere. It now finds its home folder itself (see Setup).

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
