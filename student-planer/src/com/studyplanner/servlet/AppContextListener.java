package com.studyplanner.servlet;

import com.studyplanner.service.StorageService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.File;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String realPath = sce.getServletContext().getRealPath("/WEB-INF/data");
        File dataDir;
        if (realPath != null) {
            dataDir = new File(realPath);
        } else {
            String base = System.getProperty("catalina.base", ".");
            dataDir = new File(base, "student_planer_data");
        }
        StorageService.getInstance().setDataDirectory(dataDir);
        System.out.println("[EduPlan] Initialized StorageService at: " + dataDir.getAbsolutePath());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[EduPlan] Context destroyed.");
    }
}

