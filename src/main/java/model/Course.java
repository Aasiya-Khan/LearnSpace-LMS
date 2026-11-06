package model;

public class Course {
    private final int courseId;
    private final int instructorId;
    private final Integer institutionId;   // null = public course
    private final String title;
    private final String description;
    private final String visibility;       // "PUBLIC" or "INSTITUTION"
    private final String instructorName;
    private final String categoryName;
    private final int lessonCount;

    public Course(int courseId, int instructorId, Integer institutionId, String title,
                  String description, String visibility, String instructorName,
                  String categoryName, int lessonCount) {
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.institutionId = institutionId;
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.instructorName = instructorName;
        this.categoryName = categoryName;
        this.lessonCount = lessonCount;
    }

    public int getCourseId() { return courseId; }
    public int getInstructorId() { return instructorId; }
    public Integer getInstitutionId() { return institutionId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getVisibility() { return visibility; }
    public String getInstructorName() { return instructorName; }
    public String getCategoryName() { return categoryName; }
    public int getLessonCount() { return lessonCount; }
}