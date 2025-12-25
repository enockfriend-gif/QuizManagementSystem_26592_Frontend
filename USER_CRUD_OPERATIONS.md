# User CRUD Operations - Complete Implementation

## ✅ **Your System HAS Full User CRUD Operations!**

### **🔍 READ Operations:**

#### **1. Get All Users**
- **Frontend**: `Users.js` component with DataTable
- **Backend**: `GET /api/users` - Returns all users
- **Features**: Pagination, search by username

#### **2. Get Users with Pagination**
- **Backend**: `GET /api/users/page?page=0&size=10&q=search`
- **Frontend**: Integrated in Users page with search functionality

#### **3. Get Single User**
- **Backend**: `GET /api/users/{id}`
- **Usage**: For editing user details

### **➕ CREATE Operations:**

#### **Create New User**
- **Frontend**: `UserFormModal.js` component
- **Backend**: `POST /api/users/createUser`
- **Features**:
  - Username validation (required, unique)
  - Email validation (RFC 5322 compliant, unique)
  - Password encryption (BCrypt)
  - Role assignment (STUDENT, INSTRUCTOR, ADMIN)
  - Duplicate checking

**Example Request:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "role": "STUDENT"
}
```

### **✏️ UPDATE Operations:**

#### **Update Existing User**
- **Frontend**: Edit button in Users table opens UserFormModal
- **Backend**: `PUT /api/users/{id}`
- **Features**:
  - Update username, email, role
  - Optional password update (leave empty to keep current)
  - Email validation on update
  - Maintains existing data if not provided

**Example Request:**
```json
{
  "username": "john_updated",
  "email": "john.new@example.com",
  "role": "INSTRUCTOR",
  "password": "NewPassword123"
}
```

### **🗑️ DELETE Operations:**

#### **Delete User**
- **Frontend**: Delete button with confirmation dialog
- **Backend**: `DELETE /api/users/{id}`
- **Features**:
  - Confirmation prompt before deletion
  - Proper error handling
  - Immediate UI update after deletion

## 🎯 **How to Use User CRUD:**

### **1. Access User Management**
- Login as **ADMIN** (only admins can manage users)
- Navigate to **Users** page from sidebar
- View paginated list of all users

### **2. Create New User**
- Click **"+ Create User"** button
- Fill in the form:
  - Username (required, unique)
  - Email (required, valid format, unique)
  - Password (required for new users)
  - Role (STUDENT, INSTRUCTOR, ADMIN)
- Click **"Create"**

### **3. Edit Existing User**
- Click **"Edit"** button next to any user
- Modify fields as needed
- Password field is optional (leave empty to keep current)
- Click **"Update"**

### **4. Delete User**
- Click **"Delete"** button next to any user
- Confirm deletion in the popup
- User is permanently removed

### **5. Search Users**
- Use search box in the table
- Searches by username
- Real-time filtering

## 🔒 **Security Features:**

### **Authentication & Authorization**
- Only **ADMIN** users can access user management
- JWT token validation on all requests
- Protected routes prevent unauthorized access

### **Data Validation**
- **Email**: RFC 5322 compliant validation
- **Username**: Required, unique, case-insensitive
- **Password**: BCrypt encryption, optional on updates
- **Duplicates**: Prevents duplicate usernames/emails

### **Error Handling**
- User-friendly error messages
- Validation feedback in real-time
- Proper HTTP status codes

## 📊 **Database Operations:**

### **Tables Involved**
- **users** table with columns:
  - id (Primary Key)
  - username (Unique)
  - email (Unique)
  - password (Encrypted)
  - role (ENUM)
  - location_id (Foreign Key)

### **Relationships**
- User → Location (Many-to-One)
- User → Quiz (One-to-Many as creator)
- User → QuizAttempt (One-to-Many)

## ✅ **Test Your User CRUD:**

1. **Login as Admin**: Use `friendeno123@gmail.com` / `Admin123@`
2. **Go to Users Page**: Click "Users" in sidebar
3. **Create User**: Click "+ Create User", fill form, submit
4. **Edit User**: Click "Edit" on any user, modify, save
5. **Delete User**: Click "Delete", confirm deletion
6. **Search Users**: Type in search box to filter

## 🎯 **Your User CRUD is 100% Complete!**

- ✅ **CREATE**: Add new users with validation
- ✅ **READ**: View all users with pagination & search
- ✅ **UPDATE**: Edit user details including optional password
- ✅ **DELETE**: Remove users with confirmation
- ✅ **SECURITY**: Admin-only access with proper validation
- ✅ **UI/UX**: Professional modal forms and data tables

**Your system has enterprise-grade user management functionality!**