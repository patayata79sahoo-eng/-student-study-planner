package com.studyplanner.model;

public enum Priority {
    HIGH("High", "badge-priority-high", "#ef4444"),
    MEDIUM("Medium", "badge-priority-medium", "#f59e0b"),
    LOW("Low", "badge-priority-low", "#10b981");

    private final String displayName;
    private final String cssClass;
    private final String colorCode;

    Priority(String displayName, String cssClass, String colorCode) {
        this.displayName = displayName;
        this.cssClass = cssClass;
        this.colorCode = colorCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getColorCode() {
        return colorCode;
    }

    public static Priority fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return MEDIUM;
        }
        for (Priority p : Priority.values()) {
            if (p.name().equalsIgnoreCase(text.trim()) || p.displayName.equalsIgnoreCase(text.trim())) {
                return p;
            }
        }
        return MEDIUM;
    }
}

