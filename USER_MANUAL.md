# 👥 User Manual - Online Quiz Management System

## 🎯 Getting Started

### 1. System Access
- **URL:** http://localhost:3000
- **Default Admin:** friendeno123@gmail.com / Admin123@

### 2. User Roles
- **ADMIN:** Full system access, user management
- **INSTRUCTOR:** Create/manage quizzes and questions
- **STUDENT:** Take quizzes, view results

## 🔐 Login Process

### Step 1: Enter Credentials
1. Go to login page
2. Enter username/email and password
3. Click "Login"

### Step 2: Verify OTP
1. Check your email for 6-digit code
2. Enter OTP code
3. Click "Verify"
4. You'll receive JWT token for session

## 👨‍🎓 Student Guide

### Taking a Quiz
1. **Dashboard:** View available quizzes
2. **Start Quiz:** Click "Take Quiz" button
3. **Answer Questions:** Select answers for each question
4. **Timer:** Monitor remaining time (top right)
5. **Submit:** Click "Submit Quiz" or auto-submit when time expires

### Viewing Results
1. **My Attempts:** See all quiz attempts
2. **View Results:** Click to see detailed results
3. **Score Analysis:** Review correct/incorrect answers
4. **Performance:** Track improvement over time

## 👨‍🏫 Instructor Guide

### Creating a Quiz
1. **Quizzes Page:** Click "Create Quiz"
2. **Fill Details:**
   - Title and description
   - Duration in minutes
   - Status (Draft/Published)
3. **Save Quiz**

### Building Quiz Content
1. **Quiz Builder:** Click "Build" on quiz
2. **Add Questions:** Click "Add Question"
3. **Question Types:**
   - Multiple Choice (with options)
   - True/False
4. **Set Points:** Assign points per question
5. **Preview:** Review before publishing

### Managing Questions
1. **Questions Page:** View all questions
2. **Edit/Delete:** Modify existing questions
3. **Bulk Operations:** Import/export questions

## 👨‍💼 Admin Guide

### User Management
1. **Users Page:** View all system users
2. **Create User:** Add new students/instructors
3. **Edit Users:** Modify user details and roles
4. **Location Assignment:** Set user geographic location

### System Analytics
1. **Dashboard:** View system statistics
2. **Reports:** Generate performance reports
3. **Location Analytics:** Filter by geographic regions

### System Configuration
1. **Settings:** Configure system preferences
2. **Notifications:** Manage system notifications
3. **Audit Logs:** Monitor system activities

## 🌍 Location System

### Rwanda Administrative Structure
- **Province → District → Sector → Cell → Village**
- Users assigned to specific locations
- Analytics available by geographic region

## 🔔 Notifications

### Types of Notifications
- **Quiz Published:** New quiz available
- **Quiz Graded:** Results ready
- **System Updates:** Important announcements

### Managing Notifications
1. **View:** Check notification panel
2. **Mark Read:** Click to mark as read
3. **Settings:** Configure notification preferences

## 🔍 Search Features

### Global Search
- **Top Bar:** Search across users and quizzes
- **Results:** Shows matching items
- **Quick Access:** Click to navigate

### Table Search
- **Filter Tables:** Search within data tables
- **Column Search:** Find specific entries
- **Real-time:** Results update as you type

## 📊 Analytics & Reports

### Student Analytics
- **Performance Tracking:** Score trends
- **Quiz History:** All attempts
- **Improvement Areas:** Weak topics

### Instructor Analytics
- **Quiz Performance:** Class averages
- **Question Analysis:** Difficulty assessment
- **Student Progress:** Individual tracking

### Admin Analytics
- **System Usage:** User activity
- **Geographic Reports:** Location-based data
- **Performance Metrics:** Overall statistics

## 🚨 Troubleshooting

### Common Issues

**Can't Login:**
- Check email/password
- Verify OTP in email
- Contact admin if locked out

**Quiz Won't Load:**
- Check internet connection
- Refresh browser
- Clear browser cache

**Email Not Received:**
- Check spam folder
- Verify email address
- Contact admin for manual reset

**Timer Issues:**
- Don't refresh page during quiz
- Submit before time expires
- Contact instructor if technical issues

### Getting Help
- **Contact Admin:** Use system messaging
- **Technical Support:** Check documentation
- **User Guide:** Reference this manual

## 📱 Browser Compatibility
- **Recommended:** Chrome, Firefox, Safari, Edge
- **Mobile:** Responsive design works on tablets/phones
- **JavaScript:** Must be enabled

## 🔒 Security Best Practices
- **Strong Passwords:** Use complex passwords
- **Secure Email:** Keep email account secure
- **Logout:** Always logout when finished
- **Report Issues:** Report suspicious activity