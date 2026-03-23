SESSION-SERVICE API DOCUMENTATION
Base URL: http://localhost:8085/sessions

Content-Type: application/json

1. Book a New Session
Used by a learner to request a session with a mentor.

Method: POST

URL: /

Status Initialized to: REQUESTED

Request Body:

JSON
{
    "mentorId": 101,
    "learnerId": 501,
    "sessionDate": "2026-04-20T10:30:00",
    "status": "REQUESTED"
}
2. Accept a Session
Used by a mentor to accept a pending request.

Method: PUT

URL: /{id}/accept

Example: http://localhost:8085/sessions/1/accept

Constraint: Session must currently be in REQUESTED status.

Body: None

3. Reject a Session
Used by a mentor to decline a pending request.

Method: PUT

URL: /{id}/reject

Example: http://localhost:8085/sessions/1/reject

Constraint: Session must currently be in REQUESTED status.

Body: None

4. Cancel a Session
Used by either party to cancel a session.

Method: PUT

URL: /{id}/cancel

Example: http://localhost:8085/sessions/1/cancel

Constraint: Cannot cancel if status is already COMPLETED.

Body: None

5. Get Sessions by User ID
Retrieves all sessions where the user is either the Mentor or the Learner.

Method: GET

URL: /user/{userId}

Example: http://localhost:8085/sessions/user/501

Response: Returns a list of Session objects.

DATA MODEL REFERENCE (SessionStatus Enum)
When testing or manually updating via DB, use these exact strings:

REQUESTED

ACCEPTED

REJECTED

CANCELLED

COMPLETED

Postman Quick Tips:
Date Format: Use ISO-8601 format: YYYY-MM-DDTHH:MM:SS.

Error Handling: If you receive a 500 Internal Server Error, check the console; it likely means the business logic (e.g., trying to accept a cancelled session) threw a RuntimeException.