package model;

public class Lesson {
    private final int lessonId;
    private final int courseId;
    private final String title;
    private final int lessonOrder;
    private final String videoPath;   // null if no video
    private final String pdfPath;     // null if no PDF

    public Lesson(int lessonId, int courseId, String title, int lessonOrder,
                  String videoPath, String pdfPath) {
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.title = title;
        this.lessonOrder = lessonOrder;
        this.videoPath = videoPath;
        this.pdfPath = pdfPath;
    }

    public int getLessonId() { return lessonId; }
    public int getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public int getLessonOrder() { return lessonOrder; }
    public String getVideoPath() { return videoPath; }
    public String getPdfPath() { return pdfPath; }
}