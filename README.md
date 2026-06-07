# TaskFlow

A console-based multi-threaded job processing system with JSON persistence, built in pure Java.

---

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup & Running](#setup--running)
- [How It Works](#how-it-works)
  - [Mode 1 — Job Processing](#mode-1--job-processing)
  - [Mode 2 — CRUD Operations](#mode-2--crud-operations)
- [Workflow](#workflow)
- [Architecture](#architecture)
  - [Package Breakdown](#package-breakdown)
  - [Core Flow Diagram](#core-flow-diagram)
- [Data Format](#data-format)
- [Task Status Lifecycle](#task-status-lifecycle)

---

## Overview

TaskFlow is a job processing engine that simulates a real-world task queue. It reads tasks from a JSON file, distributes them across multiple worker threads, processes them concurrently, and updates their status back to the file in real time — all while displaying a live dashboard in the console.

It has two completely separate modes:

- **Job Processing** — multi-threaded execution of tasks from the database
- **CRUD Operations** — console-based interface to create, read, update, and delete task records

---

## Project Structure

```
TaskFlow/
├── src/
│   └── main/
│       └── java/
│           ├── Main.java
│           ├── database/
│           │   ├── TaskGroup.java
│           │   ├── Task.java
│           │   ├── DataManupulator.java
│           │   └── Stateupgrader.java
│           ├── lobby/
│           │   ├── Job.java
│           │   └── JobAdder.java
│           ├── worker/
│           │   ├── Worker.java
│           │   ├── Processor.java
│           │   ├── WorkerManager.java
│           │   ├── ThreadPool.java
│           │   ├── LiveInformation.java
│           │   └── Information.java
│           ├── server/
│           │   └── Orchestrator.java
│           ├── dashboard/
│           │   ├── DashBoard.java
│           │   └── Log.java
│           └── CURD/
│               ├── APIcalls.java
│               └── StartCURD.java
└── src/
    └── main/
        └── java/
            └── database/
                └── storage.json
```

---

## Prerequisites

- **Java** 17 or above
- **Maven** (for dependency management)
- **Jackson Databind** library (declared in `pom.xml`)

The only external dependency is Jackson:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.0</version>
</dependency>
```

---

## Setup & Running

**1. Clone or download the project**

```bash
git clone <your-repo-url>
cd TaskFlow
```

**2. Build with Maven**

```bash
mvn clean compile
```

**3. Run**

```bash
mvn exec:java -Dexec.mainClass="Main"
```

Or run `Main.java` directly from your IDE (IntelliJ IDEA, Eclipse, VS Code with Java extension).

**4. Choose a mode**

```
1. JOB Processing.
2. CURD Operations.
```

For Job Processing, you will be asked:

```
Enter the amount of time that the server should run:
Enter what number of workers should work:
```

> **Note:** The file path in `TaskGroup.java` uses `src\main\java\database\storage.json`. If you are on macOS or Linux, change the separator to a forward slash: `src/main/java/database/storage.json`.

---

## How It Works

### Mode 1 — Job Processing

You choose how long the server runs (in seconds) and how many worker threads to spawn. TaskFlow then:

1. Reads all tasks from `storage.json`
2. Filters out tasks already marked `COMPLETED`
3. Loads remaining tasks into a shared queue
4. Worker threads pick tasks from the queue and process them concurrently
5. Each task's status is updated in the JSON file as it moves through the pipeline
6. The dashboard prints live metrics every second and a final summary when time is up

**Example session:**

```
1. JOB Processing.
2. CURD Operations.
> 1

Enter the amount of time that the server should run:
> 5

Enter what number of workers should work:
> 5

===== DASHBOARD =====
Running Job : 0
Completed jobs : 0
Running Job : 5
Completed jobs : 3
...
Worker 1: 4
Worker 2: 2
Worker 3: 3
Total Job done : 13
```

---

### Mode 2 — CRUD Operations

A console menu for managing records in `storage.json` directly.

```
1. Get data.      → View a user and all their tasks by ID
2. Post data.     → Create a new user with tasks
3. Update data.   → Edit ID, name, task type, task description
4. Delete data.   → Remove a user (with confirmation prompt)
5. Exit.
```

CRUD runs independently from job processing. Running both at the same time is not supported.

---

## Workflow

```
User Input (runtime + worker count)
        │
        ▼
  Orchestrator.start()
        │
        ├──► JobAdder (Thread)          ← Producer
        │        │
        │        │  reads storage.json
        │        │  skips COMPLETED tasks
        │        │  puts Jobs into BlockingQueue
        │        │  marks each as "Waiting" in JSON
        │
        ├──► WorkerManager              ← Manages pool
        │        │
        │        └──► Worker x N        ← Consumers
        │                 │
        │                 │  takes Job from queue
        │                 │  marks as "WORKING" in JSON
        │                 │  Processor simulates work (random 500–3000ms sleep)
        │                 │  marks as "COMPLETED" in JSON
        │
        ├──► DashBoard                  ← Observer
        │        │
        │        │  every 1 second: prints live running + completed count
        │        │  at shutdown: prints per-worker summary + total
        │
        └──► ScheduledExecutorService
                 │
                 └──  after N seconds: shuts down JobAdder + WorkerManager
```

---

## Architecture

### Package Breakdown

| Package | Responsibility |
|---|---|
| `database` | Data models (`TaskGroup`, `Task`), JSON read/write (`DataManupulator`), thread-safe status patching (`Stateupgrader`) |
| `lobby` | Job wrapper object (`Job`), producer thread that loads tasks into the queue (`JobAdder`) |
| `worker` | Consumer threads (`Worker`), work simulation (`Processor`), thread pool management (`ThreadPool`, `WorkerManager`), live counters (`LiveInformation`), per-worker stats (`Information`) |
| `server` | Top-level coordinator (`Orchestrator`) that wires everything together and manages the shutdown timer |
| `dashboard` | Real-time console metrics (`DashBoard`), thread-safe printing (`Log`) |
| `CURD` | Console CRUD interface (`APIcalls`, `StartCURD`), runs independently of the processing pipeline |

---

### Core Flow Diagram

```
┌─────────────────────────────────────────────────────┐
│                     Orchestrator                     │
│                                                     │
│  ┌──────────┐    BlockingQueue    ┌──────────────┐  │
│  │ JobAdder │ ──────────────────► │ WorkerManager│  │
│  │(Producer)│                    │              │  │
│  └──────────┘                    │  Worker 1    │  │
│                                  │  Worker 2    │  │
│  ┌──────────┐                    │  Worker N    │  │
│  │DashBoard │ ◄── LiveInformation│              │  │
│  └──────────┘                    └──────────────┘  │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │              storage.json                   │   │
│  │  (read by JobAdder, updated by Stateupgrader│   │
│  │   after every status change)                │   │
│  └─────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

**Thread safety is handled by:**
- `BlockingQueue` — naturally thread-safe hand-off between producer and consumers
- `Stateupgrader` — uses a `synchronized` block + full JSON read-modify-write so no two threads corrupt the file simultaneously
- `AtomicInteger` in `LiveInformation` — lock-free counters for dashboard metrics
- `volatile boolean` in `JobAdder` — safe visibility of the shutdown flag across threads
- `Log.print()` — `synchronized` console output so dashboard lines don't interleave

---

## Data Format

Tasks are stored in `src/main/java/database/storage.json` as a map of user ID to `TaskGroup`.

```json
{
  "1": {
    "id": "1",
    "name": "Muhaimin Mukammel",
    "task": [
      {
        "taskno": 1,
        "type": "email",
        "work": "send welcome email",
        "status": "Database"
      },
      {
        "taskno": 2,
        "type": "call",
        "work": "follow up with client",
        "status": "Database"
      }
    ]
  }
}
```

| Field | Type | Description |
|---|---|---|
| `id` | String | Unique identifier for the task group (user) |
| `name` | String | Name of the user |
| `taskno` | int | Task number within the group |
| `type` | String | Category of task (`email`, `call`, `meeting`, `report`, `data`) |
| `work` | String | Description of what the task involves |
| `status` | String | Current state of the task (see below) |

---

## Task Status Lifecycle

A task moves through these states during job processing:

```
Database  ──►  Waiting  ──►  WORKING  ──►  COMPLETED
   │               │
   │         (set by JobAdder       (set by Worker
   │          when loaded            via Stateupgrader)
   │          into queue)
   │
(initial state when
 created via CRUD,
 or reset after PATCH)
```

Tasks already in `COMPLETED` state are skipped by `JobAdder` and not re-queued.


 ## Documents
 - Strengths → [Docs/Strengths.md](Docs/Strengths.md)
 - Limitations → [Docs/Limitations.md](Docs/Limitations.md)
 - Extensions → [Docs/Extensions.md](Docs/Extensions.md)
 - Architecture → [Docs/Architecture.md](Docs/Architecture.md)
