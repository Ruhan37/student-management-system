package com.project.auth.config;

import com.project.auth.entity.*;
import com.project.auth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * ====================================================================
 * DATA INITIALIZER - Seeds database with sample data
 * ====================================================================
 *
 * CommandLineRunner: Runs ONCE when application starts
 *
 * WHAT THIS CREATES:
 * 1. Sample Departments (CSE, EEE, BBA, etc.)
 * 2. Pre-created Teacher accounts (can't self-register)
 * 3. Sample Courses
 *
 * TEACHER CREDENTIALS (for testing):
 * - Email: teacher1@school.com, Password: teacher123
 * - Email: teacher2@school.com, Password: teacher123
 *
 * IMPORTANT: This only runs if data doesn't exist!
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Only initialize if no data exists
        if (departmentRepository.count() == 0) {
            System.out.println("========================================");
            System.out.println("   INITIALIZING SAMPLE DATA...         ");
            System.out.println("========================================");

            initializeDepartments();
            initializeTeachers();
            initializeCourses();

            System.out.println("========================================");
            System.out.println("   SAMPLE DATA INITIALIZED!            ");
            System.out.println("========================================");
            printCredentials();
        }
    }

    private void initializeDepartments() {
        List<Department> departments = Arrays.asList(
            Department.builder()
                .name("Computer Science & Engineering")
                .code("CSE")
                .description("Department of Computer Science and Engineering - Covering software development, algorithms, AI, and more.")
                .build(),

            Department.builder()
                .name("Electrical & Electronic Engineering")
                .code("EEE")
                .description("Department of Electrical and Electronic Engineering - Power systems, electronics, and telecommunications.")
                .build(),

            Department.builder()
                .name("Business Administration")
                .code("BBA")
                .description("Department of Business Administration - Management, marketing, finance, and entrepreneurship.")
                .build(),

            Department.builder()
                .name("English")
                .code("ENG")
                .description("Department of English - Literature, linguistics, and communication studies.")
                .build(),

            Department.builder()
                .name("Mathematics")
                .code("MATH")
                .description("Department of Mathematics - Pure and applied mathematics, statistics.")
                .build()
        );

        departmentRepository.saveAll(departments);
        System.out.println("✓ Created " + departments.size() + " departments");
    }

    private void initializeTeachers() {
        // Get departments
        Department cse = departmentRepository.findByCode("CSE").orElseThrow();
        Department eee = departmentRepository.findByCode("EEE").orElseThrow();
        Department bba = departmentRepository.findByCode("BBA").orElseThrow();

        // Create Teacher 1 - CSE
        User teacherUser1 = User.builder()
                .email("teacher1@school.com")
                .password(passwordEncoder.encode("teacher123"))
                .role(Role.ROLE_TEACHER)
                .build();

        Teacher teacher1 = Teacher.builder()
                .name("Dr. John Smith")
                .email("teacher1@school.com")
                .teacherId("TCH2024001")
                .phone("+880-1711-111111")
                .specialization("Software Engineering, Web Development")
                .qualification("Ph.D. in Computer Science")
                .joinDate(LocalDate.of(2020, 1, 15))
                .department(cse)
                .user(teacherUser1)
                .build();

        teacherRepository.save(teacher1);

        // Create Teacher 2 - EEE
        User teacherUser2 = User.builder()
                .email("teacher2@school.com")
                .password(passwordEncoder.encode("teacher123"))
                .role(Role.ROLE_TEACHER)
                .build();

        Teacher teacher2 = Teacher.builder()
                .name("Dr. Sarah Johnson")
                .email("teacher2@school.com")
                .teacherId("TCH2024002")
                .phone("+880-1722-222222")
                .specialization("Power Systems, Renewable Energy")
                .qualification("Ph.D. in Electrical Engineering")
                .joinDate(LocalDate.of(2019, 8, 1))
                .department(eee)
                .user(teacherUser2)
                .build();

        teacherRepository.save(teacher2);

        // Create Teacher 3 - BBA
        User teacherUser3 = User.builder()
                .email("teacher3@school.com")
                .password(passwordEncoder.encode("teacher123"))
                .role(Role.ROLE_TEACHER)
                .build();

        Teacher teacher3 = Teacher.builder()
                .name("Prof. Michael Brown")
                .email("teacher3@school.com")
                .teacherId("TCH2024003")
                .phone("+880-1733-333333")
                .specialization("Marketing, Strategic Management")
                .qualification("MBA, DBA")
                .joinDate(LocalDate.of(2018, 6, 1))
                .department(bba)
                .user(teacherUser3)
                .build();

        teacherRepository.save(teacher3);

        System.out.println("✓ Created 3 teacher accounts");
    }

    private void initializeCourses() {
        // Get teachers
        Teacher teacher1 = teacherRepository.findByEmail("teacher1@school.com").orElseThrow();
        Teacher teacher2 = teacherRepository.findByEmail("teacher2@school.com").orElseThrow();
        Teacher teacher3 = teacherRepository.findByEmail("teacher3@school.com").orElseThrow();

        List<Course> courses = Arrays.asList(
            // CSE Courses (Teacher 1)
            Course.builder()
                .name("Introduction to Programming")
                .code("CSE101")
                .description("Learn the fundamentals of programming using Java. Covers variables, loops, functions, and OOP basics.")
                .credits(3)
                .maxStudents(40)
                .teacher(teacher1)
                .active(true)
                .build(),

            Course.builder()
                .name("Data Structures & Algorithms")
                .code("CSE201")
                .description("Study of fundamental data structures (arrays, linked lists, trees, graphs) and algorithms (sorting, searching).")
                .credits(3)
                .maxStudents(35)
                .teacher(teacher1)
                .active(true)
                .build(),

            Course.builder()
                .name("Web Development")
                .code("CSE301")
                .description("Full-stack web development with HTML, CSS, JavaScript, and modern frameworks.")
                .credits(3)
                .maxStudents(30)
                .teacher(teacher1)
                .active(true)
                .build(),

            // EEE Courses (Teacher 2)
            Course.builder()
                .name("Circuit Analysis")
                .code("EEE101")
                .description("Fundamental analysis of electrical circuits, Kirchhoff's laws, and circuit theorems.")
                .credits(3)
                .maxStudents(35)
                .teacher(teacher2)
                .active(true)
                .build(),

            Course.builder()
                .name("Digital Electronics")
                .code("EEE201")
                .description("Digital logic design, Boolean algebra, combinational and sequential circuits.")
                .credits(3)
                .maxStudents(30)
                .teacher(teacher2)
                .active(true)
                .build(),

            // BBA Courses (Teacher 3)
            Course.builder()
                .name("Principles of Management")
                .code("BBA101")
                .description("Introduction to management theories, planning, organizing, leading, and controlling.")
                .credits(3)
                .maxStudents(50)
                .teacher(teacher3)
                .active(true)
                .build(),

            Course.builder()
                .name("Marketing Management")
                .code("BBA201")
                .description("Marketing strategies, consumer behavior, branding, and digital marketing.")
                .credits(3)
                .maxStudents(45)
                .teacher(teacher3)
                .active(true)
                .build()
        );

        courseRepository.saveAll(courses);
        System.out.println("✓ Created " + courses.size() + " courses");
    }

    private void printCredentials() {
        System.out.println("\n========================================");
        System.out.println("   TEST CREDENTIALS                     ");
        System.out.println("========================================");
        System.out.println("\n🎓 TEACHER ACCOUNTS:");
        System.out.println("   Email: teacher1@school.com");
        System.out.println("   Password: teacher123");
        System.out.println("   (Dr. John Smith - CSE)");
        System.out.println();
        System.out.println("   Email: teacher2@school.com");
        System.out.println("   Password: teacher123");
        System.out.println("   (Dr. Sarah Johnson - EEE)");
        System.out.println();
        System.out.println("   Email: teacher3@school.com");
        System.out.println("   Password: teacher123");
        System.out.println("   (Prof. Michael Brown - BBA)");
        System.out.println();
        System.out.println("👨‍🎓 STUDENT ACCOUNTS:");
        System.out.println("   Students can self-register via /signup");
        System.out.println("========================================\n");
    }
}
