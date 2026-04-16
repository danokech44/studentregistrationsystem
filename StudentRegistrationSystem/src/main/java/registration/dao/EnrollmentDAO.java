package registration.dao;

import registration.db.DatabaseConnection;
import registration.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    /**
     * Enrolls a student in a specific course.
     * @param studentNumber the student's unique identifier
     * @param courseId the course's ID
     * @throws SQLException if database access fails
     */
    public void enrollStudent(String studentNumber, int courseId) throws SQLException {
        String sql = "INSERT INTO enrollments (student_number, course_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentNumber);
            stmt.setInt(2, courseId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("[DEBUG] Enrolled student " + studentNumber + " in course " + courseId + " - rows affected: " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to enroll student: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all courses that a student is currently enrolled in.
     * @param studentNumber the student's unique identifier
     * @return a list of Course objects, never null
     * @throws SQLException if database access fails
     */
    public List<Course> getEnrolledCoursesForStudent(String studentNumber) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.course_id, c.course_name " +
                "FROM courses c " +
                "JOIN enrollments e ON c.course_id = e.course_id " +
                "WHERE e.student_number = ? " +
                "ORDER BY c.course_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int courseId = rs.getInt("course_id");
                    String courseName = rs.getString("course_name");
                    courses.add(new Course(courseId, courseName));
                }
            }
            System.out.println("[DEBUG] Retrieved " + courses.size() + " enrolled courses for student " + studentNumber);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to retrieve enrolled courses: " + e.getMessage());
            throw e;
        }
        return courses;
    }

    /**
     * Checks if a student is already enrolled in a specific course.
     * @param studentNumber the student's unique identifier
     * @param courseId the course's ID
     * @return true if enrolled, false otherwise
     * @throws SQLException if database access fails
     */
    public boolean isEnrolled(String studentNumber, int courseId) throws SQLException {
        String sql = "SELECT 1 FROM enrollments WHERE student_number = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentNumber);
            stmt.setInt(2, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean enrolled = rs.next();
                System.out.println("[DEBUG] Check enrollment: student " + studentNumber + ", course " + courseId + " -> " + enrolled);
                return enrolled;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to check enrollment: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Optional: Removes a student's enrollment from a course.
     * @param studentNumber the student's unique identifier
     * @param courseId the course's ID
     * @throws SQLException if database access fails
     */
    public void unenrollStudent(String studentNumber, int courseId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE student_number = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentNumber);
            stmt.setInt(2, courseId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("[DEBUG] Unenrolled student " + studentNumber + " from course " + courseId + " - rows affected: " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to unenroll student: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Diagnostic: Prints all enrollments currently in the database.
     * Useful for debugging when the UI doesn't show expected data.
     */
    public void printAllEnrollments() {
        String sql = "SELECT s.name, s.student_number, c.course_name " +
                "FROM students s " +
                "JOIN enrollments e ON s.student_number = e.student_number " +
                "JOIN courses c ON e.course_id = c.course_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("[DEBUG] Current enrollments in database:");
            while (rs.next()) {
                System.out.println("  " + rs.getString("student_number") + " - " +
                        rs.getString("name") + " -> " +
                        rs.getString("course_name"));
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to print enrollments: " + e.getMessage());
        }
    }
}
