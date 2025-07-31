// Enhanced Customer Satisfaction Survey Dashboard JavaScript

class SurveyDashboard {
    constructor() {
        this.surveyData = [];
        this.filteredData = [];
        this.charts = {};
        this.currentSection = 'overview';
        this.init();
    }

    // Initialize the dashboard
    init() {
        this.setupEventListeners();
        this.setupNavigation();
        this.setupThemeToggle();
        this.setupSidebar();
        this.loadSampleData();
        this.renderDashboard();
        this.updateQuickStats();
        this.setupActivityFeed();
    }

    // Setup event listeners
    setupEventListeners() {
        // File upload
        const fileInput = document.getElementById('excel-upload');
        const uploadBtn = document.getElementById('nav-upload-btn');
        const refreshBtn = document.getElementById('refresh-btn');
        const modal = document.getElementById('upload-modal');
        const closeBtn = document.querySelector('.close');
        const uploadArea = document.getElementById('upload-area');
        const uploadLink = document.querySelector('.upload-link');

        // Upload modal controls
        uploadBtn?.addEventListener('click', () => {
            modal.style.display = 'block';
        });

        uploadLink?.addEventListener('click', () => {
            fileInput.click();
        });

        closeBtn?.addEventListener('click', () => {
            modal.style.display = 'none';
        });

        window.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.style.display = 'none';
            }
        });

        // Drag and drop functionality
        uploadArea?.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadArea.classList.add('dragover');
        });

        uploadArea?.addEventListener('dragleave', () => {
            uploadArea.classList.remove('dragover');
        });

        uploadArea?.addEventListener('drop', (e) => {
            e.preventDefault();
            uploadArea.classList.remove('dragover');
            const files = e.dataTransfer.files;
            if (files.length > 0) {
                this.handleFileUpload({ target: { files: files } });
            }
        });

        fileInput?.addEventListener('change', (e) => this.handleFileUpload(e));

        // Filter controls
        document.getElementById('date-filter')?.addEventListener('change', () => this.applyFilters());
        document.getElementById('rating-filter')?.addEventListener('change', () => this.applyFilters());
        
        // Filter action buttons
        document.getElementById('apply-filters')?.addEventListener('click', () => this.applyFilters());
        document.getElementById('reset-filters')?.addEventListener('click', () => this.resetFilters());

        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => {
            if (e.ctrlKey || e.metaKey) {
                switch (e.key) {
                    case '1':
                        e.preventDefault();
                        this.navigateToSection('overview');
                        break;
                    case '2':
                        e.preventDefault();
                        this.navigateToSection('analytics');
                        break;
                    case '3':
                        e.preventDefault();
                        this.navigateToSection('insights');
                        break;
                    case '4':
                        e.preventDefault();
                        this.navigateToSection('data');
                        break;
                    case 'r':
                        e.preventDefault();
                        this.refreshDashboard();
                        break;
                }
            }
        });
    }

    // Setup navigation
    setupNavigation() {
        const navLinks = document.querySelectorAll('.nav-link');
        const hamburger = document.getElementById('hamburger');
        const navMenu = document.getElementById('nav-menu');

        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const section = link.getAttribute('data-section');
                if (section) {
                    this.navigateToSection(section);
                }
            });
        });

        // Mobile hamburger menu
        hamburger?.addEventListener('click', () => {
            navMenu?.classList.toggle('active');
        });
    }

    // Navigate to specific section
    navigateToSection(sectionName) {
        // Update navigation links
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('data-section') === sectionName) {
                link.classList.add('active');
            }
        });

        // Update content sections
        document.querySelectorAll('.content-section').forEach(section => {
            section.classList.remove('active');
        });

        const targetSection = document.getElementById(sectionName);
        if (targetSection) {
            targetSection.classList.add('active');
            this.currentSection = sectionName;
            
            // Trigger specific updates for each section
            this.onSectionChange(sectionName);
        }
    }

    // Handle section-specific updates
    onSectionChange(sectionName) {
        switch (sectionName) {
            case 'overview':
                this.updateActivityFeed();
                break;
            case 'analytics':
                this.renderCharts();
                break;
            case 'insights':
                this.generateInsights();
                break;
            case 'data':
                this.updatePerformanceTable();
                this.updateComments();
                break;
        }
    }

    // Setup theme toggle
    setupThemeToggle() {
        const themeToggle = document.getElementById('theme-toggle');
        const currentTheme = localStorage.getItem('theme') || 'light';
        
        document.documentElement.setAttribute('data-theme', currentTheme);
        this.updateThemeIcon(currentTheme);

        themeToggle?.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme');
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
            this.updateThemeIcon(newTheme);
        });
    }

    // Update theme icon
    updateThemeIcon(theme) {
        const themeToggle = document.getElementById('theme-toggle');
        const icon = themeToggle?.querySelector('i');
        if (icon) {
            icon.className = theme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
        }
    }

    // Setup sidebar
    setupSidebar() {
        const hamburger = document.getElementById('hamburger');
        const sidebar = document.getElementById('sidebar');

        hamburger?.addEventListener('click', () => {
            sidebar?.classList.toggle('open');
        });

        // Close sidebar when clicking outside on mobile
        document.addEventListener('click', (e) => {
            if (window.innerWidth <= 768) {
                if (!sidebar?.contains(e.target) && !hamburger?.contains(e.target)) {
                    sidebar?.classList.remove('open');
                }
            }
        });
    }

    // Handle Excel file upload
    handleFileUpload(event) {
        const file = event.target.files[0];
        if (!file) return;

        // Show loading overlay
        this.showLoadingOverlay('Processing your file...');

        const statusDiv = document.getElementById('upload-status');
        statusDiv.style.display = 'block';
        statusDiv.className = 'upload-status';
        statusDiv.textContent = 'Processing file...';

        const reader = new FileReader();
        reader.onload = (e) => {
            try {
                const data = new Uint8Array(e.target.result);
                const workbook = XLSX.read(data, { type: 'array' });
                const firstSheetName = workbook.SheetNames[0];
                const worksheet = workbook.Sheets[firstSheetName];
                const jsonData = XLSX.utils.sheet_to_json(worksheet);

                this.processExcelData(jsonData);
                statusDiv.className = 'upload-status success';
                statusDiv.innerHTML = `
                    <i class="fas fa-check-circle"></i>
                    Successfully loaded ${jsonData.length} survey responses!
                `;
                
                setTimeout(() => {
                    document.getElementById('upload-modal').style.display = 'none';
                    this.hideLoadingOverlay();
                }, 2000);

            } catch (error) {
                statusDiv.className = 'upload-status error';
                statusDiv.innerHTML = `
                    <i class="fas fa-exclamation-triangle"></i>
                    Error processing file: ${error.message}
                `;
                this.hideLoadingOverlay();
            }
        };

        reader.readAsArrayBuffer(file);
    }

    // Show loading overlay
    showLoadingOverlay(message = 'Loading...') {
        const overlay = document.getElementById('loading-overlay');
        const messageEl = overlay?.querySelector('p');
        if (messageEl) messageEl.textContent = message;
        if (overlay) overlay.style.display = 'flex';
    }

    // Hide loading overlay
    hideLoadingOverlay() {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) overlay.style.display = 'none';
    }

    // Process Excel data
    processExcelData(rawData) {
        this.showLoadingOverlay('Processing survey data...');
        
        setTimeout(() => {
            this.surveyData = rawData.map((row, index) => {
                return {
                    id: index + 1,
                    date: this.parseDate(row['Date'] || row['Timestamp'] || new Date()),
                    customerId: row['Customer ID'] || `CUST${index + 1}`,
                    foodQuality: this.parseRating(row['Food Quality']),
                    serviceQuality: this.parseRating(row['Service Quality']),
                    hygiene: this.parseRating(row['Hygiene']),
                    valueForMoney: this.parseRating(row['Value for Money']),
                    overallSatisfaction: this.parseOverallRating(row['Overall Satisfaction']),
                    comments: row['Comments'] || '',
                    npsScore: this.parseRating(row['NPS Score']) || this.calculateNPS(row)
                };
            });

            this.filteredData = [...this.surveyData];
            this.renderDashboard();
            this.updateQuickStats();
            this.updateActivityFeed();
            this.hideLoadingOverlay();
            
            this.showNotification(`Successfully loaded ${this.surveyData.length} survey responses!`, 'success');
        }, 1000);
    }

    // Load sample data for demonstration
    loadSampleData() {
        this.surveyData = this.generateSampleData(150);
        this.filteredData = [...this.surveyData];
    }

    // Generate sample survey data
    generateSampleData(count) {
        const satisfactionLevels = ['Highly Satisfied', 'Satisfied', 'Neutral', 'Dissatisfied', 'Highly Dissatisfied'];
        const comments = [
            'Outstanding food quality and excellent service! Will definitely return.',
            'Good value for money and clean environment.',
            'Service was prompt and staff was very friendly.',
            'Food was delicious but the portions could be larger.',
            'Average experience overall, nothing special.',
            'Great atmosphere and the food exceeded expectations.',
            'Service was slow but the food quality made up for it.',
            'Hygiene standards were excellent, very impressed.',
            'A bit expensive but worth it for the quality.',
            'Perfect dining experience for a special occasion!',
            'Food quality needs improvement, disappointed.',
            'Excellent customer service and attention to detail.',
            'Clean facility and well-maintained dining area.',
            'Value for money is questionable given the portion sizes.',
            'Staff went above and beyond to accommodate our needs.',
            'Food was cold when served, not acceptable.',
            'Best restaurant experience in a long time!',
            'Good selection of menu items and reasonable prices.',
            'Service quality varies depending on the time of day.',
            'Impressed with the hygiene protocols followed.'
        ];

        const data = [];
        for (let i = 0; i < count; i++) {
            const date = new Date();
            date.setDate(date.getDate() - Math.floor(Math.random() * 365));

            const overallIndex = Math.floor(Math.random() * satisfactionLevels.length);
            const overall = satisfactionLevels[overallIndex];
            
            // Generate ratings that correlate with overall satisfaction
            const baseRating = 5 - overallIndex; // Higher satisfaction = higher base rating
            const variation = () => Math.max(1, Math.min(5, baseRating + Math.floor(Math.random() * 3) - 1));

            data.push({
                id: i + 1,
                date: date,
                customerId: `CUST${String(i + 1).padStart(3, '0')}`,
                foodQuality: variation(),
                serviceQuality: variation(),
                hygiene: variation(),
                valueForMoney: variation(),
                overallSatisfaction: overall,
                comments: Math.random() > 0.4 ? comments[Math.floor(Math.random() * comments.length)] : '',
                npsScore: Math.max(0, Math.min(10, baseRating * 2 + Math.floor(Math.random() * 3) - 1))
            });
        }

        return data.sort((a, b) => b.date - a.date);
    }

    // Helper functions (keeping the existing ones)
    randomRating() {
        const weights = [1, 2, 3, 4, 4, 5, 5, 5];
        return weights[Math.floor(Math.random() * weights.length)];
    }

    parseDate(dateValue) {
        if (dateValue instanceof Date) return dateValue;
        if (typeof dateValue === 'number') {
            return new Date((dateValue - 25569) * 86400 * 1000);
        }
        return new Date(dateValue);
    }

    parseRating(value) {
        const num = parseInt(value);
        return isNaN(num) ? 3 : Math.max(1, Math.min(5, num));
    }

    parseOverallRating(value) {
        if (!value) return 'Neutral';
        const normalized = value.toString().toLowerCase();
        if (normalized.includes('highly satisfied') || normalized.includes('excellent')) return 'Highly Satisfied';
        if (normalized.includes('satisfied') || normalized.includes('good')) return 'Satisfied';
        if (normalized.includes('dissatisfied') || normalized.includes('poor')) return 'Dissatisfied';
        if (normalized.includes('highly dissatisfied') || normalized.includes('terrible')) return 'Highly Dissatisfied';
        return 'Neutral';
    }

    calculateNPS(row) {
        const satisfaction = this.parseOverallRating(row['Overall Satisfaction']);
        switch (satisfaction) {
            case 'Highly Satisfied': return Math.floor(Math.random() * 2) + 9;
            case 'Satisfied': return Math.floor(Math.random() * 2) + 7;
            case 'Neutral': return Math.floor(Math.random() * 3) + 5;
            case 'Dissatisfied': return Math.floor(Math.random() * 3) + 2;
            case 'Highly Dissatisfied': return Math.floor(Math.random() * 2);
            default: return 5;
        }
    }

    // Apply filters
    applyFilters() {
        const dateFilter = document.getElementById('date-filter')?.value || 'all';
        const ratingFilter = document.getElementById('rating-filter')?.value || 'all';

        let filtered = [...this.surveyData];

        if (dateFilter !== 'all') {
            const now = new Date();
            let cutoffDate = new Date();

            switch (dateFilter) {
                case 'last-30':
                    cutoffDate.setDate(now.getDate() - 30);
                    break;
                case 'last-90':
                    cutoffDate.setDate(now.getDate() - 90);
                    break;
                case 'last-year':
                    cutoffDate.setFullYear(now.getFullYear() - 1);
                    break;
            }

            filtered = filtered.filter(item => item.date >= cutoffDate);
        }

        if (ratingFilter !== 'all') {
            const targetRating = ratingFilter.split('-').map(word => 
                word.charAt(0).toUpperCase() + word.slice(1)
            ).join(' ');
            
            filtered = filtered.filter(item => item.overallSatisfaction === targetRating);
        }

        this.filteredData = filtered;
        this.renderDashboard();
        this.updateQuickStats();
        this.showNotification('Filters applied successfully!', 'info');
    }

    // Reset filters
    resetFilters() {
        document.getElementById('date-filter').value = 'all';
        document.getElementById('rating-filter').value = 'all';
        this.filteredData = [...this.surveyData];
        this.renderDashboard();
        this.updateQuickStats();
        this.showNotification('Filters reset!', 'info');
    }

    // Update quick stats in sidebar
    updateQuickStats() {
        const lastUpdated = document.getElementById('last-updated');
        const dataPoints = document.getElementById('data-points');
        const avgRating = document.getElementById('avg-rating');

        if (lastUpdated) lastUpdated.textContent = 'Just now';
        if (dataPoints) dataPoints.textContent = this.filteredData.length.toLocaleString();
        
        if (avgRating && this.filteredData.length > 0) {
            const satisfactionScores = {
                'Highly Satisfied': 5,
                'Satisfied': 4,
                'Neutral': 3,
                'Dissatisfied': 2,
                'Highly Dissatisfied': 1
            };

            const avg = this.filteredData.reduce((sum, item) => 
                sum + satisfactionScores[item.overallSatisfaction], 0) / this.filteredData.length;
            
            avgRating.textContent = avg.toFixed(1);
        }
    }

    // Setup activity feed
    setupActivityFeed() {
        this.updateActivityFeed();
        // Update activity feed every 30 seconds
        setInterval(() => this.updateActivityFeed(), 30000);
    }

    // Update activity feed
    updateActivityFeed() {
        const container = document.getElementById('activity-feed');
        if (!container) return;

        const activities = [
            { icon: 'fas fa-upload', title: 'New survey data uploaded', time: '2 minutes ago' },
            { icon: 'fas fa-chart-line', title: 'Satisfaction rate increased by 5%', time: '15 minutes ago' },
            { icon: 'fas fa-comment', title: '3 new customer comments received', time: '32 minutes ago' },
            { icon: 'fas fa-star', title: 'Food quality rating improved', time: '1 hour ago' },
            { icon: 'fas fa-bell', title: 'Weekly analytics report generated', time: '2 hours ago' }
        ];

        container.innerHTML = activities.map(activity => `
            <div class="activity-item">
                <div class="activity-icon">
                    <i class="${activity.icon}"></i>
                </div>
                <div class="activity-content">
                    <div class="activity-title">${activity.title}</div>
                    <div class="activity-time">${activity.time}</div>
                </div>
            </div>
        `).join('');
    }

    // Render the entire dashboard
    renderDashboard() {
        this.updateMetrics();
        if (this.currentSection === 'analytics') {
            this.renderCharts();
        }
        if (this.currentSection === 'data') {
            this.updatePerformanceTable();
            this.updateComments();
        }
        if (this.currentSection === 'insights') {
            this.generateInsights();
        }
    }

    // Update key metrics (keeping existing implementation but updating IDs)
    updateMetrics() {
        const data = this.filteredData;
        
        const satisfactionScores = {
            'Highly Satisfied': 5,
            'Satisfied': 4,
            'Neutral': 3,
            'Dissatisfied': 2,
            'Highly Dissatisfied': 1
        };

        const avgSatisfaction = data.reduce((sum, item) => 
            sum + satisfactionScores[item.overallSatisfaction], 0) / data.length;

        const overallSatEl = document.getElementById('overall-satisfaction');
        if (overallSatEl) overallSatEl.textContent = avgSatisfaction.toFixed(1) + '/5';

        const totalResponsesEl = document.getElementById('total-responses');
        if (totalResponsesEl) totalResponsesEl.textContent = data.length.toLocaleString();

        const satisfiedCount = data.filter(item => 
            item.overallSatisfaction === 'Satisfied' || item.overallSatisfaction === 'Highly Satisfied'
        ).length;
        const satisfactionRate = (satisfiedCount / data.length * 100).toFixed(1);
        const satisfactionRateEl = document.getElementById('satisfaction-rate');
        if (satisfactionRateEl) satisfactionRateEl.textContent = satisfactionRate + '%';

        const npsData = data.filter(item => item.npsScore !== undefined);
        if (npsData.length > 0) {
            const promoters = npsData.filter(item => item.npsScore >= 9).length;
            const detractors = npsData.filter(item => item.npsScore <= 6).length;
            const nps = ((promoters - detractors) / npsData.length * 100).toFixed(0);
            const npsEl = document.getElementById('nps-score');
            if (npsEl) npsEl.textContent = nps;
        }

        // Update trends
        const trendElement = document.getElementById('overall-trend');
        if (trendElement) {
            if (avgSatisfaction >= 4) {
                trendElement.innerHTML = '<i class="fas fa-arrow-up"></i> Excellent';
                trendElement.style.color = '#48bb78';
            } else if (avgSatisfaction >= 3.5) {
                trendElement.innerHTML = '<i class="fas fa-arrow-right"></i> Good';
                trendElement.style.color = '#ed8936';
            } else {
                trendElement.innerHTML = '<i class="fas fa-arrow-down"></i> Needs Improvement';
                trendElement.style.color = '#f56565';
            }
        }
    }

    // Keep all existing chart rendering methods but update for new structure
    renderCharts() {
        this.renderSatisfactionPieChart();
        this.renderAttributesBarChart();
        this.renderTrendsLineChart();
        this.renderRadarChart();
    }

    // Render satisfaction pie chart (keeping existing implementation)
    renderSatisfactionPieChart() {
        const ctx = document.getElementById('satisfaction-pie-chart')?.getContext('2d');
        if (!ctx) return;
        
        if (this.charts.pieChart) {
            this.charts.pieChart.destroy();
        }

        const satisfactionCounts = this.filteredData.reduce((acc, item) => {
            acc[item.overallSatisfaction] = (acc[item.overallSatisfaction] || 0) + 1;
            return acc;
        }, {});

        this.charts.pieChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: Object.keys(satisfactionCounts),
                datasets: [{
                    data: Object.values(satisfactionCounts),
                    backgroundColor: [
                        '#48bb78', '#4299e1', '#ed8936', '#f56565', '#9c27b0'
                    ],
                    borderWidth: 3,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true
                        }
                    }
                }
            }
        });
    }

    // Keep all other existing methods for charts, tables, comments, insights...
    // (I'll include the key ones that need updates for the new structure)

    // Render attributes bar chart
    renderAttributesBarChart() {
        const ctx = document.getElementById('attributes-bar-chart')?.getContext('2d');
        if (!ctx) return;
        
        if (this.charts.barChart) {
            this.charts.barChart.destroy();
        }

        const attributes = ['foodQuality', 'serviceQuality', 'hygiene', 'valueForMoney'];
        const attributeLabels = ['Food Quality', 'Service Quality', 'Hygiene', 'Value for Money'];
        
        const averages = attributes.map(attr => {
            const sum = this.filteredData.reduce((total, item) => total + item[attr], 0);
            return sum / this.filteredData.length;
        });

        this.charts.barChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: attributeLabels,
                datasets: [{
                    label: 'Average Rating',
                    data: averages,
                    backgroundColor: [
                        '#667eea', '#764ba2', '#48bb78', '#ed8936'
                    ],
                    borderColor: [
                        '#667eea', '#764ba2', '#48bb78', '#ed8936'
                    ],
                    borderWidth: 2,
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 5,
                        ticks: {
                            stepSize: 1
                        },
                        grid: {
                            color: 'rgba(0,0,0,0.1)'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
    }

    // Continue with other existing methods but update selectors for new structure...
    // (Include all other existing methods with minimal changes for new DOM structure)

    // Render trends line chart
    renderTrendsLineChart() {
        const ctx = document.getElementById('trends-line-chart')?.getContext('2d');
        if (!ctx) return;
        
        if (this.charts.lineChart) {
            this.charts.lineChart.destroy();
        }

        const monthlyData = this.filteredData.reduce((acc, item) => {
            const monthKey = `${item.date.getFullYear()}-${String(item.date.getMonth() + 1).padStart(2, '0')}`;
            if (!acc[monthKey]) {
                acc[monthKey] = [];
            }
            acc[monthKey].push(item);
            return acc;
        }, {});

        const sortedMonths = Object.keys(monthlyData).sort();
        const satisfactionScores = {
            'Highly Satisfied': 5,
            'Satisfied': 4,
            'Neutral': 3,
            'Dissatisfied': 2,
            'Highly Dissatisfied': 1
        };

        const monthlyAverages = sortedMonths.map(month => {
            const monthData = monthlyData[month];
            const avg = monthData.reduce((sum, item) => 
                sum + satisfactionScores[item.overallSatisfaction], 0) / monthData.length;
            return avg;
        });

        this.charts.lineChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: sortedMonths.map(month => {
                    const [year, monthNum] = month.split('-');
                    return new Date(year, monthNum - 1).toLocaleDateString('en-US', { year: 'numeric', month: 'short' });
                }),
                datasets: [{
                    label: 'Average Satisfaction',
                    data: monthlyAverages,
                    borderColor: '#667eea',
                    backgroundColor: 'rgba(102, 126, 234, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointBackgroundColor: '#667eea',
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2,
                    pointRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 5,
                        ticks: {
                            stepSize: 0.5
                        },
                        grid: {
                            color: 'rgba(0,0,0,0.1)'
                        }
                    },
                    x: {
                        grid: {
                            color: 'rgba(0,0,0,0.1)'
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    }
                }
            }
        });
    }

    // Render radar chart
    renderRadarChart() {
        const ctx = document.getElementById('radar-chart')?.getContext('2d');
        if (!ctx) return;
        
        if (this.charts.radarChart) {
            this.charts.radarChart.destroy();
        }

        const attributes = ['foodQuality', 'serviceQuality', 'hygiene', 'valueForMoney'];
        const attributeLabels = ['Food Quality', 'Service Quality', 'Hygiene', 'Value for Money'];
        
        const averages = attributes.map(attr => {
            const sum = this.filteredData.reduce((total, item) => total + item[attr], 0);
            return sum / this.filteredData.length;
        });

        this.charts.radarChart = new Chart(ctx, {
            type: 'radar',
            data: {
                labels: attributeLabels,
                datasets: [{
                    label: 'Current Performance',
                    data: averages,
                    borderColor: '#667eea',
                    backgroundColor: 'rgba(102, 126, 234, 0.2)',
                    pointBackgroundColor: '#667eea',
                    pointBorderColor: '#fff',
                    pointHoverBackgroundColor: '#fff',
                    pointHoverBorderColor: '#667eea',
                    borderWidth: 2,
                    pointRadius: 5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    r: {
                        beginAtZero: true,
                        max: 5,
                        ticks: {
                            stepSize: 1
                        },
                        grid: {
                            color: 'rgba(0,0,0,0.1)'
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    }
                }
            }
        });
    }

    // Update performance table (keeping existing logic)
    updatePerformanceTable() {
        const tbody = document.getElementById('performance-table-body');
        if (!tbody) return;
        
        tbody.innerHTML = '';

        const attributes = [
            { key: 'foodQuality', label: 'Food Quality' },
            { key: 'serviceQuality', label: 'Service Quality' },
            { key: 'hygiene', label: 'Hygiene' },
            { key: 'valueForMoney', label: 'Value for Money' }
        ];

        attributes.forEach(attr => {
            const values = this.filteredData.map(item => item[attr.key]);
            const average = values.reduce((sum, val) => sum + val, 0) / values.length;
            const satisfiedCount = values.filter(val => val >= 4).length;
            const satisfactionRate = (satisfiedCount / values.length * 100).toFixed(1);
            
            let priority = 'Low';
            let priorityClass = 'priority-low';
            if (average < 3.5) {
                priority = 'High';
                priorityClass = 'priority-high';
            } else if (average < 4) {
                priority = 'Medium';
                priorityClass = 'priority-medium';
            }

            const row = tbody.insertRow();
            row.innerHTML = `
                <td><strong>${attr.label}</strong></td>
                <td>${average.toFixed(2)}/5</td>
                <td>${satisfactionRate}%</td>
                <td>${values.length.toLocaleString()}</td>
                <td>
                    <i class="fas fa-arrow-${average >= 4 ? 'up' : average >= 3.5 ? 'right' : 'down'}"></i>
                    ${average >= 4 ? 'Improving' : average >= 3.5 ? 'Stable' : 'Declining'}
                </td>
                <td><span class="${priorityClass}">${priority}</span></td>
            `;
        });
    }

    // Update comments section
    updateComments() {
        const container = document.getElementById('comments-container');
        if (!container) return;
        
        container.innerHTML = '';

        const recentComments = this.filteredData
            .filter(item => item.comments)
            .sort((a, b) => b.date - a.date)
            .slice(0, 10);

        if (recentComments.length === 0) {
            container.innerHTML = '<div class="no-comments">No comments available for the current filter criteria.</div>';
            return;
        }

        recentComments.forEach(item => {
            const commentDiv = document.createElement('div');
            commentDiv.className = 'comment-item';
            
            const ratingClass = item.overallSatisfaction.toLowerCase().replace(/\s+/g, '-');
            
            commentDiv.innerHTML = `
                <div class="comment-header">
                    <span class="comment-rating rating-${ratingClass}">
                        <i class="fas fa-star"></i>
                        ${item.overallSatisfaction}
                    </span>
                    <span class="comment-date">
                        <i class="fas fa-calendar"></i>
                        ${item.date.toLocaleDateString()}
                    </span>
                </div>
                <div class="comment-text">${item.comments}</div>
            `;
            
            container.appendChild(commentDiv);
        });
    }

    // Generate insights (keeping existing logic but with enhanced presentation)
    generateInsights() {
        const data = this.filteredData;
        
        const attributes = {
            'Food Quality': data.reduce((sum, item) => sum + item.foodQuality, 0) / data.length,
            'Service Quality': data.reduce((sum, item) => sum + item.serviceQuality, 0) / data.length,
            'Hygiene': data.reduce((sum, item) => sum + item.hygiene, 0) / data.length,
            'Value for Money': data.reduce((sum, item) => sum + item.valueForMoney, 0) / data.length
        };

        // Top performing areas
        const topPerforming = Object.entries(attributes)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 3);

        const topList = document.getElementById('top-performing-list');
        if (topList) {
            topList.innerHTML = topPerforming.map(([attr, score]) => 
                `<li><i class="fas fa-trophy"></i> ${attr}: ${score.toFixed(2)}/5 - Keep up the excellent work!</li>`
            ).join('');
        }

        // Areas for improvement
        const needsImprovement = Object.entries(attributes)
            .filter(([_, score]) => score < 4)
            .sort((a, b) => a[1] - b[1]);

        const improvementList = document.getElementById('improvement-areas-list');
        if (improvementList) {
            if (needsImprovement.length > 0) {
                improvementList.innerHTML = needsImprovement.map(([attr, score]) => 
                    `<li><i class="fas fa-exclamation-triangle"></i> ${attr}: ${score.toFixed(2)}/5 - Focus area for enhancement</li>`
                ).join('');
            } else {
                improvementList.innerHTML = '<li><i class="fas fa-check-circle"></i> All areas performing well! 🎉</li>';
            }
        }

        // Recommendations
        const recommendations = this.generateRecommendations(attributes, data);
        const recommendationsList = document.getElementById('recommendations-list');
        if (recommendationsList) {
            recommendationsList.innerHTML = recommendations.map(rec => 
                `<li><i class="fas fa-lightbulb"></i> ${rec}</li>`
            ).join('');
        }
    }

    // Generate specific recommendations (keeping existing logic)
    generateRecommendations(attributes, data) {
        const recommendations = [];
        
        const satisfiedCount = data.filter(item => 
            item.overallSatisfaction === 'Satisfied' || item.overallSatisfaction === 'Highly Satisfied'
        ).length;
        const satisfactionRate = satisfiedCount / data.length;

        if (satisfactionRate < 0.7) {
            recommendations.push('Overall satisfaction is below 70% - conduct detailed customer interviews');
        }

        Object.entries(attributes).forEach(([attr, score]) => {
            if (score < 3.5) {
                switch (attr) {
                    case 'Food Quality':
                        recommendations.push('Implement food quality training programs for kitchen staff');
                        break;
                    case 'Service Quality':
                        recommendations.push('Enhance customer service training and response times');
                        break;
                    case 'Hygiene':
                        recommendations.push('Review and strengthen hygiene protocols and monitoring');
                        break;
                    case 'Value for Money':
                        recommendations.push('Analyze pricing strategy and consider menu optimization');
                        break;
                }
            }
        });

        if (recommendations.length === 0) {
            recommendations.push('Excellent performance across all metrics - focus on maintaining standards');
            recommendations.push('Consider implementing loyalty programs to enhance customer retention');
            recommendations.push('Share best practices with other locations or departments');
        }

        return recommendations;
    }

    // Refresh dashboard
    refreshDashboard() {
        this.showLoadingOverlay('Refreshing dashboard...');
        
        setTimeout(() => {
            this.applyFilters();
            this.updateActivityFeed();
            this.hideLoadingOverlay();
            this.showNotification('Dashboard refreshed successfully!', 'success');
        }, 1000);
    }

    // Show notification
    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-triangle' : 'info-circle'}"></i>
            ${message}
        `;
        notification.style.cssText = `
            position: fixed;
            top: 90px;
            right: 20px;
            padding: 1rem 1.5rem;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            z-index: 1000;
            animation: slideIn 0.3s ease;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            min-width: 300px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
            background: ${type === 'success' ? '#48bb78' : type === 'error' ? '#f56565' : '#4299e1'};
        `;

        document.body.appendChild(notification);

        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }
}

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.surveyDashboard = new SurveyDashboard();
});

// Add CSS animations for notifications
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
    
    .no-comments {
        text-align: center;
        color: var(--text-secondary);
        padding: 2rem;
        font-style: italic;
    }
`;
document.head.appendChild(style); 