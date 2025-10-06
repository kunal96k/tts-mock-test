const testConfig = {
    totalQuestions: 30,
    duration: 30 * 60,
    tabSwitchLimit: 3
};

let testState = {
    currentQuestion: 0,
    answers: {},
    tabSwitches: 0,
    timeRemaining: testConfig.duration,
    timerInterval: null,
    startTime: null,
    isTestActive: false,
    fullscreenExited: false
};

const questions = Array.from({length: 30}, (_, i) => ({
    id: i + 1,
    question: `What is the output of the following Java code snippet? (Question ${i + 1})`,
    options: [
        'Option A: Compilation Error',
        'Option B: Runtime Exception',
        'Option C: Prints "Hello World"',
        'Option D: No output'
    ],
    // correctAnswer: Math.floor(Math.random() * 4)
    correctAnswer: 0
}));

function startTest() {
    document.getElementById('startScreen').style.display = 'none';
    document.getElementById('testScreen').style.display = 'block';
    testState.isTestActive = true;
    enterFullscreen();
    initializeTest();
    startTimer();
    setupTabSwitchDetection();
    setupFullscreenDetection();
}

function enterFullscreen() {
    const elem = document.documentElement;
    if (elem.requestFullscreen) {
        elem.requestFullscreen().catch(err => console.log('Fullscreen error:', err));
    } else if (elem.webkitRequestFullscreen) {
        elem.webkitRequestFullscreen();
    } else if (elem.msRequestFullscreen) {
        elem.msRequestFullscreen();
    }
}

function exitFullscreen() {
    if (document.exitFullscreen) {
        document.exitFullscreen().catch(err => console.log('Exit fullscreen error:', err));
    } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
    } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
    }
}

function setupFullscreenDetection() {
    const handleFullscreenChange = () => {
        if (!document.fullscreenElement && !document.webkitFullscreenElement && 
            !document.mozFullScreenElement && testState.isTestActive && !testState.fullscreenExited) {
            testState.fullscreenExited = true;
            testState.tabSwitches++;
            showWarningBanner();
            
            if (testState.tabSwitches >= testConfig.tabSwitchLimit) {
                autoSubmitTest('Fullscreen exit limit exceeded');
            }
        }
    };

    document.addEventListener('fullscreenchange', handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', handleFullscreenChange);
    document.addEventListener('mozfullscreenchange', handleFullscreenChange);
}

function initializeTest() {
    testState.startTime = Date.now();
    renderQuestionNavigator();
    renderQuestion();
}

function renderQuestionNavigator() {
    const nav = document.getElementById('questionNav');
    nav.innerHTML = questions.map((_, i) => {
        let classes = 'question-nav-btn';
        if (testState.answers[i + 1] !== undefined) {
            classes += ' answered';
        }
        if (i === testState.currentQuestion) {
            classes += ' current';
        }
        return `<button class="${classes}" onclick="goToQuestion(${i})">${i + 1}</button>`;
    }).join('');
}

function renderQuestion() {
    const q = questions[testState.currentQuestion];
    const container = document.getElementById('questionContainer');
    
    container.innerHTML = `
        <div class="question-card">
            <div class="question-header">
                <span class="question-number">Question ${q.id} of ${testConfig.totalQuestions}</span>
                <span class="question-marks">1 Mark</span>
            </div>
            <div class="question-text">${q.question}</div>
            <div class="options">
                ${q.options.map((opt, i) => `
                    <label class="option ${testState.answers[q.id] === i ? 'selected' : ''}">
                        <input type="radio" name="question${q.id}" value="${i}" 
                               ${testState.answers[q.id] === i ? 'checked' : ''}
                               onchange="saveAnswer(${q.id}, ${i})">
                        <span class="option-text">${opt}</span>
                    </label>
                `).join('')}
            </div>
        </div>
    `;

    updateNavigationButtons();
}

function saveAnswer(questionId, optionIndex) {
    testState.answers[questionId] = optionIndex;
    
    const labels = document.querySelectorAll('.option');
    labels.forEach(label => label.classList.remove('selected'));
    event.target.closest('.option').classList.add('selected');
    
    renderQuestionNavigator();
}

function goToQuestion(index) {
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
    if (testState.currentQuestion < questions.length - 1) {
        testState.currentQuestion++;
        renderQuestion();
        renderQuestionNavigator();
        window.scrollTo(0, 0);
    }
}

function updateNavigationButtons() {
    document.getElementById('prevBtn').style.display = 
        testState.currentQuestion === 0 ? 'none' : 'inline-flex';
    
    if (testState.currentQuestion === questions.length - 1) {
        document.getElementById('nextBtn').style.display = 'none';
        document.getElementById('submitBtn').style.display = 'inline-flex';
    } else {
        document.getElementById('nextBtn').style.display = 'inline-flex';
        document.getElementById('submitBtn').style.display = 'none';
    }
}

function startTimer() {
    updateTimerDisplay();
    testState.timerInterval = setInterval(() => {
        testState.timeRemaining--;
        updateTimerDisplay();
        
        if (testState.timeRemaining <= 0) {
            clearInterval(testState.timerInterval);
            autoSubmitTest('Time expired');
        }
    }, 1000);
}

function updateTimerDisplay() {
    const minutes = Math.floor(testState.timeRemaining / 60);
    const seconds = testState.timeRemaining % 60;
    const display = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    document.getElementById('timerValue').textContent = display;
    
    if (testState.timeRemaining <= 300) {
        document.getElementById('timerValue').classList.add('warning');
    }
}

function showWarningBanner() {
    const banner = document.getElementById('warningBanner');
    document.getElementById('attemptsLeft').textContent = 
        testConfig.tabSwitchLimit - testState.tabSwitches;
    banner.classList.add('show');
    
    setTimeout(() => {
        banner.classList.remove('show');
    }, 5000);
}

function setupTabSwitchDetection() {
    const tabSwitchHandler = function() {
        if (document.hidden && testState.isTestActive) {
            testState.tabSwitches++;
            showWarningBanner();
            
            if (testState.tabSwitches >= testConfig.tabSwitchLimit) {
                document.removeEventListener('visibilitychange', tabSwitchHandler);
                autoSubmitTest('Tab switch limit exceeded');
            }
        }
    };

    document.addEventListener('visibilitychange', tabSwitchHandler);
}

function submitTest() {
    const unanswered = questions.length - Object.keys(testState.answers).length;
    if (unanswered > 0) {
        if (!confirm(`You have ${unanswered} unanswered questions. Do you want to submit?`)) {
            return;
        }
    }
    
    if (confirm('Are you sure you want to submit the test? This action cannot be undone.')) {
        finishTest();
    }
}

function autoSubmitTest(reason) {
    if (!testState.isTestActive) return;
    
    testState.isTestActive = false;
    alert(`Test auto-submitted: ${reason}`);
    finishTest();
}

function finishTest() {
    testState.isTestActive = false;
    clearInterval(testState.timerInterval);
    exitFullscreen();
    const results = calculateResults();
    showResults(results);
}

function calculateResults() {
    let correct = 0;
    let wrong = 0;
    let unanswered = 0;
    const reviewData = [];

    questions.forEach(q => {
        const userAnswer = testState.answers[q.id];
        if (userAnswer === undefined) {
            unanswered++;
            reviewData.push({
                questionNum: q.id,
                question: q.question,
                options: q.options,
                status: 'unanswered',
                userAnswer: 'Not answered',
                userAnswerIndex: null,
                correctAnswer: q.options[q.correctAnswer],
                correctAnswerIndex: q.correctAnswer
            });
        } else if (userAnswer === q.correctAnswer) {
            correct++;
            reviewData.push({
                questionNum: q.id,
                question: q.question,
                options: q.options,
                status: 'correct',
                userAnswer: q.options[userAnswer],
                userAnswerIndex: userAnswer,
                correctAnswer: q.options[q.correctAnswer],
                correctAnswerIndex: q.correctAnswer
            });
        } else {
            wrong++;
            reviewData.push({
                questionNum: q.id,
                question: q.question,
                options: q.options,
                status: 'incorrect',
                userAnswer: q.options[userAnswer],
                userAnswerIndex: userAnswer,
                correctAnswer: q.options[q.correctAnswer],
                correctAnswerIndex: q.correctAnswer
            });
        }
    });

    // Calculate score in percentage
    const score = Math.round((correct / questions.length) * 100);

    // Time taken
    const timeSpent = testConfig.duration - testState.timeRemaining;
    const minutes = Math.floor(timeSpent / 60);
    const seconds = timeSpent % 60;

    // Passing Marks (35% of total marks)
    const passingMarks = Math.ceil((testConfig.totalQuestions * 35) / 100);

    return {
        score,                  
        correct,                
        wrong,                   
        unanswered,                 
        totalMarks: questions.length,
        marksObtained: correct,     
        timeTaken: `${minutes}:${seconds.toString().padStart(2, '0')}`,
        passingMarks,              
        passed: correct >= passingMarks,  
        reviewData
    };
}

function getGrade(score) {
    if (score >= 90) return 'A+';
    if (score >= 80) return 'A';
    if (score >= 70) return 'B';
    if (score >= 60) return 'C';
    if (score >= 50) return 'D';
    if (score >= 35) return 'E';  
    return 'F'; 
}


function showResults(results) {
    document.getElementById('testScreen').style.display = 'none';
    document.getElementById('resultScreen').style.display = 'block';
    window.scrollTo(0, 0);
    
    const now = new Date();
    document.getElementById('testDate').textContent = now.toLocaleDateString('en-US', { 
        year: 'numeric', month: 'short', day: 'numeric' 
    });
    document.getElementById('assessmentId').textContent = 'TTS-' + now.getTime().toString().slice(-8);
    
    const scoreCircle = document.getElementById('scoreCircle');
    const circumference = 565.48;
    const offset = circumference - (results.score / 100) * circumference;
    setTimeout(() => {
        scoreCircle.style.transition = 'stroke-dashoffset 1.5s ease-in-out';
        scoreCircle.style.strokeDashoffset = offset;
    }, 100);
    
    document.getElementById('scoreValue').textContent = results.score;
    document.getElementById('marksObtained').textContent = results.marksObtained;
    document.getElementById('totalMarks').textContent = results.totalMarks;
    document.getElementById('correctCount').textContent = results.correct;
    document.getElementById('wrongCount').textContent = results.wrong;
    document.getElementById('unansweredCount').textContent = results.unanswered;
    document.getElementById('timeTaken').textContent = results.timeTaken;
    
    document.getElementById('gradeBadge').textContent = getGrade(results.score);
    
    const verdict = document.getElementById('verdict');
    if (results.passed) {
        verdict.innerHTML = '<i class="bx bx-check-circle"></i> PASSED';
        verdict.className = 'result-verdict pass';
    } else {
        verdict.innerHTML = '<i class="bx bx-x-circle"></i> FAILED';
        verdict.className = 'result-verdict fail';
    }
    
    renderAnswerReview(results.reviewData);
}

let currentFilter = 'all';
let allReviewData = [];

function renderAnswerReview(reviewData) {
    allReviewData = reviewData;
    const container = document.getElementById('answerReview');
    
    const filteredData = currentFilter === 'all' 
        ? reviewData 
        : reviewData.filter(item => item.status === currentFilter);
    
    if (filteredData.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: #64748b; padding: 40px;">No questions found in this category.</p>';
        return;
    }
    
    container.innerHTML = filteredData.map((item) => `
        <div class="answer-review ${item.status}" onclick="toggleReview(this)">
            <div class="review-header-row">
                <span class="review-question">
                    Question ${item.questionNum}
                    <i class='bx bx-chevron-down expand-icon'></i>
                </span>
                <span class="review-result ${item.status}">
                    ${item.status === 'correct' ? '✓ Correct' : 
                      item.status === 'incorrect' ? '✗ Incorrect' : '○ Unanswered'}
                </span>
            </div>
            <div class="review-content">
                <p><strong>Question:</strong> ${item.question}</p>
                <p><strong>Your Answer:</strong> ${item.userAnswer}</p>
                ${item.status !== 'correct' ? 
                  `<p><strong>Correct Answer:</strong> ${item.correctAnswer}</p>` : ''}
            </div>
        </div>
    `).join('');
}

function toggleReview(element) {
    element.classList.toggle('expanded');
}

function filterReview(filter) {
    currentFilter = filter;
    
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    renderAnswerReview(allReviewData);
}

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
    const contentWidth = pageWidth - (margin * 2);
    let yPos = 0;

    // Get data from DOM
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

    // ===== HEADER FUNCTION =====
    function addPageHeader() {
        // Purple gradient header bar
        doc.setFillColor(102, 126, 234);
        doc.rect(0, 0, pageWidth, 45, 'F');
        
        // Company name
        doc.setTextColor(255, 255, 255);
        doc.setFontSize(22);
        doc.setFont('helvetica', 'bold');
        doc.text('TechnoKraft', margin, 18);
        
        doc.setFontSize(8);
        doc.setFont('helvetica', 'normal');
        doc.text('Training & Solution Pvt. Ltd.', margin, 24);
        
        // Report title on right
        doc.setFontSize(11);
        doc.setFont('helvetica', 'bold');
        doc.text('ASSESSMENT REPORT', pageWidth - margin, 16, { align: 'right' });
        
        doc.setFontSize(7);
        doc.setFont('helvetica', 'normal');
        doc.text(testDate, pageWidth - margin, 22, { align: 'right' });
        
        // White separator line
        doc.setDrawColor(255, 255, 255);
        doc.setLineWidth(0.5);
        doc.line(margin, 38, pageWidth - margin, 38);
        
        return 55;
    }

    // ===== PAGE 1: SUMMARY =====
    yPos = addPageHeader();

    // Report ID Section
    doc.setFillColor(248, 250, 252);
    doc.roundedRect(margin, yPos, contentWidth, 14, 2, 2, 'F');
    
    doc.setTextColor(102, 126, 234);
    doc.setFontSize(8);
    doc.setFont('helvetica', 'bold');
    doc.text('REPORT ID: ', margin + 4, yPos + 6);
    
    doc.setTextColor(30, 41, 59);
    doc.setFontSize(9);
    doc.text(assessmentId, margin + 28, yPos + 6);
    
    doc.setTextColor(102, 126, 234);
    doc.text('ASSESSMENT: ', margin + 4, yPos + 11);
    
    doc.setTextColor(30, 41, 59);
    doc.text('Java Programming Final Test', margin + 34, yPos + 11);
    
    yPos += 22;

    // Main Score Card
    doc.setFillColor(255, 255, 255);
    doc.setDrawColor(226, 232, 240);
    doc.setLineWidth(0.5);
    doc.roundedRect(margin, yPos, contentWidth, 50, 3, 3, 'FD');
    
    // Score circle (simplified as rectangle with large text)
    const scoreBoxX = margin + 15;
    const scoreBoxY = yPos + 10;
    const scoreBoxSize = 30;
    
    doc.setFillColor(102, 126, 234);
    doc.circle(scoreBoxX + scoreBoxSize/2, scoreBoxY + scoreBoxSize/2, scoreBoxSize/2, 'F');
    
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.text(`${score}%`, scoreBoxX + scoreBoxSize/2, scoreBoxY + scoreBoxSize/2 + 3, { align: 'center' });
    
    // Score labels
    doc.setTextColor(100, 116, 139);
    doc.setFontSize(8);
    doc.setFont('helvetica', 'normal');
    doc.text('Overall Score', scoreBoxX + scoreBoxSize/2, scoreBoxY + scoreBoxSize + 5, { align: 'center' });
    
    // Status badge
    const badgeX = margin + 65;
    const badgeY = yPos + 18;
    
    if (isPassed) {
        doc.setFillColor(220, 252, 231);
        doc.setTextColor(5, 150, 105);
    } else {
        doc.setFillColor(254, 226, 226);
        doc.setTextColor(220, 38, 38);
    }
    
    doc.roundedRect(badgeX, badgeY, 40, 10, 2, 2, 'F');
    doc.setFontSize(9);
    doc.setFont('helvetica', 'bold');
    doc.text(isPassed ? 'PASSED' : 'FAILED', badgeX + 20, badgeY + 7, { align: 'center' });
    
    // Grade
    doc.setTextColor(30, 41, 59);
    doc.setFontSize(10);
    doc.text('Grade: ', badgeX + 50, badgeY + 2);
    
    doc.setFillColor(102, 126, 234);
    doc.roundedRect(badgeX + 64, badgeY - 2, 16, 10, 2, 2, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(11);
    doc.setFont('helvetica', 'bold');
    doc.text(grade, badgeX + 72, badgeY + 5, { align: 'center' });
    
    // Marks obtained
    doc.setTextColor(71, 85, 105);
    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    doc.text('Marks Obtained:', badgeX, badgeY + 15);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(30, 41, 59);
    doc.text(`${marksObtained} / ${totalMarks}`, badgeX + 35, badgeY + 15);
    
    yPos += 58;

    // Statistics Row
    doc.setFontSize(10);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(30, 41, 59);
    doc.text('PERFORMANCE BREAKDOWN', margin, yPos);
    
    doc.setDrawColor(102, 126, 234);
    doc.setLineWidth(1);
    doc.line(margin, yPos + 1.5, margin + 58, yPos + 1.5);
    
    yPos += 10;

    const statWidth = (contentWidth - 6) / 3;
    const statHeight = 24;
    
    // Correct
    doc.setFillColor(240, 253, 244);
    doc.setDrawColor(187, 247, 208);
    doc.setLineWidth(0.5);
    doc.roundedRect(margin, yPos, statWidth, statHeight, 2, 2, 'FD');
    
    doc.setTextColor(5, 150, 105);
    doc.setFontSize(7);
    doc.setFont('helvetica', 'bold');
    doc.text('CORRECT', margin + statWidth/2, yPos + 6, { align: 'center' });
    
    doc.setFontSize(16);
    doc.text(correct, margin + statWidth/2, yPos + 16, { align: 'center' });
    
    // Wrong
    doc.setFillColor(254, 242, 242);
    doc.setDrawColor(252, 165, 165);
    doc.roundedRect(margin + statWidth + 3, yPos, statWidth, statHeight, 2, 2, 'FD');
    
    doc.setTextColor(220, 38, 38);
    doc.setFontSize(7);
    doc.text('WRONG', margin + statWidth + 3 + statWidth/2, yPos + 6, { align: 'center' });
    
    doc.setFontSize(16);
    doc.text(wrong, margin + statWidth + 3 + statWidth/2, yPos + 16, { align: 'center' });
    
    // Unanswered
    doc.setFillColor(255, 251, 235);
    doc.setDrawColor(253, 224, 71);
    doc.roundedRect(margin + (statWidth + 3) * 2, yPos, statWidth, statHeight, 2, 2, 'FD');
    
    doc.setTextColor(217, 119, 6);
    doc.setFontSize(7);
    doc.text('UNANSWERED', margin + (statWidth + 3) * 2 + statWidth/2, yPos + 6, { align: 'center' });
    
    doc.setFontSize(16);
    doc.text(unanswered, margin + (statWidth + 3) * 2 + statWidth/2, yPos + 16, { align: 'center' });
    
    yPos += 32;

    // Time Taken
    doc.setFillColor(248, 250, 252);
    doc.roundedRect(margin, yPos, contentWidth, 12, 2, 2, 'F');
    
    doc.setTextColor(71, 85, 105);
    doc.setFontSize(8);
    doc.setFont('helvetica', 'bold');
    doc.text('TIME TAKEN: ', margin + 4, yPos + 5);
    
    doc.setTextColor(30, 41, 59);
    doc.setFontSize(9);
    doc.text(timeTaken, margin + 28, yPos + 5);
    
    doc.setTextColor(71, 85, 105);
    doc.text('DURATION: ', margin + 50, yPos + 5);
    
    doc.setTextColor(30, 41, 59);
    doc.text('30:00', margin + 70, yPos + 5);
    
    doc.setTextColor(71, 85, 105);
    doc.text('PASSING SCORE: ', margin + 90, yPos + 5);
    
    doc.setTextColor(30, 41, 59);
    doc.text('35%', margin + 116, yPos + 5);
    
    yPos += 20;

    // ===== PAGE 2+: DETAILED ANALYSIS =====
    doc.addPage();
    yPos = addPageHeader();
    
    doc.setFontSize(11);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(30, 41, 59);
    doc.text('DETAILED QUESTION ANALYSIS', margin, yPos);
    
    doc.setDrawColor(102, 126, 234);
    doc.setLineWidth(1);
    doc.line(margin, yPos + 1.5, margin + 68, yPos + 1.5);
    
    yPos += 10;

    allReviewData.forEach((item, index) => {
        const minHeight = 40;
        
        // Check if we need a new page
        if (yPos > pageHeight - 60) {
            doc.addPage();
            yPos = addPageHeader();
        }

        // Question box
        let boxColor, borderColor;
        if (item.status === 'correct') {
            boxColor = [240, 253, 244];
            borderColor = [134, 239, 172];
        } else if (item.status === 'incorrect') {
            boxColor = [254, 242, 242];
            borderColor = [252, 165, 165];
        } else {
            boxColor = [255, 251, 235];
            borderColor = [253, 224, 71];
        }
        
        doc.setFillColor(...boxColor);
        doc.setDrawColor(...borderColor);
        doc.setLineWidth(0.8);
        doc.roundedRect(margin, yPos, contentWidth, minHeight, 2, 2, 'FD');
        
        // Question number and status
        doc.setFont('helvetica', 'bold');
        doc.setFontSize(9);
        doc.setTextColor(30, 41, 59);
        doc.text(`Q${item.questionNum}`, margin + 3, yPos + 6);
        
        // Status icon
        const statusIcon = item.status === 'correct' ? '✓' : item.status === 'incorrect' ? '✗' : '○';
        if (item.status === 'correct') {
            doc.setTextColor(5, 150, 105);
        } else if (item.status === 'incorrect') {
            doc.setTextColor(220, 38, 38);
        } else {
            doc.setTextColor(217, 119, 6);
        }
        doc.setFontSize(11);
        doc.text(statusIcon, pageWidth - margin - 4, yPos + 6);
        
        // Question text
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(8);
        doc.setTextColor(51, 65, 85);
        const questionLines = doc.splitTextToSize(item.question, contentWidth - 10);
        doc.text(questionLines.slice(0, 2), margin + 3, yPos + 12);
        
        // Your Answer
        doc.setFont('helvetica', 'bold');
        doc.setFontSize(7);
        doc.setTextColor(71, 85, 105);
        doc.text('Your Answer:', margin + 3, yPos + 24);
        
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(7);
        doc.setTextColor(30, 41, 59);
        const userAnswerText = doc.splitTextToSize(item.userAnswer, contentWidth - 32);
        doc.text(userAnswerText[0], margin + 24, yPos + 24);
        
        // Correct Answer (if wrong or unanswered)
        if (item.status !== 'correct') {
            doc.setFont('helvetica', 'bold');
            doc.setFontSize(7);
            doc.setTextColor(5, 150, 105);
            doc.text('Correct Answer:', margin + 3, yPos + 31);
            
            doc.setFont('helvetica', 'normal');
            doc.setFontSize(7);
            doc.setTextColor(30, 41, 59);
            const correctAnswerText = doc.splitTextToSize(item.correctAnswer, contentWidth - 38);
            doc.text(correctAnswerText[0], margin + 30, yPos + 31);
        }
        
        yPos += minHeight + 3;
    });

    // ===== ADD FOOTER TO ALL PAGES =====
    const totalPages = doc.internal.getNumberOfPages();
    for (let i = 1; i <= totalPages; i++) {
        doc.setPage(i);
        
        // Footer line
        doc.setDrawColor(226, 232, 240);
        doc.setLineWidth(0.3);
        doc.line(margin, pageHeight - 15, pageWidth - margin, pageHeight - 15);
        
        // Footer text
        doc.setFontSize(7);
        doc.setTextColor(148, 163, 184);
        doc.setFont('helvetica', 'normal');
        doc.text('TechnoKraft Online Assessment Platform', margin, pageHeight - 10);
        doc.text(`Page ${i} of ${totalPages}`, pageWidth / 2, pageHeight - 10, { align: 'center' });
        doc.text(`ID: ${assessmentId}`, pageWidth - margin, pageHeight - 10, { align: 'right' });
    }

    // Save the PDF
    const fileName = `TechnoKraft_Assessment_${assessmentId}_${new Date().toISOString().split('T')[0]}.pdf`;
    doc.save(fileName);
    
    alert('✓ PDF Report downloaded successfully!\n\nFilename: ' + fileName);
}

function emailReport() {
    const email = prompt('Enter your email address to receive the test report:');
    if (email && email.includes('@')) {
        const reportData = {
            email: email,
            assessmentId: document.getElementById('assessmentId').textContent,
            testDate: document.getElementById('testDate').textContent,
            score: document.getElementById('scoreValue').textContent,
            marksObtained: document.getElementById('marksObtained').textContent,
            totalMarks: document.getElementById('totalMarks').textContent,
            grade: document.getElementById('gradeBadge').textContent,
            verdict: document.getElementById('verdict').textContent.trim(),
            correct: document.getElementById('correctCount').textContent,
            wrong: document.getElementById('wrongCount').textContent,
            unanswered: document.getElementById('unansweredCount').textContent,
            timeTaken: document.getElementById('timeTaken').textContent,
            reviewData: allReviewData
        };
        
        // For future backend integration:
        // fetch('/api/send-report', {
        //     method: 'POST',
        //     headers: { 'Content-Type': 'application/json' },
        //     body: JSON.stringify(reportData)
        // });
        
        alert(`✓ Email Scheduled!\n\nYour detailed assessment report will be sent to:\n${email}\n\nYou will receive:\n• Complete Performance Summary\n• Detailed Answer Analysis\n• Grade Certificate\n• Recommendations for Improvement\n\nPlease check your inbox in 5-10 minutes.\n(Note: This is a demo - Email integration requires backend setup)`);
    } else if (email) {
        alert('⚠ Please enter a valid email address.');
    }
}

function backToDashboard() {
    if (confirm('Are you sure you want to return to dashboard?')) {
        location.reload();
    }
}

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