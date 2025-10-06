document.addEventListener('DOMContentLoaded', function() {
    console.log("Admin dashboard script loaded"); // Fixed typo: cosole -> console

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

    // Section navigation - FIXED: Now accepts just sectionName
    window.showSection = function(sectionName) {
        // Hide all sections
        document.querySelectorAll('.section-content').forEach(s => s.style.display = 'none');

        // Remove active class from all nav links
        document.querySelectorAll('.nav-links li a').forEach(a => a.classList.remove('active'));

        // Show the selected section
        const section = document.getElementById(sectionName + '-section');
        if (section) {
            section.style.display = 'block';
        }

        // Add active class to the clicked link (find by checking onclick attribute)
        document.querySelectorAll('.nav-links li a').forEach(link => {
            const onclick = link.getAttribute('onclick');
            if (onclick && onclick.includes(`'${sectionName}'`)) {
                link.classList.add('active');
            }
        });
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