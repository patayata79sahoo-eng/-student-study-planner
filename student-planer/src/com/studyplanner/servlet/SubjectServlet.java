package com.studyplanner.servlet;

import com.studyplanner.model.Subject;
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
import java.util.List;

@WebServlet(name = "SubjectServlet", urlPatterns = {"/subjects"})
public class SubjectServlet extends HttpServlet {
    private final PlannerService service = PlannerService.getInstance();

    private static final String[] COLOR_PRESETS = {
        "#2563eb", // Royal Blue
        "#059669", // Emerald Green
        "#7c3aed", // Violet / Purple
        "#d97706", // Amber / Warm Orange
        "#dc2626", // Crimson Red
        "#0284c7", // Sky Blue
        "#4f46e5", // Indigo
        "#db2777"  // Deep Pink
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        String cp = req.getContextPath();
        String action = req.getParameter("action");

        if ("new".equalsIgnoreCase(action)) {
            renderSubjectForm(req, resp, null);
            return;
        } else if ("edit".equalsIgnoreCase(action)) {
            String id = req.getParameter("id");
            Subject subject = service.getSubjectById(id);
            if (subject == null) {
                resp.sendRedirect(cp + "/subjects?msg=" + urlEncode("Subject not found") + "&type=error");
                return;
            }
            renderSubjectForm(req, resp, subject);
            return;
        } else if ("delete_confirm".equalsIgnoreCase(action)) {
            String id = req.getParameter("id");
            Subject subject = service.getSubjectById(id);
            if (subject == null) {
                resp.sendRedirect(cp + "/subjects?msg=" + urlEncode("Subject not found") + "&type=error");
                return;
            }
            renderDeleteConfirm(req, resp, subject);
            return;
        }

        renderSubjectList(req, resp);
    }

    private void renderSubjectList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String cp = req.getContextPath();
        List<PlannerService.SubjectProgress> progressList = service.getSubjectProgressList();

        StringBuilder body = new StringBuilder();

        // Page Header
        body.append("<div class=\"page-header\">\n")
            .append("  <div class=\"page-title-group\">\n")
            .append("    <h1 class=\"page-title\">&#128218; Subject Management</h1>\n")
            .append("    <p class=\"page-subtitle\">Organize your courses, track instructor info, and monitor study progress per subject.</p>\n")
            .append("  </div>\n")
            .append("  <div class=\"page-actions\">\n")
            .append("    <a href=\"").append(cp).append("/subjects?action=new\" class=\"btn btn-primary\">+ Add Subject</a>\n")
            .append("  </div>\n")
            .append("</div>\n");

        if (progressList.isEmpty()) {
            body.append("<div class=\"dashboard-card\">\n")
                .append("  <div class=\"card-content\">\n")
                .append("    <div class=\"empty-state\">\n")
                .append("      <div class=\"empty-icon\">&#128218;</div>\n")
                .append("      <p class=\"empty-title\">No subjects added yet</p>\n")
                .append("      <p class=\"empty-desc\">Create your semester courses to start assigning tasks and timetable slots.</p>\n")
                .append("      <a href=\"").append(cp).append("/subjects?action=new\" class=\"btn btn-primary\">+ Add First Subject</a>\n")
                .append("    </div>\n")
                .append("  </div>\n")
                .append("</div>\n");
        } else {
            body.append("<div class=\"subjects-grid\">\n");
            for (PlannerService.SubjectProgress sp : progressList) {
                Subject s = sp.getSubject();
                String col = s.getColor();

                body.append("  <div class=\"subject-card\" style=\"border-top: 4px solid ").append(col).append(";\">\n")
                    .append("    <div class=\"subject-card-body\">\n")
                    .append("      <div class=\"subject-card-header\">\n")
                    .append("        <div class=\"subject-header-main\">\n")
                    .append("          <span class=\"subject-code-tag\" style=\"background-color: ").append(col).append("18; color: ").append(col).append(";\">")
                    .append(HtmlTemplate.escape(s.getCode()))
                    .append("</span>\n")
                    .append("          <h3 class=\"subject-title\">").append(HtmlTemplate.escape(s.getName())).append("</h3>\n")
                    .append("        </div>\n")
                    .append("        <span class=\"color-dot-indicator\" style=\"background-color: ").append(col).append(";\" title=\"Color Theme\"></span>\n")
                    .append("      </div>\n");

                body.append("      <div class=\"subject-meta-list\">\n");
                if (!s.getInstructor().isEmpty()) {
                    body.append("        <div class=\"subject-meta-item\">\n")
                        .append("          <span class=\"meta-icon\">&#128104;&#8205;&#127979;</span>\n")
                        .append("          <span class=\"meta-text\">").append(HtmlTemplate.escape(s.getInstructor())).append("</span>\n")
                        .append("        </div>\n");
                }
                body.append("        <div class=\"subject-meta-item\">\n")
                    .append("          <span class=\"meta-icon\">&#9201;</span>\n")
                    .append("          <span class=\"meta-text\">Target: <strong>").append(s.getTargetHours()).append(" hrs/week</strong></span>\n")
                    .append("        </div>\n")
                    .append("      </div>\n");

                // Progress Bar Section
                body.append("      <div class=\"subject-progress-section\">\n")
                    .append("        <div class=\"subject-progress-labels\">\n")
                    .append("          <span class=\"progress-label-left\">Task Completion</span>\n")
                    .append("          <span class=\"progress-label-right\"><strong>").append(sp.getCompletedTasks()).append("/").append(sp.getTotalTasks()).append("</strong> (").append(sp.getPercentage()).append("%)</span>\n")
                    .append("        </div>\n")
                    .append("        <div class=\"progress-bar-container\">\n")
                    .append("          <div class=\"progress-bar-fill\" style=\"width: ").append(sp.getPercentage()).append("%; background-color: ").append(col).append(";\"></div>\n")
                    .append("        </div>\n")
                    .append("        <div class=\"subject-hours-stat\">\n")
                    .append("          <span>Pending: ").append(sp.getPendingTasks()).append("</span>\n")
                    .append("          <span>&bull;</span>\n")
                    .append("          <span>Rem. Effort: ~").append(String.format("%.1f", sp.getRemainingHours())).append("h</span>\n")
                    .append("        </div>\n")
                    .append("      </div>\n");

                // Card Footer Actions
                body.append("      <div class=\"subject-card-footer\">\n")
                    .append("        <a href=\"").append(cp).append("/tasks?subjectId=").append(HtmlTemplate.escape(s.getId())).append("\" class=\"btn btn-xs btn-outline-primary\">View Tasks &rarr;</a>\n")
                    .append("        <div class=\"card-actions-right\">\n")
                    .append("          <a href=\"").append(cp).append("/subjects?action=edit&id=").append(HtmlTemplate.escape(s.getId())).append("\" class=\"btn-icon\" title=\"Edit Subject\">&#9998; Edit</a>\n")
                    .append("          <a href=\"").append(cp).append("/subjects?action=delete_confirm&id=").append(HtmlTemplate.escape(s.getId())).append("\" class=\"btn-icon btn-icon-danger\" title=\"Delete Subject\">&#128465; Delete</a>\n")
                    .append("        </div>\n")
                    .append("      </div>\n")
                    .append("    </div>\n")
                    .append("  </div>\n");
            }
            body.append("</div>\n");
        }

        String html = HtmlTemplate.renderPage("Subjects", "subjects", body.toString(), cp, req);
        PrintWriter out = resp.getWriter();
        out.write(html);
    }

    private void renderSubjectForm(HttpServletRequest req, HttpServletResponse resp, Subject subject) throws IOException {
        String cp = req.getContextPath();
        boolean isEdit = (subject != null);
        String selectedColor = isEdit ? subject.getColor() : COLOR_PRESETS[0];

        StringBuilder body = new StringBuilder();

        body.append("<div class=\"form-page-container\">\n")
            .append("  <div class=\"form-card\">\n")
            .append("    <div class=\"form-card-header\">\n")
            .append("      <h2 class=\"form-card-title\">").append(isEdit ? "&#9998; Edit Subject" : "+ Add New Subject").append("</h2>\n")
            .append("      <p class=\"form-card-subtitle\">").append(isEdit ? "Update subject title, course code or target study hours." : "Add a course or module to your study schedule.").append("</p>\n")
            .append("    </div>\n")
            .append("    <form method=\"post\" action=\"").append(cp).append("/subjects\" class=\"vertical-form\">\n")
            .append("      <input type=\"hidden\" name=\"action\" value=\"").append(isEdit ? "edit" : "add").append("\">\n");

        if (isEdit) {
            body.append("      <input type=\"hidden\" name=\"id\" value=\"").append(HtmlTemplate.escape(subject.getId())).append("\">\n");
        }

        // Subject Name
        body.append("      <div class=\"form-group\">\n")
            .append("        <label for=\"sub-name\" class=\"form-label\">Subject Name <span class=\"req\">*</span></label>\n")
            .append("        <input type=\"text\" id=\"sub-name\" name=\"name\" required class=\"form-input\" placeholder=\"e.g. Data Structures & Algorithms\" value=\"")
            .append(isEdit ? HtmlTemplate.escape(subject.getName()) : "")
            .append("\">\n")
            .append("      </div>\n");

        // Code & Target Hours Row
        body.append("      <div class=\"form-row\">\n")
            .append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"sub-code\" class=\"form-label\">Course Code <span class=\"req\">*</span></label>\n")
            .append("          <input type=\"text\" id=\"sub-code\" name=\"code\" required class=\"form-input\" placeholder=\"e.g. CS-201\" value=\"")
            .append(isEdit ? HtmlTemplate.escape(subject.getCode()) : "")
            .append("\">\n")
            .append("        </div>\n")
            .append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"sub-hours\" class=\"form-label\">Target Hours / Week</label>\n")
            .append("          <input type=\"number\" id=\"sub-hours\" name=\"targetHours\" min=\"1\" max=\"40\" class=\"form-input\" value=\"")
            .append(isEdit ? subject.getTargetHours() : 5)
            .append("\">\n")
            .append("        </div>\n")
            .append("      </div>\n");

        // Instructor Name
        body.append("      <div class=\"form-group\">\n")
            .append("        <label for=\"sub-instructor\" class=\"form-label\">Instructor / Professor Name</label>\n")
            .append("        <input type=\"text\" id=\"sub-instructor\" name=\"instructor\" class=\"form-input\" placeholder=\"e.g. Dr. Alan Turing\" value=\"")
            .append(isEdit ? HtmlTemplate.escape(subject.getInstructor()) : "")
            .append("\">\n")
            .append("      </div>\n");

        // Color Selection (Color presets + Custom color input)
        body.append("      <div class=\"form-group\">\n")
            .append("        <label class=\"form-label\">Theme Accent Color</label>\n")
            .append("        <div class=\"color-picker-group\">\n");

        for (String c : COLOR_PRESETS) {
            boolean checked = c.equalsIgnoreCase(selectedColor);
            body.append("          <label class=\"color-radio-label\" style=\"background-color: ").append(c).append(";\">\n")
                .append("            <input type=\"radio\" name=\"color\" value=\"").append(c).append("\" ").append(checked ? "checked" : "").append(">\n")
                .append("            <span class=\"color-radio-check\">&#10003;</span>\n")
                .append("          </label>\n");
        }
        body.append("          <div class=\"custom-color-input-box\">\n")
            .append("            <input type=\"color\" name=\"colorCustom\" id=\"colorCustom\" value=\"").append(selectedColor).append("\" title=\"Custom Color\">\n")
            .append("            <label for=\"colorCustom\" class=\"color-custom-label\">Custom</label>\n")
            .append("          </div>\n");

        body.append("        </div>\n")
            .append("      </div>\n");

        // Action Buttons
        body.append("      <div class=\"form-actions-bar\">\n")
            .append("        <button type=\"submit\" class=\"btn btn-primary\">").append(isEdit ? "Save Subject" : "Create Subject").append("</button>\n")
            .append("        <a href=\"").append(cp).append("/subjects\" class=\"btn btn-secondary\">Cancel</a>\n")
            .append("      </div>\n")
            .append("    </form>\n")
            .append("  </div>\n")
            .append("</div>\n");

        String html = HtmlTemplate.renderPage(isEdit ? "Edit Subject" : "New Subject", "subjects", body.toString(), cp, req);
        PrintWriter out = resp.getWriter();
        out.write(html);
    }

    private void renderDeleteConfirm(HttpServletRequest req, HttpServletResponse resp, Subject subject) throws IOException {
        String cp = req.getContextPath();
        StringBuilder body = new StringBuilder();

        body.append("<div class=\"confirm-modal-wrapper\">\n")
            .append("  <div class=\"confirm-card\">\n")
            .append("    <div class=\"confirm-icon-box\">&#9888;</div>\n")
            .append("    <h2 class=\"confirm-title\">Delete Subject?</h2>\n")
            .append("    <p class=\"confirm-text\">Are you sure you want to delete <strong>&ldquo;").append(HtmlTemplate.escape(subject.getName())).append("&rdquo;</strong> (").append(HtmlTemplate.escape(subject.getCode())).append(")?</p>\n")
            .append("    <p class=\"confirm-subtext\">Any associated tasks will become unassigned, and its timetable slots will be removed.</p>\n")
            .append("    <form method=\"post\" action=\"").append(cp).append("/subjects\" class=\"confirm-actions-form\">\n")
            .append("      <input type=\"hidden\" name=\"action\" value=\"delete\">\n")
            .append("      <input type=\"hidden\" name=\"id\" value=\"").append(HtmlTemplate.escape(subject.getId())).append("\">\n")
            .append("      <button type=\"submit\" class=\"btn btn-danger\">Yes, Delete Subject</button>\n")
            .append("      <a href=\"").append(cp).append("/subjects\" class=\"btn btn-secondary\">Cancel</a>\n")
            .append("    </form>\n")
            .append("  </div>\n")
            .append("</div>\n");

        String html = HtmlTemplate.renderPage("Confirm Delete Subject", "subjects", body.toString(), cp, req);
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
            String name = req.getParameter("name");
            String code = req.getParameter("code");
            String instructor = req.getParameter("instructor");
            String color = req.getParameter("color");
            String colorCustom = req.getParameter("colorCustom");
            String targetHoursStr = req.getParameter("targetHours");

            if (name == null || name.trim().isEmpty() || code == null || code.trim().isEmpty()) {
                resp.sendRedirect(cp + "/subjects?action=new&msg=" + urlEncode("Subject name and code are required") + "&type=error");
                return;
            }

            String finalColor = (color != null && !color.trim().isEmpty()) ? color.trim() : "#2563eb";
            if (colorCustom != null && !colorCustom.trim().isEmpty() && !colorCustom.equalsIgnoreCase("#000000")) {
                finalColor = colorCustom.trim();
            }

            int hours = 4;
            try {
                if (targetHoursStr != null && !targetHoursStr.trim().isEmpty()) {
                    hours = Integer.parseInt(targetHoursStr.trim());
                }
            } catch (NumberFormatException ignored) {}

            Subject subject = new Subject(null, name.trim(), code.trim(), finalColor, instructor, hours);
            service.saveSubject(subject);

            resp.sendRedirect(cp + "/subjects?msg=" + urlEncode("Subject \"" + subject.getName() + "\" added successfully!") + "&type=success");
            return;

        } else if ("edit".equalsIgnoreCase(action)) {
            String id = req.getParameter("id");
            Subject existing = service.getSubjectById(id);
            if (existing == null) {
                resp.sendRedirect(cp + "/subjects?msg=" + urlEncode("Subject not found") + "&type=error");
                return;
            }

            String name = req.getParameter("name");
            String code = req.getParameter("code");
            String instructor = req.getParameter("instructor");
            String color = req.getParameter("color");
            String colorCustom = req.getParameter("colorCustom");
            String targetHoursStr = req.getParameter("targetHours");

            if (name != null && !name.trim().isEmpty()) {
                existing.setName(name.trim());
            }
            if (code != null && !code.trim().isEmpty()) {
                existing.setCode(code.trim());
            }
            existing.setInstructor(instructor != null ? instructor.trim() : "");

            if (color != null && !color.trim().isEmpty()) {
                existing.setColor(color.trim());
            }
            if (colorCustom != null && !colorCustom.trim().isEmpty() && !colorCustom.equalsIgnoreCase("#000000")) {
                existing.setColor(colorCustom.trim());
            }

            try {
                if (targetHoursStr != null && !targetHoursStr.trim().isEmpty()) {
                    existing.setTargetHours(Integer.parseInt(targetHoursStr.trim()));
                }
            } catch (NumberFormatException ignored) {}

            service.saveSubject(existing);
            resp.sendRedirect(cp + "/subjects?msg=" + urlEncode("Subject \"" + existing.getName() + "\" updated successfully!") + "&type=success");
            return;

        } else if ("delete".equalsIgnoreCase(action)) {
            String id = req.getParameter("id");
            boolean deleted = service.deleteSubject(id);
            if (deleted) {
                resp.sendRedirect(cp + "/subjects?msg=" + urlEncode("Subject deleted successfully.") + "&type=info");
            } else {
                resp.sendRedirect(cp + "/subjects?msg=" + urlEncode("Failed to delete subject.") + "&type=error");
            }
            return;
        }

        resp.sendRedirect(cp + "/subjects");
    }

    private String urlEncode(String text) {
        if (text == null) return "";
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}

