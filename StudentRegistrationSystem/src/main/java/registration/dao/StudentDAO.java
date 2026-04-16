package registration.dao;

import registration.db.DatabaseConnection;
import registration.model.Student;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDAO implements GenericDAO<Student, String> {

    @Override
    public void save(Student student) throws SQLException {
        String sql = "INSERT INTO students (student_number, name, fee_balance) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getIdNumber());
            stmt.setString(2, student.getName());
            stmt.setBigDecimal(3, student.getFeeBalance());
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Student> findById(String studentNumber) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Student(
                        rs.getString("name"),
                        rs.getString("student_number"),
                        rs.getBigDecimal("fee_balance")
                ));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(new Student(
                        rs.getString("name"),
                        rs.getString("student_number"),
                        rs.getBigDecimal("fee_balance")
                ));
            }
        }
        return students;
    }

    @Override
    public void update(Student student) throws SQLException {
        String sql = "UPDATE students SET name = ?, fee_balance = ? WHERE student_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getName());
            stmt.setBigDecimal(2, student.getFeeBalance());
            stmt.setString(3, student.getIdNumber());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(String studentNumber) throws SQLException {
        String sql = "DELETE FROM students WHERE student_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentNumber);
            stmt.executeUpdate();
        }
    }

    // Overloaded method (polymorphism)
    public Optional<Student> findById(int dummy) throws SQLException {
        throw new UnsupportedOperationException("Use String student number");
    }
}
