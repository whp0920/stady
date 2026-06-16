const { createApp, ref, reactive, onMounted, nextTick } = Vue

createApp({
    setup() {
        // ==================== 导航 ====================
        const activeTab = ref('records')
        const recordsSub = ref('list')   // 'add' | 'list'
        const journalSub = ref('write')  // 'write' | 'history'
        const categoryStats = ref([])

        // ==================== 登录 ====================
        const loggedIn = ref(false)
        const loginForm = reactive({ username: '', password: '' })
        const USER_API = '/api/users'

        function getAuthHeaders() {
            const token = localStorage.getItem('token')
            return token ? { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' } : { 'Content-Type': 'application/json' }
        }

        async function doLogin() {
            const res = await fetch(`${USER_API}/login`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(loginForm)
            })
            const json = await res.json()
            if (json.code === 200) {
                localStorage.setItem('token', json.data)
                loggedIn.value = true
                loadRecords(); loadJournals(); loadStats()
                showToast('登录成功')
            } else {
                showToast(json.message || '登录失败', 'error')
            }
        }

        async function doRegister() {
            const res = await fetch(`${USER_API}/register`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(loginForm)
            })
            const json = await res.json()
            if (json.code === 200) {
                showToast('注册成功，请登录')
            } else {
                showToast(json.message || '注册失败', 'error')
            }
        }

        function doLogout() {
            localStorage.removeItem('token')
            loggedIn.value = false
        }

        // ==================== 学习记录 ====================
        const records = ref([])
        const editing = ref(false)
        const form = reactive({ title: '', category: 'backend', level: 'heard', duration: 30, note: '', customCategory: '' })
        const editId = ref(null)
        const stats = reactive({ total: 0, todayMinutes: 0, weekHours: 0 })
        const RECORDS_API = '/api/records'
        const recordsPage = reactive({ current: 1, size: 10, total: 0 })
        const categories = [
            { label: '后端', value: 'backend' },
            { label: '前端', value: 'frontend' },
            { label: '基础', value: 'basics' },
            { label: '其他', value: 'other' }
        ]

        // ==================== 日记 ====================
        const journalForm = reactive({ happy: '', fulfilled: '', improve: '', grateful: '', note: '', mood: '' })
        const todayJournal = ref(null)
        const journals = ref([])
        const JOURNAL_API = '/api/journal'
        const journalPage = reactive({ current: 1, size: 10, total: 0 })
        const moods = [
            { emoji: '😄', label: '开心', value: 'happy' },
            { emoji: '😐', label: '一般', value: 'neutral' },
            { emoji: '😞', label: '难过', value: 'sad' }
        ]

        // ==================== 弹窗 ====================
        const modal = reactive({ show: false, title: '', items: [] })
        function showModal(title, obj) {
            modal.title = title
            modal.items = []
            for (const [k, v] of Object.entries(obj)) {
                if (v && k !== 'id' && k !== 'createdAt' && k !== 'updatedAt') {
                    modal.items.push({ key: k, value: v })
                }
            }
            modal.show = true
        }

        // ==================== 通用 ====================
        const toast = reactive({ show: false, msg: '', type: 'success' })
        function showToast(msg, type = 'success') {
            toast.msg = msg; toast.type = type; toast.show = true
            setTimeout(() => { toast.show = false }, 2000)
        }

        // ==================== 学习记录方法 ====================

        let chartCategory = null, chartWeekly = null

        async function loadStats(){
            const res = await fetch('/api/records/stats', { headers: getAuthHeaders() })
            const json = await res.json()
            if (json.code === 200) {
                categoryStats.value = json.data
                await nextTick()
                renderCharts()
            }
        }

        function renderCharts() {
            // 分类饼图
            const ctx1 = document.getElementById('chartCategory')
            if (ctx1) {
                if (chartCategory) chartCategory.destroy()
                chartCategory = new Chart(ctx1, {
                    type: 'doughnut',
                    data: {
                        labels: categoryStats.value.map(s => catLabel(s.name)),
                        datasets: [{
                            data: categoryStats.value.map(s => s.minutes),
                            backgroundColor: ['#5470c6', '#91cc75', '#fac858', '#ee6666']
                        }]
                    },
                    options: {
                        plugins: { legend: { position: 'bottom' } },
                        cutout: '55%'
                    }
                })
            }

            // 柱状图（Mock：展示最近7天学习时长）
            const ctx2 = document.getElementById('chartWeekly')
            if (ctx2) {
                if (chartWeekly) chartWeekly.destroy()
                const days = []
                const data = []
                const now = new Date()
                for (let i = 6; i >= 0; i--) {
                    const d = new Date(now); d.setDate(d.getDate() - i)
                    const key = d.toISOString().slice(0, 10)
                    days.push(d.getMonth() + 1 + '/' + d.getDate())
                    // 从 records 计算当天时长
                    const min = records.value
                        .filter(r => r.createdAt && r.createdAt.startsWith(key))
                        .reduce((s, r) => s + (r.duration || 0), 0)
                    data.push(min)
                }
                chartWeekly = new Chart(ctx2, {
                    type: 'bar',
                    data: {
                        labels: days,
                        datasets: [{
                            label: '学习时长（分钟）',
                            data: data,
                            backgroundColor: '#5470c6'
                        }]
                    },
                    options: {
                        plugins: { legend: { display: false } },
                        scales: { y: { beginAtZero: true } }
                    }
                })
            }
        }
        async function loadRecords() {
            const res = await fetch(`${RECORDS_API}/page?page=${recordsPage.current}&size=${recordsPage.size}`, { headers: getAuthHeaders() })
            const json = await res.json()
            if (json.code === 200) {
                records.value = json.data.records
                recordsPage.total = json.data.total
                recordsPage.current = json.data.current
            }
            // 统计用全量数据
            const all = await fetch(RECORDS_API, { headers: getAuthHeaders() })
            const allJson = await all.json()
            if (allJson.code === 200) calcStats(allJson.data)
        }

        function calcStats(list) {
            stats.total = list.length
            const today = new Date().toISOString().slice(0, 10)
            stats.todayMinutes = list.filter(r => r.createdAt && r.createdAt.startsWith(today))
                .reduce((s, r) => s + (r.duration || 0), 0)
            const now = new Date()
            const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - now.getDay())
            stats.weekHours = Math.round(list.filter(r => r.createdAt && new Date(r.createdAt) >= weekStart)
                .reduce((s, r) => s + (r.duration || 0), 0) / 60 * 10) / 10
        }

        function goRecordsPage(p) {
            recordsPage.current = p
            loadRecords()
        }

        async function submitRecord() {
            const cat = form.category === 'other' && form.customCategory ? form.customCategory : form.category
            const body = { title: form.title, category: cat, level: form.level, duration: form.duration, note: form.note }
            let url = RECORDS_API, method = 'POST'
            if (editing.value) { url = `${RECORDS_API}/${editId.value}`; method = 'PUT' }
            const res = await fetch(url, { method, headers: getAuthHeaders(), body: JSON.stringify(body) })
            const json = await res.json()
            if (json.code === 200) { resetForm(); await loadRecords(); recordsSub.value = 'list'; showToast(editing.value ? '已更新' : '已添加') }
            else { showToast(json.message || '操作失败', 'error') }
        }

        function startEdit(row) { editing.value = true; editId.value = row.id; Object.assign(form, row); recordsSub.value = 'add' }
        function cancelEdit() { resetForm() }
        function resetForm() {
            editing.value = false; editId.value = null
            Object.assign(form, { title: '', category: 'backend', level: 'heard', duration: 30, note: '', customCategory: '' })
        }

        async function handleDelete(id) {
            if (!confirm('确定删除这条记录吗？')) return
            const res = await fetch(`${RECORDS_API}/${id}`, { method: 'DELETE', headers: getAuthHeaders() })
            const json = await res.json()
            if (json.code === 200) { await loadRecords(); showToast('已删除') }
            else { showToast(json.message || '删除失败', 'error') }
        }

        function catLabel(c) { return { backend: '后端', frontend: '前端', basics: '基础', other: '其他' }[c] || c }
        function levelLabel(l) { return { heard: '听说过', can_explain: '能解释', can_write: '能写' }[l] || l }

        // ==================== 日记方法 ====================
        async function loadJournals() {
            const res = await fetch(`${JOURNAL_API}/page?page=${journalPage.current}&size=${journalPage.size}`, { headers: getAuthHeaders() })
            const json = await res.json()
            if (json.code === 200) {
                journals.value = json.data.records
                journalPage.total = json.data.total
                journalPage.current = json.data.current
            }
            await loadTodayJournal()
        }

        function goJournalPage(p) {
            journalPage.current = p
            loadJournals()
        }

        async function loadTodayJournal() {
            const res = await fetch(JOURNAL_API, { headers: getAuthHeaders() })
            const json = await res.json()
            if (json.code === 200 && json.data && json.data.length > 0) {
                const today = new Date().toISOString().slice(0, 10)
                todayJournal.value = json.data.find(j => j.createdAt && j.createdAt.startsWith(today)) || null
            }
        }

        async function submitJournal() {
            const res = await fetch(JOURNAL_API, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(journalForm)
            })
            const json = await res.json()
            if (json.code === 200) {
                showToast('日记已保存')
                journalSub.value = 'history'
                await loadJournals()
            } else {
                showToast(json.message || '保存失败', 'error')
            }
        }

        async function deleteJournal(id) {
            if (!confirm('确定删除这条日记吗？')) return
            const res = await fetch(`${JOURNAL_API}/${id}`, { method: 'DELETE', headers: getAuthHeaders() })
            const json = await res.json()
            if (json.code === 200) { await loadJournals(); showToast('已删除') }
            else { showToast(json.message || '删除失败', 'error') }
        }

        function moodLabel(m) { return { happy: '😄 开心', neutral: '😐 一般', sad: '😞 难过' }[m] || '' }

        function formatDate(d) {
            if (!d) return ''
            return d.slice(0, 16).replace('T', ' ')
        }

        // ==================== 启动 ====================
        onMounted(async () => {
            const token = localStorage.getItem('token')
            if (!token) return

            // 尝试用已有 Token 加载数据
            loggedIn.value = true
            try {
                await loadRecords()
                await loadJournals()
                await loadStats()
            } catch (e) {
                // Token 过期或无效 → 退回登录
                localStorage.removeItem('token')
                loggedIn.value = false
            }
        })

        return {
            loggedIn, loginForm, doLogin, doRegister, doLogout,
            activeTab, recordsSub, journalSub, categoryStats, loadStats,
            records, form, editing, stats, recordsPage, categories,
            submitRecord, startEdit, cancelEdit, handleDelete, catLabel, levelLabel, goRecordsPage, showModal,
            journalForm, todayJournal, journals, journalPage, moods,
            submitJournal, deleteJournal, moodLabel, loadJournals, goJournalPage, formatDate,
            toast, modal
        }
    }
}).mount('#app')
