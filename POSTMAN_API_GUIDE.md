# Postman API Testing Guide

## Base URL
```
http://localhost:8080
```

---

## 📋 USERS API (`/api/users`)

### 1. List All Users
**Method:** `GET`  
**URL:** `http://localhost:8080/api/users`  
**Headers:** None required  
**Body:** None

---

### 2. Get User by ID
**Method:** `GET`  
**URL:** `http://localhost:8080/api/users/{id}`  
**Example:** `http://localhost:8080/api/users/1`  
**Headers:** None required  
**Body:** None

---

### 3. Create User
**Method:** `POST`  
**URL:** `http://localhost:8080/api/users/createUser`  
**Headers:** 
```
Content-Type: application/json
```
**Body:** (raw JSON)
```json
{
  "username": "john_doe",
  "password": "password123",
  "email": "john.doe@example.com",
  "role": "STUDENT"
}
```

**Role Options:** `STUDENT`, `TEACHER`, `ADMIN`

---

### 4. Update User
**Method:** `PUT`  
**URL:** `http://localhost:8080/api/users/{id}`  
**Example:** `http://localhost:8080/api/users/1`  
**Headers:** 
```
Content-Type: application/json
```
**Body:** (raw JSON)
```json
{
  "username": "john_doe_updated",
  "password": "newpassword123",
  "email": "john.updated@example.com",
  "role": "TEACHER",
  "location": {
    "id": 1
  }
}
```

---

### 5. Delete User
**Method:** `DELETE`  
**URL:** `http://localhost:8080/api/users/{id}`  
**Example:** `http://localhost:8080/api/users/1`  
**Headers:** None required  
**Body:** None

---

## 📝 QUIZZES API (`/api/quizzes`)

### 1. List All Quizzes
**Method:** `GET`  
**URL:** `http://localhost:8080/api/quizzes`  
**Headers:** None required  
**Body:** None

---

### 2. Get Quiz by ID
**Method:** `GET`  
**URL:** `http://localhost:8080/api/quizzes/{id}`  
**Example:** `http://localhost:8080/api/quizzes/1`  
**Headers:** None required  
**Body:** None

---

### 3. Create Quiz
**Method:** `POST`  
**URL:** `http://localhost:8080/api/quizzes`  
**Headers:** 
```
Content-Type: application/json
```
**Body:** (raw JSON)
```json
{
  "title": "Java Basics Quiz",
  "status": "DRAFT",
  "startTime": "2024-01-15T10:00:00+00:00",
  "endTime": "2024-01-15T12:00:00+00:00",
  "durationMinutes": 60,
  "createdBy": {
    "id": 1
  }
}
```

**Status Options:** `DRAFT`, `PUBLISHED`, `ACTIVE`, `COMPLETED`

---

### 4. Update Quiz
**Method:** `PUT`  
**URL:** `http://localhost:8080/api/quizzes/{id}`  
**Example:** `http://localhost:8080/api/quizzes/1`  
**Headers:** 
```
Content-Type: application/json
```
**Body:** (raw JSON)
```json
{
  "title": "Advanced Java Quiz",
  "status": "PUBLISHED",
  "startTime": "2024-01-20T10:00:00+00:00",
  "endTime": "2024-01-20T12:00:00+00:00",
  "durationMinutes": 90,
  "createdBy": {
    "id": 1
  }
}
```

---

### 5. Delete Quiz
**Method:** `DELETE`  
**URL:** `http://localhost:8080/api/quizzes/{id}`  
**Example:** `http://localhost:8080/api/quizzes/1`  
**Headers:** None required  
**Body:** None

---

## ❓ QUESTIONS API (`/api/questions`)

### 1. List All Questions
**Method:** `GET`  
**URL:** `http://localhost:8080/api/questions`  
**Headers:** None required  
**Body:** None

---

### 2. Get Question by ID
**Method:** `GET`  
**URL:** `http://localhost:8080/api/questions/{id}`  
**Example:** `http://localhost:8080/api/questions/1`  
**Headers:** None required  
**Body:** None

---

### 3. Create Question
**Method:** `POST`  
**URL:** `http://localhost:8080/api/questions`  
**Headers:** 
```
Content-Type: application/json
```
**Body:** (raw JSON)
```json
{
  "text": "What is the main purpose of JVM in Java?",
  "type": "SINGLE_CHOICE",
  "category": "Java Basics",
  "quiz": {
    "id": 1
  },
  "options": [
    {
      "text": "Option A",
      "isCorrect": true
    },
    {
      "text": "Option B",
      "isCorrect": false
    }
  ]
}
```

**Type Options:** `SINGLE_CHOICE`, `MULTIPLE_CHOICE`, `TRUE_FALSE`, `TEXT`

---

### 4. Update Question
**Method:** `PUT`  
**URL:** `http://localhost:8080/api/questions/{id}`  
**Example:** `http://localhost:8080/api/questions/1`  
**Headers:** 
```
Content-Type: application/json
```
**Body:** (raw JSON)
```json
{
  "text": "What is the difference between JDK and JRE?",
  "type": "MULTIPLE_CHOICE",
  "category": "Java Fundamentals",
  "quiz": {
    "id": 1
  },
  "options": [
    {
      "text": "Updated Option A",
      "isCorrect": true
    }
  ]
}
```

---

### 5. Delete Question
**Method:** `DELETE`  
**URL:** `http://localhost:8080/api/questions/{id}`  
**Example:** `http://localhost:8080/api/questions/1`  
**Headers:** None required  
**Body:** None

---

## 📊 QUIZ ATTEMPTS API (`/api/attempts`)

### 1. List All Attempts
**Method:** `GET`  
**URL:** `http://localhost:8080/api/attempts`  
**Headers:** None required  
**Body:** None

---

### 2. Get Attempt by ID
**Method:** `GET`  
**URL:** `http://localhost:8080/api/attempts/{id}`  
**Example:** `http://localhost:8080/api/attempts/1`  
**Headers:** None required  
**Body:** None

---

### 3. Get Average Score for Quiz
**Method:** `GET`  
**URL:** `http://localhost:8080/api/attempts/quiz/{quizId}/average-score`  
**Example:** `http://localhost:8080/api/attempts/quiz/1/average-score`  
**Headers:** None required  
**Body:** None

---

### 4. Create Quiz Attempt
**Method:** `POST`  
**URL:** `http://localhost:8080/api/attempts`  
**Headers:** 
```
Content-Type: application/json
```
**Body:** (raw JSON)
```json
{
  "user": {
    "id": 1
  },
  "quiz": {
    "id": 1
  },
  "status": "IN_PROGRESS",
  "score": null,
  "startedAt": "2024-01-15T10:00:00+00:00",
  "submittedAt": null
}
```

**Status Options:** `PENDING`, `IN_PROGRESS`, `SUBMITTED`, `GRADED`

---

### 5. Update Quiz Attempt
**Method:** `PUT`  
**URL:** `http://localhost:8080/api/attempts/{id}`  
**Example:** `http://localhost:8080/api/attempts/1`  
**Headers:** 
```
Content-Type: application/json
```
**Body:** (raw JSON)
```json
{
  "user": {
    "id": 1
  },
  "quiz": {
    "id": 1
  },
  "status": "GRADED",
  "score": 85.5,
  "submittedAt": "2024-01-15T11:30:00+00:00"
}
```

---

### 6. Delete Quiz Attempt
**Method:** `DELETE`  
**URL:** `http://localhost:8080/api/attempts/{id}`  
**Example:** `http://localhost:8080/api/attempts/1`  
**Headers:** None required  
**Body:** None

---

## 👨‍💼 ADMIN API (`/api/admin`)

### 1. List All Users (Admin)
**Method:** `GET`  
**URL:** `http://localhost:8080/api/admin/users`  
**Headers:** None required  
**Body:** None

---

### 2. Update User Role
**Method:** `PATCH`  
**URL:** `http://localhost:8080/api/admin/users/{id}/role`  
**Example:** `http://localhost:8080/api/admin/users/1/role?role=TEACHER`  
**Headers:** None required  
**Query Parameters:**
- `role`: `STUDENT`, `TEACHER`, or `ADMIN`

---

### 3. Delete User (Admin)
**Method:** `DELETE`  
**URL:** `http://localhost:8080/api/admin/users/{id}`  
**Example:** `http://localhost:8080/api/admin/users/1`  
**Headers:** None required  
**Body:** None

---

## 📈 REPORTS API (`/api/reports`)

### 1. Export Scores CSV
**Method:** `GET`  
**URL:** `http://localhost:8080/api/reports/scores/csv`  
**Headers:** None required  
**Body:** None  
**Note:** This will download a CSV file named `quiz-scores.csv`

---

## 📝 Postman Collection JSON

You can import this JSON into Postman:

```json
{
  "info": {
    "name": "Online Quiz Management System",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Users",
      "item": [
        {
          "name": "List All Users",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/users",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "users"]
            }
          }
        },
        {
          "name": "Get User by ID",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/users/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "users", "1"]
            }
          }
        },
        {
          "name": "Create User",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"john_doe\",\n  \"password\": \"password123\",\n  \"email\": \"john.doe@example.com\",\n  \"role\": \"STUDENT\"\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/users/createUser",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "users", "createUser"]
            }
          }
        },
        {
          "name": "Update User",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"john_doe_updated\",\n  \"password\": \"newpassword123\",\n  \"email\": \"john.updated@example.com\",\n  \"role\": \"TEACHER\"\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/users/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "users", "1"]
            }
          }
        },
        {
          "name": "Delete User",
          "request": {
            "method": "DELETE",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/users/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "users", "1"]
            }
          }
        }
      ]
    },
    {
      "name": "Quizzes",
      "item": [
        {
          "name": "List All Quizzes",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/quizzes",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "quizzes"]
            }
          }
        },
        {
          "name": "Get Quiz by ID",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/quizzes/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "quizzes", "1"]
            }
          }
        },
        {
          "name": "Create Quiz",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"title\": \"Java Basics Quiz\",\n  \"status\": \"DRAFT\",\n  \"startTime\": \"2024-01-15T10:00:00+00:00\",\n  \"endTime\": \"2024-01-15T12:00:00+00:00\",\n  \"durationMinutes\": 60,\n  \"createdBy\": {\n    \"id\": 1\n  }\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/quizzes",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "quizzes"]
            }
          }
        },
        {
          "name": "Update Quiz",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"title\": \"Advanced Java Quiz\",\n  \"status\": \"PUBLISHED\",\n  \"startTime\": \"2024-01-20T10:00:00+00:00\",\n  \"endTime\": \"2024-01-20T12:00:00+00:00\",\n  \"durationMinutes\": 90,\n  \"createdBy\": {\n    \"id\": 1\n  }\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/quizzes/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "quizzes", "1"]
            }
          }
        },
        {
          "name": "Delete Quiz",
          "request": {
            "method": "DELETE",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/quizzes/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "quizzes", "1"]
            }
          }
        }
      ]
    }
  ]
}
```

---

## 💡 Tips for Testing in Postman

1. **Set Environment Variable:**
   - Create an environment variable `base_url` = `http://localhost:8080`
   - Use `{{base_url}}/api/users` in your requests

2. **Date Format:**
   - Use ISO 8601 format: `2024-01-15T10:00:00+00:00` for dates

3. **Testing Flow:**
   - First create a User (POST)
   - Then create a Quiz with that user's ID
   - Create Questions for that Quiz
   - Create Quiz Attempts

4. **Common Response Codes:**
   - `200 OK` - Success
   - `201 Created` - Resource created
   - `204 No Content` - Successful deletion
   - `404 Not Found` - Resource not found


