package com.studyplanner.servlet;

import com.studyplanner.model.Subject;
import com.studyplanner.model.TimetableSlot;
import com.studyplanner.service.PlannerService;
import com.studyplanner.util.HtmlTemplate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "TimetableServlet", urlPatterns = {"/timetable"})
public class TimetableServlet extends HttpServlet {
    private final PlannerService service = PlannerService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        String cp = req.getContextPath();
        List<Subject> subjects = service.getAllSubjects();
        List<TimetableSlot> allSlots = service.getAllTimetableSlots();

        String todayName = LocalDate.now().getDayOfWeek().name();
        String currentDayName = todayName.substring(0, 1) + todayName.substring(1).toLowerCase();

        StringBuilder body = new StringBuilder();

        // Page Header
        body.append("<div class=\"page-header\">\n")
            .append("  <div class=\"page-title-group\">\n")
            .append("    <h1 class=\"page-title\">&#128197; Weekly Study Timetable</h1>\n")
            .append("    <p class=\"page-subtitle\">Map out lectures, labs, and dedicated study blocks to master your week.</p>\n")
            .append("  </div>\n")
            .append("  <div class=\"page-actions\">\n")
            .append("    <a href=\"#add-slot-section\" class=\"btn btn-primary\">+ Schedule New Slot</a>\n")
            .append("  </div>\n")
            .append("</div>\n");

        // Timetable summary stats
        body.append("<div class=\"timetable-summary-banner\">\n")
            .append("  <div class=\"summary-stat-item\">\n")
            .append("    <span class=\"stat-num\">").append(allSlots.size()).append("</span>\n")
            .append("    <span class=\"stat-desc\">Weekly Sessions</span>\n")
            .append("  </div>\n")
            .append("  <div class=\"summary-stat-item\">\n")
            .append("    <span class=\"stat-num text-primary\">").append(service.getTodaySlots().size()).append("</span>\n")
            .append("    <span class=\"stat-desc\">Today's Sessions (").append(currentDayName).append(")</span>\n")
            .append("  </div>\n")
            .append("  <div class=\"summary-stat-item\">\n")
            .append("    <span class=\"stat-num text-green\">").append(subjects.size()).append("</span>\n")
            .append("    <span class=\"stat-desc\">Covered Subjects</span>\n")
            .append("  </div>\n")
            .append("</div>\n");

        // 7-Day Weekly Grid View
        body.append("<div class=\"timetable-grid\">\n");

        for (String day : TimetableSlot.DAYS_OF_WEEK) {
            boolean isToday = day.equalsIgnoreCase(currentDayName);
            List<TimetableSlot> daySlots = service.getSlotsByDay(day);

            body.append("  <div class=\"timetable-day-col ").append(isToday ? "timetable-day-today" : "").append("\">\n")
                .append("    <div class=\"timetable-day-header\">\n")
                .append("      <div class=\"day-title-group\">\n")
                .append("        <span class=\"day-name\">").append(day).append("</span>\n");
                if (isToday) {
                    body.append("        <span class=\"today-tag\">TODAY</span>\n");
                }
            body.append("      </div>\n")
                .append("      <span class=\"day-slot-count\">").append(daySlots.size()).append(" ").append(daySlots.size() == 1 ? "slot" : "slots").append("</span>\n")
                .append("    </div>\n")
                .append("    <div class=\"timetable-day-body\">\n");

            if (daySlots.isEmpty()) {
                body.append("      <div class=\"timetable-empty-day\">\n")
                    .append("        <span>No classes or study blocks</span>\n")
                    .append("      </div>\n");
            } else {
                for (TimetableSlot slot : daySlots) {
                    Subject sub = service.getSubjectById(slot.getSubjectId());
                    String color = (sub != null) ? sub.getColor() : "#2563eb";

                    body.append("      <div class=\"timetable-slot-card\" style=\"border-left: 4px solid ").append(color).append(";\">\n")
                        .append("        <div class=\"slot-time-row\">\n")
                        .append("          <span class=\"slot-time\">").append(HtmlTemplate.escape(slot.getStartTime())).append(" &ndash; ").append(HtmlTemplate.escape(slot.getEndTime())).append("</span>\n")
                        .append("          <form method=\"post\" action=\"").append(cp).append("/timetable\" class=\"inline-form\">\n")
                        .append("            <input type=\"hidden\" name=\"action\" value=\"delete\">\n")
                        .append("            <input type=\"hidden\" name=\"id\" value=\"").append(HtmlTemplate.escape(slot.getId())).append("\">\n")
                        .append("            <button type=\"submit\" class=\"slot-delete-btn\" title=\"Remove slot\" onclick=\"return true;\">&times;</button>\n")
                        .append("          </form>\n")
                        .append("        </div>\n")
                        .append("        <div class=\"slot-subject-name\" style=\"color: ").append(color).append(";\">")
                        .append(sub != null ? HtmlTemplate.escape(sub.getName()) : "General Study")
                        .append("</div>\n");

                    if (sub != null && !sub.getCode().isEmpty()) {
                        body.append("        <div class=\"slot-subject-code\">").append(HtmlTemplate.escape(sub.getCode())).append("</div>\n");
                    }

                    body.append("        <div class=\"slot-tags-row\">\n")
                        .append("          <span class=\"badge-activity activity-").append(HtmlTemplate.escape(slot.getActivityType().toLowerCase().replace(" ", "-"))).append("\">")
                        .append(HtmlTemplate.escape(slot.getActivityType()))
                        .append("</span>\n");

                    if (!slot.getLocation().isEmpty()) {
                        body.append("          <span class=\"slot-location-tag\">&#128205; ").append(HtmlTemplate.escape(slot.getLocation())).append("</span>\n");
                    }

                    body.append("        </div>\n")
                        .append("      </div>\n");
                }
            }

            body.append("    </div>\n")
                .append("  </div>\n");
        }

        body.append("</div>\n");

        // Add Slot Form Card
        body.append("<div id=\"add-slot-section\" class=\"dashboard-card timetable-form-card\">\n")
            .append("  <div class=\"card-header-bar\">\n")
            .append("    <h2 class=\"card-title\">+ Schedule a Timetable Slot</h2>\n")
            .append("  </div>\n")
            .append("  <div class=\"card-content\">\n")
            .append("    <form method=\"post\" action=\"").append(cp).append("/timetable\" class=\"vertical-form\">\n")
            .append("      <input type=\"hidden\" name=\"action\" value=\"add\">\n")
            .append("      <div class=\"form-row\">\n")
            .append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"slot-day\" class=\"form-label\">Day of Week <span class=\"req\">*</span></label>\n")
            .append("          <select id=\"slot-day\" name=\"dayOfWeek\" required class=\"form-select\">\n");
        for (String d : TimetableSlot.DAYS_OF_WEEK) {
            boolean sel = d.equalsIgnoreCase(currentDayName);
            body.append("            <option value=\"").append(d).append("\" ").append(sel ? "selected" : "").append(">").append(d).append("</option>\n");
        }
        body.append("          </select>\n")
            .append("        </div>\n");

        body.append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"slot-start\" class=\"form-label\">Start Time <span class=\"req\">*</span></label>\n")
            .append("          <input type=\"time\" id=\"slot-start\" name=\"startTime\" required class=\"form-input\" value=\"09:00\">\n")
            .append("        </div>\n");

        body.append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"slot-end\" class=\"form-label\">End Time <span class=\"req\">*</span></label>\n")
            .append("          <input type=\"time\" id=\"slot-end\" name=\"endTime\" required class=\"form-input\" value=\"10:30\">\n")
            .append("        </div>\n")
            .append("      </div>\n");

        body.append("      <div class=\"form-row\">\n")
            .append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"slot-subject\" class=\"form-label\">Subject <span class=\"req\">*</span></label>\n")
            .append("          <select id=\"slot-subject\" name=\"subjectId\" required class=\"form-select\">\n")
            .append("            <option value=\"\">-- Select Subject --</option>\n");
        for (Subject s : subjects) {
            body.append("            <option value=\"").append(HtmlTemplate.escape(s.getId())).append("\">")
                .append(HtmlTemplate.escape(s.getCode())).append(" - ").append(HtmlTemplate.escape(s.getName()))
                .append("</option>\n");
        }
        body.append("          </select>\n")
            .append("        </div>\n");

        String[] activities = {"Lecture", "Lab", "Self-Study", "Revision", "Project Work", "Discussion", "Tutorial", "Seminar"};
        body.append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"slot-activity\" class=\"form-label\">Activity Type</label>\n")
            .append("          <select id=\"slot-activity\" name=\"activityType\" class=\"form-select\">\n");
        for (String act : activities) {
            body.append("            <option value=\"").append(act).append("\">").append(act).append("</option>\n");
        }
        body.append("          </select>\n")
            .append("        </div>\n");

        body.append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"slot-loc\" class=\"form-label\">Location / Room</label>\n")
            .append("          <input type=\"text\" id=\"slot-loc\" name=\"location\" class=\"form-input\" placeholder=\"e.g. Hall 101 or Library Lab B\">\n")
            .append("        </div>\n")
            .append("      </div>\n");

        body.append("      <div class=\"form-actions-bar\">\n")
            .append("        <button type=\"submit\" class=\"btn btn-primary\">Add Timetable Slot</button>\n")
            .append("      </div>\n")
            .append("    </form>\n")
            .append("  </div>\n")
            .append("</div>\n");

        String html = HtmlTemplate.renderPage("Weekly Timetable", "timetable", body.toString(), cp, req);
        PrintWriter out = resp.getWriter();
        out.write(html);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String cp = req.getContextPath();
        String action = req.getParameter("action");

        if ("add".equalsIgnoreCase(action)) {
            String day = req.getParameter("dayOfWeek");
            String start = req.getParameter("startTime");
            String end = req.getParameter("endTime");
            String subjectId = req.getParameter("subjectId");
            String activity = req.getParameter("activityType");
            String location = req.getParameter("location");

            if (day == null || start == null || end == null || subjectId == null || subjectId.trim().isEmpty()) {
                resp.sendRedirect(cp + "/timetable?msg=" + urlEncode("Please fill in day, time and subject.") + "&type=error");
                return;
            }

            TimetableSlot slot = new TimetableSlot(null, day, start, end, subjectId, activity, location);
            service.saveTimetableSlot(slot);

            resp.sendRedirect(cp + "/timetable?msg=" + urlEncode("New slot scheduled for " + slot.getDayOfWeek() + " (" + slot.getStartTime() + " - " + slot.getEndTime() + ")") + "&type=success");
            return;

        } else if ("delete".equalsIgnoreCase(action)) {
            String id = req.getParameter("id");
            boolean deleted = service.deleteTimetableSlot(id);
            if (deleted) {
                resp.sendRedirect(cp + "/timetable?msg=" + urlEncode("Slot removed from timetable.") + "&type=info");
            } else {
                resp.sendRedirect(cp + "/timetable?msg=" + urlEncode("Slot not found.") + "&type=error");
            }
            return;
        }

        resp.sendRedirect(cp + "/timetable");
    }

    private String urlEncode(String text) {
        if (text == null) return "";
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
