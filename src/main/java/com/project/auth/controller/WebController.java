package com.project.auth.controller;

import com.project.auth.dto.*;
import com.project.auth.entity.Role;
import com.project.auth.entity.User;
import com.project.auth.service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * ====================================================================
 * WEB CONTROLLER - Handles Thymeleaf page rendering
 * ====================================================================
 *
 * @Controller (not @RestController): Returns view names for Thymeleaf
 *
 * DIFFERENCE:
 * - @RestController: Returns data (JSON)
 * - @Controller: Returns view name → Thymeleaf finds template
 *
 * VIEW RESOLUTION:
 * return "login" → /templates/login.html
 * return "student/dashboard" → /templates/student/dashboard.html
 */
@Controller
public class WebController {

    @Autowired
    private AuthService authService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private DepartmentService departmentService;

    // ==================== PUBLIC PAGES ====================

    /**
     * Home page - redirects based on authentication status
     */
    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return redirectToDashboard(authentication);
        }
        return "redirect:/login";
    }

    /**
     * Login page
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }

        return "login";
    }

    /**
     * Handle login form submission
     */
    @PostMapping("/login")
    public String handleLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            LoginRequest loginRequest = new LoginRequest(email, password);
            AuthResponse authResponse = authService.login(loginRequest);

            // Store JWT in cookie for subsequent requests
            Cookie jwtCookie = new Cookie("jwt", authResponse.getToken());
            jwtCookie.setHttpOnly(true);  // Prevent JavaScript access (XSS protection)
            jwtCookie.setPath("/");       // Available across entire site
            jwtCookie.setMaxAge(3 * 60 * 60);  // 3 hours (matching token expiry)
            response.addCookie(jwtCookie);

            // Redirect based on role
            if (authResponse.getRole().equals("ROLE_TEACHER")) {
                return "redirect:/teacher/dashboard";
            } else {
                return "redirect:/student/dashboard";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/login";
        }
    }

    /**
     * Signup page
     */
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "signup";
    }

    /**
     * Handle signup form submission
     */
    @PostMapping("/signup")
    public String handleSignup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam Long departmentId,
            @RequestParam(required = false) String phone,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            SignupRequest signupRequest = SignupRequest.builder()
                    .name(name)
                    .email(email)
                    .password(password)
                    .confirmPassword(confirmPassword)
                    .departmentId(departmentId)
                    .phone(phone)
                    .build();

            AuthResponse authResponse = authService.signup(signupRequest);

            // Store JWT in cookie
            Cookie jwtCookie = new Cookie("jwt", authResponse.getToken());
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3 * 60 * 60);
            response.addCookie(jwtCookie);

            return "redirect:/student/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            return "signup";
        }
    }

    /**
     * Logout
     */
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Clear JWT cookie
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);  // Expires immediately
        response.addCookie(jwtCookie);

        // Clear security context
        SecurityContextHolder.clearContext();

        return "redirect:/login?logout";
    }

    /**
     * Access denied page
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    // ==================== STUDENT PAGES ====================

    /**
     * Student Dashboard
     */
    @GetMapping("/student/dashboard")
    public String studentDashboard(Model model) {
        StudentDTO student = studentService.getCurrentStudentProfile();
        List<CourseDTO> allCourses = courseService.getAllCourses();

        model.addAttribute("student", student);
        model.addAttribute("courses", allCourses);
        model.addAttribute("pageTitle", "Student Dashboard");

        return "student/dashboard";
    }

    /**
     * Student Profile Page
     */
    @GetMapping("/student/profile")
    public String studentProfile(Model model) {
        StudentDTO student = studentService.getCurrentStudentProfile();
        List<DepartmentDTO> departments = departmentService.getAllDepartments();

        model.addAttribute("student", student);
        model.addAttribute("departments", departments);
        model.addAttribute("pageTitle", "My Profile");

        return "student/profile";
    }

    /**
     * Update Student Profile
     */
    @PostMapping("/student/profile")
    public String updateStudentProfile(
            @RequestParam String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Long departmentId,
            RedirectAttributes redirectAttributes) {

        try {
            StudentUpdateDTO updateDTO = StudentUpdateDTO.builder()
                    .name(name)
                    .phone(phone)
                    .address(address)
                    .departmentId(departmentId)
                    .build();

            studentService.updateCurrentStudentProfile(updateDTO);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/student/profile";
    }

    /**
     * View Courses Page (Student)
     */
    @GetMapping("/student/courses")
    public String studentCourses(Model model) {
        StudentDTO student = studentService.getCurrentStudentProfile();
        List<CourseDTO> enrolledCourses = courseService.getCoursesByStudentId(student.getId());
        List<CourseDTO> availableCourses = courseService.getAvailableCoursesForStudent(student.getId());

        model.addAttribute("student", student);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("availableCourses", availableCourses);
        model.addAttribute("pageTitle", "My Courses");

        return "student/courses";
    }

    /**
     * Enroll in course (Student)
     */
    @PostMapping("/student/courses/enroll/{courseId}")
    public String enrollInCourse(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        try {
            StudentDTO student = studentService.getCurrentStudentProfile();
            studentService.enrollInCourse(student.getId(), courseId);
            redirectAttributes.addFlashAttribute("success", "Enrolled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/courses";
    }

    /**
     * Drop course (Student)
     */
    @PostMapping("/student/courses/drop/{courseId}")
    public String dropCourse(@PathVariable Long courseId, RedirectAttributes redirectAttributes) {
        try {
            StudentDTO student = studentService.getCurrentStudentProfile();
            studentService.dropCourse(student.getId(), courseId);
            redirectAttributes.addFlashAttribute("success", "Course dropped successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/courses";
    }

    /**
     * View Teachers (Student)
     */
    @GetMapping("/student/teachers")
    public String viewTeachers(Model model) {
        List<TeacherDTO> teachers = teacherService.getAllTeachers();

        model.addAttribute("teachers", teachers);
        model.addAttribute("pageTitle", "Teachers");

        return "student/teachers";
    }

    /**
     * View Departments (Student)
     */
    @GetMapping("/student/departments")
    public String viewDepartments(Model model) {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();

        model.addAttribute("departments", departments);
        model.addAttribute("pageTitle", "Departments");

        return "student/departments";
    }

    // ==================== TEACHER PAGES ====================

    /**
     * Teacher Dashboard
     */
    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Model model) {
        TeacherDTO teacher = teacherService.getCurrentTeacherProfile();
        List<CourseDTO> courses = courseService.getCurrentTeacherCourses();
        List<StudentDTO> students = studentService.getAllStudents();

        model.addAttribute("teacher", teacher);
        model.addAttribute("courses", courses);
        model.addAttribute("students", students);
        model.addAttribute("totalStudents", students.size());
        model.addAttribute("pageTitle", "Teacher Dashboard");

        return "teacher/dashboard";
    }

    /**
     * Teacher Profile Page
     */
    @GetMapping("/teacher/profile")
    public String teacherProfile(Model model) {
        TeacherDTO teacher = teacherService.getCurrentTeacherProfile();
        List<DepartmentDTO> departments = departmentService.getAllDepartments();

        model.addAttribute("teacher", teacher);
        model.addAttribute("departments", departments);
        model.addAttribute("pageTitle", "My Profile");

        return "teacher/profile";
    }

    /**
     * Update Teacher Profile
     */
    @PostMapping("/teacher/profile")
    public String updateTeacherProfile(
            @RequestParam String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String qualification,
            @RequestParam(required = false) Long departmentId,
            RedirectAttributes redirectAttributes) {

        try {
            TeacherUpdateDTO updateDTO = TeacherUpdateDTO.builder()
                    .name(name)
                    .phone(phone)
                    .specialization(specialization)
                    .qualification(qualification)
                    .departmentId(departmentId)
                    .build();

            teacherService.updateCurrentTeacherProfile(updateDTO);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/teacher/profile";
    }

    /**
     * Manage Students (Teacher)
     */
    @GetMapping("/teacher/students")
    public String manageStudents(Model model) {
        TeacherDTO teacher = teacherService.getCurrentTeacherProfile();
        List<StudentDTO> students = studentService.getAllStudents();

        model.addAttribute("teacher", teacher);
        model.addAttribute("students", students);
        model.addAttribute("pageTitle", "Manage Students");

        return "teacher/students";
    }

    /**
     * Delete Student (Teacher)
     */
    @PostMapping("/teacher/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("success", "Student deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/students";
    }

    /**
     * View Departments (Teacher)
     */
    @GetMapping("/teacher/departments")
    public String teacherViewDepartments(Model model) {
        TeacherDTO teacher = teacherService.getCurrentTeacherProfile();
        List<DepartmentDTO> departments = departmentService.getAllDepartments();

        model.addAttribute("teacher", teacher);
        model.addAttribute("departments", departments);
        model.addAttribute("pageTitle", "Departments");

        return "teacher/departments";
    }

    /**
     * Manage Courses (Teacher)
     */
    @GetMapping("/teacher/courses")
    public String manageCourses(Model model) {
        TeacherDTO teacher = teacherService.getCurrentTeacherProfile();
        List<CourseDTO> courses = courseService.getCurrentTeacherCourses();

        model.addAttribute("teacher", teacher);
        model.addAttribute("courses", courses);
        model.addAttribute("pageTitle", "My Courses");

        return "teacher/courses";
    }

    /**
     * Create Course Form (Teacher)
     */
    @GetMapping("/teacher/courses/new")
    public String newCourseForm(Model model) {
        TeacherDTO teacher = teacherService.getCurrentTeacherProfile();
        model.addAttribute("teacher", teacher);
        model.addAttribute("course", CourseDTO.builder().build());
        model.addAttribute("pageTitle", "Create New Course");
        return "teacher/course-form";
    }

    /**
     * Create Course (Teacher)
     */
    @PostMapping("/teacher/courses/create")
    public String createCourse(
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "3") Integer credits,
            @RequestParam(defaultValue = "30") Integer maxStudents,
            RedirectAttributes redirectAttributes) {

        try {
            CourseCreateDTO createDTO = CourseCreateDTO.builder()
                    .name(name)
                    .code(code)
                    .description(description)
                    .credits(credits)
                    .maxStudents(maxStudents)
                    .active(true)
                    .build();

            courseService.createCourse(createDTO);
            redirectAttributes.addFlashAttribute("success", "Course created successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/teacher/courses";
    }

    /**
     * Delete Course (Teacher)
     */
    @PostMapping("/teacher/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("success", "Course deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/courses";
    }

    // ==================== HELPER METHODS ====================

    private String redirectToDashboard(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (user.getRole() == Role.ROLE_TEACHER) {
            return "redirect:/teacher/dashboard";
        } else {
            return "redirect:/student/dashboard";
        }
    }
}
