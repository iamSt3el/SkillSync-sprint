# Core Java

## 1. OOP Concepts

### Four Pillars

**Encapsulation** — hide internal state, expose via controlled interface
```java
public class Mentor {
    private double rating;  // hidden
    public double getRating() { return rating; }  // controlled access
    public void setRating(double r) {
        if (r < 0 || r > 5) throw new IllegalArgumentException("Invalid rating");
        this.rating = r;
    }
}
```

**Abstraction** — expose what, hide how
```java
public interface PaymentGateway {
    PaymentResult initiate(PaymentRequest request);  // what it does
    // how Razorpay/Stripe does it internally is hidden
}
```

**Inheritance** — child class inherits from parent
```java
public class MentorNotFoundException extends RuntimeException {
    public MentorNotFoundException(Long id) {
        super("Mentor not found with id: " + id);
    }
}
```

**Polymorphism** — same method, different behavior depending on the object
```java
// Runtime polymorphism (method overriding)
PaymentGateway gateway = new RazorpayGateway();
gateway.initiate(request);  // calls RazorpayGateway's implementation

// Compile-time polymorphism (method overloading)
public MentorDTO find(Long id) { ... }
public List<MentorDTO> find(String skill) { ... }
```

---

## 2. Interfaces vs Abstract Classes

| | Interface | Abstract Class |
|---|---|---|
| Methods | abstract (default/static allowed since Java 8) | abstract + concrete |
| Variables | public static final only | any type |
| Constructor | No | Yes |
| Multiple inheritance | Yes — a class can implement many | No — single inheritance |
| When to use | Define a contract/capability | Share code among related classes |

```java
// Interface — defines capability
public interface Searchable {
    List<MentorDTO> search(SearchCriteria criteria);

    default void validateCriteria(SearchCriteria c) {  // Java 8 default method
        if (c == null) throw new IllegalArgumentException("Criteria required");
    }
}

// Abstract class — shares common behavior
public abstract class BaseService {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected void validateId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Invalid ID");
    }

    public abstract void delete(Long id);  // subclasses must implement
}
```

---

## 3. Java Memory Model

### Stack vs Heap

| Stack | Heap |
|---|---|
| Method call frames, local variables, primitives | Objects, instance variables |
| LIFO — frame pushed on method call, popped on return | Managed by Garbage Collector |
| Thread-private | Shared across threads |
| Fast | Slower (GC overhead) |
| StackOverflowError if full | OutOfMemoryError if full |

```java
public void bookSession(Long mentorId) {   // mentorId stored on stack
    Session session = new Session();        // Session object on heap
    session.setMentorId(mentorId);          // reference on stack, object on heap
}
```

### Garbage Collection

GC automatically reclaims heap memory of objects with no live references.

**Generations:**
- **Young Gen (Eden + Survivors)** — new objects created here; Minor GC is fast
- **Old Gen (Tenured)** — long-lived objects promoted here; Major GC is slow
- **Metaspace** — class metadata (replaced PermGen in Java 8+)

**GC Algorithms:**
- G1GC (default since Java 9) — balanced, low pause
- ZGC — ultra-low pause, good for large heaps
- Serial GC — single-threaded, for small apps

---

## 4. Collections Framework

### Key Interfaces

```
Collection
├── List (ordered, duplicates allowed)
│   ├── ArrayList  (dynamic array, fast random access, slow insert/delete at middle)
│   └── LinkedList (doubly linked, fast insert/delete, slow random access)
├── Set (no duplicates)
│   ├── HashSet    (unordered, O(1) ops, allows null)
│   ├── LinkedHashSet (insertion-ordered)
│   └── TreeSet    (sorted, O(log n) ops, no null)
└── Queue
    ├── LinkedList
    ├── PriorityQueue (min-heap, natural ordering)
    └── ArrayDeque (stack/queue, faster than Stack/LinkedList)

Map (key-value, not in Collection hierarchy)
├── HashMap        (unordered, O(1), allows null key)
├── LinkedHashMap  (insertion-ordered)
├── TreeMap        (sorted by key, O(log n))
└── ConcurrentHashMap (thread-safe, no null keys/values)
```

### Choosing the Right Collection

| Need | Use |
|---|---|
| Fast random access by index | ArrayList |
| Frequent insert/delete at head/tail | ArrayDeque |
| No duplicates, fast lookup | HashSet |
| No duplicates, sorted | TreeSet |
| Key-value, fast lookup | HashMap |
| Key-value, sorted by key | TreeMap |
| Thread-safe map | ConcurrentHashMap |

### HashMap Internals

- Array of buckets (default 16)
- `hashCode()` determines bucket index
- Keys with same bucket → linked list (Java 7) → balanced tree (Java 8+) when bucket size > 8
- Load factor 0.75 → resizes (doubles) when 75% full
- **Collisions** are handled by chaining

```java
// Contract: if a.equals(b) then a.hashCode() == b.hashCode()
// You MUST override both hashCode and equals when using custom objects as keys
```

---

## 5. Generics

Type safety at compile time without casting:

```java
// Without generics (old way)
List list = new ArrayList();
list.add("hello");
String s = (String) list.get(0);  // runtime ClassCastException risk

// With generics
List<String> list = new ArrayList<>();
list.add("hello");
String s = list.get(0);  // compile-time type check, no cast needed
```

### Bounded Type Parameters

```java
// Upper bound — T must be Number or a subtype
public <T extends Number> double sum(List<T> list) {
    return list.stream().mapToDouble(Number::doubleValue).sum();
}

// Wildcard — any type
public void print(List<?> list) { ... }

// Upper bounded wildcard — read-only
public double sumAll(List<? extends Number> list) { ... }

// Lower bounded wildcard — write-only
public void addNumbers(List<? super Integer> list) { ... }
```

---

## 6. Exception Handling

### Hierarchy

```
Throwable
├── Error (JVM errors — don't catch)
│   ├── OutOfMemoryError
│   └── StackOverflowError
└── Exception
    ├── Checked Exception (must handle — IOException, SQLException)
    └── RuntimeException (unchecked — don't need to declare/catch)
        ├── NullPointerException
        ├── IllegalArgumentException
        ├── IllegalStateException
        └── IndexOutOfBoundsException
```

### Checked vs Unchecked

```java
// Checked — compiler forces you to handle it
public void readFile(String path) throws IOException {
    Files.readAllBytes(Path.of(path));
}

// Unchecked — no forced handling (RuntimeException subclasses)
public Mentor findMentor(Long id) {
    return mentorRepository.findById(id)
        .orElseThrow(() -> new MentorNotFoundException(id));  // RuntimeException
}
```

**Spring convention:** Use unchecked exceptions for business errors (not found, duplicate, etc.) so they propagate up and get handled by @ControllerAdvice without polluting every method signature.

### try-with-resources

```java
// Automatically closes resources (implements AutoCloseable)
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    // use conn and ps
}  // both closed automatically even on exception
```

### Multi-catch

```java
catch (IOException | SQLException e) {
    log.error("Data access error", e);
}
```

---

## 7. Multithreading and Concurrency

### Creating Threads

```java
// 1. Extend Thread
new Thread(() -> System.out.println("Running")).start();

// 2. Implement Runnable
Runnable task = () -> System.out.println("Running");
new Thread(task).start();

// 3. ExecutorService (preferred)
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> processNotification(event));
executor.shutdown();
```

### Thread States

```
NEW → RUNNABLE → RUNNING → WAITING/BLOCKED/TIMED_WAITING → TERMINATED
```

### synchronized

```java
// Synchronized method — locks on 'this'
public synchronized void increment() {
    count++;
}

// Synchronized block — more granular
public void doWork() {
    synchronized(this) {
        count++;
    }
    // other non-synchronized work
}
```

### volatile

Ensures visibility — writes to a volatile variable are immediately visible to all threads:
```java
private volatile boolean running = true;

// Thread 1
running = false;

// Thread 2 — without volatile, might never see the update
while (running) { ... }
```

### java.util.concurrent

```java
// Atomic operations — thread-safe without synchronized
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();  // atomic read-modify-write

// ReentrantLock — more flexible than synchronized
Lock lock = new ReentrantLock();
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();
}

// CountDownLatch — wait for N events
CountDownLatch latch = new CountDownLatch(3);
// 3 threads each call latch.countDown()
latch.await();  // blocks until count reaches 0
```

### Deadlock

Two threads each holding a lock the other needs:
```java
Thread 1: locks A, waits for B
Thread 2: locks B, waits for A
// Both wait forever — deadlock
```

Prevention: always acquire locks in the same order.

---

## 8. Java 8+ Features

### Lambda Expressions

```java
// Before Java 8
Comparator<Mentor> byRating = new Comparator<Mentor>() {
    public int compare(Mentor a, Mentor b) {
        return Double.compare(b.getRating(), a.getRating());
    }
};

// Lambda
Comparator<Mentor> byRating = (a, b) -> Double.compare(b.getRating(), a.getRating());
```

### Functional Interfaces

```java
Function<String, Integer>  // takes String, returns Integer
Predicate<Mentor>          // takes Mentor, returns boolean
Consumer<Mentor>           // takes Mentor, returns void
Supplier<Mentor>           // takes nothing, returns Mentor
BiFunction<Long, String, MentorDTO>  // two args, returns MentorDTO
```

### Stream API

```java
List<MentorDTO> topMentors = mentors.stream()
    .filter(m -> m.getStatus() == APPROVED)     // filter
    .filter(m -> m.getRating() >= 4.0)          // chain filters
    .sorted(Comparator.comparing(Mentor::getRating).reversed())  // sort
    .limit(10)                                   // limit results
    .map(mapper::toDTO)                          // transform
    .collect(Collectors.toList());               // terminal operation

// GroupBy
Map<MentorStatus, List<Mentor>> byStatus = mentors.stream()
    .collect(Collectors.groupingBy(Mentor::getStatus));

// Reduce
double averageRating = mentors.stream()
    .mapToDouble(Mentor::getRating)
    .average()
    .orElse(0.0);
```

### Optional

Avoids NullPointerException — represents a value that may or may not be present:

```java
Optional<Mentor> optional = mentorRepository.findById(id);

// BAD — defeats the purpose
Mentor mentor = optional.get();  // throws if empty

// GOOD
Mentor mentor = optional.orElseThrow(() -> new MentorNotFoundException(id));
Mentor mentor = optional.orElse(defaultMentor);
optional.ifPresent(m -> log.info("Found: {}", m.getId()));

// Chain operations
String bio = mentorRepository.findByUserId(userId)
    .map(Mentor::getBio)
    .filter(b -> !b.isBlank())
    .orElse("No bio provided");
```

### Method References

```java
// Instance method
mentors.stream().map(Mentor::getBio)           // mentor -> mentor.getBio()
// Static method
.map(String::valueOf)                           // s -> String.valueOf(s)
// Constructor
.collect(Collectors.toList())
Stream.generate(Mentor::new)                   // () -> new Mentor()
```

### var (Java 10)

```java
var mentors = mentorRepository.findAll();  // type inferred as List<Mentor>
var mentor = mentors.get(0);               // type inferred as Mentor
```

### Records (Java 16)

Immutable data classes — auto-generates constructor, getters, equals, hashCode, toString:
```java
public record MentorDTO(Long id, String bio, double rating, List<String> skills) {}

// Usage
MentorDTO dto = new MentorDTO(1L, "Expert in Java", 4.8, List.of("Java", "Spring"));
dto.id();      // getter
dto.rating();  // getter
```

---

## 9. String

```java
// String is immutable — every operation creates a new String
String s = "hello";
s.toUpperCase();  // s is still "hello" — returns a NEW string
s = s.toUpperCase();  // now s = "HELLO"

// String pool — string literals are interned
String a = "hello";
String b = "hello";
a == b;          // true (same pool object)
a.equals(b);     // true

String c = new String("hello");
a == c;          // false (different object)
a.equals(c);     // true

// StringBuilder for concatenation in loops
StringBuilder sb = new StringBuilder();
for (String s : list) {
    sb.append(s).append(",");
}
String result = sb.toString();
// DON'T do: String result = ""; for (String s : list) result += s;
// Each += creates a new String — O(n²)
```

### Key String Methods
```java
s.length(), s.charAt(i), s.substring(start, end)
s.contains("sub"), s.startsWith("pre"), s.endsWith("suf")
s.indexOf("x"), s.lastIndexOf("x")
s.toLowerCase(), s.toUpperCase(), s.trim(), s.strip()
s.replace("old", "new"), s.replaceAll("regex", "new")
s.split(","), String.join(",", list)
s.isEmpty(), s.isBlank()  // isBlank() also checks whitespace-only
String.format("Hello %s, you are %d years old", name, age)
```

---

## 10. equals() and hashCode()

**Contract:**
- If `a.equals(b)` → `a.hashCode() == b.hashCode()` (must hold)
- If `a.hashCode() == b.hashCode()` → `a.equals(b)` may or may not hold (collision is OK)

Always override both together. Lombok's `@EqualsAndHashCode` does this automatically.

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Mentor)) return false;
    Mentor m = (Mentor) o;
    return Objects.equals(id, m.id);
}

@Override
public int hashCode() {
    return Objects.hash(id);
}
```

---

## Common Interview Questions

**Q: What is the difference between == and equals()?**
A: == checks reference equality (same object in memory). equals() checks logical equality (defined by the class). For Strings, use equals() — == may return false for two String objects with the same content.

**Q: What is the difference between ArrayList and LinkedList?**
A: ArrayList uses a dynamic array — O(1) random access, O(n) insert/delete in middle. LinkedList uses doubly-linked nodes — O(n) random access, O(1) insert/delete at known position. ArrayList is better for most use cases (better cache locality).

**Q: What is the difference between HashMap and ConcurrentHashMap?**
A: HashMap is not thread-safe — concurrent modifications cause ConcurrentModificationException. ConcurrentHashMap is thread-safe using segment-level locking (Java 7) or CAS operations (Java 8+). Use ConcurrentHashMap in multi-threaded code.

**Q: What is the difference between checked and unchecked exceptions?**
A: Checked exceptions extend Exception and must be declared (throws) or caught. Unchecked exceptions extend RuntimeException and don't need to be declared. Checked exceptions model recoverable conditions (file not found, network error). Unchecked exceptions model programming errors or unrecoverable conditions.

**Q: What is a lambda expression?**
A: An anonymous function — a concise way to implement a functional interface (one abstract method). `(params) -> expression` or `(params) -> { statements }`. Enables functional programming style in Java.

**Q: What is Optional and why use it?**
A: Optional<T> is a container that may or may not hold a value — it's an explicit way to represent nullability. Makes the API honest about what can be null and forces callers to handle the empty case, reducing NullPointerExceptions.

**Q: Explain HashMap internals.**
A: HashMap uses an array of buckets. hashCode() determines the bucket index. Multiple keys in the same bucket form a linked list (Java 7) or red-black tree (Java 8, when bucket size > 8). Load factor (default 0.75) triggers resize when 75% of buckets are occupied.

**Q: What is the difference between interface and abstract class in Java 8+?**
A: In Java 8+, interfaces can have default and static methods, so the line is blurred. Key differences: abstract classes can have state (instance fields), constructors, private methods. A class can implement multiple interfaces but extend only one abstract class. Use interface for capabilities/contracts, abstract class for shared code among related types.

**Q: What is volatile and when do you use it?**
A: volatile guarantees that reads/writes to a variable are visible to all threads immediately — it prevents CPU cache optimization that can cause threads to see stale values. Use for flags shared across threads where you don't need compound atomicity (use AtomicInteger for that).

**Q: What is the difference between StringBuilder and StringBuffer?**
A: Both are mutable string builders. StringBuffer is thread-safe (synchronized). StringBuilder is not thread-safe but faster. Use StringBuilder in single-threaded code (which is almost always — the synchronization overhead of StringBuffer is rarely needed).
