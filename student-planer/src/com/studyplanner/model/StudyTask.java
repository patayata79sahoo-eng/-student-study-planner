package com.studyplanner.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class StudyTask implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String id;
    private String title;
    private String subjectId;
    private String taskType;
    private Priority priority;
    private LocalDate deadline;
    private double estimatedHours;
    private boolean completed;
    private String notes;

    public StudyTask() {
        this.priority = Priority.MEDIUM;
        this.taskType = "Assignment";
        this.completed = false;
        this.estimatedHours = 2.0;
        this.deadline = LocalDate.now().plusDays(3);
    }

    public StudyTask(String id, String title, String subjectId, String taskType,
                     Priority priority, LocalDate deadline, double estimatedHours,
                     boolean completed, String notes) {
        this.id = id;
        this.title = title;
        this.subjectId = subjectId;
        this.taskType = (taskType == null || taskType.trim().isEmpty()) ? "Assignment" : taskType.trim();
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.deadline = deadline != null ? deadline : LocalDate.now().plusDays(3);
        this.estimatedHours = Math.max(0.5, estimatedHours);
        this.completed = completed;
        this.notes = notes != null ? notes.trim() : "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubjectId() {
        return subjectId != null ? subjectId : "";
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getTaskType() {
        return taskType != null ? taskType : "Assignment";
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Priority getPriority() {
        return priority != null ? priority : Priority.MEDIUM;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getDeadlineFormatted() {
        return deadline != null ? deadline.format(DATE_FORMAT) : "";
    }

    public double getEstimatedHours() {
        return estimatedHours > 0 ? estimatedHours : 1.0;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getDaysRemaining() {
        if (deadline == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    public boolean isOverdue() {
        return !completed && deadline != null && deadline.isBefore(LocalDate.now());
    }

    public boolean isDueToday() {
        return !completed && deadline != null && deadline.isEqual(LocalDate.now());
    }

    public boolean isDueTomorrow() {
        return !completed && deadline != null && deadline.isEqual(LocalDate.now().plusDays(1));
    }

    public boolean isDueSoon(int days) {
        return !completed && !isOverdue() && getDaysRemaining() <= days;
    }

    public String getDeadlineStatusText() {
        if (completed) {
            return "Completed";
        }
        if (deadline == null) {
            return "No deadline";
        }
        if (isDueToday()) {
            return "Due Today";
        }
        if (isDueTomorrow()) {
            return "Due Tomorrow";
        }
        long days = getDaysRemaining();
        if (days < 0) {
            return "Overdue by " + Math.abs(days) + " " + (Math.abs(days) == 1 ? "day" : "days");
        }
        return days + " " + (days == 1 ? "day" : "days") + " left";
    }

    public String getDeadlineBadgeClass() {
        if (completed) {
            return "badge-status-completed";
        }
        if (isOverdue()) {
            return "badge-status-overdue";
        }
        if (isDueToday() || isDueTomorrow()) {
            return "badge-status-urgent";
        }
        return "badge-status-pending";
    }
}

