# Requirements Analysis - Online Quiz Management System

## ✅ IMPLEMENTED Requirements

### 1. At least 5 entities (4 pts) ✅
**Status: IMPLEMENTED**

The system has **12+ entities**:
- User
- Quiz
- Question
- QuizAttempt
- Location
- Report
- Option
- Answer
- UserAnswer
- Notification
- AuditLog
- OtpToken

**Location:** `src/main/java/auca/ac/rw/Online/quiz/management/model/`

---

### 2. At least 5 pages, excluding login, forget password, and sign-up (5 pts) ✅
**Status: IMPLEMENTED**

The system has **14 pages** (excluding login, reset password, verify OTP):
- Dashboard
- Users
- Quizzes
- Questions
- Attempts
- Reports
- Profile
- Settings
- Locations
- TakeQuiz
- QuizBuilder
- QuizPreview
- QuizResults
- StudentDashboard

**Location:** `frontend/src/pages/`

---

### 3. Dashboard with business information summary (4 pts) ✅
**Status: IMPLEMENTED**

The dashboard includes:
- Statistics cards (Active Quizzes, Students, Instructors, Total Users, Pass Rate, Avg. Score, Students Who Took Quizzes)
- Engagement trend chart (Last 7 days)
- Active quizzes list (for instructors)
- All quizzes summary (for instructors)
- Real-time updates (every 5 seconds)

**Location:** `frontend/src/pages/Dashboard.js`

---

### 4. Pagination when displaying table data (3 pts) ✅
**Status: IMPLEMENTED**

Pagination is implemented in:
- DataTable component with Previous/Next buttons
- Page information display
- Used in: Users, Quizzes, Questions, Attempts pages

**Location:** `frontend/src/components/ui/DataTable.js`

---

### 5. Reset password using email (4 pts) ✅
**Status: IMPLEMENTED**

Features:
- Email-based password reset
- OTP sent to email
- 6-digit OTP verification
- New password update
- Success/error toast notifications
- Auto-redirect to login after success

**Location:** 
- Frontend: `frontend/src/pages/ResetPassword.js`
- Backend: `src/main/java/auca/ac/rw/Online/quiz/management/controller/AuthController.java`

---

### 7. Global search (6 pts) ✅
**Status: IMPLEMENTED**

Features:
- Global search component in topbar
- Searches across: Users, Quizzes, Questions, Attempts
- Keyboard shortcut (Cmd/Ctrl + K)
- Debounced search (300ms)
- Highlighted search results
- Navigate to results on click

**Location:**
- Frontend: `frontend/src/components/GlobalSearch.js`
- Backend: `src/main/java/auca/ac/rw/Online/quiz/management/controller/SearchController.java`

---

### 8. Search in table list by any column values (4 pts) ⚠️
**Status: PARTIALLY IMPLEMENTED**

Current Implementation:
- Search input exists in DataTable component
- Search is passed to parent component via `onSearch` callback
- Backend search implemented for specific fields (e.g., username, email for Users; title for Quizzes)

**Issue:**
- Search may not dynamically search ALL columns in the table
- Each page implements search differently (some search specific fields only)

**Location:** `frontend/src/components/ui/DataTable.js`

**Recommendation:** Enhance to search all visible column values dynamically.

---

### 9. Role-based authentication (5 pts) ✅
**Status: IMPLEMENTED**

Roles implemented:
- ADMIN: Full system access
- INSTRUCTOR: Quiz creation and management
- STUDENT: Quiz taking and viewing results

Features:
- Protected routes based on roles
- Role-based dashboard views
- Role-based data filtering
- JWT token with role claims

**Location:**
- Frontend: `frontend/src/components/ProtectedRoute.js`
- Backend: `src/main/java/auca/ac/rw/Online/quiz/management/model/EUserRole.java`

---

## ❌ NOT IMPLEMENTED Requirements

### 6. Two-factor authentication for login using email (5 pts) ❌
**Status: NOT FULLY IMPLEMENTED**

**Current State:**
- OTP system exists and is functional
- OTP can be sent via email
- OTP verification endpoint exists
- **BUT: Login endpoint BYPASSES OTP and issues token directly**

**Evidence:**
```java
// AuthController.java line 43-44
log.info("Login attempt for: {} - BYPASSING OTP", request.usernameOrEmail());
String token = authService.authenticateAndIssueToken(request.usernameOrEmail(), request.password());
```

**What's Missing:**
- Login should ALWAYS require OTP verification
- Current flow: Login → Direct token (bypasses OTP)
- Required flow: Login → Send OTP → Verify OTP → Issue token

**Location:** `src/main/java/auca/ac/rw/Online/quiz/management/controller/AuthController.java`

**Fix Required:**
- Change login endpoint to call `initiateLoginOtp()` instead of `authenticateAndIssueToken()`
- Ensure all logins go through OTP verification flow

---

## Summary

| Requirement | Points | Status | Notes |
|------------|--------|--------|-------|
| 5+ Entities | 4 | ✅ | 12+ entities implemented |
| 5+ Pages | 5 | ✅ | 14 pages implemented |
| Dashboard Summary | 4 | ✅ | Full dashboard with stats |
| Pagination | 3 | ✅ | Implemented in DataTable |
| Reset Password | 4 | ✅ | Email-based reset working |
| **2FA Login** | **5** | **❌** | **OTP bypassed in login** |
| Global Search | 6 | ✅ | Fully implemented |
| Table Search | 4 | ⚠️ | Partially implemented |
| Role-based Auth | 5 | ✅ | 3 roles implemented |

**Total Points: 40**
**Implemented: 35 points**
**Missing: 5 points (2FA Login)**

---

## Code Reusability ✅

The system demonstrates good code reusability:
- ✅ **One Button component**: `frontend/src/components/ui/Button.js`
- ✅ **One Sidebar component**: `frontend/src/components/layout/Sidebar.js`
- ✅ **One Topbar component**: `frontend/src/components/layout/Topbar.js`
- ✅ **One Footer component**: `frontend/src/components/layout/Footer.js`
- ✅ **One DataTable component**: `frontend/src/components/ui/DataTable.js`
- ✅ **Reusable modals**: UserFormModal, QuizFormModal, QuestionFormModal

---

## Priority Fix Required

**HIGH PRIORITY:** Enable Two-Factor Authentication for Login
- This is worth 5 points and is currently not working
- The infrastructure exists but is bypassed
- Fix: Modify `AuthController.login()` to use OTP flow

