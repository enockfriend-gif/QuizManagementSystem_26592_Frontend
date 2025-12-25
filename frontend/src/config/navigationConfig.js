export const ROLES = {
    ADMIN: 'ADMIN',
    INSTRUCTOR: 'INSTRUCTOR',
    STUDENT: 'STUDENT',
};

export const navigationConfig = [
    {
        path: '/',
        label: 'Dashboard',
        roles: [ROLES.ADMIN, ROLES.INSTRUCTOR, ROLES.STUDENT],
        inSidebar: true,
    },
    {
        path: '/users',
        label: 'Users',
        roles: [ROLES.ADMIN],
        inSidebar: true,
    },
    {
        path: '/quizzes',
        label: 'Quizzes',
        roles: [ROLES.INSTRUCTOR, ROLES.STUDENT],
        inSidebar: true,
    },
    {
        path: '/questions',
        label: 'Questions',
        roles: [ROLES.INSTRUCTOR],
        inSidebar: true,
    },
    {
        path: '/attempts',
        label: 'Results',
        roles: [ROLES.INSTRUCTOR, ROLES.STUDENT],
        inSidebar: true,
    },
    {
        path: '/reports',
        label: 'Reports',
        roles: [ROLES.ADMIN, ROLES.INSTRUCTOR],
        inSidebar: true,
    },
    {
        path: '/profile',
        label: 'Profile',
        roles: [ROLES.ADMIN, ROLES.INSTRUCTOR, ROLES.STUDENT],
        inSidebar: true,
    },
    {
        path: '/settings',
        label: 'Settings',
        roles: [ROLES.ADMIN],
        inSidebar: true,
    },
    {
        path: '/locations',
        label: 'Locations',
        roles: [ROLES.ADMIN],
        inSidebar: true,
    },
    // Hidden routes (not in sidebar but role-restricted)
    {
        path: '/take-quiz/:id',
        roles: [ROLES.STUDENT],
        inSidebar: false,
    },
    {
        path: '/quiz-builder/:id',
        roles: [ROLES.INSTRUCTOR],
        inSidebar: false,
    },
    {
        path: '/quiz-preview/:id',
        roles: [ROLES.INSTRUCTOR],
        inSidebar: false,
    },
    {
        path: '/quiz-results/:attemptId',
        roles: [ROLES.ADMIN, ROLES.INSTRUCTOR, ROLES.STUDENT],
        inSidebar: false,
    },
];
