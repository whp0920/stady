const { createApp, ref, reactive, onMounted } = Vue

createApp({
    setup() {
        // ==================== 导航 ====================
        const activeTab = ref('records')
        const recordsSub = ref('list')   // 'add' | 'list'
        const journalSub = ref('write')  // 'write' | 'history'

        // ==================== 学习记录 ====================
        const records = ref([])
        const editing = ref(false)
        const form = reactive({ title: '', category: 'backend', level: 'heard', duration: 30, note: '' })
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
        async function loadRecords() {
            const res = await fetch(`${RECORDS_API}/page?page=${recordsPage.current}&size=${recordsPage.size}`)
            const json = await res.json()
            if (json.code === 200) {
                records.value = json.data.records
                recordsPage.total = json.data.total
                recordsPage.current = json.data.current
            }
            // 统计用全量数据
            const all = await fetch(RECORDS_API)
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
            const body = { title: form.title, category: form.category, level: form.level, duration: form.duration, note: form.note }
            let url = RECORDS_API, method = 'POST'
            if (editing.value) { url = `${RECORDS_API}/${editId.value}`; method = 'PUT' }
            const res = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
            const json = await res.json()
            if (json.code === 200) { resetForm(); await loadRecords(); recordsSub.value = 'list'; showToast(editing.value ? '已更新' : '已添加') }
            else { showToast(json.message || '操作失败', 'error') }
        }

        function startEdit(row) { editing.value = true; editId.value = row.id; Object.assign(form, row); recordsSub.value = 'add' }
        function cancelEdit() { resetForm() }
        function resetForm() {
            editing.value = false; editId.value = null
            Object.assign(form, { title: '', category: 'backend', level: 'heard', duration: 30, note: '' })
        }

        async function handleDelete(id) {
            if (!confirm('确定删除这条记录吗？')) return
            const res = await fetch(`${RECORDS_API}/${id}`, { method: 'DELETE' })
            const json = await res.json()
            if (json.code === 200) { await loadRecords(); showToast('已删除') }
            else { showToast(json.message || '删除失败', 'error') }
        }

        function catLabel(c) { return { backend: '后端', frontend: '前端', basics: '基础', other: '其他' }[c] || c }
        function levelLabel(l) { return { heard: '听说过', can_explain: '能解释', can_write: '能写' }[l] || l }

        // ==================== 日记方法 ====================
        async function loadJournals() {
            const res = await fetch(`${JOURNAL_API}/page?page=${journalPage.current}&size=${journalPage.size}`)
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
            const res = await fetch(JOURNAL_API)
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
            const res = await fetch(`${JOURNAL_API}/${id}`, { method: 'DELETE' })
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
        onMounted(() => {
            loadRecords()
            loadJournals()
        })

        return {
            activeTab, recordsSub, journalSub,
            records, form, editing, stats, recordsPage, categories,
            submitRecord, startEdit, cancelEdit, handleDelete, catLabel, levelLabel, goRecordsPage, showModal,
            journalForm, todayJournal, journals, journalPage, moods,
            submitJournal, deleteJournal, moodLabel, loadJournals, goJournalPage, formatDate,
            toast, modal
        }
    }
}).mount('#app')
