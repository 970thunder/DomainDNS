<template>
    <div class="announcements-container" v-loading="isLoading">
        <div class="card">
            <div class="card-header">
                <h3>系统公告</h3>
                <button class="btn outline" @click="loadAnnouncements" :disabled="isLoading">
                    {{ isLoading ? '加载中...' : '刷新' }}
                </button>
            </div>

            <div v-if="announcements.length === 0 && !isLoading" class="empty-state">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor" style="color: #9ca3af;">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                    <polyline points="14,2 14,8 20,8"></polyline>
                    <line x1="16" y1="13" x2="8" y2="13"></line>
                    <line x1="16" y1="17" x2="8" y2="17"></line>
                    <polyline points="10,9 9,9 8,9"></polyline>
                </svg>
                <p>暂无公告</p>
            </div>

            <div v-else class="announcement-list">
                <div v-for="announcement in announcements" :key="announcement.id" class="announcement-item">
                    <div class="announcement-header">
                        <div class="announcement-title">
                            <span class="priority-badge" :class="getPriorityClass(announcement.priority)">
                                {{ getPriorityText(announcement.priority) }}
                            </span>
                            {{ announcement.title }}
                        </div>
                        <div class="announcement-meta">
                            <span class="publish-time">{{ formatTime(announcement.publishedAt) }}</span>
                        </div>
                    </div>
                    <div class="announcement-content" v-html="announcement.content"></div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.js'
import { apiGet } from '@/utils/api.js'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()

// 响应式数据
const isLoading = ref(false)
const announcements = ref([])

// 加载公告列表
const loadAnnouncements = async () => {
    try {
        isLoading.value = true
        const response = await apiGet('/api/admin/announcements/published?limit=20')
        announcements.value = response.data || []
    } catch (error) {
        console.error('加载公告列表失败:', error)
        ElMessage.error('加载公告列表失败')
    } finally {
        isLoading.value = false
    }
}

// 格式化时间
const formatTime = (timeStr) => {
    if (!timeStr) return '未知'
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN')
}

// 获取优先级样式类
const getPriorityClass = (priority) => {
    switch (priority) {
        case 3: return 'priority-urgent'
        case 2: return 'priority-important'
        default: return 'priority-normal'
    }
}

// 获取优先级文本
const getPriorityText = (priority) => {
    switch (priority) {
        case 3: return '紧急'
        case 2: return '重要'
        default: return '普通'
    }
}

onMounted(() => {
    loadAnnouncements()
})
</script>

<style scoped>
.announcements-container {
    padding: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.empty-state {
    text-align: center;
    padding: 60px 20px;
    color: #64748b;
}

.empty-state svg {
    margin-bottom: 16px;
}

.empty-state p {
    margin: 0;
    font-size: 16px;
}

.announcement-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.announcement-item {
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 20px;
    background: #fff;
    transition: box-shadow 0.2s ease;
}

.announcement-item:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.announcement-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
}

.announcement-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: #0f172a;
    flex: 1;
}

.priority-badge {
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
}

.priority-normal {
    background-color: #f1f5f9;
    color: #64748b;
}

.priority-important {
    background-color: #fef3c7;
    color: #92400e;
}

.priority-urgent {
    background-color: #fee2e2;
    color: #dc2626;
}

.announcement-meta {
    display: flex;
    align-items: center;
    gap: 12px;
}

.publish-time {
    font-size: 14px;
    color: #94a3b8;
}

.announcement-content {
    color: #475569;
    line-height: 1.6;
}

.announcement-content h1,
.announcement-content h2,
.announcement-content h3,
.announcement-content h4,
.announcement-content h5,
.announcement-content h6 {
    margin: 0 0 12px 0;
    font-weight: 600;
    color: #0f172a;
}

.announcement-content h1 {
    font-size: 24px;
}

.announcement-content h2 {
    font-size: 20px;
}

.announcement-content h3 {
    font-size: 18px;
}

.announcement-content h4 {
    font-size: 16px;
}

.announcement-content h5 {
    font-size: 14px;
}

.announcement-content h6 {
    font-size: 12px;
}

.announcement-content p {
    margin: 0 0 12px 0;
}

.announcement-content ul,
.announcement-content ol {
    margin: 0 0 12px 0;
    padding-left: 24px;
}

.announcement-content li {
    margin: 0 0 6px 0;
}

.announcement-content a {
    color: #6366f1;
    text-decoration: none;
}

.announcement-content a:hover {
    text-decoration: underline;
}

.announcement-content img {
    max-width: 100%;
    height: auto;
    border-radius: 6px;
    margin: 8px 0;
}

.announcement-content blockquote {
    margin: 0 0 12px 0;
    padding: 12px 16px;
    background: #f8fafc;
    border-left: 4px solid #e2e8f0;
    border-radius: 0 6px 6px 0;
    font-style: italic;
}

.announcement-content code {
    background: #f1f5f9;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-size: 0.9em;
}

.announcement-content pre {
    background: #f1f5f9;
    padding: 16px;
    border-radius: 6px;
    overflow-x: auto;
    margin: 0 0 12px 0;
}

.announcement-content pre code {
    background: none;
    padding: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .announcements-container {
        padding: 12px;
    }

    .announcement-item {
        padding: 16px;
    }

    .announcement-header {
        flex-direction: column;
        gap: 12px;
        align-items: flex-start;
    }

    .announcement-title {
        font-size: 16px;
    }

    .announcement-meta {
        width: 100%;
        justify-content: flex-end;
    }
}
</style>
