package com.college;

import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection ignored = Database.getConnection()) {
            System.out.println("Connected to MySQL successfully!");
        } catch (SQLException e) {
            System.out.println("Could not connect to MySQL. Check Database.java credentials and run schema.sql first.");
            System.out.println("Details: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n=== Student Enrollment Management ===");
            System.out.println("1. Add student");
            System.out.println("2. Create course");
            System.out.println("3. Enroll student in course");
            System.out.println("4. Display students in a course");
            System.out.println("5. Display all courses");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            int choice = readInt();
            switch (choice) {
                case 1 -> addStudent();
                case 2 -> createCourse();
                case 3 -> enrollStudent();
                case 4 -> displayStudentsInCourse();
                case 5 -> displayCourses();
                case 0 -> {
                    System.out.println("Goodbye!");
                    sc.close();
                    return;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static int readInt() {
        while (!sc.hasNextInt()) {
            System.out.print("Enter a valid number: ");
            sc.next();
        }
        int value = sc.nextInt();
        sc.nextLine();
        return value;
    }

    private static void addStudent() {
        System.out.print("Student name: ");
        String name = sc.nextLine().trim();
        System.out.print("Student email: ");
        String email = sc.nextLine().trim();
        if (name.isEmpty() || email.isEmpty()) {
            System.out.println("Name and email cannot be empty.");
            return;
        }

        String sql = "INSERT INTO students (student_name, email) VALUES (?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.executeUpdate();
            System.out.println("Student added successfully.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) System.out.println("A student with that email already exists.");
            else System.out.println("Error adding student: " + e.getMessage());
        }
    }

    private static void createCourse() {
        System.out.print("Course name: ");
        String name = sc.nextLine().trim();
        System.out.print("Instructor name: ");
        String instructor = sc.nextLine().trim();
        if (name.isEmpty() || instructor.isEmpty()) {
            System.out.println("Course and instructor names cannot be empty.");
            return;
        }

        String sql = "INSERT INTO courses (course_name, instructor) VALUES (?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, instructor);
            ps.executeUpdate();
            System.out.println("Course created successfully.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) System.out.println("A course with that name already exists.");
            else System.out.println("Error creating course: " + e.getMessage());
        }
    }

    private static void enrollStudent() {
        System.out.print("Student ID: ");
        int studentId = readInt();
        System.out.print("Course ID: ");
        int courseId = readInt();

        String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
            System.out.println("Enrollment successful.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062)
                System.out.println("Duplicate enrollment prevented: this student is already enrolled.");
            else if (e.getErrorCode() == 1452)
                System.out.println("Student ID or course ID does not exist. Check the IDs first.");
            else System.out.println("Error enrolling student: " + e.getMessage());
        }
    }

    private static void displayStudentsInCourse() {
        displayCourses();
        System.out.print("Enter the course ID to view its students: ");
        int courseId = readInt();

        String sql = "SELECT s.student_id, s.student_name, s.email, e.enrollment_date " +
                     "FROM students s JOIN enrollments e ON s.student_id = e.student_id " +
                     "JOIN courses c ON c.course_id = e.course_id " +
                     "WHERE c.course_id = ? ORDER BY s.student_name";

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("ID: %d | Name: %s | Email: %s | Enrolled: %s%n",
                            rs.getInt("student_id"), rs.getString("student_name"),
                            rs.getString("email"), rs.getTimestamp("enrollment_date"));
                }
                if (!found) System.out.println("No enrolled students found for this course (or invalid course ID).");
            }
        } catch (SQLException e) {
            System.out.println("Error displaying enrollments: " + e.getMessage());
        }
    }

    private static void displayCourses() {
        String sql = "SELECT course_id, course_name, instructor FROM courses ORDER BY course_id";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\n--- Courses ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("ID: %d | Course: %s | Instructor: %s%n",
                        rs.getInt("course_id"), rs.getString("course_name"), rs.getString("instructor"));
            }
            if (!found) System.out.println("No courses created yet.");
        } catch (SQLException e) {
            System.out.println("Error displaying courses: " + e.getMessage());
        }
    }
}
