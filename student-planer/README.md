# EduPlan &mdash; Student Study Planner Web Application

A complete, production-grade **Student Study Planner** web application designed with a modern blue-and-white theme, built strictly using **Java**, **Java Servlets**, **Apache Tomcat**, **semantic HTML5**, **CSS3**, and **Java File Handling** for persistent storage.

---

## 🎯 Strict Technology Compliance

| Requirement | Implementation Details |
|---|---|
| **Backend** | Pure **Java** (JDK 17 / OpenJDK) standard library |
| **Servlet Container** | **Java Servlets** (Jakarta Servlet 6.0) running on **Apache Tomcat 10.1** |
| **Frontend** | 100% Semantic **HTML5** & custom **CSS3** |
| **Styling** | Modern **Blue and White** theme (cards, badges, grids, metrics) |
| **Storage** | **Java File Handling** (`java.io.*`, `BufferedReader`, `BufferedWriter`, thread-safe text storage) |
| **Strictly Excluded** | **NO JavaScript**, **NO Python**, **NO React**, **NO Spring Boot**, **NO Node.js**, **NO PHP**, **NO MySQL** |

All interactions (task creation, updates, completion toggling, subject management, timetable scheduling, filtering, search) run seamlessly via standard **HTTP GET & POST** servlet workflows without a single line of JavaScript.

---

## 📂 Project Directory Structure

```
student-planer/
├── src/
│   └── com/
│       └── studyplanner/
│           ├── model/
│           │   ├── Priority.java         # High, Medium, Low priority enum with CSS badges
│           │   ├── Subject.java          # Course model (code, name, color, instructor, hours)
│           │   ├── StudyTask.java        # Task & deadline model (due dates, urgency calculations)
│           │   └── TimetableSlot.java    # Weekly schedule slot model (day, time, activity)
│           ├── service/
│           │   ├── StorageService.java   # Thread-safe Java file persistence (subjects/tasks/timetable)
│           │   └── PlannerService.java   # Business logic, analytics, filters, and calculations
│           ├── servlet/
│           │   ├── AppContextListener.java  # Initializes storage directory on Tomcat startup
│           │   ├── DashboardServlet.java    # /dashboard & / overview hub
│           │   ├── TaskServlet.java         # /tasks (Add, Edit, Delete, Toggle, Filter)
│           │   ├── SubjectServlet.java      # /subjects (Add, Edit, Delete, View progress)
│           │   ├── TimetableServlet.java    # /timetable (7-day weekly schedule grid)
│           │   └── ProgressServlet.java     # /progress (completion analytics & breakdown)
│           └── util/
│               └── HtmlTemplate.java     # Master blue & white HTML template renderer
├── webapp/
│   ├── css/
│   │   └── style.css                     # Comprehensive modern blue/white responsive stylesheet
│   ├── WEB-INF/
│   │   ├── web.xml                       # Deployment descriptor with servlet & listener mappings
│   │   ├── classes/                      # Compiled Java bytecode
│   │   └── data/                         # Persistent text storage files
│   │       ├── subjects.txt
│   │       ├── tasks.txt
│   │       └── timetable.txt
│   └── index.html                        # Automatic redirect to /dashboard
├── lib/
│   └── servlet-api.jar                   # Jakarta Servlet API library
├── pom.xml                               # Standard Maven project configuration
├── build.bat / build.ps1                 # Turnkey build scripts (compiles Java & packages WAR)
├── run.bat / run.ps1                     # Deploys to Apache Tomcat and launches the application
├── stop.bat / stop.ps1                   # Stops Apache Tomcat server
├── student-planer.war                    # Pre-packaged, ready-to-deploy WAR file
└── README.md                             # Documentation & testing guide
```

---

## ✨ Features Breakdown

### 1. 📊 Student Dashboard (`/dashboard`)
- **Hero Welcome Banner**: Greeting, academic quote, and quick-action buttons.
- **Metric Stat Cards**:
  - Total Tasks across subjects.
  - Completed Tasks counter.
  - Pending Tasks counter with total estimated remaining study hours.
  - Overdue Tasks alert counter.
  - Overall Completion Percentage with an inline progress bar.
- **Urgent & Upcoming Deadlines Widget**: Lists tasks due in the next few days with one-click completion buttons, priority badges, and countdown indicators.
- **Today's Timetable Widget**: Automatically highlights scheduled classes and study blocks for the current day of the week.
- **Subject Progress Snapshot**: Visual mini progress bars per registered subject.

### 2. 📝 Add, Edit, and Delete Study Tasks (`/tasks`)
- **Add Task (`/tasks?action=new`)**: Create tasks with Title, Subject selector, Task Type (Assignment, Project, Exam Prep, Reading, Lab Report), Priority, Due Date, Estimated Hours, and Notes.
- **Edit Task (`/tasks?action=edit&id=TASK-X`)**: Edit any field with pre-populated values and an optional completion checkbox.
- **Delete Task (`/tasks?action=delete_confirm&id=TASK-X`)**: Safe confirmation screen with Cancel and Confirm Delete buttons.
- **Filtering & Search**:
  - Filter by Subject dropdown.
  - Filter by Priority (All, High, Medium, Low).
  - Filter by Status (Pending, Completed, Overdue, Due Soon).
  - Search by keyword in titles or notes.
  - Quick-toggle navigation tabs.

### 3. 📚 Subject Management (`/subjects`)
- Add and edit subjects with Course Code (e.g. `CS-201`), Subject Name, Color Theme picker (preset palette or custom color), Instructor Name, and Target Study Hours per week.
- Visual Subject Cards displaying:
  - Custom color banner & indicator.
  - Task completion ratio (`X / Y tasks done`) and percentage bar.
  - Pending count and estimated workload.
  - "View Tasks" direct link filtering tasks for that specific subject.
- Safe delete workflow that unassigns associated tasks and removes timetable slots.

### 4. ⏰ Assignment Deadlines (`/tasks?filter=deadlines`)
- Real-time countdown tracking:
  - `Due Today` (highlighted orange badge)
  - `Due Tomorrow`
  - `X days left`
  - `Overdue by X days` (prominent red alert badge)
- Sorted chronologically by nearest deadline first so urgent assignments stay on top.

### 5. 🎯 Task Priorities (High, Medium, Low)
- **High Priority**: Red pill badge (`#ef4444`) with red alert indicator.
- **Medium Priority**: Amber pill badge (`#f59e0b`).
- **Low Priority**: Emerald green pill badge (`#10b981`).
- Priority breakdown analytics on the Progress page showing completion rates for each priority level.

### 6. ✅ Mark Tasks as Completed
- One-click completion toggle button directly on:
  - Dashboard upcoming deadlines list.
  - Tasks table (circular check button).
- Completed tasks show visual strikethrough styling, green `Completed` badges, and instantly update all progress bars and statistics across the app.
- Clicking again toggles the task back to pending if needed.

### 7. 🗓️ Weekly Timetable (`/timetable`)
- **7-Day Grid View** (Monday through Sunday) displaying:
  - Highlighting for **TODAY** with a dedicated badge and border accent.
  - Time periods (e.g. `09:00 - 10:30`).
  - Subject name color-coded with the subject's theme color.
  - Activity badges (`Lecture`, `Lab`, `Self-Study`, `Revision`, `Project Work`, `Discussion`).
  - Room / Location tags (`Hall 101`, `OS Systems Lab 3`, `Library 2nd Floor`).
  - One-click slot deletion button.
- **Schedule New Slot Form**: Add new class/study sessions to any day of the week.

### 8. 📈 Progress Tracking & Analytics (`/progress`)
- **Academic Workload Overview**:
  - Radial completion percentage display (`% Completed`).
  - Progress bar spanning the entire curriculum workload.
  - Metrics breakdown: Completed, Pending, Overdue, Remaining Effort (hours).
- **Priority Breakdown Grid**:
  - Individual completion bars and ratios for High, Medium, and Low priority tasks.
- **Subject-by-Subject Progress Table**:
  - Detailed table with subject color indicators, completion percentages, completed vs pending counters, overdue warnings, and remaining effort hours.

---

## 💾 Java File Handling Storage Engine

The application stores all data inside `webapp/WEB-INF/data/` (or Tomcat working directory) using standard Java I/O (`java.io.*` and `java.nio.*`):

1. `subjects.txt`:
   ```
   # ID|Name|Code|Color|Instructor|TargetHours
   SUB-1|Data Structures & Algorithms|CS-201|#2563eb|Prof. Alan Turing|6
   ```
2. `tasks.txt`:
   ```
   # ID|Title|SubjectId|TaskType|Priority|Deadline|EstimatedHours|Completed|Notes
   TASK-1|Implement AVL Tree and Red-Black Rebalancing|SUB-1|Assignment|HIGH|2026-11-12|4.5|false|Include rotateLeft...
   ```
3. `timetable.txt`:
   ```
   # ID|DayOfWeek|StartTime|EndTime|SubjectId|ActivityType|Location
   SLOT-1|Monday|09:00|10:30|SUB-1|Lecture|Turing Hall 101
   ```

- **Thread-safe**: Methods in `StorageService.java` use synchronization to prevent concurrency issues during simultaneous requests.
- **Delimiter & Newline Escaping**: Custom encoding ensures pipes `|` and newlines `\n` in student notes do not corrupt records.
- **Auto-Seeding**: On first run, the app automatically initializes realistic sample courses, tasks, deadlines, and timetable slots if files are empty.

---

## 🚀 How to Run Using Apache Tomcat

### Option A: Using the Automated Scripts (Recommended)

1. Open PowerShell or Command Prompt in the project folder:
   ```powershell
   cd c:\Users\patay\OneDrive\Documents\coding\student-planer
   ```
2. Build and run the app:
   ```cmd
   run.bat
   ```
   *Or with PowerShell:*
   ```powershell
   .\run.ps1
   ```
3. Open your browser and navigate to:
   ```
   http://localhost:8080/student-planer/
   ```
4. To stop Tomcat:
   ```cmd
   stop.bat
   ```

---

### Option B: Manual Tomcat Deployment

1. **Compile and package the WAR file**:
   ```cmd
   build.bat
   ```
   This generates `student-planer.war` in the project root.

2. **Copy the WAR to your Apache Tomcat `webapps` folder**:
   ```cmd
   copy student-planer.war "<TOMCAT_HOME>\webapps\"
   ```

3. **Start Apache Tomcat**:
   ```cmd
   "<TOMCAT_HOME>\bin\startup.bat"
   ```

4. **Access the application**:
   ```
   http://localhost:8080/student-planer/
   ```

---

## 🧪 How to Test Every Single Feature

Follow this step-by-step test plan to verify all 8 required features:

### Test 1: Student Dashboard
1. Navigate to `http://localhost:8080/student-planer/dashboard`.
2. Verify the **Hero Banner** displays "Welcome back to your Study Hub".
3. Check the **Stat Cards**: Total Tasks, Completed, Pending, Overdue, and Overall Completion percentage.
4. Verify the **Urgent & Upcoming Deadlines** list shows near-term tasks.
5. Verify the **Today's Timetable** box shows sessions scheduled for today's day of the week.

### Test 2: Add, Edit, and Delete Study Tasks
1. Click **Tasks** in the top navigation bar or **+ Create Task**.
2. **Add a task**:
   - Title: `Implement Graph Traversal (BFS & DFS)`
   - Subject: Select `CS-201 - Data Structures & Algorithms`
   - Task Type: `Assignment`
   - Priority: `High Priority`
   - Due Date: Pick a date 3 days from now
   - Estimated Hours: `3.5`
   - Notes: `Test on directed and undirected graphs`
   - Click **Create Task**.
   - Verify green confirmation message appears and the new task is listed in the table.
3. **Edit the task**:
   - Click the `✎ Edit` button next to the newly created task.
   - Change Priority to `Medium Priority` and Estimated Hours to `4.0`.
   - Click **Save Changes**.
   - Verify the table reflects the updated priority badge and hours.
4. **Delete a task**:
   - Click `🗑 Delete` next to the task.
   - Confirm deletion on the confirmation screen by clicking **Yes, Delete Task**.
   - Verify the task is removed from the table.

### Test 3: Subject Management
1. Click **Subjects** in the top navigation bar.
2. Verify existing subject cards (Data Structures, DBMS, Operating Systems, Web Engineering, Discrete Mathematics).
3. Click **+ Add Subject**:
   - Subject Name: `Computer Networks`
   - Course Code: `CS-305`
   - Target Hours / Week: `5`
   - Instructor: `Dr. Vint Cerf`
   - Theme Color: Choose one of the color presets.
   - Click **Create Subject**.
4. Verify the new subject card appears with its custom color accent and zero tasks.
5. Click **View Tasks →** on the card to verify filtering by that subject.

### Test 4: Assignment Deadlines Tracker
1. Click **Deadlines** in the top navigation bar (or `/tasks?filter=deadlines`).
2. Verify that tasks are sorted by due date with urgency badges:
   - Near deadlines show `Due Today` or `X days left`.
   - Overdue tasks show red `Overdue by X days` alerts.
3. Test the search filter: type a keyword (e.g. `SQL`) in the search box and click **Apply Filters**. Only matching tasks will appear.

### Test 5: Task Priorities
1. On the **Tasks** page, locate the **Priority** filter dropdown.
2. Select `High Priority` and click **Apply Filters**. Verify only High priority tasks appear with red badges.
3. Select `Low Priority` and click **Apply Filters**. Verify only Low priority tasks appear with green badges.
4. Click **Reset** to return to all tasks.

### Test 6: Mark Tasks as Completed
1. On the **Tasks** page or **Dashboard**, locate any pending task.
2. Click the circular checkbox button `○` on the left of the task.
3. Notice that:
   - The checkbox turns green with a checkmark `✓`.
   - The title is struck through with `text-strikethrough`.
   - The badge updates to green `Completed`.
   - The "Completed" stat card increases by 1 and the completion percentage increases.
4. Click the checkmark `✓` button again: the task toggles back to pending state.

### Test 7: Weekly Timetable
1. Click **Timetable** in the top navigation bar.
2. Verify the 7-day columns (Monday through Sunday). Notice the column corresponding to the current day is highlighted with a blue border and a `TODAY` badge.
3. Scroll to the **+ Schedule a Timetable Slot** section:
   - Day of Week: `Friday`
   - Start Time: `14:00`
   - End Time: `15:30`
   - Subject: Select any subject
   - Activity Type: `Discussion`
   - Location: `Room 304`
   - Click **Add Timetable Slot**.
4. Verify the new slot immediately appears in Friday's column with the subject's theme color and activity badge.
5. Click the `×` button on any slot to test deleting it.

### Test 8: Progress Tracking & Analytics
1. Click **Progress** in the top navigation bar.
2. Inspect the **Academic Workload Overview**: verify total completion percentage and workload hours.
3. Inspect the **Priority Breakdown**: verify the percentage bars for High, Medium, and Low priorities.
4. Inspect the **Subject-by-Subject Progress Table**: verify progress bars, completed vs pending counters, and remaining hours per course.
5. Click **View Tasks** next to any subject to navigate directly to its filtered task list.

---

## 🔒 Verification & Error Checking

- **Java Compilation**: 100% clean compilation using `javac` with zero errors.
- **Tomcat Verification**: Deployed and tested live on Apache Tomcat 10.1.28.
- **HTTP Responses**: All routes (`/dashboard`, `/tasks`, `/subjects`, `/timetable`, `/progress`, `/css/style.css`) return `HTTP 200 OK`.
- **Data Persistence**: Verified that creating, modifying, and deleting items updates `subjects.txt`, `tasks.txt`, and `timetable.txt` without data loss.

