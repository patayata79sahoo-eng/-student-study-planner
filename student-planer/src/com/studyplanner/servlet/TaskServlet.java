package com.studyplanner.servlet;

import com.studyplanner.model.Priority;
import com.studyplanner.model.StudyTask;
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
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "TaskServlet", urlPatterns = {"/tasks"})
public class TaskServlet extends HttpServlet {
    private final PlannerService service = PlannerService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        String cp = req.getContextPath();
        String action = req.getParameter("action");

        if ("new".equalsIgnoreCase(action)) {
            renderTaskForm(req, resp, null);
            return;
        } else if ("edit".equalsIgnoreCase(action)) {
            String id = req.getParameter("id");
            StudyTask task = service.getTaskById(id);
            if (task == null) {
                resp.sendRedirect(cp + "/tasks?msg=" + urlEncode("Task not found") + "&type=error");
                return;
            }
            renderTaskForm(req, resp, task);
            return;
        } else if ("delete_confirm".equalsIgnoreCase(action)) {
            String id = req.getParameter("id");
            StudyTask task = service.getTaskById(id);
            if (task == null) {
                resp.sendRedirect(cp + "/tasks?msg=" + urlEncode("Task not found") + "&type=error");
                return;
            }
            renderDeleteConfirm(req, resp, task);
            return;
        }

        // Default: Render Tasks List View
        renderTaskList(req, resp);
    }

    private void renderTaskList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String cp = req.getContextPath();
        String subjectFilter = req.getParameter("subjectId");
        String priorityFilter = req.getParameter("priority");
        String statusFilter = req.getParameter("status");
        String query = req.getParameter("q");
        String filterParam = req.getParameter("filter");

        // Special deadline mode check
        boolean isDeadlineMode = "deadlines".equalsIgnoreCase(filterParam);
        if (isDeadlineMode && (statusFilter == null || statusFilter.isEmpty())) {
            statusFilter = "PENDING";
        }

        List<Subject> subjects = service.getAllSubjects();
        List<StudyTask> tasks = service.filterTasks(subjectFilter, priorityFilter, statusFilter, query);

        StringBuilder body = new StringBuilder();

        // Page Header
        body.append("<div class=\"page-header\">\n")
            .append("  <div class=\"page-title-group\">\n")
            .append("    <h1 class=\"page-title\">")
            .append(isDeadlineMode ? "&#9200; Assignment &amp; Exam Deadlines" : "&#9745; Study Tasks &amp; Assignments")
            .append("</h1>\n")
            .append("    <p class=\"page-subtitle\">")
            .append(isDeadlineMode ? "Track time remaining, prioritize urgent work, and avoid late submissions."
                                   : "Manage, track, and complete your academic work with ease.")
            .append("</p>\n")
            .append("  </div>\n")
            .append("  <div class=\"page-actions\">\n")
            .append("    <a href=\"").append(cp).append("/tasks?action=new\" class=\"btn btn-primary\">+ Create Task</a>\n")
            .append("  </div>\n")
            .append("</div>\n");

        // Filter / Search Card
        body.append("<div class=\"filter-card\">\n")
            .append("  <form method=\"get\" action=\"").append(cp).append("/tasks\" class=\"filter-form\">\n");

        if (isDeadlineMode) {
            body.append("    <input type=\"hidden\" name=\"filter\" value=\"deadlines\">\n");
        }

        body.append("    <div class=\"form-group filter-search\">\n")
            .append("      <label for=\"search-input\" class=\"form-label-sm\">Search Tasks</label>\n")
            .append("      <input type=\"text\" id=\"search-input\" name=\"q\" class=\"form-input\" placeholder=\"Search by title or notes...\" value=\"").append(HtmlTemplate.escape(query)).append("\">\n")
            .append("    </div>\n");

        body.append("    <div class=\"form-group filter-select\">\n")
            .append("      <label for=\"subject-filter\" class=\"form-label-sm\">Subject</label>\n")
            .append("      <select id=\"subject-filter\" name=\"subjectId\" class=\"form-select\">\n")
            .append("        <option value=\"ALL\">All Subjects</option>\n");
        for (Subject s : subjects) {
            String selected = (subjectFilter != null && subjectFilter.equalsIgnoreCase(s.getId())) ? "selected" : "";
            body.append("        <option value=\"").append(HtmlTemplate.escape(s.getId())).append("\" ").append(selected).append(">")
                .append(HtmlTemplate.escape(s.getCode())).append(" - ").append(HtmlTemplate.escape(s.getName()))
                .append("</option>\n");
        }
        body.append("      </select>\n")
            .append("    </div>\n");

        body.append("    <div class=\"form-group filter-select\">\n")
            .append("      <label for=\"priority-filter\" class=\"form-label-sm\">Priority</label>\n")
            .append("      <select id=\"priority-filter\" name=\"priority\" class=\"form-select\">\n")
            .append("        <option value=\"ALL\">All Priorities</option>\n");
        for (Priority p : Priority.values()) {
            String selected = (priorityFilter != null && priorityFilter.equalsIgnoreCase(p.name())) ? "selected" : "";
            body.append("        <option value=\"").append(p.name()).append("\" ").append(selected).append(">")
                .append(p.getDisplayName())
                .append("</option>\n");
        }
        body.append("      </select>\n")
            .append("    </div>\n");

        body.append("    <div class=\"form-group filter-select\">\n")
            .append("      <label for=\"status-filter\" class=\"form-label-sm\">Status</label>\n")
            .append("      <select id=\"status-filter\" name=\"status\" class=\"form-select\">\n")
            .append("        <option value=\"ALL\" ").append("ALL".equalsIgnoreCase(statusFilter) ? "selected" : "").append(">All Statuses</option>\n")
            .append("        <option value=\"PENDING\" ").append("PENDING".equalsIgnoreCase(statusFilter) ? "selected" : "").append(">Pending</option>\n")
            .append("        <option value=\"COMPLETED\" ").append("COMPLETED".equalsIgnoreCase(statusFilter) ? "selected" : "").append(">Completed</option>\n")
            .append("        <option value=\"OVERDUE\" ").append("OVERDUE".equalsIgnoreCase(statusFilter) ? "selected" : "").append(">Overdue</option>\n")
            .append("        <option value=\"DUE_SOON\" ").append("DUE_SOON".equalsIgnoreCase(statusFilter) ? "selected" : "").append(">Due in &le; 3 Days</option>\n")
            .append("      </select>\n")
            .append("    </div>\n");

        body.append("    <div class=\"form-group filter-buttons\">\n")
            .append("      <button type=\"submit\" class=\"btn btn-primary btn-sm\">Apply Filters</button>\n")
            .append("      <a href=\"").append(cp).append("/tasks").append(isDeadlineMode ? "?filter=deadlines" : "").append("\" class=\"btn btn-secondary btn-sm\">Reset</a>\n")
            .append("    </div>\n")
            .append("  </form>\n")
            .append("</div>\n");

        // Status Quick-Toggle Tabs
        body.append("<div class=\"quick-tabs-container\">\n")
            .append("  <a href=\"").append(cp).append("/tasks\" class=\"tab-link ").append(!isDeadlineMode && (statusFilter == null || statusFilter.equalsIgnoreCase("ALL")) ? "tab-active" : "").append("\">All Tasks (").append(service.getTotalTaskCount()).append(")</a>\n")
            .append("  <a href=\"").append(cp).append("/tasks?status=PENDING\" class=\"tab-link ").append(!isDeadlineMode && "PENDING".equalsIgnoreCase(statusFilter) ? "tab-active" : "").append("\">Pending (").append(service.getPendingTaskCount()).append(")</a>\n")
            .append("  <a href=\"").append(cp).append("/tasks?status=COMPLETED\" class=\"tab-link ").append(!isDeadlineMode && "COMPLETED".equalsIgnoreCase(statusFilter) ? "tab-active" : "").append("\">Completed (").append(service.getCompletedTaskCount()).append(")</a>\n")
            .append("  <a href=\"").append(cp).append("/tasks?status=OVERDUE\" class=\"tab-link ").append(!isDeadlineMode && "OVERDUE".equalsIgnoreCase(statusFilter) ? "tab-active" : "").append("\">Overdue (").append(service.getOverdueTaskCount()).append(")</a>\n")
            .append("  <a href=\"").append(cp).append("/tasks?filter=deadlines\" class=\"tab-link ").append(isDeadlineMode ? "tab-active" : "").append("\">&#9200; Deadline View</a>\n")
            .append("</div>\n");

        // Tasks Table / List Card
        body.append("<div class=\"dashboard-card\">\n")
            .append("  <div class=\"card-content card-content-flush\">\n");

        if (tasks.isEmpty()) {
            body.append("    <div class=\"empty-state\">\n")
                .append("      <div class=\"empty-icon\">&#128269;</div>\n")
                .append("      <p class=\"empty-title\">No study tasks match the criteria</p>\n")
                .append("      <p class=\"empty-desc\">Try adjusting your filter or create a new task to get started.</p>\n")
                .append("      <a href=\"").append(cp).append("/tasks?action=new\" class=\"btn btn-primary\">+ Create First Task</a>\n")
                .append("    </div>\n");
        } else {
            body.append("    <table class=\"data-table\">\n")
                .append("      <thead>\n")
                .append("        <tr>\n")
                .append("          <th style=\"width: 48px; text-align: center;\">Done</th>\n")
                .append("          <th>Task Details</th>\n")
                .append("          <th>Subject</th>\n")
                .append("          <th>Type</th>\n")
                .append("          <th>Priority</th>\n")
                .append("          <th>Deadline</th>\n")
                .append("          <th>Est. Hours</th>\n")
                .append("          <th style=\"text-align: right;\">Actions</th>\n")
                .append("        </tr>\n")
                .append("      </thead>\n")
                .append("      <tbody>\n");

            for (StudyTask t : tasks) {
                Subject sub = service.getSubjectById(t.getSubjectId());
                boolean isDone = t.isCompleted();
                String rowClass = isDone ? "row-completed" : (t.isOverdue() ? "row-overdue" : "");

                body.append("        <tr class=\"").append(rowClass).append("\">\n");

                // Toggle Checkbox Column
                body.append("          <td style=\"text-align: center;\">\n")
                    .append("            <form method=\"post\" action=\"").append(cp).append("/tasks\" class=\"inline-form\">\n")
                    .append("              <input type=\"hidden\" name=\"action\" value=\"toggle\">\n")
                    .append("              <input type=\"hidden\" name=\"id\" value=\"").append(HtmlTemplate.escape(t.getId())).append("\">\n")
                    .append("              <input type=\"hidden\" name=\"redirect\" value=\"").append(cp).append("/tasks").append(isDeadlineMode ? "?filter=deadlines" : "").append("\">\n")
                    .append("              <button type=\"submit\" class=\"checkbox-btn ").append(isDone ? "checkbox-checked" : "").append("\" title=\"").append(isDone ? "Mark incomplete" : "Mark completed").append("\">")
                    .append(isDone ? "&#10003;" : "&#9675;")
                    .append("</button>\n")
                    .append("            </form>\n")
                    .append("          </td>\n");

                // Title & Notes Column
                body.append("          <td>\n")
                    .append("            <div class=\"task-table-title ").append(isDone ? "text-strikethrough" : "").append("\">")
                    .append(HtmlTemplate.escape(t.getTitle()))
                    .append("</div>\n");
                if (!t.getNotes().isEmpty()) {
                    body.append("            <div class=\"task-table-notes\">").append(HtmlTemplate.escape(t.getNotes())).append("</div>\n");
                }
                body.append("          </td>\n");

                // Subject Column
                body.append("          <td>").append(HtmlTemplate.renderSubjectPill(sub)).append("</td>\n");

                // Type Column
                body.append("          <td><span class=\"badge-type\">").append(HtmlTemplate.escape(t.getTaskType())).append("</span></td>\n");

                // Priority Column
                body.append("          <td>").append(HtmlTemplate.renderPriorityBadge(t.getPriority())).append("</td>\n");

                // Deadline Column
                body.append("          <td>\n")
                    .append("            <div class=\"deadline-cell\">\n")
                    .append("              <span class=\"deadline-date\">").append(HtmlTemplate.escape(t.getDeadlineFormatted())).append("</span>\n")
                    .append("              ").append(HtmlTemplate.renderDeadlineBadge(t)).append("\n")
                    .append("            </div>\n")
                    .append("          </td>\n");

                // Est Hours
                body.append("          <td><span class=\"hours-badge\">&#9201; ").append(t.getEstimatedHours()).append(" hrs</span></td>\n");

                // Actions Column
                body.append("          <td style=\"text-align: right; white-space: nowrap;\">\n")
                    .append("            <a href=\"").append(cp).append("/tasks?action=edit&id=").append(HtmlTemplate.escape(t.getId())).append("\" class=\"btn-icon\" title=\"Edit Task\">&#9998; Edit</a>\n")
                    .append("            <a href=\"").append(cp).append("/tasks?action=delete_confirm&id=").append(HtmlTemplate.escape(t.getId())).append("\" class=\"btn-icon btn-icon-danger\" title=\"Delete Task\">&#128465; Delete</a>\n")
                    .append("          </td>\n");

                body.append("        </tr>\n");
            }

            body.append("      </tbody>\n")
                .append("    </table>\n");
        }

        body.append("  </div>\n")
            .append("</div>\n");

        String activeNav = isDeadlineMode ? "deadlines" : "tasks";
        String html = HtmlTemplate.renderPage(isDeadlineMode ? "Deadlines" : "Study Tasks", activeNav, body.toString(), cp, req);
        PrintWriter out = resp.getWriter();
        out.write(html);
    }

    private void renderTaskForm(HttpServletRequest req, HttpServletResponse resp, StudyTask task) throws IOException {
        String cp = req.getContextPath();
        boolean isEdit = (task != null);
        List<Subject> subjects = service.getAllSubjects();

        StringBuilder body = new StringBuilder();

        body.append("<div class=\"form-page-container\">\n")
            .append("  <div class=\"form-card\">\n")
            .append("    <div class=\"form-card-header\">\n")
            .append("      <h2 class=\"form-card-title\">").append(isEdit ? "&#9998; Edit Study Task" : "+ Add New Study Task").append("</h2>\n")
            .append("      <p class=\"form-card-subtitle\">").append(isEdit ? "Update your assignment deadline, priority or details." : "Fill in the details below to schedule your new study task.").append("</p>\n")
            .append("    </div>\n")
            .append("    <form method=\"post\" action=\"").append(cp).append("/tasks\" class=\"vertical-form\">\n")
            .append("      <input type=\"hidden\" name=\"action\" value=\"").append(isEdit ? "edit" : "add").append("\">\n");

        if (isEdit) {
            body.append("      <input type=\"hidden\" name=\"id\" value=\"").append(HtmlTemplate.escape(task.getId())).append("\">\n");
        }

        // Title Input
        body.append("      <div class=\"form-group\">\n")
            .append("        <label for=\"task-title\" class=\"form-label\">Task Title <span class=\"req\">*</span></label>\n")
            .append("        <input type=\"text\" id=\"task-title\" name=\"title\" required class=\"form-input\" placeholder=\"e.g. Implement Dijkstra's Algorithm in Java\" value=\"")
            .append(isEdit ? HtmlTemplate.escape(task.getTitle()) : "")
            .append("\">\n")
            .append("      </div>\n");

        // Subject & Task Type Row
        body.append("      <div class=\"form-row\">\n")
            .append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"task-subject\" class=\"form-label\">Subject <span class=\"req\">*</span></label>\n")
            .append("          <select id=\"task-subject\" name=\"subjectId\" required class=\"form-select\">\n")
            .append("            <option value=\"\">-- Select Subject --</option>\n");
        for (Subject s : subjects) {
            boolean sel = isEdit && s.getId().equalsIgnoreCase(task.getSubjectId());
            body.append("            <option value=\"").append(HtmlTemplate.escape(s.getId())).append("\" ").append(sel ? "selected" : "").append(">")
                .append(HtmlTemplate.escape(s.getCode())).append(" - ").append(HtmlTemplate.escape(s.getName()))
                .append("</option>\n");
        }
        body.append("          </select>\n")
            .append("        </div>\n");

        String currentType = isEdit ? task.getTaskType() : "Assignment";
        String[] types = {"Assignment", "Project", "Exam Prep", "Lab Report", "Reading", "Revision", "Homework", "Presentation"};

        body.append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"task-type\" class=\"form-label\">Task Type</label>\n")
            .append("          <select id=\"task-type\" name=\"taskType\" class=\"form-select\">\n");
        for (String t : types) {
            String sel = currentType.equalsIgnoreCase(t) ? "selected" : "";
            body.append("            <option value=\"").append(t).append("\" ").append(sel).append(">").append(t).append("</option>\n");
        }
        body.append("          </select>\n")
            .append("        </div>\n")
            .append("      </div>\n");

        // Priority & Deadline Row
        Priority currentPri = isEdit ? task.getPriority() : Priority.MEDIUM;
        String deadlineVal = isEdit ? task.getDeadlineFormatted() : LocalDate.now().plusDays(3).format(StudyTask.DATE_FORMAT);
        double hoursVal = isEdit ? task.getEstimatedHours() : 2.0;

        body.append("      <div class=\"form-row\">\n")
            .append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"task-priority\" class=\"form-label\">Priority Level <span class=\"req\">*</span></label>\n")
            .append("          <select id=\"task-priority\" name=\"priority\" required class=\"form-select\">\n");
        for (Priority p : Priority.values()) {
            String sel = (p == currentPri) ? "selected" : "";
            body.append("            <option value=\"").append(p.name()).append("\" ").append(sel).append(">")
                .append(p.getDisplayName()).append(" Priority")
                .append("</option>\n");
        }
        body.append("          </select>\n")
            .append("        </div>\n");

        body.append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"task-deadline\" class=\"form-label\">Due Date / Deadline <span class=\"req\">*</span></label>\n")
            .append("          <input type=\"date\" id=\"task-deadline\" name=\"deadline\" required class=\"form-input\" value=\"").append(deadlineVal).append("\">\n")
            .append("        </div>\n");

        body.append("        <div class=\"form-group form-col\">\n")
            .append("          <label for=\"task-hours\" class=\"form-label\">Estimated Hours</label>\n")
            .append("          <input type=\"number\" id=\"task-hours\" name=\"estimatedHours\" step=\"0.5\" min=\"0.5\" max=\"100\" class=\"form-input\" value=\"").append(hoursVal).append("\">\n")
            .append("        </div>\n")
            .append("      </div>\n");

        // Completed Checkbox (if edit)
        if (isEdit) {
            body.append("      <div class=\"form-group form-checkbox-group\">\n")
                .append("        <label class=\"checkbox-label\">\n")
                .append("          <input type=\"checkbox\" name=\"completed\" value=\"true\" ").append(task.isCompleted() ? "checked" : "").append(">\n")
                .append("          <span>Mark this task as completed</span>\n")
                .append("        </label>\n")
                .append("      </div>\n");
        }

        // Notes Textarea
        body.append("      <div class=\"form-group\">\n")
            .append("        <label for=\"task-notes\" class=\"form-label\">Study Notes &amp; Instructions</label>\n")
            .append("        <textarea id=\"task-notes\" name=\"notes\" rows=\"4\" class=\"form-textarea\" placeholder=\"Add any submission links, textbook chapters, or reminders...\">")
            .append(isEdit ? HtmlTemplate.escape(task.getNotes()) : "")
            .append("</textarea>\n")
            .append("      </div>\n");

        // Action Buttons
        body.append("      <div class=\"form-actions-bar\">\n")
            .append("        <button type=\"submit\" class=\"btn btn-primary\">").append(isEdit ? "Save Changes" : "Create Task").append("</button>\n")
            .append("        <a href=\"").append(cp).append("/tasks\" class=\"btn btn-secondary\">Cancel</a>\n")
            .append("      </div>\n")
            .append("    </form>\n")
            .append("  </div>\n")
            .append("</div>\n");

        String html = HtmlTemplate.renderPage(isEdit ? "Edit Task" : "New Task", "tasks", body.toString(), cp, req);
        PrintWriter out = resp.getWriter();
        out.write(html);
    }

    private void renderDeleteConfirm(HttpServletRequest req, HttpServletResponse resp, StudyTask task) throws IOException {
        String cp = req.getContextPath();
        StringBuilder body = new StringBuilder();

        body.append("<div class=\"confirm-modal-wrapper\">\n")
            .append("  <div class=\"confirm-card\">\n")
            .append("    <div class=\"confirm-icon-box\">&#9888;</div>\n")
            .append("    <h2 class=\"confirm-title\">Delete Study Task?</h2>\n")
            .append("    <p class=\"confirm-text\">Are you sure you want to permanently delete <strong>&ldquo;").append(HtmlTemplate.escape(task.getTitle())).append("&rdquo;</strong>?</p>\n")
            .append("    <p class=\"confirm-subtext\">This action cannot be undone and will remove the task from all progress calculations.</p>\n")
            .append("    <form method=\"post\" action=\"").append(cp).append("/tasks\" class=\"confirm-actions-form\">\n")
            .append("      <input type=\"hidden\" name=\"action\" value=\"delete\">\n")
            .append("      <input type=\"hidden\" name=\"id\" value=\"").append(HtmlTemplate.escape(task.getId())).append("\">\n")
            .append("      <button type=\"submit\" class=\"btn btn-danger\">Yes, Delete Task</button>\n")
            .append("      <a href=\"").append(cp).append("/tasks\" class=\"btn btn-secondary\">Cancel</a>\n")
            .append("    </form>\n")
            .append("  </div>\n")
            .append("</div>\n");

        String html = HtmlTemplate.renderPage("Confirm Delete Task", "tasks", body.toString(), cp, req);
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
            String title = req.getParameter("title");
            String subjectId = req.getParameter("subjectId");
            String taskType = req.getParameter("taskType");
            String priorityStr = req.getParameter("priority");
            String deadlineStr = req.getParameter("deadline");
            String hoursStr = req.getParameter("estimatedHours");
            String notes = req.getParameter("notes");

            if (title == null || title.trim().isEmpty()) {
                resp.sendRedirect(cp + "/tasks?action=new&msg=" + urlEncode("Task title is required") + "&type=error");
                return;
            }

            LocalDate deadline = LocalDate.now().plusDays(3);
            try {
                if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
                    deadline = LocalDate.parse(deadlineStr.trim(), StudyTask.DATE_FORMAT);
                }
            } catch (Exception ignored) {}

            double hours = 2.0;
            try {
                if (hoursStr != null && !hoursStr.trim().isEmpty()) {
                    hours = Double.parseDouble(hoursStr.trim());
                }
            } catch (NumberFormatException ignored) {}

            Priority priority = Priority.fromString(priorityStr);
            StudyTask task = new StudyTask(null, title.trim(), subjectId, taskType, priority, deadline, hours, false, notes);
            service.saveTask(task);

            resp.sendRedirect(cp + "/tasks?msg=" + urlEncode("Task \"" + task.getTitle() + "\" created successfully!") + "&type=success");
            return;

        } else if ("edit".equalsIgnoreCase(action)) {
            String id = req.getParameter("id");
            StudyTask existing = service.getTaskById(id);
            if (existing == null) {
                resp.sendRedirect(cp + "/tasks?msg=" + urlEncode("Task not found") + "&type=error");
                return;
            }

            String title = req.getParameter("title");
            String subjectId = req.getParameter("subjectId");
            String taskType = req.getParameter("taskType");
            String priorityStr = req.getParameter("priority");
            String deadlineStr = req.getParameter("deadline");
            String hoursStr = req.getParameter("estimatedHours");
            String completedStr = req.getParameter("completed");
            String notes = req.getParameter("notes");

            if (title != null && !title.trim().isEmpty()) {
                existing.setTitle(title.trim());
            }
            existing.setSubjectId(subjectId);
            existing.setTaskType(taskType);
            existing.setPriority(Priority.fromString(priorityStr));

            try {
                if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
                    existing.setDeadline(LocalDate.parse(deadlineStr.trim(), StudyTask.DATE_FORMAT));
                }
            } catch (Exception ignored) {}

            try {
                if (hoursStr != null && !hoursStr.trim().isEmpty()) {
                    existing.setEstimatedHours(Double.parseDouble(hoursStr.trim()));
                }
            } catch (NumberFormatException ignored) {}

            existing.setCompleted("true".equalsIgnoreCase(completedStr));
            existing.setNotes(notes != null ? notes : "");

            service.saveTask(existing);
            resp.sendRedirect(cp + "/tasks?msg=" + urlEncode("Task \"" + existing.getTitle() + "\" updated successfully!") + "&type=success");
            return;

        } else if ("toggle".equalsIgnoreCase(action)) {
            String id = req.getParameter("id");
            String redirect = req.getParameter("redirect");
            boolean toggled = service.toggleTaskCompletion(id);

            String dest = (redirect != null && !redirect.trim().isEmpty()) ? redirect : (cp + "/tasks");
            String separator = dest.contains("?") ? "&" : "?";
            if (toggled) {
                StudyTask task = service.getTaskById(id);
                String state = (task != null && task.isCompleted()) ? "marked as completed! Great job!" : "marked as pending.";
                resp.sendRedirect(dest + separator + "msg=" + urlEncode("Task " + state) + "&type=success");
            } else {
                resp.sendRedirect(dest + separator + "msg=" + urlEncode("Task not found.") + "&type=error");
            }
            return;

        } else if ("delete".equalsIgnoreCase(action)) {
            String id = req.getParameter("id");
            boolean deleted = service.deleteTask(id);
            if (deleted) {
                resp.sendRedirect(cp + "/tasks?msg=" + urlEncode("Task deleted successfully.") + "&type=info");
            } else {
                resp.sendRedirect(cp + "/tasks?msg=" + urlEncode("Failed to delete task.") + "&type=error");
            }
            return;
        }

        resp.sendRedirect(cp + "/tasks");
    }

    private String urlEncode(String text) {
        if (text == null) return "";
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}

