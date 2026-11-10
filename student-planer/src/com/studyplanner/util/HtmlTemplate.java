package com.studyplanner.util;

import com.studyplanner.model.Priority;
import com.studyplanner.model.StudyTask;
import com.studyplanner.model.Subject;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HtmlTemplate {

    public static String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }

    public static String renderFlashMessage(HttpServletRequest req) {
        if (req == null) return "";
        String msg = req.getParameter("msg");
        if (msg == null || msg.trim().isEmpty()) return "";
        String type = req.getParameter("type");
        if (type == null) type = "info";

        String bannerClass = "alert-info";
        String icon = "&#9432;";
        if ("success".equalsIgnoreCase(type)) {
            bannerClass = "alert-success";
            icon = "&#10003;";
        } else if ("error".equalsIgnoreCase(type) || "danger".equalsIgnoreCase(type)) {
            bannerClass = "alert-error";
            icon = "&#9888;";
        } else if ("warning".equalsIgnoreCase(type)) {
            bannerClass = "alert-warning";
            icon = "&#9888;";
        }

        return "<div class=\"alert " + bannerClass + "\">" +
               "<span class=\"alert-icon\">" + icon + "</span>" +
               "<span class=\"alert-text\">" + escape(msg) + "</span>" +
               "</div>";
    }

    public static String renderPriorityBadge(Priority priority) {
        if (priority == null) priority = Priority.MEDIUM;
        return "<span class=\"badge " + priority.getCssClass() + "\">" +
               "<span class=\"dot\"></span>" + priority.getDisplayName() +
               "</span>";
    }

    public static String renderDeadlineBadge(StudyTask task) {
        if (task == null) return "";
        String badgeClass = task.getDeadlineBadgeClass();
        String text = task.getDeadlineStatusText();
        String icon = "&#128197; ";
        if (task.isCompleted()) {
            icon = "&#10004; ";
        } else if (task.isOverdue()) {
            icon = "&#9888; ";
        } else if (task.isDueToday() || task.isDueTomorrow()) {
            icon = "&#9200; ";
        }
        return "<span class=\"badge " + badgeClass + "\">" + icon + escape(text) + "</span>";
    }

    public static String renderSubjectPill(Subject subject) {
        if (subject == null) {
            return "<span class=\"subject-pill subject-pill-unassigned\">Unassigned</span>";
        }
        String color = subject.getColor();
        return "<span class=\"subject-pill\" style=\"background-color: " + color + "15; color: " + color + "; border-color: " + color + "40;\">" +
               "<span class=\"subject-dot\" style=\"background-color: " + color + ";\"></span>" +
               escape(subject.getCode()) + " - " + escape(subject.getName()) +
               "</span>";
    }

    public static String renderPage(String title, String activeNav, String bodyContent,
                                    String contextPath, HttpServletRequest req) {
        LocalDate now = LocalDate.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        String dashboardActive = "dashboard".equalsIgnoreCase(activeNav) ? "active" : "";
        String tasksActive = "tasks".equalsIgnoreCase(activeNav) ? "active" : "";
        String deadlinesActive = "deadlines".equalsIgnoreCase(activeNav) ? "active" : "";
        String subjectsActive = "subjects".equalsIgnoreCase(activeNav) ? "active" : "";
        String timetableActive = "timetable".equalsIgnoreCase(activeNav) ? "active" : "";
        String progressActive = "progress".equalsIgnoreCase(activeNav) ? "active" : "";

        String flash = renderFlashMessage(req);

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("  <title>").append(escape(title)).append(" | EduPlan Student Planner</title>\n");
        sb.append("  <link rel=\"stylesheet\" href=\"").append(contextPath).append("/css/style.css\">\n");
        sb.append("</head>\n");
        sb.append("<body>\n");

        // Top Navigation Header
        sb.append("<header class=\"top-nav-bar\">\n");
        sb.append("  <div class=\"nav-container\">\n");
        sb.append("    <a href=\"").append(contextPath).append("/dashboard\" class=\"brand-logo\">\n");
        sb.append("      <div class=\"logo-icon\">\n");
        sb.append("        <svg width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M22 10v6M2 10l10-5 10 5-10 5z\"></path><path d=\"M6 12v5c3 3 9 3 12 0v-5\"></path></svg>\n");
        sb.append("      </div>\n");
        sb.append("      <div class=\"brand-text\">\n");
        sb.append("        <span class=\"brand-title\">EduPlan</span>\n");
        sb.append("        <span class=\"brand-tagline\">Student Study Planner</span>\n");
        sb.append("      </div>\n");
        sb.append("    </a>\n");

        sb.append("    <nav class=\"main-navigation\">\n");
        sb.append("      <a href=\"").append(contextPath).append("/dashboard\" class=\"nav-item ").append(dashboardActive).append("\">\n");
        sb.append("        <span class=\"nav-icon\">&#128202;</span> Dashboard\n");
        sb.append("      </a>\n");
        sb.append("      <a href=\"").append(contextPath).append("/tasks\" class=\"nav-item ").append(tasksActive).append("\">\n");
        sb.append("        <span class=\"nav-icon\">&#9745;</span> Tasks\n");
        sb.append("      </a>\n");
        sb.append("      <a href=\"").append(contextPath).append("/tasks?filter=deadlines\" class=\"nav-item ").append(deadlinesActive).append("\">\n");
        sb.append("        <span class=\"nav-icon\">&#9200;</span> Deadlines\n");
        sb.append("      </a>\n");
        sb.append("      <a href=\"").append(contextPath).append("/subjects\" class=\"nav-item ").append(subjectsActive).append("\">\n");
        sb.append("        <span class=\"nav-icon\">&#128218;</span> Subjects\n");
        sb.append("      </a>\n");
        sb.append("      <a href=\"").append(contextPath).append("/timetable\" class=\"nav-item ").append(timetableActive).append("\">\n");
        sb.append("        <span class=\"nav-icon\">&#128197;</span> Timetable\n");
        sb.append("      </a>\n");
        sb.append("      <a href=\"").append(contextPath).append("/progress\" class=\"nav-item ").append(progressActive).append("\">\n");
        sb.append("        <span class=\"nav-icon\">&#128200;</span> Progress\n");
        sb.append("      </a>\n");
        sb.append("    </nav>\n");

        sb.append("    <div class=\"student-profile-badge\">\n");
        sb.append("      <div class=\"date-chip\">\n");
        sb.append("        <span class=\"calendar-glyph\">&#128197;</span> ").append(formattedDate).append("\n");
        sb.append("      </div>\n");
        sb.append("      <div class=\"user-avatar-pill\">\n");
        sb.append("        <span class=\"avatar-circle\">S</span>\n");
        sb.append("        <span class=\"student-name\">Student Portal</span>\n");
        sb.append("      </div>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");
        sb.append("</header>\n");

        // Main content wrapper
        sb.append("<main class=\"main-wrapper\">\n");
        sb.append("  <div class=\"content-container\">\n");
        if (!flash.isEmpty()) {
            sb.append(flash).append("\n");
        }
        sb.append(bodyContent).append("\n");
        sb.append("  </div>\n");
        sb.append("</main>\n");

        // Footer
        sb.append("<footer class=\"main-footer\">\n");
        sb.append("  <div class=\"footer-container\">\n");
        sb.append("    <div class=\"footer-brand\">\n");
        sb.append("      <strong>EduPlan</strong> &mdash; Modern Student Study Planner\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"footer-stack-info\">\n");
        sb.append("      <span>Built with <strong>Java Servlets</strong> &amp; <strong>Apache Tomcat</strong></span>\n");
        sb.append("      <span>&bull;</span>\n");
        sb.append("      <span>Pure Semantic <strong>HTML5</strong> &amp; <strong>CSS3</strong></span>\n");
        sb.append("      <span>&bull;</span>\n");
        sb.append("      <span>Persistent Java File Storage</span>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");
        sb.append("</footer>\n");

        sb.append("</body>\n");
        sb.append("</html>\n");

        return sb.toString();
    }
}

