-- LearnSpace schema: 9 tables + starter categories (MySQL 8)
-- Run once per database:
--   mysql -u root -p -e "CREATE DATABASE LearnSpace; CREATE DATABASE LearnSpace_test;"
--   mysql -u root -p LearnSpace      < schema.sql
--   mysql -u root -p LearnSpace_test < schema.sql

-- 1. USERS
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('INSTITUTION_ADMIN', 'INSTRUCTOR', 'STUDENT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. INSTITUTIONS
CREATE TABLE Institutions (
    institution_id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    institution_name VARCHAR(150) NOT NULL,
    description TEXT,
    join_code VARCHAR(20) UNIQUE NOT NULL,
    FOREIGN KEY (admin_id) REFERENCES Users(user_id)
);

-- 3. INSTITUTION MEMBERS
CREATE TABLE Institution_Members (
    membership_id INT AUTO_INCREMENT PRIMARY KEY,
    institution_id INT NOT NULL,
    user_id INT NOT NULL,
    member_role ENUM('INSTRUCTOR', 'STUDENT') NOT NULL,
    status ENUM('PENDING', 'ACTIVE', 'REJECTED') DEFAULT 'ACTIVE',
    UNIQUE (institution_id, user_id),
    FOREIGN KEY (institution_id) REFERENCES Institutions(institution_id),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- 4. CATEGORIES
CREATE TABLE Categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) UNIQUE NOT NULL
);

-- 5. COURSES
CREATE TABLE Courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    instructor_id INT NOT NULL,
    institution_id INT NULL,
    category_id INT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    visibility ENUM('PUBLIC', 'INSTITUTION') NOT NULL DEFAULT 'PUBLIC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (instructor_id) REFERENCES Users(user_id),
    FOREIGN KEY (institution_id) REFERENCES Institutions(institution_id),
    FOREIGN KEY (category_id) REFERENCES Categories(category_id)
);

-- 6. LESSONS
CREATE TABLE Lessons (
    lesson_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    lesson_order INT NOT NULL,
    video_path VARCHAR(500),
    pdf_path VARCHAR(500),
    FOREIGN KEY (course_id) REFERENCES Courses(course_id) ON DELETE CASCADE
);

-- 7. ENROLLMENTS
CREATE TABLE Enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES Users(user_id),
    FOREIGN KEY (course_id) REFERENCES Courses(course_id) ON DELETE CASCADE
);

-- 8. LESSON PROGRESS
CREATE TABLE Lesson_Progress (
    progress_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    lesson_id INT NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    UNIQUE (student_id, lesson_id),
    FOREIGN KEY (student_id) REFERENCES Users(user_id),
    FOREIGN KEY (lesson_id) REFERENCES Lessons(lesson_id) ON DELETE CASCADE
);

-- 9. ANNOUNCEMENTS
CREATE TABLE Announcements (
    announcement_id INT AUTO_INCREMENT PRIMARY KEY,
    institution_id INT NOT NULL,
    author_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (institution_id) REFERENCES Institutions(institution_id),
    FOREIGN KEY (author_id) REFERENCES Users(user_id)
);

-- Starter data
INSERT INTO Categories (category_name)
VALUES ('Programming'), ('Design'), ('Business');
