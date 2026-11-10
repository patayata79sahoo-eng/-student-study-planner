package com.studyplanner.servlet;

import com.studyplanner.model.StudyTask;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet(name = "DashboardServlet", urlPatterns = {"", "/dashboard"})
public class DashboardServlet extends HttpServlet {
    private final PlannerService service = PlannerService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        String cp = req.getContextPath();

        int totalTasks = service.getTotalTaskCount();
        int completedTasks = service.getCompletedTaskCount();
        int pendingTasks = service.getPendingTaskCount();
        int overdueTasks = service.getOverdueTaskCount();
        int completionPct = service.getOverallCompletionPercentage();
        int totalSubjects = service.getAllSubjects().size();
        double remainingHours = service.getRemainingEstimatedHours();
        List<StudyTask> upcomingTasks = service.getUpcomingDeadlines(5);
        List<TimetableSlot> todaySlots = service.getTodaySlots();
        List<PlannerService.SubjectProgress> subjectProgressList = service.getSubjectProgressList();

        StringBuilder body = new StringBuilder();

        // Hero Welcome Banner
        body.append("<section class=\"hero-banner\">\n")
            .append("  <div class=\"hero-content\">\n")
            .append("    <h1 class=\"hero-title\">Welcome back to your Study Hub</h1>\n")
            .append("    <p class=\"hero-subtitle\">Stay organized, conquer your deadlines, and track your academic progress with clarity.</p>\n")
            .append("  </div>\n")
            .append("  <div class=\"hero-actions\">\n")
            .append("    <a href=\"").append(cp).append("/tasks?action=new\" class=\"btn btn-primary\">+ Add Study Task</a>\n")
            .append("    <a href=\"").append(cp).append("/timetable\" class=\"btn btn-outline-white\">View Weekly Timetable</a>\n")
            .append("  </div>\n")
            .append("</section>\n");

        // Stat Metric Cards
        body.append("<section class=\"stats-grid\">\n");

        // Total Tasks
        body.append("  <div class=\"stat-card\">\n")
            .append("    <div class=\"stat-header\">\n")
            .append("      <span class=\"stat-label\">Total Tasks</span>\n")
            .append("      <span class=\"stat-icon icon-blue\">&#128221;</span>\n")
            .append("    </div>\n")
            .append("    <div class=\"stat-value\">").append(totalTasks).append("</div>\n")
            .append("    <div class=\"stat-footer\">Across ").append(totalSubjects).append(" active subjects</div>\n")
            .append("  </div>\n");

        // Completed
        body.append("  <div class=\"stat-card\">\n")
            .append("    <div class=\"stat-header\">\n")
            .append("      <span class=\"stat-label\">Completed</span>\n")
            .append("      <span class=\"stat-icon icon-green\">&#10004;</span>\n")
            .append("    </div>\n")
            .append("    <div class=\"stat-value text-green\">").append(completedTasks).append("</div>\n")
            .append("    <div class=\"stat-footer\">Great momentum!</div>\n")
            .append("  </div>\n");

        // Pending
        body.append("  <div class=\"stat-card\">\n")
            .append("    <div class=\"stat-header\">\n")
            .append("      <span class=\"stat-label\">Pending Tasks</span>\n")
            .append("      <span class=\"stat-icon icon-amber\">&#9200;</span>\n")
            .append("    </div>\n")
            .append("    <div class=\"stat-value text-amber\">").append(pendingTasks).append("</div>\n")
            .append("    <div class=\"stat-footer\">~").append(String.format("%.1f", remainingHours)).append(" hrs estimated</div>\n")
            .append("  </div>\n");

        // Overdue
        body.append("  <div class=\"stat-card\">\n")
            .append("    <div class=\"stat-header\">\n")
            .append("      <span class=\"stat-label\">Overdue</span>\n")
            .append("      <span class=\"stat-icon icon-red\">&#9888;</span>\n")
            .append("    </div>\n")
            .append("    <div class=\"stat-value text-red\">").append(overdueTasks).append("</div>\n")
            .append("    <div class=\"stat-footer\">").append(overdueTasks > 0 ? "Requires prompt focus" : "All caught up!").append("</div>\n")
            .append("  </div>\n");

        // Completion Rate
        body.append("  <div class=\"stat-card stat-card-highlight\">\n")
            .append("    <div class=\"stat-header\">\n")
            .append("      <span class=\"stat-label\">Overall Completion</span>\n")
            .append("      <span class=\"stat-icon icon-royal\">&#128200;</span>\n")
            .append("    </div>\n")
            .append("    <div class=\"stat-value text-primary\">").append(completionPct).append("%</div>\n")
            .append("    <div class=\"progress-bar-container\">\n")
            .append("      <div class=\"progress-bar-fill\" style=\"width: ").append(completionPct).append("%;\"></div>\n")
            .append("    </div>\n")
            .append("  </div>\n");

        body.append("</section>\n");

        // Two Column Layout: Upcoming Deadlines & Today's Schedule
        body.append("<div class=\"dashboard-columns-grid\">\n");

        // Column 1: Urgent & Upcoming Deadlines
        body.append("  <div class=\"dashboard-card\">\n")
            .append("    <div class=\"card-header-bar\">\n")
            .append("      <div class=\"card-title-group\">\n")
            .append("        <h2 class=\"card-title\">&#9200; Urgent &amp; Upcoming Deadlines</h2>\n")
            .append("        <span class=\"badge-count\">").append(upcomingTasks.size()).append("</span>\n")
            .append("      </div>\n")
            .append("      <a href=\"").append(cp).append("/tasks?filter=deadlines\" class=\"card-link\">View All &rarr;</a>\n")
            .append("    </div>\n")
            .append("    <div class=\"card-content\">\n");

        if (upcomingTasks.isEmpty()) {
            body.append("      <div class=\"empty-state\">\n")
                .append("        <div class=\"empty-icon\">&#127881;</div>\n")
                .append("        <p class=\"empty-title\">No pending deadlines!</p>\n")
                .append("        <p class=\"empty-desc\">You are completely up to date with your assignments.</p>\n")
                .append("        <a href=\"").append(cp).append("/tasks?action=new\" class=\"btn btn-sm btn-primary\">+ Add New Task</a>\n")
                .append("      </div>\n");
        } else {
            body.append("      <div class=\"task-mini-list\">\n");
            for (StudyTask task : upcomingTasks) {
                Subject sub = service.getSubjectById(task.getSubjectId());
                body.append("        <div class=\"task-mini-item ").append(task.isOverdue() ? "task-item-overdue" : "").append("\">\n")
                    .append("          <div class=\"task-mini-left\">\n")
                    .append("            <form method=\"post\" action=\"").append(cp).append("/tasks\" class=\"inline-form\">\n")
                    .append("              <input type=\"hidden\" name=\"action\" value=\"toggle\">\n")
                    .append("              <input type=\"hidden\" name=\"id\" value=\"").append(HtmlTemplate.escape(task.getId())).append("\">\n")
                    .append("              <input type=\"hidden\" name=\"redirect\" value=\"").append(cp).append("/dashboard\">\n")
                    .append("              <button type=\"submit\" class=\"checkbox-btn\" title=\"Mark as completed\">&#9675;</button>\n")
                    .append("            </form>\n")
                    .append("            <div class=\"task-mini-info\">\n")
                    .append("              <div class=\"task-mini-title\">")
                    .append(HtmlTemplate.escape(task.getTitle()))
                    .append("              </div>\n")
                    .append("              <div class=\"task-mini-meta\">\n")
                    .append("                ").append(HtmlTemplate.renderSubjectPill(sub)).append("\n")
                    .append("                <span class=\"meta-type\">").append(HtmlTemplate.escape(task.getTaskType())).append("</span>\n")
                    .append("                <span class=\"meta-hours\">&#9201; ").append(task.getEstimatedHours()).append("h</span>\n")
                    .append("              </div>\n")
                    .append("            </div>\n")
                    .append("          </div>\n")
                    .append("          <div class=\"task-mini-right\">\n")
                    .append("            ").append(HtmlTemplate.renderPriorityBadge(task.getPriority())).append("\n")
                    .append("            ").append(HtmlTemplate.renderDeadlineBadge(task)).append("\n")
                    .append("          </div>\n")
                    .append("        </div>\n");
            }
            body.append("      </div>\n");
        }
        body.append("    </div>\n")
            .append("  </div>\n");

        // Column 2: Today's Schedule & Subject Progress
        body.append("  <div class=\"dashboard-right-col\">\n");

        // Today's Timetable Card
        String todayName = LocalDate.now().getDayOfWeek().name();
        String dayFormatted = todayName.substring(0, 1) + todayName.substring(1).toLowerCase();

        body.append("    <div class=\"dashboard-card\">\n")
            .append("      <div class=\"card-header-bar\">\n")
            .append("        <div class=\"card-title-group\">\n")
            .append("          <h2 class=\"card-title\">&#128197; Today's Timetable (").append(dayFormatted).append(")</h2>\n")
            .append("          <span class=\"badge-count\">").append(todaySlots.size()).append("</span>\n")
            .append("        </div>\n")
            .append("        <a href=\"").append(cp).append("/timetable\" class=\"card-link\">Weekly View &rarr;</a>\n")
            .append("      </div>\n")
            .append("      <div class=\"card-content\">\n");

        if (todaySlots.isEmpty()) {
            body.append("        <div class=\"empty-state-sm\">\n")
                .append("          <span class=\"empty-icon\">&#9749;</span>\n")
                .append("          <p class=\"empty-title\">No sessions scheduled for today</p>\n")
                .append("          <p class=\"empty-desc\">Enjoy your study break or work on self-paced revision.</p>\n")
                .append("          <a href=\"").append(cp).append("/timetable\" class=\"btn btn-sm btn-outline-primary\">+ Plan Today's Slot</a>\n")
                .append("        </div>\n");
        } else {
            body.append("        <div class=\"today-slot-list\">\n");
            for (TimetableSlot slot : todaySlots) {
                Subject sub = service.getSubjectById(slot.getSubjectId());
                body.append("          <div class=\"today-slot-item\">\n")
                    .append("            <div class=\"slot-time-badge\">")
                    .append(HtmlTemplate.escape(slot.getStartTime())).append(" &ndash; ").append(HtmlTemplate.escape(slot.getEndTime()))
                    .append("            </div>\n")
                    .append("            <div class=\"slot-details\">\n")
                    .append("              <div class=\"slot-subject-title\">")
                    .append(sub != null ? HtmlTemplate.escape(sub.getName()) : "General Study")
                    .append("              </div>\n")
                    .append("              <div class=\"slot-meta\">\n")
                    .append("                <span class=\"slot-activity-badge activity-").append(HtmlTemplate.escape(slot.getActivityType().toLowerCase().replace(" ", "-"))).append("\">")
                    .append(HtmlTemplate.escape(slot.getActivityType()))
                    .append("                </span>\n")
                    .append("                <span class=\"slot-loc\">&#128205; ").append(HtmlTemplate.escape(slot.getLocation())).append("</span>\n")
                    .append("              </div>\n")
                    .append("            </div>\n")
                    .append("          </div>\n");
            }
            body.append("        </div>\n");
        }
        body.append("      </div>\n")
            .append("    </div>\n");

        // Subject Progress Snapshot Card
        body.append("    <div class=\"dashboard-card\">\n")
            .append("      <div class=\"card-header-bar\">\n")
            .append("        <div class=\"card-title-group\">\n")
            .append("          <h2 class=\"card-title\">&#128218; Subject Progress</h2>\n")
            .append("        </div>\n")
            .append("        <a href=\"").append(cp).append("/progress\" class=\"card-link\">Full Analytics &rarr;</a>\n")
            .append("      </div>\n")
            .append("      <div class=\"card-content\">\n");

        if (subjectProgressList.isEmpty()) {
            body.append("        <p class=\"empty-desc\">No subjects registered yet.</p>\n");
        } else {
            body.append("        <div class=\"subject-progress-snapshot-list\">\n");
            for (PlannerService.SubjectProgress sp : subjectProgressList) {
                body.append("          <div class=\"sub-progress-row\">\n")
                    .append("            <div class=\"sub-progress-info\">\n")
                    .append("              <span class=\"sub-name\" style=\"color: ").append(sp.getSubject().getColor()).append(";\">")
                    .append(HtmlTemplate.escape(sp.getSubject().getCode())).append(" &ndash; ").append(HtmlTemplate.escape(sp.getSubject().getName()))
                    .append("              </span>\n")
                    .append("              <span class=\"sub-count\">").append(sp.getCompletedTasks()).append("/").append(sp.getTotalTasks()).append(" tasks (").append(sp.getPercentage()).append("%)</span>\n")
                    .append("            </div>\n")
                    .append("            <div class=\"progress-bar-container-sm\">\n")
                    .append("              <div class=\"progress-bar-fill\" style=\"width: ").append(sp.getPercentage()).append("%; background-color: ").append(sp.getSubject().getColor()).append(";\"></div>\n")
                    .append("            </div>\n")
                    .append("          </div>\n");
            }
            body.append("        </div>\n");
        }
        body.append("      </div>\n")
            .append("    </div>\n");

        body.append("  </div>\n"); // End right col
        body.append("</div>\n"); // End dashboard-columns-grid

        String html = HtmlTemplate.renderPage("Dashboard", "dashboard", body.toString(), cp, req);
        PrintWriter out = resp.getWriter();
        out.write(html);
    }
}

