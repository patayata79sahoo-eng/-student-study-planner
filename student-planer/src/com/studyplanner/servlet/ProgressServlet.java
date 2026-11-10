package com.studyplanner.servlet;

import com.studyplanner.model.Priority;
import com.studyplanner.model.StudyTask;
import com.studyplanner.service.PlannerService;
import com.studyplanner.util.HtmlTemplate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "ProgressServlet", urlPatterns = {"/progress"})
public class ProgressServlet extends HttpServlet {
    private final PlannerService service = PlannerService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        String cp = req.getContextPath();

        List<StudyTask> allTasks = service.getAllTasks();
        int total = allTasks.size();
        int completed = service.getCompletedTaskCount();
        int pending = service.getPendingTaskCount();
        int overdue = service.getOverdueTaskCount();
        int pct = service.getOverallCompletionPercentage();

        double totalHours = service.getTotalEstimatedHours();
        double remainingHours = service.getRemainingEstimatedHours();
        double completedHours = Math.max(0, totalHours - remainingHours);

        List<PlannerService.SubjectProgress> subjectList = service.getSubjectProgressList();

        // Calculate Priority Stats
        int highTotal = 0, highDone = 0;
        int medTotal = 0, medDone = 0;
        int lowTotal = 0, lowDone = 0;

        for (StudyTask t : allTasks) {
            if (t.getPriority() == Priority.HIGH) {
                highTotal++;
                if (t.isCompleted()) highDone++;
            } else if (t.getPriority() == Priority.MEDIUM) {
                medTotal++;
                if (t.isCompleted()) medDone++;
            } else if (t.getPriority() == Priority.LOW) {
                lowTotal++;
                if (t.isCompleted()) lowDone++;
            }
        }

        int highPct = highTotal > 0 ? (int) Math.round((highDone * 100.0) / highTotal) : 0;
        int medPct = medTotal > 0 ? (int) Math.round((medDone * 100.0) / medTotal) : 0;
        int lowPct = lowTotal > 0 ? (int) Math.round((lowDone * 100.0) / lowTotal) : 0;

        StringBuilder body = new StringBuilder();

        // Page Header
        body.append("<div class=\"page-header\">\n")
            .append("  <div class=\"page-title-group\">\n")
            .append("    <h1 class=\"page-title\">&#128200; Study Progress &amp; Analytics</h1>\n")
            .append("    <p class=\"page-subtitle\">Real-time visibility into your task completion rates, subject mastery, and priority distribution.</p>\n")
            .append("  </div>\n")
            .append("</div>\n");

        // Primary Progress Overview Card
        body.append("<div class=\"dashboard-card progress-overview-card\">\n")
            .append("  <div class=\"progress-hero-row\">\n")
            .append("    <div class=\"progress-circle-box\">\n")
            .append("      <div class=\"progress-percentage-display\">").append(pct).append("%</div>\n")
            .append("      <div class=\"progress-circle-caption\">Total Completion</div>\n")
            .append("    </div>\n")
            .append("    <div class=\"progress-hero-stats\">\n")
            .append("      <h2 class=\"progress-headline\">Academic Workload Overview</h2>\n")
            .append("      <p class=\"progress-subtext\">You have completed <strong>").append(completed).append("</strong> out of <strong>").append(total).append("</strong> scheduled tasks. ")
            .append(overdue > 0 ? "<span class=\"text-red\"><strong>" + overdue + "</strong> overdue tasks require attention.</span>" : "<span class=\"text-green\">All deadlines are currently under control.</span>")
            .append("</p>\n")
            .append("      <div class=\"progress-bar-container-lg\">\n")
            .append("        <div class=\"progress-bar-fill\" style=\"width: ").append(pct).append("%;\"></div>\n")
            .append("      </div>\n")
            .append("      <div class=\"progress-hero-metrics-grid\">\n")
            .append("        <div class=\"metric-pill\"><span class=\"metric-dot dot-green\"></span><strong>").append(completed).append("</strong> Completed</div>\n")
            .append("        <div class=\"metric-pill\"><span class=\"metric-dot dot-amber\"></span><strong>").append(pending).append("</strong> Pending</div>\n")
            .append("        <div class=\"metric-pill\"><span class=\"metric-dot dot-red\"></span><strong>").append(overdue).append("</strong> Overdue</div>\n")
            .append("        <div class=\"metric-pill\"><span class=\"metric-dot dot-blue\"></span><strong>~").append(String.format("%.1f", remainingHours)).append("h</strong> Remaining Effort</div>\n")
            .append("      </div>\n")
            .append("    </div>\n")
            .append("  </div>\n")
            .append("</div>\n");

        // Priority Breakdown Cards
        body.append("<h2 class=\"section-subheading\">&#127919; Priority Breakdown</h2>\n")
            .append("<div class=\"priority-cards-grid\">\n");

        // High Priority Card
        body.append("  <div class=\"priority-stat-card card-pri-high\">\n")
            .append("    <div class=\"pri-header\">\n")
            .append("      <span class=\"badge badge-priority-high\"><span class=\"dot\"></span>High Priority</span>\n")
            .append("      <span class=\"pri-pct\">").append(highPct).append("%</span>\n")
            .append("    </div>\n")
            .append("    <div class=\"progress-bar-container\">\n")
            .append("      <div class=\"progress-bar-fill fill-red\" style=\"width: ").append(highPct).append("%;\"></div>\n")
            .append("    </div>\n")
            .append("    <div class=\"pri-footer\">\n")
            .append("      <span><strong>").append(highDone).append("</strong> / ").append(highTotal).append(" tasks done</span>\n")
            .append("      <a href=\"").append(cp).append("/tasks?priority=HIGH\" class=\"pri-link\">View &rarr;</a>\n")
            .append("    </div>\n")
            .append("  </div>\n");

        // Medium Priority Card
        body.append("  <div class=\"priority-stat-card card-pri-medium\">\n")
            .append("    <div class=\"pri-header\">\n")
            .append("      <span class=\"badge badge-priority-medium\"><span class=\"dot\"></span>Medium Priority</span>\n")
            .append("      <span class=\"pri-pct\">").append(medPct).append("%</span>\n")
            .append("    </div>\n")
            .append("    <div class=\"progress-bar-container\">\n")
            .append("      <div class=\"progress-bar-fill fill-amber\" style=\"width: ").append(medPct).append("%;\"></div>\n")
            .append("    </div>\n")
            .append("    <div class=\"pri-footer\">\n")
            .append("      <span><strong>").append(medDone).append("</strong> / ").append(medTotal).append(" tasks done</span>\n")
            .append("      <a href=\"").append(cp).append("/tasks?priority=MEDIUM\" class=\"pri-link\">View &rarr;</a>\n")
            .append("    </div>\n")
            .append("  </div>\n");

        // Low Priority Card
        body.append("  <div class=\"priority-stat-card card-pri-low\">\n")
            .append("    <div class=\"pri-header\">\n")
            .append("      <span class=\"badge badge-priority-low\"><span class=\"dot\"></span>Low Priority</span>\n")
            .append("      <span class=\"pri-pct\">").append(lowPct).append("%</span>\n")
            .append("    </div>\n")
            .append("    <div class=\"progress-bar-container\">\n")
            .append("      <div class=\"progress-bar-fill fill-green\" style=\"width: ").append(lowPct).append("%;\"></div>\n")
            .append("    </div>\n")
            .append("    <div class=\"pri-footer\">\n")
            .append("      <span><strong>").append(lowDone).append("</strong> / ").append(lowTotal).append(" tasks done</span>\n")
            .append("      <a href=\"").append(cp).append("/tasks?priority=LOW\" class=\"pri-link\">View &rarr;</a>\n")
            .append("    </div>\n")
            .append("  </div>\n");

        body.append("</div>\n");

        // Subject Breakdown Detailed Table
        body.append("<h2 class=\"section-subheading\">&#128218; Subject-by-Subject Progress</h2>\n")
            .append("<div class=\"dashboard-card\">\n")
            .append("  <div class=\"card-content card-content-flush\">\n")
            .append("    <table class=\"data-table\">\n")
            .append("      <thead>\n")
            .append("        <tr>\n")
            .append("          <th>Subject</th>\n")
            .append("          <th>Progress</th>\n")
            .append("          <th style=\"text-align: center;\">Tasks Done</th>\n")
            .append("          <th style=\"text-align: center;\">Pending</th>\n")
            .append("          <th style=\"text-align: center;\">Overdue</th>\n")
            .append("          <th>Remaining Work</th>\n")
            .append("          <th style=\"text-align: right;\">Action</th>\n")
            .append("        </tr>\n")
            .append("      </thead>\n")
            .append("      <tbody>\n");

        for (PlannerService.SubjectProgress sp : subjectList) {
            String col = sp.getSubject().getColor();
            body.append("        <tr>\n")
                .append("          <td>\n")
                .append("            <div class=\"subject-table-cell\">\n")
                .append("              <span class=\"color-bar-inline\" style=\"background-color: ").append(col).append(";\"></span>\n")
                .append("              <div>\n")
                .append("                <div class=\"sub-cell-name\">").append(HtmlTemplate.escape(sp.getSubject().getName())).append("</div>\n")
                .append("                <div class=\"sub-cell-code\">").append(HtmlTemplate.escape(sp.getSubject().getCode())).append(" &bull; ").append(HtmlTemplate.escape(sp.getSubject().getInstructor())).append("</div>\n")
                .append("              </div>\n")
                .append("            </div>\n")
                .append("          </td>\n")
                .append("          <td style=\"min-width: 180px;\">\n")
                .append("            <div class=\"progress-label-micro\"><span>").append(sp.getPercentage()).append("%</span></div>\n")
                .append("            <div class=\"progress-bar-container-sm\">\n")
                .append("              <div class=\"progress-bar-fill\" style=\"width: ").append(sp.getPercentage()).append("%; background-color: ").append(col).append(";\"></div>\n")
                .append("            </div>\n")
                .append("          </td>\n")
                .append("          <td style=\"text-align: center;\"><span class=\"badge-count-success\">").append(sp.getCompletedTasks()).append(" / ").append(sp.getTotalTasks()).append("</span></td>\n")
                .append("          <td style=\"text-align: center;\">").append(sp.getPendingTasks()).append("</td>\n")
                .append("          <td style=\"text-align: center;\">").append(sp.getOverdueTasks() > 0 ? "<span class=\"text-red font-bold\">" + sp.getOverdueTasks() + "</span>" : "0").append("</td>\n")
                .append("          <td>~").append(String.format("%.1f", sp.getRemainingHours())).append(" hrs</td>\n")
                .append("          <td style=\"text-align: right;\">\n")
                .append("            <a href=\"").append(cp).append("/tasks?subjectId=").append(HtmlTemplate.escape(sp.getSubject().getId())).append("\" class=\"btn btn-xs btn-outline-primary\">View Tasks</a>\n")
                .append("          </td>\n")
                .append("        </tr>\n");
        }

        body.append("      </tbody>\n")
            .append("    </table>\n")
            .append("  </div>\n")
            .append("</div>\n");

        String html = HtmlTemplate.renderPage("Progress Tracking", "progress", body.toString(), cp, req);
        PrintWriter out = resp.getWriter();
        out.write(html);
    }
}

