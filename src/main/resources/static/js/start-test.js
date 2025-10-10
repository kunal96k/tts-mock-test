// ==================== UTILITY FUNCTIONS ====================
function getCsrfToken() {
    const token = document.querySelector('meta[name="_csrf"]');
    const header = document.querySelector('meta[name="_csrf_header"]');
    return token && header ? { header: header.content, token: token.content } : null;
}

// ==================== TEST CONFIGURATION ====================
let testState = {
    currentQuestion: 0,
    answers: {},
    tabSwitches: 0,
    timeRemaining: testConfig.duration,
    timerInterval: null,
    startTime: null,
    isTestActive: false,
    fullscreenExited: false,
    questions: []
};

// Log initial configuration
console.log('========== TEST CONFIGURATION ==========');
console.log('testConfig:', testConfig);
console.log('testState initialized:', testState);

// ==================== TEST INITIALIZATION ====================
async function startTest() {
    console.log('========== START TEST CLICKED ==========');
    console.log('testConfig at start:', testConfig);

    try {
        // Validate configuration before proceeding
        if (!testConfig.testId) {
            console.error('ERROR: testId is missing');
            alert('Error: Test ID is not configured. Please refresh and try again.');
            return;
        }

        if (!testConfig.questionBankId) {
            console.error('ERROR: questionBankId is missing');
            alert('Error: Question Bank ID is not configured. Please refresh and try again.');
            return;
        }

        if (!testConfig.totalQuestions || testConfig.totalQuestions === 0) {
            console.error('ERROR: totalQuestions is missing or zero');
            alert('Error: Total questions not configured. Please refresh and try again.');
            return;
        }

        console.log('Configuration validated successfully');
        showLoadingState();

        // Prepare request payload
        const requestBody = {
            questionBankId: testConfig.questionBankId,
            testName: testConfig.testName || 'Online Test',
            totalQuestions: testConfig.totalQuestions,
            durationMinutes: testConfig.duration / 60,
            passingPercentage: testConfig.passingPercentage || 35,
            tabSwitchLimit: testConfig.tabSwitchLimit || 3
        };

        console.log('Request payload:', requestBody);

        // Prepare headers
        const csrf = getCsrfToken();
        const headers = { 'Content-Type': 'application/json' };
        if (csrf) {
            headers[csrf.header] = csrf.token;
            console.log('CSRF token added to headers');
        } else {
            console.warn('WARNING: CSRF token not found');
        }

        console.log('Sending POST request to /api/test/initialize...');

        // Make API call
        const response = await fetch('/api/test/initialize', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(requestBody)
        });

        console.log('Response received:', {
            status: response.status,
            statusText: response.statusText,
            ok: response.ok,
            redirected: response.redirected
        });

        // Check for authentication redirect
        if (response.status === 401 || response.redirected) {
            console.error('ERROR: Authentication failed or session expired');
            alert('Your session has expired. Please login again.');
            window.location.href = '/login';
            return;
        }

        // Validate content type
        const contentType = response.headers.get('content-type');
        console.log('Response content-type:', contentType);

        if (!contentType || !contentType.includes('application/json')) {
            console.error('ERROR: Expected JSON response but got:', contentType);
            const text = await response.text();
            console.error('Response body:', text.substring(0, 500));
            throw new Error('Server returned non-JSON response. Please check server logs.');
        }

        // Parse response
        const data = await response.json();
        console.log('========== API RESPONSE ==========');
        console.log('Response data:', data);

        if (!response.ok) {
            console.error('ERROR: Response not OK');
            console.error('Error from server:', data.error);
            throw new Error(data.error || `Server error: ${response.status}`);
        }

        if (!data.success) {
            console.error('ERROR: Success flag is false');
            console.error('Error message:', data.error);
            throw new Error(data.error || 'Failed to initialize test');
        }

        // Validate questions
        if (!data.questions || !Array.isArray(data.questions)) {
            console.error('ERROR: Questions array is missing or invalid');
            console.error('Data structure:', Object.keys(data));
            throw new Error('Invalid response format: questions array missing');
        }

        if (data.questions.length === 0) {
            console.error('ERROR: Questions array is empty');
            throw new Error('No questions available for this test');
        }

        console.log(`Successfully received ${data.questions.length} questions`);

        // Log first question as sample
        console.log('Sample question:', {
            id: data.questions[0].id,
            questionText: data.questions[0].questionText?.substring(0, 50) + '...',
            hasOptions: {
                A: !!data.questions[0].optionA,
                B: !!data.questions[0].optionB,
                C: !!data.questions[0].optionC,
                D: !!data.questions[0].optionD
            },
            correctAnswer: data.questions[0].correctAnswer,
            marks: data.questions[0].marks,
            difficulty: data.questions[0].difficultyLevel
        });

        // Map questions to test format
        testState.questions = data.questions.map((q, index) => {
            const mapped = {
                id: q.id,
                questionNumber: index + 1,
                question: q.questionText,
                options: [q.optionA, q.optionB, q.optionC, q.optionD],
                marks: q.marks || 1,
                difficultyLevel: q.difficultyLevel || 'MEDIUM'
            };

            // Validate question structure
            if (!mapped.question) {
                console.error(`ERROR: Question ${index + 1} has no text`);
            }
            if (mapped.options.some(opt => !opt)) {
                console.warn(`WARNING: Question ${index + 1} has missing options`);
            }

            return mapped;
        });

        console.log('Questions mapped successfully:', testState.questions.length);
        console.log('Test state updated:', {
            totalQuestions: testState.questions.length,
            currentQuestion: testState.currentQuestion,
            answersCount: Object.keys(testState.answers).length
        });

        // Hide start screen and show test screen
        console.log('Switching to test screen...');
        document.getElementById('startScreen').style.display = 'none';
        document.getElementById('testScreen').style.display = 'block';

        // Activate test
        testState.isTestActive = true;
        testState.timeRemaining = testConfig.duration;

        // Initialize UI
        console.log('Initializing test UI...');
        enterFullscreen();
        initializeTest();
        startTimer();
        setupTabSwitchDetection();
        setupFullscreenDetection();

        console.log('========== TEST STARTED SUCCESSFULLY ==========');

    } catch (error) {
        console.error('========== TEST START ERROR ==========');
        console.error('Error:', error);
        console.error('Error stack:', error.stack);

        alert(`Failed to start test:\n\n${error.message}\n\nPlease check the console for details and contact administrator if the problem persists.`);
        hideLoadingState();
    }
}

function showLoadingState() {
    console.log('Showing loading state...');
    const startButton = document.querySelector('.btn-primary');
    if (startButton) {
        startButton.disabled = true;
        startButton.innerHTML = '<i class="bx bx-loader-alt bx-spin"></i> Loading Questions...';
    }
}

function hideLoadingState() {
    console.log('Hiding loading state...');
    const startButton = document.querySelector('.btn-primary');
    if (startButton) {
        startButton.disabled = false;
        startButton.innerHTML = '<i class="bx bx-play"></i> Start Test in Fullscreen';
    }
}

// ==================== FULLSCREEN MANAGEMENT ====================
function enterFullscreen() {
    console.log('Entering fullscreen mode...');
    const elem = document.documentElement;
    if (elem.requestFullscreen) {
        elem.requestFullscreen().catch(err => console.error('Fullscreen error:', err));
    } else if (elem.webkitRequestFullscreen) {
        elem.webkitRequestFullscreen();
    } else if (elem.msRequestFullscreen) {
        elem.msRequestFullscreen();
    }
}

function exitFullscreen() {
    console.log('Exiting fullscreen mode...');
    if (document.exitFullscreen) {
        document.exitFullscreen().catch(err => console.error('Exit fullscreen error:', err));
    } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
    } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
    }
}

function setupFullscreenDetection() {
    console.log('Setting up fullscreen detection...');
    const handleFullscreenChange = () => {
        if (!document.fullscreenElement && !document.webkitFullscreenElement &&
            !document.mozFullScreenElement && testState.isTestActive && !testState.fullscreenExited) {

            console.warn('WARNING: Fullscreen exited by user');
            testState.fullscreenExited = true;
            testState.tabSwitches++;
            showWarningBanner();

            if (testState.tabSwitches >= testConfig.tabSwitchLimit) {
                console.error('CRITICAL: Tab switch limit exceeded');
                document.removeEventListener('fullscreenchange', handleFullscreenChange);
                autoSubmitTest('Fullscreen exit limit exceeded');
            }
        }
    };

    document.addEventListener('fullscreenchange', handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', handleFullscreenChange);
    document.addEventListener('mozfullscreenchange', handleFullscreenChange);
}

// ==================== TEST RENDERING ====================
function initializeTest() {
    console.log('Initializing test...');
    testState.startTime = Date.now();
    testState.timeRemaining = testConfig.duration;

    console.log('Rendering question navigator...');
    renderQuestionNavigator();

    console.log('Rendering first question...');
    renderQuestion();
}

function renderQuestionNavigator() {
    const nav = document.getElementById('questionNav');
    if (!nav) {
        console.error('ERROR: questionNav element not found');
        return;
    }

    nav.innerHTML = testState.questions.map((q, i) => {
        let classes = 'question-nav-btn';
        if (testState.answers[q.id] !== undefined) {
            classes += ' answered';
        }
        if (i === testState.currentQuestion) {
            classes += ' current';
        }
        return `<button class="${classes}" onclick="goToQuestion(${i})">${i + 1}</button>`;
    }).join('');

    console.log('Question navigator rendered with', testState.questions.length, 'buttons');
}

function renderQuestion() {
    const q = testState.questions[testState.currentQuestion];
    const container = document.getElementById('questionContainer');

    if (!container) {
        console.error('ERROR: questionContainer element not found');
        return;
    }

    if (!q) {
        console.error('ERROR: No question at index', testState.currentQuestion);
        return;
    }

    console.log('Rendering question', q.questionNumber, '/', testConfig.totalQuestions);

    container.innerHTML = `
        <div class="question-card">
            <div class="question-header">
                <span class="question-number">Question ${q.questionNumber} of ${testConfig.totalQuestions}</span>
                <span class="question-marks">${q.marks} Mark${q.marks > 1 ? 's' : ''}</span>
                ${q.difficultyLevel ? `<span class="difficulty-badge ${q.difficultyLevel.toLowerCase()}">${q.difficultyLevel}</span>` : ''}
            </div>
            <div class="question-text">${q.question}</div>
            <div class="options">
                ${q.options.map((opt, i) => {
                    const optionLetter = String.fromCharCode(65 + i);
                    return `
                    <label class="option ${testState.answers[q.id] === optionLetter ? 'selected' : ''}">
                        <input type="radio" name="question${q.id}" value="${optionLetter}"
                               ${testState.answers[q.id] === optionLetter ? 'checked' : ''}
                               onchange="saveAnswer(${q.id}, '${optionLetter}')">
                        <span class="option-text"><strong>${optionLetter}.</strong> ${opt}</span>
                    </label>
                `}).join('')}
            </div>
        </div>
    `;

    updateNavigationButtons();
}

function saveAnswer(questionId, optionLetter) {
    console.log('Saving answer: Question', questionId, '= Option', optionLetter);
    testState.answers[questionId] = optionLetter;

    const labels = document.querySelectorAll('.option');
    labels.forEach(label => label.classList.remove('selected'));

    if (typeof event !== 'undefined' && event.target) {
        const option = event.target.closest('.option');
        if (option) option.classList.add('selected');
    }

    renderQuestionNavigator();
}

// ==================== NAVIGATION ====================
function goToQuestion(index) {
    if (index < 0 || index >= testState.questions.length) return;
    console.log('Navigating to question', index + 1);
    testState.currentQuestion = index;
    renderQuestion();
    renderQuestionNavigator();
    window.scrollTo(0, 0);
}

function previousQuestion() {
    if (testState.currentQuestion > 0) {
        testState.currentQuestion--;
        renderQuestion();
        renderQuestionNavigator();
        window.scrollTo(0, 0);
    }
}

function nextQuestion() {
    if (testState.currentQuestion < testState.questions.length - 1) {
        testState.currentQuestion++;
        renderQuestion();
        renderQuestionNavigator();
        window.scrollTo(0, 0);
    }
}

function updateNavigationButtons() {
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const submitBtn = document.getElementById('submitBtn');

    if (prevBtn) {
        prevBtn.style.display = testState.currentQuestion === 0 ? 'none' : 'inline-flex';
    }

    if (testState.currentQuestion === testState.questions.length - 1) {
        if (nextBtn) nextBtn.style.display = 'none';
        if (submitBtn) submitBtn.style.display = 'inline-flex';
    } else {
        if (nextBtn) nextBtn.style.display = 'inline-flex';
        if (submitBtn) submitBtn.style.display = 'none';
    }
}

// ==================== TIMER ====================
function startTimer() {
    console.log('Starting timer with', testState.timeRemaining, 'seconds');
    updateTimerDisplay();

    testState.timerInterval = setInterval(() => {
        testState.timeRemaining--;
        updateTimerDisplay();

        if (testState.timeRemaining <= 0) {
            console.warn('WARNING: Time expired');
            clearInterval(testState.timerInterval);
            autoSubmitTest('Time expired');
        }
    }, 1000);
}

function updateTimerDisplay() {
    const timerElement = document.getElementById('timerValue');
    if (!timerElement) return;

    const minutes = Math.floor(testState.timeRemaining / 60);
    const seconds = testState.timeRemaining % 60;
    const display = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;

    timerElement.textContent = display;

    if (testState.timeRemaining <= 300) {
        timerElement.classList.add('warning');
    }
}

// ==================== TAB SWITCHING DETECTION ====================
function setupTabSwitchDetection() {
    console.log('Setting up tab switch detection...');
    const tabSwitchHandler = function() {
        if (document.hidden && testState.isTestActive) {
            testState.tabSwitches++;
            console.warn('WARNING: Tab switch detected. Count:', testState.tabSwitches);
            showWarningBanner();

            if (testState.tabSwitches >= testConfig.tabSwitchLimit) {
                console.error('CRITICAL: Tab switch limit reached');
                document.removeEventListener('visibilitychange', tabSwitchHandler);
                autoSubmitTest('Tab switch limit exceeded');
            }
        }
    };

    document.addEventListener('visibilitychange', tabSwitchHandler);
}

function showWarningBanner() {
    const banner = document.getElementById('warningBanner');
    const attemptsLeft = document.getElementById('attemptsLeft');

    if (banner && attemptsLeft) {
        attemptsLeft.textContent = testConfig.tabSwitchLimit - testState.tabSwitches;
        banner.classList.add('show');

        setTimeout(() => {
            banner.classList.remove('show');
        }, 5000);
    }
}

// ==================== TEST SUBMISSION ====================
async function submitTest() {
    console.log('========== SUBMIT TEST CLICKED ==========');
    const unanswered = testState.questions.length - Object.keys(testState.answers).length;

    console.log('Test status:', {
        totalQuestions: testState.questions.length,
        answeredQuestions: Object.keys(testState.answers).length,
        unansweredQuestions: unanswered
    });

    if (unanswered > 0) {
        if (!confirm(`You have ${unanswered} unanswered questions. Do you want to submit?`)) {
            console.log('User cancelled submission');
            return;
        }
    }

    if (confirm('Are you sure you want to submit the test? This action cannot be undone.')) {
        console.log('User confirmed submission');
        await finishTest();
    } else {
        console.log('User cancelled final submission');
    }
}

function autoSubmitTest(reason) {
    if (!testState.isTestActive) return;

    console.log('========== AUTO SUBMIT TRIGGERED ==========');
    console.log('Reason:', reason);

    testState.isTestActive = false;
    alert(`Test auto-submitted: ${reason}`);
    finishTest();
}

async function finishTest() {
    console.log('========== FINISHING TEST ==========');
    testState.isTestActive = false;
    clearInterval(testState.timerInterval);
    exitFullscreen();

    // Show loading state
    const testScreen = document.getElementById('testScreen');
    if (testScreen) {
        testScreen.innerHTML = `
            <div style="display: flex; justify-content: center; align-items: center; height: 100vh;">
                <div style="text-align: center;">
                    <i class='bx bx-loader-alt bx-spin' style="font-size: 48px; color: #667eea;"></i>
                    <p style="margin-top: 20px; font-size: 18px; color: #64748b;">Evaluating your answers...</p>
                </div>
            </div>
        `;
    }

    try {
        // Prepare answers array
        const answers = testState.questions.map(q => ({
            questionId: q.id,
            selectedOption: testState.answers[q.id] || null
        }));

        console.log('Prepared answers:', answers.length, 'answers');
        console.log('Answered:', answers.filter(a => a.selectedOption !== null).length);
        console.log('Unanswered:', answers.filter(a => a.selectedOption === null).length);

        const timeTaken = testConfig.duration - testState.timeRemaining;
        console.log('Time taken:', timeTaken, 'seconds');

        const submission = {
            studentId: testConfig.studentId || 1,
            testId: testConfig.testId,
            questionBankId: testConfig.questionBankId,
            answers: answers,
            timeTakenSeconds: timeTaken,
            tabSwitches: testState.tabSwitches
        };

        console.log('Submission payload:', submission);

        // Prepare headers
        const csrf = getCsrfToken();
        const headers = { 'Content-Type': 'application/json' };
        if (csrf) {
            headers[csrf.header] = csrf.token;
        }

        console.log('Sending POST request to /api/test/submit...');

        const response = await fetch('/api/test/submit', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(submission)
        });

        console.log('Submit response:', {
            status: response.status,
            statusText: response.statusText,
            ok: response.ok,
            redirected: response.redirected
        });

        // Check for authentication issues
        if (response.status === 401 || response.redirected) {
            console.error('ERROR: Authentication failed');
            alert('Your session has expired. Your answers were not saved. Please login again.');
            window.location.href = '/login';
            return;
        }

        // Validate content type
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            console.error('ERROR: Non-JSON response received');
            throw new Error('Server returned non-JSON response');
        }

        const data = await response.json();
        console.log('========== SUBMIT RESPONSE ==========');
        console.log('Response data:', data);

        if (!response.ok) {
            console.error('ERROR: Response not OK');
            throw new Error(data.error || `Server error: ${response.status}`);
        }

        if (!data.success) {
            console.error('ERROR: Submission failed');
            throw new Error(data.error || 'Test submission failed');
        }

        console.log('Test submitted successfully');
        console.log('Results:', data.result);

        showResults(data.result, timeTaken);

    } catch (error) {
        console.error('========== SUBMISSION ERROR ==========');
        console.error('Error:', error);
        console.error('Error stack:', error.stack);

        alert(`Failed to submit test:\n\n${error.message}\n\nPlease contact administrator.`);
        window.location.href = '/student-dashboard';
    }
}

// ==================== RESULTS DISPLAY ====================
function showResults(results, timeSpent) {
    console.log('========== DISPLAYING RESULTS ==========');
    console.log('Results:', results);
    console.log('Time spent:', timeSpent, 'seconds');

    document.getElementById('testScreen').style.display = 'none';
    document.getElementById('resultScreen').style.display = 'block';
    window.scrollTo(0, 0);

    const minutes = Math.floor(timeSpent / 60);
    const seconds = timeSpent % 60;
    const timeTaken = `${minutes}:${seconds.toString().padStart(2, '0')}`;

    const now = new Date();
    const testDateEl = document.getElementById('testDate');
    const assessmentIdEl = document.getElementById('assessmentId');

    if (testDateEl) {
        testDateEl.textContent = now.toLocaleDateString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric'
        });
    }

    if (assessmentIdEl) {
        assessmentIdEl.textContent = 'TTS-' + now.getTime().toString().slice(-8);
    }

    // Animate score circle
    const scoreCircle = document.getElementById('scoreCircle');
    if (scoreCircle) {
        const circumference = 565.48;
        const offset = circumference - (results.scorePercentage / 100) * circumference;
        setTimeout(() => {
            scoreCircle.style.transition = 'stroke-dashoffset 1.5s ease-in-out';
            scoreCircle.style.strokeDashoffset = offset;
        }, 100);
    }

    // Update all result fields
    const scoreValue = document.getElementById('scoreValue');
    if (scoreValue) scoreValue.textContent = Math.round(results.scorePercentage);

    const marksObtained = document.getElementById('marksObtained');
    if (marksObtained) marksObtained.textContent = results.obtainedMarks;

    const totalMarks = document.getElementById('totalMarks');
    if (totalMarks) totalMarks.textContent = results.totalMarks;

    const correctCount = document.getElementById('correctCount');
    if (correctCount) correctCount.textContent = results.correctAnswers;

    const wrongCount = document.getElementById('wrongCount');
    if (wrongCount) wrongCount.textContent = results.wrongAnswers;

    const unansweredCount = document.getElementById('unansweredCount');
    if (unansweredCount) unansweredCount.textContent = results.unanswered;

    const timeTakenEl = document.getElementById('timeTaken');
    if (timeTakenEl) timeTakenEl.textContent = timeTaken;

    const gradeBadge = document.getElementById('gradeBadge');
    if (gradeBadge) gradeBadge.textContent = results.grade;

    const verdict = document.getElementById('verdict');
    if (verdict) {
        if (results.passed) {
            verdict.innerHTML = '<i class="bx bx-check-circle"></i> PASSED';
            verdict.className = 'result-verdict pass';
        } else {
            verdict.innerHTML = '<i class="bx bx-x-circle"></i> FAILED';
            verdict.className = 'result-verdict fail';
        }
    }

    if (results.reviewData) {
        console.log('Rendering answer review with', results.reviewData.length, 'items');
        renderAnswerReview(results.reviewData);
    }

    console.log('========== RESULTS DISPLAYED SUCCESSFULLY ==========');
}

// ==================== ANSWER REVIEW ====================
let currentFilter = 'all';
let allReviewData = [];

function renderAnswerReview(reviewData) {
    allReviewData = reviewData;
    const container = document.getElementById('answerReview');
    if (!container) return;

    const filteredData = currentFilter === 'all'
        ? reviewData
        : reviewData.filter(item => item.status === currentFilter);

    if (filteredData.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: #64748b; padding: 40px;">No questions found in this category.</p>';
        return;
    }

    container.innerHTML = filteredData.map((item, index) => {
        const question = testState.questions.find(q => q.id === item.questionId);
        const questionNumber = question ? question.questionNumber : index + 1;

        return `
        <div class="answer-review ${item.status}" onclick="toggleReview(this)">
            <div class="review-header-row">
                <span class="review-question">
                    Question ${questionNumber}
                    <i class='bx bx-chevron-down expand-icon'></i>
                </span>
                <span class="review-result ${item.status}">
                    ${item.status === 'correct' ? '✓ Correct' :
                      item.status === 'incorrect' ? '✗ Incorrect' : '○ Unanswered'}
                </span>
            </div>
            <div class="review-content">
                <p><strong>Question:</strong> ${item.questionText}</p>
                <div class="review-options">
                    <p><strong>Your Answer:</strong>
                        <span class="answer-badge ${item.status === 'correct' ? 'correct' : item.status === 'incorrect' ? 'wrong' : 'unanswered'}">
                            ${item.userAnswer || 'Not Answered'}
                        </span>
                    </p>
                    ${item.status !== 'correct' ?
                      `<p><strong>Correct Answer:</strong>
                        <span class="answer-badge correct">${item.correctAnswer}</span>
                       </p>` : ''}
                </div>
                ${item.explanation ?
                  `<div class="explanation-box">
                    <strong>Explanation:</strong> ${item.explanation}
                   </div>` : ''}
            </div>
        </div>
    `}).join('');
}

function toggleReview(element) {
    if (element) {
        element.classList.toggle('expanded');
    }
}

function filterReview(filter) {
    currentFilter = filter;
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    if (typeof event !== 'undefined' && event.target) {
        event.target.classList.add('active');
    }
    renderAnswerReview(allReviewData);
}

// ==================== PDF GENERATION ====================
function downloadPDF() {
    if (typeof window.jspdf === 'undefined' || !window.jspdf.jsPDF) {
        alert('PDF library is not loaded. Please refresh the page and try again.');
        return;
    }

    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const margin = 20;

    const testDate = document.getElementById('testDate').textContent;
    const assessmentId = document.getElementById('assessmentId').textContent;
    const score = document.getElementById('scoreValue').textContent;
    const marksObtained = document.getElementById('marksObtained').textContent;
    const totalMarks = document.getElementById('totalMarks').textContent;
    const correct = document.getElementById('correctCount').textContent;
    const wrong = document.getElementById('wrongCount').textContent;
    const unanswered = document.getElementById('unansweredCount').textContent;
    const timeTaken = document.getElementById('timeTaken').textContent;
    const grade = document.getElementById('gradeBadge').textContent;
    const verdict = document.getElementById('verdict').textContent.trim();
    const isPassed = verdict.includes('PASSED');

    let yPos = 20;

    // Header
    doc.setFillColor(102, 126, 234);
    doc.rect(0, 0, pageWidth, 40, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(16);
    doc.setFont('helvetica', 'bold');
    doc.text('ASSESSMENT REPORT', pageWidth / 2, 20, { align: 'center' });
    doc.setFontSize(10);
    doc.text(testDate, pageWidth / 2, 30, { align: 'center' });

    yPos = 55;

    // Report details
    doc.setTextColor(30, 41, 59);
    doc.setFontSize(10);
    doc.text(`Report ID: ${assessmentId}`, margin, yPos);
    doc.text(`Assessment: ${testConfig.testName}`, margin, yPos + 7);

    yPos += 20;

    // Score
    doc.setFontSize(14);
    doc.text(`Score: ${score}%`, margin, yPos);
    doc.text(`Grade: ${grade}`, pageWidth - margin - 30, yPos);

    yPos += 10;

    // Status
    doc.setFontSize(12);
    if (isPassed) {
        doc.setTextColor(5, 150, 105);
        doc.text('✓ PASSED', margin, yPos);
    } else {
        doc.setTextColor(220, 38, 38);
        doc.text('✗ FAILED', margin, yPos);
    }

    yPos += 15;

    // Stats
    doc.setTextColor(30, 41, 59);
    doc.setFontSize(10);
    doc.text(`Marks: ${marksObtained} / ${totalMarks}`, margin, yPos);
    doc.text(`Correct: ${correct}`, margin, yPos + 7);
    doc.text(`Wrong: ${wrong}`, margin + 60, yPos + 7);
    doc.text(`Unanswered: ${unanswered}`, margin + 120, yPos + 7);
    doc.text(`Time: ${timeTaken}`, margin, yPos + 14);

    // Footer
    doc.setFontSize(8);
    doc.setTextColor(148, 163, 184);
    doc.text('TechnoKraft Online Assessment Platform', margin, pageHeight - 10);
    doc.text(`ID: ${assessmentId}`, pageWidth - margin - 40, pageHeight - 10);

    const fileName = `TechnoKraft_Assessment_${assessmentId}_${new Date().toISOString().split('T')[0]}.pdf`;
    doc.save(fileName);

    console.log('PDF downloaded:', fileName);
    alert('✓ PDF Report downloaded successfully!');
}

function emailReport() {
    const email = prompt('Enter your email address:');
    if (email && email.includes('@')) {
        alert(`✓ Report will be sent to: ${email}\n\n(Email integration requires backend setup)`);
    } else if (email) {
        alert('⚠ Please enter a valid email address.');
    }
}

function backToDashboard() {
    if (confirm('Return to dashboard?')) {
        window.location.href = '/student-dashboard';
    }
}

// ==================== SECURITY ====================
document.addEventListener('contextmenu', function(e) {
    if (testState.isTestActive) {
        e.preventDefault();
        return false;
    }
});

document.addEventListener('keydown', function(e) {
    if (testState.isTestActive) {
        if (e.keyCode === 123 ||
            (e.ctrlKey && e.shiftKey && (e.keyCode === 73 || e.keyCode === 74)) ||
            (e.ctrlKey && e.keyCode === 85)) {
            e.preventDefault();
            return false;
        }
    }
});

window.addEventListener('beforeunload', function(e) {
    if (testState.isTestActive) {
        e.preventDefault();
        e.returnValue = '';
        return '';
    }
});

console.log('========== TEST SCRIPT LOADED ==========');