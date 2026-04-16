package registration.model;

public class Enrollment {
    private String studentNumber;
    private int courseId;

    public Enrollment(String studentNumber, int courseId) {
        this.studentNumber = studentNumber;
        this.courseId = courseId;
    }

    public String getStudentNumber() { return studentNumber; }
    public int getCourseId() { return courseId; }
}