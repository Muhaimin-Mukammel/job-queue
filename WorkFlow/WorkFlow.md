# TaskFlow — Detailed System Flowcharts

---

## Table of Contents

1. [Mode 1: Job Processing Flow](#mode-1-job-processing-flow)
2. [Mode 2: CRUD Operations Flow](#mode-2-crud-operations-flow)
3. [Cross-Cutting: StateUpgrader (Thread-Safe Write)](#cross-cutting-stateupgrader-thread-safe-write)
4. [Component Interaction Map](#component-interaction-map)

---

## Mode 1: Job Processing Flow

```
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                              Main.java (Entry Point)                            │
                                │                                                                                 │
                                │   TaskGroup database = new TaskGroup()  ──────► loads file reference            │
                                │                        (src\main\java\database\storage.json)                    │
                                │                                                                                 │
                                │   Orchestrator orchestrator = new Orchestrator(database)                        │
                                │                                                                                 │
                                │   User inputs:                                                                  │
                                │   ┌──────────────────────────────────────────────────────┐                      │
                                │   │  1. JOB Processing  ◄── user presses 1               │                      │
                                │   │  2. CRUD Operations                                  │                      │
                                │   └──────────────────────────────────────────────────────┘                      │
                                │            │                                                                    │
                                │            ▼                                                                    │
                                │   Prompt: "Enter runtime in seconds"  →  int run                                │
                                │   Prompt: "Enter number of workers"   →  int workercount                        │
                                │            │                                                                    │
                                │            ▼                                                                    │
                                │   orchestrator.start(run, workercount)                                          │
                                └─────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                                        ▼
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                         Orchestrator.start()                                    │
                                │                                                                                 │
                                │   ┌─────────────────────────────────────────────────────────────────────────┐   │
                                │   │  STEP 1 — Create JobAdder (Producer Thread)                             │   │
                                │   │                                                                         │   │
                                │   │  JobAdder jobAdder = new JobAdder(database)                             │   │
                                │   │  BlockingQueue<Job> queue = jobAdder.getQueue()                         │   │
                                │   │       └──► LinkedBlockingQueue<Job> (unbounded, shared queue)           │   │
                                │   └─────────────────────────────────────────────────────────────────────────┘   │
                                │                              │                                                  │
                                │                              ▼                                                  │
                                │   ┌─────────────────────────────────────────────────────────────────────────┐   │
                                │   │  STEP 2 — Create and Start WorkerManager (Consumer Pool)                │   │
                                │   │                                                                         │   │
                                │   │  WorkerManager workerManager = new WorkerManager(queue, database,       │   │
                                │   │                                                  workercount)           │   │
                                │   │       └──► internally creates ThreadPool(queue, database)               │   │
                                │   │                                                                         │   │
                                │   │  workerManager.startWorkerManager()                                     │   │
                                │   │       └──► threadpool.startThreadpool(workercount)                      │   │
                                │   │              └──► for i = 1 to workercount:                             │   │
                                │   │                       Worker w = new Worker(queue, database,            │   │
                                │   │                                             "Worker i:", liveInfo)      │   │
                                │   │                       Thread t = new Thread(w)                          │   │
                                │   │                       t.start()  ──────────────────────────────────────►│   │
                                │   │                                   (workers now BLOCKING on queue.take())│   │
                                │   └─────────────────────────────────────────────────────────────────────────┘   │
                                │                              │                                                  │
                                │                              ▼                                                  │
                                │   ┌─────────────────────────────────────────────────────────────────────────┐   │
                                │   │  STEP 3 — Start JobAdder Thread (Producer)                              │   │
                                │   │                                                                         │   │
                                │   │  Thread Ja = new Thread(jobAdder)                                       │   │
                                │   │  Ja.start()  ─────────────────────────────────────────────────────────► │   │
                                │   │              (producer now runs JobAdder.run() — see below)             │   │
                                │   └─────────────────────────────────────────────────────────────────────────┘   │
                                │                              │                                                  │
                                │                              ▼                                                  │
                                │   ┌─────────────────────────────────────────────────────────────────────────┐   │
                                │   │  STEP 4 — Start Dashboard                                               │   │
                                │   │                                                                         │   │
                                │   │  DashBoard dashBoard = new DashBoard(queue, database,                   │   │
                                │   │                                      workerManager, run)                │   │
                                │   │  dashBoard.Dashboard()                                                  │   │
                                │   │       └──► ScheduledExecutorService (single thread)                     │   │
                                │   │              Task A — every 1 second:                                   │   │
                                │   │                  Log.print("Running Job : " + getworkingjobinfo())      │   │
                                │   │                  Log.print("Completed jobs : " + getdonejobinfo())      │   │
                                │   │              Task B — after runtime seconds:                            │   │
                                │   │                  print per-worker stats + total                         │   │
                                │   │                  active = false; scheduler.shutdown()                   │   │
                                │   └─────────────────────────────────────────────────────────────────────────┘   │
                                │                              │                                                  │
                                │                              ▼                                                  │
                                │   ┌─────────────────────────────────────────────────────────────────────────┐   │
                                │   │  STEP 5 — Schedule Auto-Shutdown (ScheduledExecutorService)             │   │
                                │   │                                                                         │   │
                                │   │  scheduler.schedule(after runtime seconds):                             │   │
                                │   │      jobAdder.shutdown()         ─── sets isRunning=false,              │   │
                                │   │                                       clears queue, interrupts thread   │   │
                                │   │      workerManager.shutDownworkers() ─ interrupts all worker threads    │   │
                                │   │      workerManager.clearworker()     ─ clears workerObject list         │   │
                                │   │      scheduler.shutdown()                                               │   │
                                │   └─────────────────────────────────────────────────────────────────────────┘   │
                                │                              │                                                  │
                                │                              ▼                                                  │
                                │                         Ja.join()  ◄── main thread waits for producer to end    │
                                └─────────────────────────────────────────────────────────────────────────────────┘
```

---

### JobAdder.run() — The Producer Thread

```
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                          JobAdder.run()  [Producer Thread]                      │
                                │                                                                                 │
                                │   ┌─────────────────────────────────────┐                                       │
                                │   │  Init Stateupgrader(database.file)  │                                       │
                                │   │  Init DataManupulator(database)     │                                       │
                                │   │  manupulate.load()                  │ ──► reads storage.json into           │
                                │   │                                     │     LinkedHashMap<String,TaskGroup>   │
                                │   └─────────────────────────────────────┘                                       │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   for each entry in LinkedHashMap  (iterates in insertion order)                │
                                │   ┌─────────────────────────────────────────────────────────────────────────┐   │
                                │   │                                                                         │   │
                                │   │   ┌───────────────────────────────────────────┐                         │   │
                                │   │   │  isRunning == false?                       │                        │   │
                                │   │   │  YES ──► break outer loop                  │                        │   │
                                │   │   │  NO  ──► continue                          │                        │   │
                                │   │   └───────────────────────────────────────────┘                         │   │
                                │   │                    │                                                    │   │
                                │   │                    ▼                                                    │   │
                                │   │   String id = entry.getKey()                                            │   │
                                │   │   TaskGroup group = entry.getValue()                                    │   │
                                │   │   List<Task> tasks = group.getTask()                                    │   │
                                │   │                    │                                                    │   │
                                │   │                    ▼                                                    │   │
                                │   │   ┌────────────────────────────┐                                        │   │
                                │   │   │  tasks == null?            │                                        │   │
                                │   │   │  YES ──► continue          │ (skip this group)                      │   │
                                │   │   │  NO  ──► proceed           │                                        │   │
                                │   │   └────────────────────────────┘                                        │   │
                                │   │                    │                                                    │   │
                                │   │                    ▼                                                    │   │
                                │   │   for each Task t in tasks:                                             │   │
                                │   │   ┌─────────────────────────────────────────────────────────────────┐   │   │
                                │   │   │                                                                 │   │   │
                                │   │   │   ┌────────────────────────────────────────────────────────┐    │   │   │
                                │   │   │   │  isRunning == false OR Thread.isInterrupted()?         │    │   │   │
                                │   │   │   │  YES ──► break inner loop                              │    │   │   │
                                │   │   │   │  NO  ──► continue                                      │    │   │   │
                                │   │   │   └────────────────────────────────────────────────────────┘    │   │   │
                                │   │   │                    │                                            │   │   │
                                │   │   │                    ▼                                            │   │   │
                                │   │   │   ┌─────────────────────────────────────────────────────┐       │   │   │
                                │   │   │   │  t.getStatus() == "COMPLETED" ?                     │       │   │   │
                                │   │   │   │  YES ──► continue  (skip already done tasks)        │       │   │   │
                                │   │   │   │  NO  ──► proceed                                    │       │   │   │
                                │   │   │   └─────────────────────────────────────────────────────┘       │   │   │
                                │   │   │                    │                                            │   │   │
                                │   │   │                    ▼                                            │   │   │
                                │   │   │   int taskid = t.getTaskno()                                    │   │   │
                                │   │   │                    │                                            │   │   │
                                │   │   │                    ▼                                            │   │   │
                                │   │   │   queue.put(new Job(id, t))                                     │   │   │
                                │   │   │       └──► BLOCKS if queue is full (unbounded ∴ never blocks)   │   │   │
                                │   │   │       └──► throws InterruptedException ──► interrupt+return     │   │   │
                                │   │   │                    │                                            │   │   │
                                │   │   │                    ▼                                            │   │   │
                                │   │   │   stateupgrader.upgrade(id, taskid, "Waiting")                  │   │   │
                                │   │   │       └──► synchronized file read-modify-write                  │   │   │
                                │   │   │       └──► storage.json task status → "Waiting"                 │   │   │
                                │   │   │                                                                 │   │   │
                                │   │   └─────────────────────────────────────────────────────────────────│   │   │
                                │   │                                                                         │   │  
                                │   └──────────────────────────────────────────────────────────────────────────   │ 
                                │                                                                                 │  
                                │   (Producer thread finishes — Ja.join() in Orchestrator returns)                │  
                                └─────────────────────────────────────────────────────────────────────────────────┘
```

---

### Worker.run() — Each Consumer Thread (runs concurrently, one per worker)

```
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                     Worker.run()  [Consumer Thread — N instances]               │
                                │                                                                                 │
                                │   Init Processor(queue, database.getFile())                                     │
                                │       └──► Processor creates its own Stateupgrader instance                     │
                                │                                                                                 │
                                │   ┌─────────────────────────────────────────────────────────────────────────┐   │
                                │   │                     MAIN LOOP (while not interrupted)                   │   │
                                │   │                                                                         │   │
                                │   │   ┌────────────────────────────────────────────────────────────────┐    │   │
                                │   │   │  Job job = queue.take()                                        │    │   │
                                │   │   │      └──► BLOCKS here until a job is available                 │    │   │
                                │   │   │      └──► InterruptedException ──► interrupt flag set ──► break│    │   │
                                │   │   └────────────────────────────────────────────────────────────────┘    │   │
                                │   │                    │                                                    │   │
                                │   │                    ▼                                                    │   │
                                │   │   liveInformation.Jobworking()                                          │   │
                                │   │       └──► Livejobcounter.incrementAndGet()  (AtomicInteger)            │   │
                                │   │                    │                                                    │   │
                                │   │                    ▼                                                    │   │
                                │   │   String id    = job.getTaskid()                                        │   │
                                │   │   Task task    = job.getTask()                                          │   │
                                │   │   int taskid   = task.getTaskno()                                       │   │
                                │   │                    │                                                    │   │
                                │   │                    ▼                                                    │   │
                                │   │   ┌──────────────────────── try block ─────────────────────────────┐    │   │
                                │   │   │                                                                │    │   │
                                │   │   │   stateupgrader.upgrade(id, taskid, "WORKING")                 │    │   │
                                │   │   │       └──► synchronized(lock) {                                │    │   │
                                │   │   │               read storage.json → find task → set "WORKING"    │    │   │
                                │   │   │               write storage.json                               │    │   │
                                │   │   │           }                                                    │    │   │
                                │   │   │                    │                                           │    │   │
                                │   │   │                    ▼                                           │    │   │
                                │   │   │   processor.workProcessor(job, file, id, taskid)               │    │   │
                                │   │   │   ┌────────────────────────────────────────────────────────┐   │    │   │
                                │   │   │   │  Processor.workProcessor():                            │   │    │   │
                                │   │   │   │                                                        │   │    │   │
                                │   │   │   │  stateupgrader.upgrade(id, taskid, "WORKING")          │   │    │   │
                                │   │   │   │  [NOTE: duplicate status write — learning artifact]    │   │    │   │
                                │   │   │   │                    │                                   │   │    │   │
                                │   │   │   │                    ▼                                   │   │    │   │
                                │   │   │   │  randomtime = 500 + Random.nextInt(2500)  ms           │   │    │   │
                                │   │   │   │  [simulates variable workload: 500ms – 3000ms]         │   │    │   │
                                │   │   │   │                    │                                   │   │    │   │
                                │   │   │   │                    ▼                                   │   │    │   │
                                │   │   │   │  Thread.sleep(randomtime)                              │   │    │   │
                                │   │   │   │      └──► InterruptedException ──► interrupt + return  │   │    │   │
                                │   │   │   └────────────────────────────────────────────────────────┘   │    │   │
                                │   │   │                    │                                           │    │   │
                                │   │   │                    ▼                                           │    │   │
                                │   │   │   numberofworkdone += 1                                        │    │   │
                                │   │   │                    │                                           │    │   │
                                │   │   │                    ▼                                           │    │   │
                                │   │   │   stateupgrader.upgrade(id, taskid, "COMPLETED")               │    │   │
                                │   │   │       └──► synchronized(lock) {                                │    │   │
                                │   │   │               read storage.json → find task → set "COMPLETED"  │    │   │
                                │   │   │               write storage.json                               │    │   │
                                │   │   │           }                                                    │    │   │
                                │   │   │                                                                │    │   │
                                │   │   ├──────────────────────── catch Exception ───────────────────────┤    │   │
                                │   │   │   e.printStackTrace()                                          │    │   │
                                │   │   │                                                                │    │   │
                                │   │   ├──────────────────────── finally ───────────────────────────────┤    │   │
                                │   │   │   liveInformation.JobDone()                                    │    │   │
                                │   │   │       └──► Livejobcounter.decrementAndGet()  (working -1)      │    │   │
                                │   │   │       └──► Livedonejobcounter.incrementAndGet()  (done +1)     │    │   │
                                │   │   └────────────────────────────────────────────────────────────────┘    │   │
                                │   │                                                                         │   │
                                │   │   ──── loops back to queue.take() ────────────────────────────────────► │   │
                                │   │                                                                         │   │
                                │   └─────────────────────────────────────────────────────────────────────────┘   │
                                │                                                                                 │
                                │   [Thread interrupted by WorkerManager.shutDownworkers()  ──►  loop exits]      │
                                └─────────────────────────────────────────────────────────────────────────────────┘
```

---

### Shutdown Sequence

```
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                     Shutdown Sequence (after runtime expires)                   │
                                │                                                                                 │
                                │   ScheduledExecutorService fires at T = runtime seconds                         │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   jobAdder.shutdown()                                                           │
                                │       ├──► isRunning = false        (volatile — visible to producer thread)     │
                                │       ├──► queue.clear()            (removes unprocessed jobs)                  │
                                │       └──► thread.interrupt()       (wakes producer if blocked)                 │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   workerManager.shutDownworkers()                                               │
                                │       └──► threadpool.shutdownThreadpool()                                      │
                                │               ├──► for each worker thread: thread.interrupt()                   │
                                │               │         └──► Worker.run() catches InterruptedException          │
                                │               │               └──► Thread.currentThread().interrupt()           │
                                │               │               └──► break → exits main loop                      │
                                │               ├──► for each worker thread: thread.join()                        │
                                │               │         └──► waits for each worker to fully stop                │
                                │               └──► workers.clear()                                              │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   workerManager.clearworker()                                                   │
                                │       └──► threadpool.clearWorkers() ──► workerObject.clear()                   │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   scheduler.shutdown()  (Orchestrator's scheduler)                              │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   Dashboard Task B fires simultaneously (same scheduler, same delay):           │
                                │       └──► print per-worker stats + total jobs done                             │
                                │       └──► active = false; dashboardScheduler.shutdown()                        │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   Ja.join() returns (producer already stopped) ──► main() exits                 │
                                └─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Mode 2: CRUD Operations Flow

```
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                              Main.java (Entry Point)                            │
                                │                                                                                 │
                                │   User presses 2  ──►  orchestrator.startCURD()                                 │
                                └─────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                                        ▼
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                        Orchestrator.startCURD()                                 │
                                │                                                                                 │
                                │   StartCURD startCURD = new StartCURD(database)                                 │
                                │   startCURD.STARTCURD()                                                         │
                                └─────────────────────────────────────────────────────────────────────────────────┘
                                                        │
                                                        ▼
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                          StartCURD.STARTCURD()                                  │
                                │                                                                                 │
                                │   APIcalls apIcalls = new APIcalls(database)                                    │
                                │       └──► DataManupulator, ObjectMapper, Scanner initialized                   │
                                │                                                                                 │
                                │   ┌─────────────────────────────────────────────────────────────────────────┐   │
                                │   │                         MENU LOOP (while true)                          │   │
                                │   │                                                                         │   │
                                │   │   Print menu:                                                           │   │
                                │   │       1. Get data                                                       │   │
                                │   │       2. Post data                                                      │   │
                                │   │       3. Update data                                                    │   │
                                │   │       4. Delete data                                                    │   │
                                │   │       5. Exit                                                           │   │
                                │   │                    │                                                    │   │
                                │   │                    ▼                                                    │   │
                                │   │   int key = scanner.nextInt()                                           │   │
                                │   │                    │                                                    │   │
                                │   │         ┌──────────┼──────────────────────────┐                         │   │
                                │   │         │          │          │               │                         │   │
                                │   │         ▼          ▼          ▼               ▼                         │   │
                                │   │       key=1      key=2      key=3           key=4      key=5            │   │
                                │   │      getdata   postdata   patchdata       deletedata   break            │   │
                                │   │         │          │          │               │           │             │   │
                                │   │         ▼          ▼          ▼               ▼           ▼             │   │
                                │   │    [GET]      [POST]       [PATCH]         [DELETE]    "Execution       │   │
                                │   │    below      below        below            below       finished"       │   │
                                │   └─────────────────────────────────────────────────────────────────────────┘   │
                                └─────────────────────────────────────────────────────────────────────────────────┘
```

---

### GET — APIcalls.getdata()

```
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                           GET  — APIcalls.getdata()                             │
                                │                                                                                 │
                                │   Prompt: "Enter the ID of the User"                                            │
                                │   String id = sc.nextLine()                                                     │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   manupulate.load()                                                             │
                                │       └──► reads storage.json → LinkedHashMap<String, TaskGroup>                │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   TaskGroup taskGroup = data.get(id)                                            │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ┌────────────────────────────────────────────────────────────┐                │
                                │   │  taskGroup == null?                                        │                │
                                │   │  YES ──► NullPointerException (no null guard — known bug)  │                │ 
                                │   │  NO  ──► proceed                                           │                │
                                │   └────────────────────────────────────────────────────────────┘                │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   Print: taskGroup.getName()                                                    │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   for each Task in taskGroup.getTask():                                         │
                                │       Print: taskno, type, work, status                                         │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   returns to menu loop                                                          │
                                └─────────────────────────────────────────────────────────────────────────────────┘
```

---

### POST — APIcalls.postdata()

```
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                          POST  — APIcalls.postdata()                            │
                                │                                                                                 │
                                │   Prompt: "Enter the ID of the User"                                            │
                                │   String id = sc.nextLine()                                                     │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   manupulate.load()  ──►  LinkedHashMap<String, TaskGroup>                      │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   TaskGroup taskGroup = data.get(id)                                            │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ┌────────────────────────────────────────────────────────────┐                │
                                │   │  taskGroup == null?  (ID does not exist — safe to create)  │                │
                                │   │                                                            │                │
                                │   │  YES ──► proceed                                           │                │
                                │   │  NO  ──► print "User with this ID already exists"          │                │
                                │   │          return to menu                                    │                │
                                │   └────────────────────────────────────────────────────────────┘                │
                                │                    │ (YES branch)                                               │
                                │                    ▼                                                            │
                                │   Prompt: "Enter name of the user"  →  String name                              │
                                │   Prompt: "Enter number of tasks"   →  int numberoftasks                        │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   List<Task> task = new ArrayList<>()                                           │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   for i = 1 to numberoftasks:                                                   │
                                │   ┌─────────────────────────────────────────────────────────┐                   │
                                │   │   Prompt: "Enter type of the task"  →  String type      │                   │
                                │   │   Prompt: "Enter the task"          →  String work      │                   │
                                │   │   String status = "Database"        (hardcoded default) │                   │
                                │   │   task.add(new Task(i, type, work, status))             │                   │
                                │   └─────────────────────────────────────────────────────────┘                   │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   manupulate.write(id, name, task)                                              │
                                │       └──► data.put(id, new TaskGroup(id, name, task))                          │
                                │       └──► mapper.writerWithDefaultPrettyPrinter()                              │
                                │                   .writeValue(storage.json, data)                               │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   returns to menu loop                                                          │
                                └─────────────────────────────────────────────────────────────────────────────────┘
```

---

### PATCH — APIcalls.patchdata()

```
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                        PATCH  — APIcalls.patchdata()                            │
                                │                                                                                 │
                                │   Prompt: "Enter the ID of the User"                                            │
                                │   String id = sc.nextLine()                                                     │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   manupulate.load()  ──►  LinkedHashMap<String, TaskGroup>                      │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   TaskGroup taskGroup = data.get(id)                                            │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ┌────────────────────────────────────────────────────────┐                    │
                                │   │  taskGroup == null?                                    │                    │
                                │   │  YES ──► print "User does not exist"  →  return        │                    │
                                │   │  NO  ──► proceed                                       │                    │
                                │   └────────────────────────────────────────────────────────┘                    │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   Print all existing user info (name, all task fields)                          │
                                │   String oldId = taskGroup.getId()                                              │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   Print: "--- Update any info --- Note: Press enter to skip"                    │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   Prompt: "Enter new ID"  →  String newId                                       │
                                │   ┌───────────────────────────────────────────────────┐                         │
                                │   │  newId.isEmpty()?                                 │                         │
                                │   │  YES ──► newId = oldId  (no change)               │                         │
                                │   │  NO  ──► taskGroup.setId(newId)                   │                         │
                                │   └───────────────────────────────────────────────────┘                         │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   Prompt: "Enter new name"  →  String name                                      │
                                │   ┌───────────────────────────────────────────────────┐                         │
                                │   │  name.isEmpty()?                                  │                         │
                                │   │  YES ──► keep existing name                       │                         │
                                │   │  NO  ──► taskGroup.setName(name)                  │                         │
                                │   └───────────────────────────────────────────────────┘                         │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   for each Task in taskGroup.getTask():                                         │
                                │   ┌─────────────────────────────────────────────────────────┐                   │
                                │   │   Prompt: "Enter new Task type"  →  String tasktype     │                   │
                                │   │   ┌───────────────────────────────────────────────┐     │                   │
                                │   │   │  tasktype.isEmpty() ? skip : tasks.setType()  │     │                   │
                                │   │   └───────────────────────────────────────────────┘     │                   │
                                │   │   Prompt: "Enter new Task"  →  String work              │                   │
                                │   │   ┌───────────────────────────────────────────────┐     │                   │
                                │   │   │  work.isEmpty() ? skip : tasks.setWork()      │     │                   │
                                │   │   └───────────────────────────────────────────────┘     │                   │
                                │   │   tasks.setStatus("Database")   (always reset to fresh) │                   │
                                │   └─────────────────────────────────────────────────────────┘                   │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ┌───────────────────────────────────────────────────────────┐                 │
                                │   │  oldId != newId?                                          │                 │
                                │   │  YES ──► data.remove(oldId)   (remove old key)            │                 │
                                │   │  NO  ──► skip remove                                      │                 │
                                │   └───────────────────────────────────────────────────────────┘                 │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   data.put(newId, taskGroup)                                                    │
                                │   manupulate.writeAll(data)                                                     │
                                │       └──► mapper.writerWithDefaultPrettyPrinter()                              │
                                │                   .writeValue(storage.json, entire map)                         │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   returns to menu loop                                                          │
                                └─────────────────────────────────────────────────────────────────────────────────┘
```

---

### DELETE — APIcalls.deletedata()

```
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                        DELETE  — APIcalls.deletedata()                          │
                                │                                                                                 │
                                │   Prompt: "Enter the ID of the User to delete"                                  │
                                │   String id = sc.nextLine()                                                     │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   manupulate.load()  ──►  LinkedHashMap<String, TaskGroup>                      │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   TaskGroup taskGroup = data.get(id)                                            │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ┌────────────────────────────────────────────────────────┐                    │
                                │   │  taskGroup == null?                                    │                    │
                                │   │  YES ──► print "User does not exist"  →  return        │                    │
                                │   │  NO  ──► proceed                                       │                    │
                                │   └────────────────────────────────────────────────────────┘                    │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   Print: "--- User found ---"                                                   │
                                │   Print: taskGroup.getName()                                                    │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   Prompt: "Are you sure you want to delete? (y/n)"                              │
                                │   String confirm = sc.nextLine()                                                │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ┌────────────────────────────────────────────────────────────┐                │
                                │   │  confirm.equalsIgnoreCase("y")?                            │                │
                                │   │                                                            │                │
                                │   │  YES ──► data.remove(id)                                   │                │
                                │   │          manupulate.writeAll(data)                         │                │
                                │   │              └──► storage.json rewritten without that user │                │
                                │   │          print "User deleted successfully"                 │                │
                                │   │                                                            │                │
                                │   │  NO  ──► print "Delete cancelled"                          │                │
                                │   └────────────────────────────────────────────────────────────┘                │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   returns to menu loop                                                          │
                                └─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Cross-Cutting: StateUpgrader (Thread-Safe Write)

```
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                  Stateupgrader.upgrade(id, taskNo, state)                       │
                                │         Called by: JobAdder, Worker, Processor  (concurrent context)            │
                                │                                                                                 │
                                │   synchronized (lock)  ◄── static Object lock — ONE shared lock across ALL      │
                                │   {                        instances; serializes all file writes                │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ObjectNode root = (ObjectNode) mapper.readTree(file)                          │
                                │       └──► reads entire storage.json fresh from disk                            │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ┌───────────────────────────┐                                                 │
                                │   │  root == null ? return    │                                                 │
                                │   └───────────────────────────┘                                                 │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   JsonNode dbNode = root.get(id)  ──► finds the user's JSON object              │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ┌────────────────────────────────────────────────────────┐                    │
                                │   │  dbNode == null OR !dbNode.isObject() ? return         │                    │
                                │   └────────────────────────────────────────────────────────┘                    │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ObjectNode node = (ObjectNode) dbNode                                         │
                                │   JsonNode tasksNode = node.get("task")                                         │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ┌────────────────────────────────────────────────────────┐                    │
                                │   │  tasksNode == null OR !tasksNode.isArray() ? return    │                    │
                                │   └────────────────────────────────────────────────────────┘                    │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   ArrayNode tasks = (ArrayNode) tasksNode                                       │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   for each JsonNode t in tasks:                                                 │
                                │   ┌─────────────────────────────────────────────────────────┐                   │
                                │   │   !t.isObject() ? continue                              │                   │
                                │   │   ObjectNode task = (ObjectNode) t                      │                   │
                                │   │   JsonNode taskNoNode = task.get("taskno")              │                   │
                                │   │   taskNoNode == null ? continue                         │                   │
                                │   │                │                                        │                   │
                                │   │                ▼                                        │                   │
                                │   │   ┌─────────────────────────────────────────────┐       │                   │
                                │   │   │  taskNoNode.asInt() == taskNo?              │       │                   │
                                │   │   │  YES ──► task.put("status", state)          │       │                   │
                                │   │   │          break                              │       │                   │
                                │   │   │  NO  ──► continue loop                      │       │                   │
                                │   │   └─────────────────────────────────────────────┘       │                   │
                                │   └─────────────────────────────────────────────────────────┘                   │
                                │                    │                                                            │
                                │                    ▼                                                            │
                                │   mapper.writerWithDefaultPrettyPrinter().writeValue(file, root)                │
                                │       └──► writes entire modified JSON tree back to storage.json                │
                                │                    │                                                            │
                                │   }  ◄── synchronized block ends; next thread can now enter                     │
                                └─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Component Interaction Map

```
                                ┌─────────────────────────────────────────────────────────────────────────────────┐
                                │                   Full Component Interaction (Job Processing Mode)              │
                                │                                                                                 │
                                │  ┌──────────┐    creates    ┌──────────────┐   creates   ┌──────────────────┐   │
                                │  │  Main    │──────────────►│ Orchestrator │────────────►│   JobAdder       │   │
                                │  └──────────┘               └──────────────┘             │  (Producer)      │   │
                                │                                    │                     └────────┬─────────┘   │
                                │                                    │ creates                      │             │
                                │                                    ▼                              │ puts Job    │
                                │                             ┌─────────────────┐                  ▼              │
                                │                             │  WorkerManager  │         ┌─────────────────┐     │
                                │                             └────────┬────────┘         │ BlockingQueue   │     │
                                │                                      │ creates          │  <Job>          │     │
                                │                                      ▼                  └────────┬────────┘     │
                                │                             ┌─────────────────┐                  │              │
                                │                             │   ThreadPool    │                  │ takes Job    │
                                │                             └────────┬────────┘                  ▼              │
                                │                                      │ creates N         ┌───────────────┐      │
                                │                                      ▼                   │    Worker     │      │
                                │                             ┌─────────────────┐          │  (Consumer)   │      │
                                │                             │  LiveInformation│◄─────────┤  × N threads  │      │
                                │                             │  (AtomicInteger)│          └───────┬───────┘      │
                                │                             └─────────────────┘                  │              │
                                │                                                                   │ calls       │
                                │                                    ┌──────────────────────────────┤             │
                                │                                    │                              │             │
                                │                                    ▼                              ▼             │
                                │                           ┌─────────────────┐          ┌──────────────────┐     │
                                │                           │  Stateupgrader  │          │    Processor     │     │
                                │                           │  (synchronized) │◄─────────│  (work sim)      │     │
                                │                           └────────┬────────┘          └──────────────────┘     │
                                │                                    │ read/write                                 │
                                │                                    ▼                                            │
                                │                           ┌─────────────────┐                                   │
                                │                           │  storage.json   │                                   │
                                │                           │  (file DB)      │                                   │
                                │                           └─────────────────┘                                   │
                                │                                    ▲                                            │
                                │                                    │ read/write                                 │
                                │                           ┌─────────────────┐                                   │
                                │                           │ DataManupulator │ ◄── used by JobAdder (load)       │
                                │                           └─────────────────┘     and CRUD (load/write/writeAll)│
                                │                                                                                 │
                                │  ┌──────────────────────────────────────────────────────────────┐               │
                                │  │  DashBoard  ──(reads)──►  LiveInformation (via WorkerManager)│               │
                                │  │             ──(reads)──►  Information list (per-worker stats)│               │
                                │  │             ──(uses) ──►  Log.print() for synchronized output│               │
                                │  └──────────────────────────────────────────────────────────────┘               │
                                └─────────────────────────────────────────────────────────────────────────────────┘
```

---

### Task Status State Machine

```
                                                ┌───────────────┐
                                                │   "Database"  │  ◄── initial status (set by CRUD POST)
                                                └───────┬───────┘
                                                        │
                                                        │  JobAdder reads task,
                                                        │  puts Job into queue,
                                                        │  calls Stateupgrader
                                                        ▼
                                                ┌───────────────┐
                                                │   "Waiting"   │  ◄── in BlockingQueue, not yet taken
                                                └───────┬───────┘
                                                        │
                                                        │  Worker.run() calls queue.take()
                                                        │  then stateupgrader.upgrade(..., "WORKING")
                                                        ▼
                                                ┌───────────────┐
                                                │   "WORKING"   │  ◄── being processed (sleep simulation)
                                                └───────┬───────┘
                                                        │
                                                        │  processor.workProcessor() finishes
                                                        │  stateupgrader.upgrade(..., "COMPLETED")
                                                        ▼
                                                ┌───────────────┐
                                                │  "COMPLETED"  │  ◄── terminal state; skipped by JobAdder
                                                └───────────────┘       on next run
```

---

*End of Flowcharts — TaskFlow v1.0*
*Generated June 2026*