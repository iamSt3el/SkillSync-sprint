# Spring Data JPA

## What is Spring Data JPA?

Spring Data JPA is a layer on top of JPA (Java Persistence API) that eliminates boilerplate data access code. JPA itself is a specification (an interface); **Hibernate** is the most popular implementation. Spring Data JPA makes working with Hibernate much easier.

**Stack:**
```
Your Code
    ↓
Spring Data JPA (repositories, query generation)
    ↓
JPA (specification — EntityManager API)
    ↓
Hibernate (implementation — does the actual SQL)
    ↓
JDBC
    ↓
MySQL
```

---

## Entity

An entity is a Java class mapped to a DB table:

```java
@Entity
@Table(name = "mentors")
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 1000)
    private String bio;

    @Enumerated(EnumType.STRING)
    private MentorStatus status;  // PENDING, APPROVED, REJECTED

    private double rating;

    @ElementCollection
    @CollectionTable(name = "mentor_skills")
    private List<String> skills;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Key Annotations

| Annotation | Purpose |
|---|---|
| `@Entity` | Marks class as a JPA entity |
| `@Table(name="...")` | Specify table name (optional, defaults to class name) |
| `@Id` | Primary key |
| `@GeneratedValue` | Auto-generate PK (IDENTITY = DB auto-increment) |
| `@Column` | Map field to column, set constraints |
| `@Enumerated(EnumType.STRING)` | Store enum as string (not integer ordinal) |
| `@Transient` | Field not persisted to DB |
| `@CreationTimestamp` | Set once on insert (Hibernate) |
| `@UpdateTimestamp` | Updated on every update (Hibernate) |

---

## Repository

Spring Data JPA generates repository implementations automatically:

```java
// Basic CRUD — Spring generates the implementation
public interface MentorRepository extends JpaRepository<Mentor, Long> {
    // JpaRepository gives you: save, findById, findAll, delete, count, existsById, etc.
}
```

**Repository hierarchy:**
```
Repository (marker interface)
    ↓
CrudRepository (save, findById, findAll, delete)
    ↓
PagingAndSortingRepository (findAll(Pageable), findAll(Sort))
    ↓
JpaRepository (flush, saveAndFlush, findAll(Example), deleteInBatch)
```

Use `JpaRepository<Entity, IdType>` for most cases.

---

## Derived Query Methods

Spring parses method names and generates SQL:

```java
public interface MentorRepository extends JpaRepository<Mentor, Long> {

    // SELECT * FROM mentors WHERE user_id = ?
    Optional<Mentor> findByUserId(Long userId);

    // SELECT * FROM mentors WHERE status = ?
    List<Mentor> findByStatus(MentorStatus status);

    // SELECT * FROM mentors WHERE status = ? AND rating >= ?
    List<Mentor> findByStatusAndRatingGreaterThanEqual(MentorStatus status, double minRating);

    // SELECT * FROM mentors WHERE status = ? ORDER BY rating DESC
    List<Mentor> findByStatusOrderByRatingDesc(MentorStatus status);

    // SELECT COUNT(*) FROM mentors WHERE status = ?
    long countByStatus(MentorStatus status);

    // SELECT * FROM mentors WHERE user_id = ? (exists check)
    boolean existsByUserId(Long userId);

    // DELETE FROM mentors WHERE status = ?
    void deleteByStatus(MentorStatus status);
}
```

**Keyword reference:**
- `findBy`, `readBy`, `queryBy` — SELECT
- `And`, `Or` — combine conditions
- `GreaterThan`, `LessThan`, `Between`, `Like`, `Containing`, `StartingWith`
- `OrderBy...Asc/Desc`
- `Top3`, `First` — limit results

---

## @Query — Custom JPQL

For complex queries that derived methods can't express:

```java
// JPQL (uses entity/field names, not table/column names)
@Query("SELECT m FROM Mentor m WHERE m.status = 'APPROVED' AND m.rating >= :minRating")
List<Mentor> findApprovedMentorsWithMinRating(@Param("minRating") double minRating);

// Native SQL
@Query(value = "SELECT * FROM mentors WHERE status = 'APPROVED' LIMIT :limit",
       nativeQuery = true)
List<Mentor> findTopApprovedMentors(@Param("limit") int limit);

// Update/delete query
@Modifying
@Transactional
@Query("UPDATE Mentor m SET m.rating = :rating WHERE m.id = :id")
int updateRating(@Param("id") Long id, @Param("rating") double rating);
```

---

## Relationships

### One-to-Many / Many-to-One

```java
// One Session has one Mentor (Many Sessions → One Mentor)
@Entity
public class Session {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id")
    private Mentor mentor;
}

// One Mentor has many Sessions
@Entity
public class Mentor {
    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL)
    private List<Session> sessions;
}
```

### Many-to-Many

```java
@Entity
public class Mentor {
    @ManyToMany
    @JoinTable(
        name = "mentor_skills",
        joinColumns = @JoinColumn(name = "mentor_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> skills;
}
```

---

## Fetch Types

| Type | Behavior |
|---|---|
| `EAGER` | Load related entities immediately with the parent |
| `LAZY` | Load related entities only when accessed |

**Default behavior:**
- `@OneToMany`, `@ManyToMany` → LAZY (default)
- `@ManyToOne`, `@OneToOne` → EAGER (default)

**Always prefer LAZY** and let JPA fetch when needed. EAGER can cause N+1 problems and slow down queries.

---

## N+1 Problem

Happens when fetching a list of entities triggers additional queries for each entity's relationships:

```java
// 1 query to get all mentors
List<Mentor> mentors = mentorRepository.findAll();

// N queries — one per mentor to get their sessions
for (Mentor m : mentors) {
    System.out.println(m.getSessions().size());  // each triggers a SELECT
}
```

**Solution — JOIN FETCH in JPQL:**
```java
@Query("SELECT m FROM Mentor m JOIN FETCH m.sessions")
List<Mentor> findAllWithSessions();
```

Or use **@EntityGraph**:
```java
@EntityGraph(attributePaths = {"sessions"})
List<Mentor> findAll();
```

---

## @Transactional

```java
@Service
public class MentorService {

    @Transactional  // starts a transaction, commits on success, rolls back on exception
    public MentorDTO approveMentor(Long mentorId) {
        Mentor mentor = mentorRepository.findById(mentorId)
            .orElseThrow(() -> new MentorNotFoundException(mentorId));
        mentor.setStatus(MentorStatus.APPROVED);
        mentorRepository.save(mentor);
        // both saves succeed or both roll back
        userService.updateRole(mentor.getUserId(), "MENTOR");
        return mapper.toDTO(mentor);
    }
}
```

**Key properties:**
```java
@Transactional(
    propagation = Propagation.REQUIRED,    // join existing tx or create new (default)
    isolation = Isolation.READ_COMMITTED,  // isolation level
    readOnly = true,                       // optimization for read-only ops
    rollbackFor = Exception.class          // rollback on checked exceptions too
)
```

**Propagation types:**
| Propagation | Behavior |
|---|---|
| `REQUIRED` | Use existing tx; create new if none (default) |
| `REQUIRES_NEW` | Always create new tx; suspend existing |
| `NESTED` | Nested tx with savepoint |
| `SUPPORTS` | Use tx if exists; no tx if none |
| `NEVER` | Throw exception if tx exists |

---

## Pagination

```java
@GetMapping("/mentors")
public Page<MentorDTO> getMentors(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("rating").descending());
    return mentorRepository.findByStatus(MentorStatus.APPROVED, pageable)
                           .map(mapper::toDTO);
}
```

---

## Auditing

Spring Data JPA can auto-populate created/updated timestamps and user:

```java
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private String createdBy;
}

// Enable in main class:
@EnableJpaAuditing
@SpringBootApplication
public class Application { ... }
```

---

## JPA vs Hibernate vs Spring Data JPA

| | JPA | Hibernate | Spring Data JPA |
|---|---|---|---|
| What it is | Specification (interface) | Implementation of JPA | Layer on top of JPA |
| Who defines it | Jakarta EE | Red Hat | Spring |
| What it provides | EntityManager API | Actual SQL generation | Repository pattern, query methods |
| Analogy | JDBC interface | MySQL driver | Spring JDBC Template |

---

## Common Interview Questions

**Q: What is the difference between JPA, Hibernate, and Spring Data JPA?**
A: JPA is a specification (set of interfaces). Hibernate is the implementation of that specification — it generates and executes SQL. Spring Data JPA is a layer on top that provides the repository pattern and eliminates boilerplate — you define an interface, Spring generates the implementation using Hibernate under the hood.

**Q: What is the N+1 problem and how do you fix it?**
A: When fetching a list triggers N additional queries for each element's lazy-loaded relationship. Fix with JOIN FETCH in JPQL or @EntityGraph to load everything in one query.

**Q: Difference between EAGER and LAZY fetching?**
A: EAGER loads related entities immediately when the parent is loaded. LAZY loads them only when first accessed. Always prefer LAZY to avoid loading data you don't need.

**Q: What is @Transactional and where do you put it?**
A: Marks a method (or class) to run inside a DB transaction. Put it on service methods, not repository or controller. If the method throws an unchecked exception, the transaction rolls back automatically.

**Q: What is the difference between save() and saveAndFlush()?**
A: save() may batch the insert/update until the transaction commits or Hibernate decides to flush. saveAndFlush() immediately syncs to DB. Use saveAndFlush() when you need the DB state visible immediately (e.g., for the next query in the same transaction).

**Q: What does @GeneratedValue(strategy = GenerationType.IDENTITY) mean?**
A: Tells Hibernate to let the DB auto-generate the primary key (MySQL AUTO_INCREMENT). Other strategies: SEQUENCE (uses a DB sequence), TABLE (uses a separate PK table), AUTO (Hibernate chooses).

**Q: When should you use @Query vs derived query methods?**
A: Derived methods are fine for simple conditions. Use @Query for joins, aggregations, complex conditions, or native SQL. @Query is also clearer when the derived method name becomes very long.

**Q: What is the difference between Propagation.REQUIRED and REQUIRES_NEW?**
A: REQUIRED joins an existing transaction if one exists, or creates a new one. REQUIRES_NEW always creates a new transaction and suspends the existing one. Use REQUIRES_NEW when you need the inner operation to commit independently (e.g., audit logging that must succeed even if the outer transaction rolls back).
