# TaskFlow: A Custom Multi-Threaded Job Processing System with JSON Persistence
  
**Project Duration**: 10 days of planning + 4 days of intensive coding (built completely from scratch)  
**Type**: Learning Project (2nd Major Project)  
**Language**: Java (Pure Core Java, no frameworks)  
**Goal**: Learn concurrency, file-based persistence, JSON handling, multi-threading, and system design through building a realistic task orchestration engine.

---

## Introduction

**TaskFlow** is a console-based task management and processing system that simulates a real-world job queue with worker threads. It allows users to:

1. **Manage tasks** via CRUD operations (stored in `storage.json`).
2. **Process tasks** using multiple worker threads with a shared blocking queue.
3. **Monitor** real-time progress through a live dashboard.

The entire system was designed and implemented by a 17-year-old developer as a learning exercise. Every design decision, class, and line of code was added with a clear educational or functional reason. No external frameworks (Spring, etc.) were used — only core Java, Jackson for JSON, and standard concurrency utilities.

This project helped deepen understanding of:
- Multi-threading and thread safety
- Producer-Consumer pattern
- JSON serialization/deserialization with Jackson
- File I/O with synchronization
- Separation of Concerns (SoC)
- Basic system monitoring

---

## Project Architecture

The project is divided into logical packages:

- **`database`** — Data models and persistence layer
- **`lobby`** — Job queue and job admission logic
- **`worker`** — Worker threads, thread pool, and processing logic
- **`server`** — Orchestration and startup
- **`dashboard`** — Real-time monitoring
- **`CURD`** — Command-line CRUD operations (separate mode)

**Core Flow**:
1. Tasks are stored in `storage.json` as `TaskGroup` → `List<Task>`.
2. `JobAdder` (producer) reads pending tasks and puts them into a `BlockingQueue<Job>`.
3. Multiple `Worker` threads (consumers) take jobs, simulate work, and update status.
4. `StateUpgrader` safely updates task status in the JSON file.
5. Dashboard shows live metrics.

---

## Detailed Component Breakdown

### 1. Database Layer

#### `TaskGroup.java`
- Represents a group of tasks belonging to one user/entity.
- Contains `id`, `name`, and `List<Task>`.
- Holds the reference to `storage.json`.

#### `Task.java`
- Individual task unit with `taskno`, `type`, `work`, `status`.
- Simple POJO with getters/setters.

#### `DataManupulator.java` (Note: Typo — should be `DataManipulator`)
- Handles reading and writing the entire JSON database.
- Uses `LinkedHashMap<String, TaskGroup>` to preserve insertion order.
- `load()`, `write()`, `writeAll()` methods.

#### `Stateupgrader.java`
- **Critical component** for thread-safe status updates.
- Uses `synchronized` block + full JSON read-modify-write.
- Parses JSON with Jackson `JsonNode`/`ObjectNode`/`ArrayNode` to update specific task status without losing data.
- Reason for design: Direct object modification in memory wasn't safe across threads, so file-level atomic updates were chosen.

---

### 2. Lobby Layer (Job Admission)

#### `Job.java`
- Lightweight carrier object containing `taskid` (group ID) and `Task` reference.
- Passed through the queue.

#### `JobAdder.java`
- **Producer** thread.
- Loads all tasks from database.
- Skips already `COMPLETED` tasks.
- Puts pending tasks into `BlockingQueue<Job>`.
- Immediately upgrades status to `"Waiting"` using `Stateupgrader`.
- Has shutdown mechanism with volatile flag and interruption handling.

---

### 3. Worker Layer

#### `Worker.java`
- Implements `Runnable`.
- Takes jobs from queue.
- Updates status: `WAITING` → `WORKING` → `COMPLETED`.
- Uses `Processor` for actual work simulation.
- Tracks personal work count.

#### `Processor.java`
- Simulates real work with random sleep (500ms - 3000ms).
- Updates status to `WORKING` (some duplication with Worker exists for learning reasons).

#### `WorkerManager.java` & `ThreadPool.java`
- Manages a dynamic pool of worker threads.
- Creates `Worker` objects and wraps them in `Thread`.
- Provides info collection and graceful shutdown.

#### `LiveInformation.java`
- Uses `AtomicInteger` for thread-safe live counters (working jobs + completed jobs).
- Shared across all workers.

#### `Information.java`
- Immutable summary of a worker's performance.

---

### 4. Server / Orchestration

#### `Orchestrator.java`
- Main coordinator.
- Starts `JobAdder`, `WorkerManager`, and `DashBoard`.
- Uses `ScheduledExecutorService` to auto-shutdown after specified runtime.

---

### 5. Dashboard

#### `DashBoard.java`
- Two scheduled tasks:
  1. Every 1 second: Print live working + completed count.
  2. At the end of runtime: Print final worker statistics.
- Uses `Log.print()` for synchronized console output.

#### `Log.java`
- Simple synchronized logger to prevent console output corruption from multiple threads.

---

### 6. CRUD Operations

#### `APIcalls.java` + `StartCURD.java`
- Full CRUD support:
  - **POST**: Create new user + multiple tasks.
  - **GET**: View user and all tasks.
  - **PATCH**: Update ID, name, and individual task fields.
  - **DELETE**: Delete user with confirmation.
- Separate execution mode from job processing.

---

### 7. Main Entry Point

#### `Main.java`
- Simple menu to choose between **JOB Processing** or **CRUD Operations**.
- Takes runtime and worker count as input.

---

## Storage Format (`storage.json`)

Uses a map of user ID → TaskGroup. Example structure is provided in the original code (50 sample users with 2 tasks each).

All tasks start with status `"Database"` or `"COMPLETED"`.

---

## Key Features

1. **Multi-threaded Job Processing**
   - Configurable number of workers.
   - Producer-Consumer pattern using `LinkedBlockingQueue`.
   - Random work simulation.

2. **Persistent Task Management**
   - JSON file as database.
   - Full CRUD via console.

3. **Real-time Dashboard**
   - Live metrics every second.
   - Final summary with per-worker stats.

4. **Thread Safety**
   - Synchronized status updates.
   - Atomic counters.
   - Proper interruption handling.

5. **Graceful Shutdown**
   - Auto shutdown after specified time.
   - Manual shutdown support.

6. **Educational Design**
   - Heavy use of comments.
   - Clear separation of concerns.
   - Explicit state management.

---

## Design Decisions & Reasons (Learning Focused)

- **Why JSON + File?**  
  To learn serialization, file I/O, and persistence without databases.

- **Why `Stateupgrader` with full read/write?**  
  To practice deep Jackson `JsonNode` manipulation and ensure atomic updates in concurrent environment.

- **Why separate `Processor`?**  
  To demonstrate further separation of concerns (Worker = orchestration, Processor = business logic).

- **Why `LinkedHashMap`?**  
  To preserve order of users.

- **Why `volatile boolean` + interruption?**  
  To learn proper thread lifecycle management.

- **Why random sleep?**  
  To simulate variable workload realistically.

- **Why two modes (CRUD vs Processing)?**  
  To keep data management separate from execution for clarity.

---

## Limitations & Known Issues

1. **Performance & Scalability**
   - Full JSON rewrite on every status change → bad for large datasets.
   - No batching or caching.

2. **Thread Safety Gaps**
   - `DataManupulator` is not fully thread-safe if CRUD and processing run together.
   - Multiple `Stateupgrader` instances (could be singleton).

3. **Error Handling**
   - Many `printStackTrace()` instead of proper logging/recovery.
   - Scanner input can break on wrong input types.

4. **Code Quality**
   - Typos in class names (`Manupulator`, `Stateupgrader`).
   - Some code duplication (status updates).
   - Hardcoded file path with Windows separators.
   - Magic strings for status instead of Enum.

5. **Features Missing**
   - No task priority.
   - No retry mechanism on failure.
   - No proper validation.
   - No concurrent CRUD during processing.
   - No progress percentage or ETA.

6. **Platform Dependency**
   - File path uses `\\` (Windows-specific).

7. **Resource Management**
   - Potential thread leaks if shutdown is not clean.
   - No resource limits on queue size.

---

## Testing & Demo Results

The system was tested with:
- 5 workers, 5-second runtime → successfully processed 13 jobs.
- CRUD operations (POST, GET, PATCH, DELETE) work correctly.
- Dashboard shows live updates and final summary.

All sample data (50 users) loads correctly.

---

## Learning Outcomes (Personal Reflection)

As a 17-year-old developer on my **second major project**, this was an incredible learning experience:

- Mastered Java concurrency (`BlockingQueue`, `ExecutorService`, `synchronized`, `AtomicInteger`, `volatile`).
- Learned deep JSON manipulation with Jackson.
- Understood the importance of proper shutdown and interruption handling.
- Experienced real challenges of shared mutable state.
- Improved debugging skills with multi-threaded race conditions.
- Learned to write more structured, commented code.

This project took **10 days of planning** (diagrams, thinking about architecture) and **4 days of nonstop coding**. Every single class exists for a reason — mostly to learn that specific concept.

---

## Future Improvements (Roadmap)

1. Fix all naming issues.
2. Introduce `TaskStatus` enum.
3. Create a proper `TaskRepository` with better locking/caching.
4. Add logging (SLF4J).
5. Migrate to Spring Boot + H2/SQLite.
6. Add REST API.
7. Implement task retry, priorities, and failure handling.
8. Unit + Integration tests.
9. Configuration file support.
10. GUI (JavaFX or Swing).

---

## Conclusion

**TaskFlow** is a fully functional, from-scratch multi-threaded task processing engine built purely for learning. Despite its limitations, it successfully demonstrates core backend engineering concepts like concurrency, persistence, monitoring, and system orchestration.

> "The goal was never perfection — it was understanding."

**Made with passion, curiosity, and countless hours of debugging.**

---

**End of Documentation**  
*Generated as part of project review - June 2026*