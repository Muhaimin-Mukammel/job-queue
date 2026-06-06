# Limitations

## 1. Full JSON Rewrite on Every State Change

Currently, every status update performs:

1. Read entire JSON file
2. Modify task state
3. Rewrite entire file

### Consequences
- Poor scalability
- Increased disk I/O
- Performance degradation for large datasets

This approach was chosen primarily for educational purposes.

---

## 2. File-Based Storage

JSON is useful for learning but has practical limitations.

### Issues
- No indexing
- No transactions
- No concurrent access guarantees
- Limited scalability

A database solution would be more suitable for larger workloads.

---

## 3. No Task Prioritization

All tasks are processed in queue order.

### Missing Features
- High priority jobs
- Urgent task handling
- Scheduling strategies

---

## 4. No Retry Mechanism

Failed tasks cannot be retried automatically.

### Impact
- Temporary failures permanently stop task execution.
- Manual intervention is required.

---

## 5. Limited Error Recovery

Many exceptions are logged without recovery logic.

### Examples
- File access failures
- Corrupted JSON
- Invalid user input

The application may terminate unexpectedly in some scenarios.

---

## 6. Hardcoded Configuration

Several values are currently fixed in code.

### Examples
- File locations
- Timing values
- Processing delays

This reduces portability and flexibility.

---

## 7. Platform Dependency

The storage path currently uses Windows-specific separators.

### Example

```java
src\\main\\java\\database\\storage.json
```

This may require modification on Linux or macOS systems.

---

## 8. String-Based Task States

Task statuses are represented as Strings.

### Risks
- Typographical mistakes
- Inconsistent naming
- Runtime errors

An enum would provide stronger type safety.

---

## 9. Limited Validation

Input validation is minimal.

### Potential Problems
- Empty names
- Invalid task data
- Duplicate identifiers

Additional validation would improve robustness.

---

## 10. No Automated Testing

The project currently lacks:

- Unit tests
- Integration tests
- Concurrency tests

This makes regression detection more difficult.

---

## 11. No Authentication or Authorization

Any user can access and modify stored data.

This is acceptable for a learning project but unsuitable for production environments.

---

## 12. Console-Based Interface

Interaction is limited to terminal input/output.

### Missing Interfaces
- REST API
- Web dashboard
- Desktop GUI

User experience is therefore limited.

---

## 13. Single Process Design

The system runs within a single JVM instance.

### Missing Capabilities
- Distributed workers
- Horizontal scaling
- Network-based task distribution

---

## 14. Queue Persistence

The queue itself is not persisted.

If the application stops unexpectedly:

- Waiting jobs are lost from memory.
- Processing state must be reconstructed from storage.

---

## 15. Educational Trade-Offs

Several design decisions prioritize learning over performance.

Examples include:
- Full file rewrites
- Manual thread management
- Simplified failure handling

These choices were intentional and helped deepen understanding of backend concepts.