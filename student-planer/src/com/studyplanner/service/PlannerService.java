package com.studyplanner.service;

import com.studyplanner.model.Priority;
import com.studyplanner.model.StudyTask;
import com.studyplanner.model.Subject;
import com.studyplanner.model.TimetableSlot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PlannerService {
    private static PlannerService instance;
    private final StorageService storage;

    private PlannerService() {
        this.storage = StorageService.getInstance();
    }

    public static synchronized PlannerService getInstance() {
        if (instance == null) {
            instance = new PlannerService();
        }
        return instance;
    }

    // --- SUBJECT METHODS ---
    public synchronized List<Subject> getAllSubjects() {
        return storage.loadSubjects();
    }

    public synchronized Subject getSubjectById(String id) {
        if (id == null) return null;
        for (Subject s : getAllSubjects()) {
            if (s.getId().equalsIgnoreCase(id.trim())) {
                return s;
            }
        }
        return null;
    }

    public synchronized String getNextSubjectId() {
        List<Subject> list = getAllSubjects();
        int max = 0;
        for (Subject s : list) {
            if (s.getId() != null && s.getId().startsWith("SUB-")) {
                try {
                    int val = Integer.parseInt(s.getId().substring(4));
                    if (val > max) max = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return "SUB-" + (max + 1);
    }

    public synchronized void saveSubject(Subject subject) {
        if (subject == null) return;
        List<Subject> list = getAllSubjects();
        boolean found = false;
        if (subject.getId() == null || subject.getId().trim().isEmpty()) {
            subject.setId(getNextSubjectId());
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equalsIgnoreCase(subject.getId())) {
                list.set(i, subject);
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(subject);
        }
        storage.saveSubjects(list);
    }

    public synchronized boolean deleteSubject(String id) {
        if (id == null) return false;
        List<Subject> list = getAllSubjects();
        boolean removed = list.removeIf(s -> s.getId().equalsIgnoreCase(id.trim()));
        if (removed) {
            storage.saveSubjects(list);
            // clean up tasks with deleted subject or keep them with orphaned note
            List<StudyTask> tasks = getAllTasks();
            for (StudyTask t : tasks) {
                if (t.getSubjectId().equalsIgnoreCase(id.trim())) {
                    t.setSubjectId(""); // unassigned
                }
            }
            storage.saveTasks(tasks);

            // remove timetable slots for deleted subject
            List<TimetableSlot> slots = getAllTimetableSlots();
            slots.removeIf(slot -> slot.getSubjectId().equalsIgnoreCase(id.trim()));
            storage.saveTimetable(slots);
        }
        return removed;
    }

    // --- TASK METHODS ---
    public synchronized List<StudyTask> getAllTasks() {
        return storage.loadTasks();
    }

    public synchronized StudyTask getTaskById(String id) {
        if (id == null) return null;
        for (StudyTask t : getAllTasks()) {
            if (t.getId().equalsIgnoreCase(id.trim())) {
                return t;
            }
        }
        return null;
    }

    public synchronized String getNextTaskId() {
        List<StudyTask> list = getAllTasks();
        int max = 0;
        for (StudyTask t : list) {
            if (t.getId() != null && t.getId().startsWith("TASK-")) {
                try {
                    int val = Integer.parseInt(t.getId().substring(5));
                    if (val > max) max = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return "TASK-" + (max + 1);
    }

    public synchronized void saveTask(StudyTask task) {
        if (task == null) return;
        List<StudyTask> list = getAllTasks();
        if (task.getId() == null || task.getId().trim().isEmpty()) {
            task.setId(getNextTaskId());
        }
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equalsIgnoreCase(task.getId())) {
                list.set(i, task);
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(task);
        }
        storage.saveTasks(list);
    }

    public synchronized boolean deleteTask(String id) {
        if (id == null) return false;
        List<StudyTask> list = getAllTasks();
        boolean removed = list.removeIf(t -> t.getId().equalsIgnoreCase(id.trim()));
        if (removed) {
            storage.saveTasks(list);
        }
        return removed;
    }

    public synchronized boolean toggleTaskCompletion(String id) {
        StudyTask task = getTaskById(id);
        if (task != null) {
            task.setCompleted(!task.isCompleted());
            saveTask(task);
            return true;
        }
        return false;
    }

    public List<StudyTask> filterTasks(String subjectId, String priority, String status, String query) {
        List<StudyTask> list = getAllTasks();

        if (subjectId != null && !subjectId.trim().isEmpty() && !subjectId.equals("ALL")) {
            list = list.stream()
                .filter(t -> t.getSubjectId().equalsIgnoreCase(subjectId.trim()))
                .collect(Collectors.toList());
        }

        if (priority != null && !priority.trim().isEmpty() && !priority.equals("ALL")) {
            list = list.stream()
                .filter(t -> t.getPriority().name().equalsIgnoreCase(priority.trim()))
                .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
            if (status.equalsIgnoreCase("COMPLETED")) {
                list = list.stream().filter(StudyTask::isCompleted).collect(Collectors.toList());
            } else if (status.equalsIgnoreCase("PENDING")) {
                list = list.stream().filter(t -> !t.isCompleted()).collect(Collectors.toList());
            } else if (status.equalsIgnoreCase("OVERDUE")) {
                list = list.stream().filter(StudyTask::isOverdue).collect(Collectors.toList());
            } else if (status.equalsIgnoreCase("DUE_SOON")) {
                list = list.stream().filter(t -> t.isDueSoon(3)).collect(Collectors.toList());
            }
        }

        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            list = list.stream()
                .filter(t -> t.getTitle().toLowerCase().contains(q) || t.getNotes().toLowerCase().contains(q))
                .collect(Collectors.toList());
        }

        // Sort: pending first, then by deadline ascending, then priority
        list.sort((a, b) -> {
            if (a.isCompleted() != b.isCompleted()) {
                return a.isCompleted() ? 1 : -1;
            }
            if (a.getDeadline() != null && b.getDeadline() != null) {
                int comp = a.getDeadline().compareTo(b.getDeadline());
                if (comp != 0) return comp;
            }
            return a.getPriority().compareTo(b.getPriority());
        });

        return list;
    }

    // --- TIMETABLE METHODS ---
    public synchronized List<TimetableSlot> getAllTimetableSlots() {
        List<TimetableSlot> list = storage.loadTimetable();
        Collections.sort(list);
        return list;
    }

    public synchronized List<TimetableSlot> getSlotsByDay(String dayOfWeek) {
        List<TimetableSlot> list = getAllTimetableSlots();
        return list.stream()
            .filter(s -> s.getDayOfWeek().equalsIgnoreCase(dayOfWeek))
            .sorted(Comparator.comparing(TimetableSlot::getStartTime))
            .collect(Collectors.toList());
    }

    public synchronized String getNextSlotId() {
        List<TimetableSlot> list = getAllTimetableSlots();
        int max = 0;
        for (TimetableSlot s : list) {
            if (s.getId() != null && s.getId().startsWith("SLOT-")) {
                try {
                    int val = Integer.parseInt(s.getId().substring(5));
                    if (val > max) max = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return "SLOT-" + (max + 1);
    }

    public synchronized void saveTimetableSlot(TimetableSlot slot) {
        if (slot == null) return;
        List<TimetableSlot> list = getAllTimetableSlots();
        if (slot.getId() == null || slot.getId().trim().isEmpty()) {
            slot.setId(getNextSlotId());
        }
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equalsIgnoreCase(slot.getId())) {
                list.set(i, slot);
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(slot);
        }
        Collections.sort(list);
        storage.saveTimetable(list);
    }

    public synchronized boolean deleteTimetableSlot(String id) {
        if (id == null) return false;
        List<TimetableSlot> list = getAllTimetableSlots();
        boolean removed = list.removeIf(s -> s.getId().equalsIgnoreCase(id.trim()));
        if (removed) {
            storage.saveTimetable(list);
        }
        return removed;
    }

    // --- ANALYTICS & STATS ---
    public int getTotalTaskCount() {
        return getAllTasks().size();
    }

    public int getCompletedTaskCount() {
        return (int) getAllTasks().stream().filter(StudyTask::isCompleted).count();
    }

    public int getPendingTaskCount() {
        return (int) getAllTasks().stream().filter(t -> !t.isCompleted()).count();
    }

    public int getOverdueTaskCount() {
        return (int) getAllTasks().stream().filter(StudyTask::isOverdue).count();
    }

    public int getOverallCompletionPercentage() {
        int total = getTotalTaskCount();
        if (total == 0) return 0;
        return (int) Math.round((getCompletedTaskCount() * 100.0) / total);
    }

    public double getTotalEstimatedHours() {
        return getAllTasks().stream().mapToDouble(StudyTask::getEstimatedHours).sum();
    }

    public double getRemainingEstimatedHours() {
        return getAllTasks().stream().filter(t -> !t.isCompleted()).mapToDouble(StudyTask::getEstimatedHours).sum();
    }

    public List<StudyTask> getUpcomingDeadlines(int limit) {
        return getAllTasks().stream()
            .filter(t -> !t.isCompleted())
            .sorted(Comparator.comparing(StudyTask::getDeadline))
            .limit(limit)
            .collect(Collectors.toList());
    }

    public List<TimetableSlot> getTodaySlots() {
        String todayName = LocalDate.now().getDayOfWeek().name();
        // Capitalize e.g. MONDAY -> Monday
        String formatted = todayName.substring(0, 1) + todayName.substring(1).toLowerCase();
        return getSlotsByDay(formatted);
    }

    // Subject Progress Data Transfer Object
    public static class SubjectProgress {
        private final Subject subject;
        private final int totalTasks;
        private final int completedTasks;
        private final int pendingTasks;
        private final int overdueTasks;
        private final double remainingHours;

        public SubjectProgress(Subject subject, int totalTasks, int completedTasks,
                               int pendingTasks, int overdueTasks, double remainingHours) {
            this.subject = subject;
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.pendingTasks = pendingTasks;
            this.overdueTasks = overdueTasks;
            this.remainingHours = remainingHours;
        }

        public Subject getSubject() { return subject; }
        public int getTotalTasks() { return totalTasks; }
        public int getCompletedTasks() { return completedTasks; }
        public int getPendingTasks() { return pendingTasks; }
        public int getOverdueTasks() { return overdueTasks; }
        public double getRemainingHours() { return remainingHours; }

        public int getPercentage() {
            if (totalTasks == 0) return 0;
            return (int) Math.round((completedTasks * 100.0) / totalTasks);
        }
    }

    public List<SubjectProgress> getSubjectProgressList() {
        List<Subject> subjects = getAllSubjects();
        List<StudyTask> tasks = getAllTasks();
        List<SubjectProgress> list = new ArrayList<>();

        for (Subject sub : subjects) {
            List<StudyTask> subTasks = tasks.stream()
                .filter(t -> t.getSubjectId().equalsIgnoreCase(sub.getId()))
                .collect(Collectors.toList());

            int total = subTasks.size();
            int completed = (int) subTasks.stream().filter(StudyTask::isCompleted).count();
            int pending = total - completed;
            int overdue = (int) subTasks.stream().filter(StudyTask::isOverdue).count();
            double remHours = subTasks.stream().filter(t -> !t.isCompleted()).mapToDouble(StudyTask::getEstimatedHours).sum();

            list.add(new SubjectProgress(sub, total, completed, pending, overdue, remHours));
        }
        return list;
    }
}

