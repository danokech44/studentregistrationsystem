package registration.controller;

import registration.dao.CourseDAO;
import registration.dao.EnrollmentDAO;
import registration.dao.StudentDAO;
import registration.model.Course;
import registration.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private TextField nameField;
    @FXML private TextField studentNumberField;
    @FXML private TextField feeBalanceField;

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> colStudentNumber;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, BigDecimal> colFeeBalance;

    @FXML private ComboBox<Course> courseComboBox;

    // TableView for enrolled courses
    @FXML private TableView<Course> enrolledCoursesTable;
    @FXML private TableColumn<Course, String> colCourseName;

    @FXML private Button addStudentButton;
    @FXML private Button enrollButton;
    @FXML private Button refreshButton;

    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    private final ObservableList<Student> studentObservableList = FXCollections.observableArrayList();
    private final ObservableList<Course> courseObservableList = FXCollections.observableArrayList();
    private final ObservableList<Course> enrolledCoursesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Student table columns
        colStudentNumber.setCellValueFactory(new PropertyValueFactory<>("idNumber"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colFeeBalance.setCellValueFactory(new PropertyValueFactory<>("feeBalance"));
        studentTable.setItems(studentObservableList);

        // Course combo box
        courseComboBox.setItems(courseObservableList);

        // Enrolled courses table column (using lambda for reliability)
        colCourseName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCourseName())
        );
        enrolledCoursesTable.setItems(enrolledCoursesList);

        loadStudents();
        loadCourses();

        // Selection listener for student table
        studentTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        System.out.println("[DEBUG] Selected student: " + newSelection.getIdNumber());
                        loadEnrolledCourses(newSelection.getIdNumber());
                    } else {
                        enrolledCoursesList.clear();
                    }
                });

        // OPTIONAL: Add a dummy course to test rendering (uncomment to test)
        // enrolledCoursesList.add(new Course(999, "TEST COURSE - TABLE WORKS"));
    }

    private void loadStudents() {
        try {
            studentObservableList.clear();
            List<Student> all = studentDAO.findAll();
            studentObservableList.addAll(all);
            System.out.println("[DEBUG] Loaded " + all.size() + " students");
        } catch (SQLException e) {
            System.err.println("[ERROR] loadStudents failed:");
            e.printStackTrace();
            showError("Database Error", "Failed to load students: " + e.getMessage());
        }
    }

    private void loadCourses() {
        try {
            courseObservableList.clear();
            List<Course> all = courseDAO.findAll();
            courseObservableList.addAll(all);
            System.out.println("[DEBUG] Loaded " + all.size() + " courses");
        } catch (SQLException e) {
            System.err.println("[ERROR] loadCourses failed:");
            e.printStackTrace();
            showError("Database Error", "Failed to load courses: " + e.getMessage());
        }
    }

    private void loadEnrolledCourses(String studentNumber) {
        try {
            System.out.println("[DEBUG] Loading enrolled courses for: " + studentNumber);
            List<Course> courses = enrollmentDAO.getEnrolledCoursesForStudent(studentNumber);
            System.out.println("[DEBUG] Retrieved " + courses.size() + " courses from DB");
            for (Course c : courses) {
                System.out.println("[DEBUG]   - " + c.getCourseName());
            }

            enrolledCoursesList.setAll(courses);  // Replace all items
            System.out.println("[DEBUG] TableView now contains " + enrolledCoursesList.size() + " items");
        } catch (SQLException e) {
            System.err.println("[ERROR] loadEnrolledCourses failed for " + studentNumber);
            e.printStackTrace();
            showError("Database Error", "Failed to load enrolled courses: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddStudent() {
        String name = nameField.getText().trim();
        String studentNumber = studentNumberField.getText().trim();
        String feeStr = feeBalanceField.getText().trim();

        if (name.isEmpty() || studentNumber.isEmpty() || feeStr.isEmpty()) {
            showWarning("Input Error", "All fields are required.");
            return;
        }

        try {
            BigDecimal feeBalance = new BigDecimal(feeStr);
            Student student = new Student(name, studentNumber, feeBalance);
            studentDAO.save(student);
            studentObservableList.add(student);
            clearForm();
            System.out.println("[DEBUG] Added student: " + studentNumber);
        } catch (NumberFormatException e) {
            showWarning("Input Error", "Fee balance must be a valid number.");
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to add student:");
            e.printStackTrace();
            showError("Database Error", "Failed to add student: " + e.getMessage());
        }
    }

    @FXML
    private void handleEnroll() {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        Course selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();

        if (selectedStudent == null || selectedCourse == null) {
            showWarning("Selection Error", "Please select a student and a course.");
            return;
        }

        if (!selectedStudent.hasClearedFees()) {
            showWarning("Enrollment Denied", "Student has outstanding fees. Enrollment not allowed.");
            return;
        }

        try {
            if (enrollmentDAO.isEnrolled(selectedStudent.getIdNumber(), selectedCourse.getCourseId())) {
                showInfo("Already Enrolled", "Student is already enrolled in this course.");
                return;
            }
            enrollmentDAO.enrollStudent(selectedStudent.getIdNumber(), selectedCourse.getCourseId());
            System.out.println("[DEBUG] Enrolled student " + selectedStudent.getIdNumber() + " in " + selectedCourse.getCourseName());
            loadEnrolledCourses(selectedStudent.getIdNumber());
            showInfo("Success", "Student enrolled successfully.");
        } catch (SQLException e) {
            System.err.println("[ERROR] Enrollment failed:");
            e.printStackTrace();
            showError("Database Error", "Enrollment failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadStudents();
        loadCourses();
        enrolledCoursesList.clear();
    }

    // ==================== NEW DELETE HANDLERS ====================

    @FXML
    private void handleDeleteStudent() {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showWarning("Selection Error", "Please select a student to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Student");
        confirm.setContentText("Are you sure you want to delete " + selectedStudent.getName() +
                " (" + selectedStudent.getIdNumber() + ")?\nThis will also remove all their enrollments.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                studentDAO.delete(selectedStudent.getIdNumber());
                studentObservableList.remove(selectedStudent);
                enrolledCoursesList.clear(); // Clear enrolled courses display
                System.out.println("[DEBUG] Deleted student: " + selectedStudent.getIdNumber());
                showInfo("Success", "Student deleted successfully.");
            } catch (SQLException e) {
                System.err.println("[ERROR] Failed to delete student:");
                e.printStackTrace();
                showError("Database Error", "Failed to delete student: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDropCourse() {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        Course selectedCourse = enrolledCoursesTable.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showWarning("Selection Error", "Please select a student first.");
            return;
        }

        if (selectedCourse == null) {
            showWarning("Selection Error", "Please select a course to drop.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Drop");
        confirm.setHeaderText("Drop Course");
        confirm.setContentText("Are you sure you want to drop " + selectedCourse.getCourseName() +
                " for " + selectedStudent.getName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                enrollmentDAO.unenrollStudent(selectedStudent.getIdNumber(), selectedCourse.getCourseId());
                loadEnrolledCourses(selectedStudent.getIdNumber()); // Refresh the list
                System.out.println("[DEBUG] Dropped course: " + selectedCourse.getCourseName());
                showInfo("Success", "Course dropped successfully.");
            } catch (SQLException e) {
                System.err.println("[ERROR] Failed to drop course:");
                e.printStackTrace();
                showError("Database Error", "Failed to drop course: " + e.getMessage());
            }
        }
    }

    // ==================== END NEW DELETE HANDLERS ====================

    private void clearForm() {
        nameField.clear();
        studentNumberField.clear();
        feeBalanceField.clear();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}