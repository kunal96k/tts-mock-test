document.addEventListener('DOMContentLoaded', function() {
    console.log("Admin dashboard script loaded");
    /**
     * Admin Dashboard Section Navigation with URL Fragments
     * Add this to your admin-dashboard.js file
     */

    // ==================== SECTION NAVIGATION ====================

    /**
     * Show specific section and update URL
     */

    /**
     * Handle URL hash on page load and navigation
     */
//    function handleUrlHash() {
//        const hash = window.location.hash.substring(1); // Remove the '#'
//
//        console.log('Current URL hash:', hash);
//
//        if (hash) {
//            // Map of valid sections
//            const validSections = [
//                'dashboard',
//                'students',
//                'create-test',
//                'results',
//                'questions',
//                'subjects',
//                'reports'
//            ];
//
//            if (validSections.includes(hash)) {
//                // Small delay to ensure DOM is ready
//                setTimeout(() => {
//                    showSection(hash);
//                    console.log('Navigated to section from URL:', hash);
//                }, 100);
//            } else {
//                console.warn('Invalid section in URL hash:', hash);
//                // Default to dashboard
//                showSection('dashboard');
//            }
//        } else {
//            // No hash, show dashboard by default
//            showSection('dashboard');
//        }
//    }

    /**
     * Initialize on page load
     */
    document.addEventListener('DOMContentLoaded', function() {
        console.log('Admin Dashboard initialized');

//        // Handle initial URL hash
//        handleUrlHash();

        // Auto-show modal if there's an error
        const hasTestError = document.querySelector('.error-message') !== null;
        const hasSubjectError = document.querySelector('.error-message') !== null;
        const hasStudentError = document.querySelector('.error-message') !== null;
        const hasUploadError = document.querySelector('.error-message') !== null;

        if (hasTestError) {
            console.log('Test error detected, opening modal');
            setTimeout(() => {
                openModal('createTestModal');
            }, 300);
        }

        if (hasSubjectError) {
            console.log('Subject error detected, opening modal');
            setTimeout(() => {
                openModal('addSubjectModal');
            }, 300);
        }

        if (hasStudentError) {
            console.log('Student error detected, opening modal');
            setTimeout(() => {
                openModal('addStudentModal');
            }, 300);
        }

        if (hasUploadError) {
            console.log('Upload error detected, opening modal');
            setTimeout(() => {
                openModal('uploadQuestionsModal');
            }, 300);
        }

        // Auto-dismiss success/error messages after 5 seconds
        const messages = document.querySelectorAll('.success-message, .error-message');
        messages.forEach(message => {
            setTimeout(() => {
                message.style.opacity = '0';
                setTimeout(() => {
                    message.style.display = 'none';
                }, 300);
            }, 5000);
        });
    });

    // ==================== MODAL FUNCTIONS ====================

    /**
     * Open modal
     */
    function openModal(modalId) {
        console.log('Opening modal:', modalId);
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'block';
            document.body.style.overflow = 'hidden'; // Prevent background scroll
        } else {
            console.error('Modal not found:', modalId);
        }
    }

    /**
     * Close modal
     */
    function closeModal(modalId) {
        console.log('Closing modal:', modalId);
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'none';
            document.body.style.overflow = 'auto'; // Restore scroll
        }
    }

    /**
     * Close modal when clicking outside
     */
    window.onclick = function(event) {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
            document.body.style.overflow = 'auto';
        }
    }

    // ==================== DELETE FUNCTIONS ====================

    /**
     * Delete Test with confirmation
     */
    function deleteTest(testId) {
        if (!confirm('Are you sure you want to delete this test? This action cannot be undone.')) {
            return;
        }

        console.log('Deleting test:', testId);

        // Get CSRF token
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        // Send DELETE request
        fetch(`/admin/tests/${testId}/delete`, {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                console.log('Test deleted successfully');
                // Redirect will happen automatically to #create-test section
                window.location.href = '/admin/dashboard#create-test';
            } else {
                return response.text().then(text => {
                    throw new Error(text || 'Failed to delete test');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error deleting test: ' + error.message);
        });
    }

    /**
     * Delete Subject with confirmation
     */
    function deleteSubject(subjectId) {
        if (!confirm('Are you sure you want to delete this subject? This will also affect related tests.')) {
            return;
        }

        console.log('Deleting subject:', subjectId);

        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch(`/admin/subjects/${subjectId}/delete`, {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                console.log('Subject deleted successfully');
                window.location.href = '/admin/dashboard#subjects';
            } else {
                return response.text().then(text => {
                    throw new Error(text || 'Failed to delete subject');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error deleting subject: ' + error.message);
        });
    }

    /**
     * Delete Student with confirmation
     */
    function deleteStudent(studentId) {
        if (!confirm('Are you sure you want to delete this student? This action cannot be undone.')) {
            return;
        }

        console.log('Deleting student:', studentId);

        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch(`/admin/students/${studentId}/delete`, {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                console.log('Student deleted successfully');
                window.location.href = '/admin/dashboard#students';
            } else {
                return response.text().then(text => {
                    throw new Error(text || 'Failed to delete student');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error deleting student: ' + error.message);
        });
    }

    // ==================== SEARCH FUNCTIONS ====================

    /**
     * Live search for students
     */
    function searchStudentsLive() {
        const query = document.getElementById('studentSearch').value;
        console.log('Searching students:', query);

        if (query.length < 2 && query.length > 0) {
            return; // Wait for at least 2 characters
        }

        // Show loading indicator
        const loadingIndicator = document.getElementById('loadingIndicator');
        if (loadingIndicator) {
            loadingIndicator.style.display = 'block';
        }

        // Get CSRF token
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        // Fetch search results
        fetch(`/admin/students/search?query=${encodeURIComponent(query)}`, {
            method: 'GET',
            headers: {
                [csrfHeader]: csrfToken
            }
        })
        .then(response => response.json())
        .then(students => {
            console.log('Search results:', students);
            updateStudentTable(students);

            // Show/hide clear button
            const clearBtn = document.getElementById('clearBtn');
            if (clearBtn) {
                clearBtn.style.display = query.length > 0 ? 'inline-block' : 'none';
            }
        })
        .catch(error => {
            console.error('Search error:', error);
            alert('Error searching students: ' + error.message);
        })
        .finally(() => {
            // Hide loading indicator
            if (loadingIndicator) {
                loadingIndicator.style.display = 'none';
            }
        });
    }

    /**
     * Clear search
     */
    function clearSearch() {
        document.getElementById('studentSearch').value = '';
        document.getElementById('clearBtn').style.display = 'none';
        window.location.reload();
    }

    /**
     * Update student table with search results
     */
    function updateStudentTable(students) {
        const tableBody = document.getElementById('studentTableBody');
        const emptyState = document.getElementById('emptyState');

        if (students.length === 0) {
            tableBody.innerHTML = '';
            if (emptyState) {
                emptyState.style.display = 'block';
            }
            return;
        }

        if (emptyState) {
            emptyState.style.display = 'none';
        }

        tableBody.innerHTML = students.map(student => `
            <tr>
                <td>${student.studentId}</td>
                <td>${student.fullName}</td>
                <td>${student.email}</td>
                <td>${new Date(student.registeredDate).toLocaleDateString()}</td>
                <td>${student.testsTaken || 0}</td>
                <td>
                    <span class="status-badge ${student.enabled ? 'status-pass' : 'status-fail'}">
                        ${student.enabled ? 'Active' : 'Inactive'}
                    </span>
                </td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn btn-success" title="Download Report"
                                onclick="alert('Download report coming soon!')">
                            <i class="bx bx-download"></i>
                        </button>
                        <button class="action-btn btn-danger" title="Delete"
                                onclick="deleteStudent(${student.id})">
                            <i class="bx bx-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    // ==================== UTILITY FUNCTIONS ====================

    /**
     * Update file upload display
     */
    function updateFileName(input) {
        const fileName = input.files[0]?.name || 'No file selected';
        const fileUploadText = document.getElementById('fileUploadText');
        if (fileUploadText) {
            fileUploadText.textContent = fileName;
        }
        console.log('File selected:', fileName);
    }

    /**
     * Toggle profile dropdown
     */
    function toggleProfileDropdown() {
        const dropdown = document.getElementById('profileDropdown');
        if (dropdown) {
            dropdown.style.display = dropdown.style.display === 'block' ? 'none' : 'block';
        }
    }

    // Close dropdown when clicking outside
    document.addEventListener('click', function(event) {
        const profileDetails = document.querySelector('.profile-details');
        const dropdown = document.getElementById('profileDropdown');

        if (dropdown && !profileDetails.contains(event.target)) {
            dropdown.style.display = 'none';
        }
    });


    // Sidebar toggle
    const sidebar = document.querySelector(".sidebar");
    const sidebarBtn = document.querySelector(".sidebarBtn");
    if (sidebarBtn) {
        sidebarBtn.addEventListener('click', function() {
            sidebar.classList.toggle("active");
            if (sidebar.classList.contains("active")) {
                sidebarBtn.classList.replace("bx-menu", "bx-menu-alt-right");
            } else {
                sidebarBtn.classList.replace("bx-menu-alt-right", "bx-menu");
            }
        });
    }

    // Profile dropdown
    window.toggleProfileDropdown = function() {
        const dropdown = document.getElementById('profileDropdown');
        if (dropdown) dropdown.classList.toggle('show');
    };

    // Section navigation -
    // Replace the showSection function with this:
    window.showSection = function(sectionName) {
        console.log('Navigating to section:', sectionName);

        // Hide all sections
        document.querySelectorAll('.section-content').forEach(s => {
            s.style.display = 'none';
        });

        // Remove active class from all nav links
        document.querySelectorAll('.nav-links li a').forEach(a => {
            a.classList.remove('active');
        });

        // Show the selected section
        const section = document.getElementById(sectionName + '-section');
        if (section) {
            section.style.display = 'block';
        }

        // Add active class to the clicked link
        document.querySelectorAll('.nav-links li a').forEach(link => {
            const onclick = link.getAttribute('onclick');
            if (onclick && onclick.includes(`'${sectionName}'`)) {
                link.classList.add('active');
            }
        });

        // Update URL WITHOUT triggering hashchange
        history.replaceState(null, null, '#' + sectionName);
    };


    // Modal functions
    window.openModal = function(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'block';
            // Add show class if you have CSS for it
            modal.classList.add('show');
        }
    };

    window.closeModal = function(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'none';
            modal.classList.remove('show');
        }
    };

    // Global click for dropdowns and modals
    window.addEventListener('click', function(event) {
        // Close dropdown if clicked outside
        if (!event.target.matches('.profile-details') &&
            !event.target.closest('.profile-details')) {
            const dropdown = document.getElementById("profileDropdown");
            if (dropdown && dropdown.classList.contains('show')) {
                dropdown.classList.remove('show');
            }
        }

        // Close modal if clicked on background
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
            event.target.classList.remove('show');
        }
    });

    // Close modal when clicking the X button
    document.querySelectorAll('.modal .close').forEach(closeBtn => {
        closeBtn.addEventListener('click', function() {
            const modal = this.closest('.modal');
            if (modal) {
                modal.style.display = 'none';
                modal.classList.remove('show');
            }
        });
    });

    console.log("Admin dashboard script initialized");
});


// End of admin-dashboard.js

// Get CSRF token from meta tags
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // Store original students data
    let allStudentsData = [];
    let searchTimeout;

    // Delete Student Function - Temporarily disabled
    function deleteStudent(studentId) {
        alert('Delete functionality will be implemented soon!\nStudent ID: ' + studentId);
    }

    // Delete Question Bank Function - Temporarily disabled
    function deleteQuestionBank(bankId) {
        alert('Delete functionality will be implemented soon!\nBank ID: ' + bankId);
    }

    // Delete Subject Function - Temporarily disabled
    function deleteSubject(subjectId) {
        alert('Delete functionality will be implemented soon!\nSubject ID: ' + subjectId);
    }

    // Live Search Function
    function searchStudentsLive() {
        const searchInput = document.getElementById('studentSearch');
        const query = searchInput.value.trim();
        const clearBtn = document.getElementById('clearBtn');
        const loadingIndicator = document.getElementById('loadingIndicator');
        const tableContainer = document.getElementById('studentTableContainer');

        // Show/hide clear button
        if (query) {
            clearBtn.style.display = 'inline-block';
        } else {
            clearBtn.style.display = 'none';
        }

        // Clear previous timeout
        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }

        // Debounce search - wait 300ms after user stops typing
        searchTimeout = setTimeout(() => {
            // Show loading
            loadingIndicator.style.display = 'block';

            // Make API call
            fetch(`/admin/students/search?query=${encodeURIComponent(query)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    [header]: token
                }
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Search failed');
                }
                return response.json();
            })
            .then(students => {
                // Hide loading
                loadingIndicator.style.display = 'none';

                // Update table with results
                updateStudentTable(students, query);
            })
            .catch(error => {
                console.error('Error searching students:', error);
                loadingIndicator.style.display = 'none';
                alert('Error searching students. Please try again.');
            });
        }, 300);
    }

    function updateStudentTable(students, query) {
    const tableContainer = document.getElementById('studentTableContainer');
    const tbody = document.getElementById('studentTableBody');
    const table = tbody.closest('table');

    // Clear existing content
    tableContainer.innerHTML = '';

    if (students && students.length > 0) {
        // Create and populate table
        const tableHTML = `
            <table>
                <thead>
                    <tr>
                        <th>Student ID</th>
                        <th>Full Name</th>
                        <th>Email</th>
                        <th>Registered Date</th>
                        <th>Tests Taken</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    ${students.map(student => `
                        <tr>
                            <td>${student.studentId}</td>
                            <td>${student.fullName}</td>
                            <td>${student.email}</td>
                            <td>${formatDate(student.registeredDate)}</td>
                            <td>${student.testsTaken}</td>
                            <td>
                                <span class="status-badge ${student.enabled ? 'status-pass' : 'status-fail'}">
                                    ${student.enabled ? 'Active' : 'Inactive'}
                                </span>
                            </td>
                            <td>
                                <div class="action-buttons">
                                    <button class="action-btn btn-success"
                                            title="Download Report"
                                            onclick="alert('Download report coming soon!')">
                                        <i class="bx bx-download"></i>
                                    </button>
                                    <button class="action-btn btn-danger"
                                            title="Delete"
                                            onclick="deleteStudent(${student.id})">
                                        <i class="bx bx-trash"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>`;
        tableContainer.innerHTML = tableHTML;
    } else {
        // Show empty state message in table style
        const emptyStateHTML = `
            <div class="empty-state" style="padding: 40px; text-align: center; background: #fff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                <i class="bx bx-search" style="font-size: 48px; color: #ccc; margin-bottom: 15px;"></i>
                <h3 style="color: #333; margin-bottom: 10px;">No Students Found</h3>
                ${query ?
                    `<p style="color: #666;">No students match the search term "<strong>${query}</strong>"</p>
                     <button onclick="clearSearch()" class="btn btn-secondary" style="margin-top: 15px; height:fit-content;">
                       Clear Search
                     </button>`
                    :
                    '<p style="color: #666;">No students have been added yet</p>'
                }
            </div>`;
        tableContainer.innerHTML = emptyStateHTML;
    }
}

    // Clear Search
    function clearSearch() {
        const searchInput = document.getElementById('studentSearch');
        searchInput.value = '';
        document.getElementById('clearBtn').style.display = 'none';
        searchStudentsLive();
    }

    // Format Date Helper
    function formatDate(dateString) {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                       'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        return `${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}`;
    }

    // Add CSRF token to all AJAX requests
document.addEventListener('DOMContentLoaded', function() {
    // Setup AJAX with CSRF token
    const originalFetch = window.fetch;
    window.fetch = function(url, options) {
        options = options || {};
        options.headers = options.headers || {};

        if (options.method && options.method.toUpperCase() !== 'GET') {
            options.headers[header] = token;
        }

        return originalFetch(url, options);
    };

    console.log('Live search enabled for students');

    // Show dashboard on load - NO HASH CHECKING
    showSection('dashboard');

    // Check if there's a search query - if yes, show students section
    const searchQuery = /*[[${searchQuery}]]*/ null;
    if (searchQuery) {
        showSection('students');
    }
});

    // Modal Functions
    function openModal(modalId) {
        document.getElementById(modalId).style.display = 'block';
        document.body.style.overflow = 'hidden';
    }

    function closeModal(modalId) {
        document.getElementById(modalId).style.display = 'none';
        document.body.style.overflow = 'auto';
    }

    // Close modal when clicking outside
    window.onclick = function(event) {
        const modals = document.getElementsByClassName('modal');
        for (let modal of modals) {
            if (event.target == modal) {
                modal.style.display = 'none';
                document.body.style.overflow = 'auto';
            }
        }
    }

    // Auto-show modal if there's an error
    const hasSubjectError = /*[[${subjectError}]]*/ null;
    const hasStudentError = /*[[${studentError}]]*/ null;

    if (hasSubjectError) {
        openModal('addSubjectModal');
    }

    if (hasStudentError) {
        openModal('addStudentModal');
    }

    // Auto-hide success/error messages after 5 seconds
    setTimeout(function() {
        const messages = document.querySelectorAll('.error-message, .success-message');
        messages.forEach(function(msg) {
            if (msg.closest('.modal')) return;
            msg.style.transition = 'opacity 0.5s';
            msg.style.opacity = '0';
            setTimeout(function() {
                msg.remove();
            }, 500);
        });
    }, 5000);

    // Convert subject code to uppercase as user types
    const subjectCodeInput = document.getElementById('subjectCode');
    if (subjectCodeInput) {
        subjectCodeInput.addEventListener('input', function(e) {
            e.target.value = e.target.value.toUpperCase();
        });
    }

    // Real-time validation feedback
    const form = document.querySelector('#addSubjectModal form');
    if (form) {
        const inputs = form.querySelectorAll('input[required], textarea[required]');
        inputs.forEach(input => {
            input.addEventListener('blur', function() {
                if (this.validity.valid) {
                    this.style.borderColor = '#28a745';
                } else if (this.value) {
                    this.style.borderColor = '#dc3545';
                }
            });
        });
    }


    // Update file name display when file is selected
function updateFileName(input) {
    const fileUploadText = document.getElementById('fileUploadText');
    const fileUploadArea = document.getElementById('fileUploadArea');

    if (input.files && input.files[0]) {
        const fileName = input.files[0].name;
        const fileSize = (input.files[0].size / 1024 / 1024).toFixed(2); // Convert to MB

        fileUploadText.innerHTML = `
            <strong>${fileName}</strong><br>
            <span style="font-size: 12px; color: #666;">Size: ${fileSize} MB</span>
        `;
        fileUploadArea.style.borderColor = '#28a745';
        fileUploadArea.style.background = '#f0fff4';
    } else {
        fileUploadText.textContent = 'Click to upload or drag and drop';
        fileUploadArea.style.borderColor = '#ddd';
        fileUploadArea.style.background = '#fafafa';
    }
}

// Drag and drop functionality
const fileUploadArea = document.getElementById('fileUploadArea');
if (fileUploadArea) {
    fileUploadArea.addEventListener('dragover', function(e) {
        e.preventDefault();
        this.style.borderColor = '#0066cc';
        this.style.background = '#f0f8ff';
    });

    fileUploadArea.addEventListener('dragleave', function(e) {
        e.preventDefault();
        this.style.borderColor = '#ddd';
        this.style.background = '#fafafa';
    });

    fileUploadArea.addEventListener('drop', function(e) {
        e.preventDefault();
        const csvFile = document.getElementById('csvFile');
        const files = e.dataTransfer.files;

        if (files.length > 0) {
            csvFile.files = files;
            updateFileName(csvFile);
        }

        this.style.borderColor = '#ddd';
        this.style.background = '#fafafa';
    });
}

// Auto-show modal if there's an upload error
const hasUploadError = /*[[${uploadError}]]*/ null;
if (hasUploadError) {
    openModal('uploadQuestionsModal');
}