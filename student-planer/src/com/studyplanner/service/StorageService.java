package com.studyplanner.service;

import com.studyplanner.model.Priority;
import com.studyplanner.model.StudyTask;
import com.studyplanner.model.Subject;
import com.studyplanner.model.TimetableSlot;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

public class StorageService {
    private static StorageService instance;
    private File dataDirectory;

    private static final String SUBJECTS_FILE = "subjects.txt";
    private static final String TASKS_FILE = "tasks.txt";
    private static final String TIMETABLE_FILE = "timetable.txt";

    private StorageService() {
        // default data directory fallback
        String baseDir = System.getProperty("catalina.base");
        if (baseDir != null && !baseDir.trim().isEmpty()) {
            this.dataDirectory = new File(baseDir, "webapps/student-planer/WEB-INF/data");
        } else {
            this.dataDirectory = new File("webapp/WEB-INF/data");
        }
        ensureDirectoryAndSeed();
    }

    public static synchronized StorageService getInstance() {
        if (instance == null) {
            instance = new StorageService();
        }
        return instance;
    }

    public synchronized void setDataDirectory(File dir) {
        if (dir != null) {
            this.dataDirectory = dir;
            ensureDirectoryAndSeed();
        }
    }

    public File getDataDirectory() {
        return dataDirectory;
    }

    private void ensureDirectoryAndSeed() {
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        File subFile = new File(dataDirectory, SUBJECTS_FILE);
        File taskFile = new File(dataDirectory, TASKS_FILE);
        File timeFile = new File(dataDirectory, TIMETABLE_FILE);

        if (!subFile.exists() || subFile.length() == 0) {
            seedInitialData();
        }
    }

    private void seedInitialData() {
        List<Subject> subjects = Arrays.asList(
            new Subject("SUB-1", "Data Structures & Algorithms", "CS-201", "#2563eb", "Prof. Alan Turing", 6),
            new Subject("SUB-2", "Database Management Systems", "CS-204", "#059669", "Dr. Edgar Codd", 5),
            new Subject("SUB-3", "Operating Systems & Concurrency", "CS-206", "#7c3aed", "Dr. Linus Torvalds", 6),
            new Subject("SUB-4", "Web Engineering & Architecture", "CS-208", "#d97706", "Prof. Tim Berners-Lee", 4),
            new Subject("SUB-5", "Discrete Mathematics & Logic", "MATH-102", "#dc2626", "Dr. Katherine Johnson", 5)
        );
        saveSubjects(subjects);

        LocalDate today = LocalDate.now();
        List<StudyTask> tasks = Arrays.asList(
            new StudyTask("TASK-1", "Implement AVL Tree and Red-Black Rebalancing", "SUB-1", "Assignment",
                Priority.HIGH, today.plusDays(2), 4.5, false, "Include rotateLeft, rotateRight, and benchmark test suite."),
            new StudyTask("TASK-2", "Normalize Database Schema to 3NF and BCNF", "SUB-2", "Assignment",
                Priority.HIGH, today.plusDays(1), 3.0, false, "Resolve functional dependencies for University Registrar database."),
            new StudyTask("TASK-3", "Implement Semaphore Mutex in Producer-Consumer", "SUB-3", "Lab Report",
                Priority.MEDIUM, today.plusDays(5), 3.5, false, "Run with 8 worker threads and verify race-free execution."),
            new StudyTask("TASK-4", "Responsive Web Layout & Color Harmony", "SUB-4", "Project",
                Priority.HIGH, today.plusDays(7), 5.0, false, "Ensure clean CSS styling with blue and white hierarchy."),
            new StudyTask("TASK-5", "Recurrence Relations & Master Theorem Proofs", "SUB-5", "Exam Prep",
                Priority.MEDIUM, today.plusDays(9), 4.0, false, "Solve chapter 4 end problems (odds 1-25)."),
            new StudyTask("TASK-6", "Storage Engine Indexing & B+ Trees Reading", "SUB-2", "Reading",
                Priority.LOW, today.plusDays(12), 2.0, false, "Read pages 310-345 in Database System Concepts."),
            new StudyTask("TASK-7", "Array-Based Circular Queue Implementation", "SUB-1", "Lab Report",
                Priority.MEDIUM, today.minusDays(3), 2.5, true, "Completed on time. All 10 test fixtures passed!"),
            new StudyTask("TASK-8", "POSIX System Calls & Process Forking", "SUB-3", "Assignment",
                Priority.LOW, today.minusDays(1), 2.0, true, "Verified fork, exec, waitpid logic with zero zombie processes.")
        );
        saveTasks(tasks);

        List<TimetableSlot> slots = Arrays.asList(
            new TimetableSlot("SLOT-1", "Monday", "09:00", "10:30", "SUB-1", "Lecture", "Turing Hall 101"),
            new TimetableSlot("SLOT-2", "Monday", "11:00", "12:30", "SUB-2", "Lecture", "Babbage Room 204"),
            new TimetableSlot("SLOT-3", "Tuesday", "10:00", "12:00", "SUB-3", "Lab", "OS Systems Lab 3"),
            new TimetableSlot("SLOT-4", "Tuesday", "14:00", "16:00", "SUB-1", "Self-Study", "Central Library - 2nd Floor"),
            new TimetableSlot("SLOT-5", "Wednesday", "09:00", "10:30", "SUB-5", "Lecture", "Euler Hall 302"),
            new TimetableSlot("SLOT-6", "Wednesday", "11:00", "12:30", "SUB-4", "Lecture", "Web Lab B"),
            new TimetableSlot("SLOT-7", "Thursday", "10:00", "11:30", "SUB-1", "Discussion", "Study Room 5"),
            new TimetableSlot("SLOT-8", "Thursday", "14:00", "16:00", "SUB-2", "Lab", "Database Lab 1"),
            new TimetableSlot("SLOT-9", "Friday", "09:00", "10:30", "SUB-3", "Lecture", "Turing Hall 101"),
            new TimetableSlot("SLOT-10", "Friday", "11:00", "12:30", "SUB-4", "Project Work", "Innovation Studio"),
            new TimetableSlot("SLOT-11", "Saturday", "10:00", "12:30", "SUB-5", "Revision", "Home Desk")
        );
        saveTimetable(slots);
    }

    // Encoding & decoding helpers to preserve newlines and delimiters
    private static String encodeField(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("|", "\\p")
                   .replace("\n", "\\n")
                   .replace("\r", "");
    }

    private static String decodeField(String text) {
        if (text == null) return "";
        return text.replace("\\n", "\n")
                   .replace("\\p", "|")
                   .replace("\\\\", "\\");
    }

    // --- SUBJECTS PERSISTENCE ---
    public synchronized List<Subject> loadSubjects() {
        List<Subject> list = new ArrayList<>();
        File file = new File(dataDirectory, SUBJECTS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 6) {
                    String id = decodeField(parts[0]);
                    String name = decodeField(parts[1]);
                    String code = decodeField(parts[2]);
                    String color = decodeField(parts[3]);
                    String instructor = decodeField(parts[4]);
                    int hours = 4;
                    try {
                        hours = Integer.parseInt(decodeField(parts[5]));
                    } catch (NumberFormatException ignored) {}
                    list.add(new Subject(id, name, code, color, instructor, hours));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized void saveSubjects(List<Subject> subjects) {
        File file = new File(dataDirectory, SUBJECTS_FILE);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write("# ID|Name|Code|Color|Instructor|TargetHours\n");
            for (Subject s : subjects) {
                writer.write(encodeField(s.getId()) + "|"
                        + encodeField(s.getName()) + "|"
                        + encodeField(s.getCode()) + "|"
                        + encodeField(s.getColor()) + "|"
                        + encodeField(s.getInstructor()) + "|"
                        + encodeField(String.valueOf(s.getTargetHours())) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- TASKS PERSISTENCE ---
    public synchronized List<StudyTask> loadTasks() {
        List<StudyTask> list = new ArrayList<>();
        File file = new File(dataDirectory, TASKS_FILE);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 9) {
                    String id = decodeField(parts[0]);
                    String title = decodeField(parts[1]);
                    String subjectId = decodeField(parts[2]);
                    String taskType = decodeField(parts[3]);
                    Priority priority = Priority.fromString(decodeField(parts[4]));
                    LocalDate deadline = LocalDate.now().plusDays(3);
                    try {
                        deadline = LocalDate.parse(decodeField(parts[5]), StudyTask.DATE_FORMAT);
                    } catch (Exception ignored) {}
                    double estHours = 2.0;
                    try {
                        estHours = Double.parseDouble(decodeField(parts[6]));
                    } catch (NumberFormatException ignored) {}
                    boolean completed = Boolean.parseBoolean(decodeField(parts[7]));
                    String notes = decodeField(parts[8]);

                    list.add(new StudyTask(id, title, subjectId, taskType, priority, deadline, estHours, completed, notes));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized void saveTasks(List<StudyTask> tasks) {
        File file = new File(dataDirectory, TASKS_FILE);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write("# ID|Title|SubjectId|TaskType|Priority|Deadline|EstimatedHours|Completed|Notes\n");
            for (StudyTask t : tasks) {
                writer.write(encodeField(t.getId()) + "|"
                        + encodeField(t.getTitle()) + "|"
                        + encodeField(t.getSubjectId()) + "|"
                        + encodeField(t.getTaskType()) + "|"
                        + encodeField(t.getPriority().name()) + "|"
                        + encodeField(t.getDeadlineFormatted()) + "|"
                        + encodeField(String.valueOf(t.getEstimatedHours())) + "|"
                        + encodeField(String.valueOf(t.isCompleted())) + "|"
                        + encodeField(t.getNotes()) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- TIMETABLE PERSISTENCE ---
    public synchronized List<TimetableSlot> loadTimetable() {
        List<TimetableSlot> list = new ArrayList<>();
        File file = new File(dataDirectory, TIMETABLE_FILE);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 7) {
                    String id = decodeField(parts[0]);
                    String day = decodeField(parts[1]);
                    String start = decodeField(parts[2]);
                    String end = decodeField(parts[3]);
                    String subId = decodeField(parts[4]);
                    String activity = decodeField(parts[5]);
                    String loc = decodeField(parts[6]);
                    list.add(new TimetableSlot(id, day, start, end, subId, activity, loc));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized void saveTimetable(List<TimetableSlot> slots) {
        File file = new File(dataDirectory, TIMETABLE_FILE);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write("# ID|DayOfWeek|StartTime|EndTime|SubjectId|ActivityType|Location\n");
            for (TimetableSlot s : slots) {
                writer.write(encodeField(s.getId()) + "|"
                        + encodeField(s.getDayOfWeek()) + "|"
                        + encodeField(s.getStartTime()) + "|"
                        + encodeField(s.getEndTime()) + "|"
                        + encodeField(s.getSubjectId()) + "|"
                        + encodeField(s.getActivityType()) + "|"
                        + encodeField(s.getLocation()) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

