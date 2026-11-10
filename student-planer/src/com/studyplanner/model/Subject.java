package com.studyplanner.model;

import java.io.Serializable;

public class Subject implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String code;
    private String color;
    private String instructor;
    private int targetHours;

    public Subject() {
    }

    public Subject(String id, String name, String code, String color, String instructor, int targetHours) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.color = (color == null || color.trim().isEmpty()) ? "#2563eb" : color.trim();
        this.instructor = (instructor == null) ? "" : instructor.trim();
        this.targetHours = Math.max(1, targetHours);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code != null ? code : "";
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getColor() {
        return (color != null && !color.trim().isEmpty()) ? color : "#2563eb";
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getInstructor() {
        return instructor != null ? instructor : "";
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public int getTargetHours() {
        return targetHours > 0 ? targetHours : 4;
    }

    public void setTargetHours(int targetHours) {
        this.targetHours = targetHours;
    }
}

