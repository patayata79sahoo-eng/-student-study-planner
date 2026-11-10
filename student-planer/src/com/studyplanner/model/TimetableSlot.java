package com.studyplanner.model;

import java.io.Serializable;

public class TimetableSlot implements Serializable, Comparable<TimetableSlot> {
    private static final long serialVersionUID = 1L;

    public static final String[] DAYS_OF_WEEK = {
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    private String id;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String subjectId;
    private String activityType;
    private String location;

    public TimetableSlot() {
        this.dayOfWeek = "Monday";
        this.startTime = "09:00";
        this.endTime = "10:00";
        this.activityType = "Lecture";
        this.location = "Main Campus";
    }

    public TimetableSlot(String id, String dayOfWeek, String startTime, String endTime,
                         String subjectId, String activityType, String location) {
        this.id = id;
        this.dayOfWeek = normalizeDay(dayOfWeek);
        this.startTime = (startTime == null || startTime.trim().isEmpty()) ? "09:00" : startTime.trim();
        this.endTime = (endTime == null || endTime.trim().isEmpty()) ? "10:00" : endTime.trim();
        this.subjectId = subjectId;
        this.activityType = (activityType == null || activityType.trim().isEmpty()) ? "Lecture" : activityType.trim();
        this.location = (location == null || location.trim().isEmpty()) ? "Main Campus" : location.trim();
    }

    public static String normalizeDay(String day) {
        if (day == null) return "Monday";
        for (String d : DAYS_OF_WEEK) {
            if (d.equalsIgnoreCase(day.trim())) {
                return d;
            }
        }
        return "Monday";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDayOfWeek() {
        return dayOfWeek != null ? dayOfWeek : "Monday";
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = normalizeDay(dayOfWeek);
    }

    public String getStartTime() {
        return startTime != null ? startTime : "09:00";
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime != null ? endTime : "10:00";
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getSubjectId() {
        return subjectId != null ? subjectId : "";
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getActivityType() {
        return activityType != null ? activityType : "Lecture";
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getLocation() {
        return location != null ? location : "";
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getDayIndex() {
        for (int i = 0; i < DAYS_OF_WEEK.length; i++) {
            if (DAYS_OF_WEEK[i].equalsIgnoreCase(dayOfWeek)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int compareTo(TimetableSlot other) {
        if (other == null) return 1;
        int dayComp = Integer.compare(this.getDayIndex(), other.getDayIndex());
        if (dayComp != 0) return dayComp;
        return this.getStartTime().compareTo(other.getStartTime());
    }
}

