<template>
    <el-dialog :model-value="visible" @update:model-value="$emit('update:visible', $event)" title="系统公告" width="600px"
        :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false" class="announcement-modal">
        <div class="announcement-content">
            <div v-if="announcements.length === 0" class="empty-state">
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
                    <div class="announcement-body" v-html="announcement.content"></div>
                </div>
            </div>
        </div>

        <template #footer>
            <div class="dialog-footer">
                <el-button @click="viewAllAnnouncements" type="primary" plain>
                    查看全部公告
                </el-button>
                <el-button @click="confirmRead" type="primary">
                    我知道了
                </el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { apiGet } from '@/utils/api.js'
import { ElMessage } from 'element-plus'

const router = useRouter()

// Props
const props = defineProps({
    visible: {
        type: Boolean,
        default: false
    }
})

// Emits
const emit = defineEmits(['update:visible', 'confirm'])

// 响应式数据
const announcements = ref([])
const isLoading = ref(false)

// 加载公告
const loadAnnouncements = async () => {
    try {
        isLoading.value = true
        const response = await apiGet('/api/admin/announcements/published?limit=5')
        announcements.value = response.data || []
    } catch (error) {
        console.error('加载公告失败:', error)
        ElMessage.error('加载公告失败')
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

// 查看全部公告
const viewAllAnnouncements = () => {
    emit('update:visible', false)
    router.push('/user/announcements')
}

// 确认已读
const confirmRead = () => {
    emit('confirm')
    emit('update:visible', false)
}

// 监听visible变化
watch(() => props.visible, (newVal) => {
    if (newVal) {
        loadAnnouncements()
    }
})

onMounted(() => {
    if (props.visible) {
        loadAnnouncements()
    }
})
</script>

<style scoped>
.announcement-modal :deep(.el-dialog__header) {
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
    padding: 16px 20px;
}

.announcement-modal :deep(.el-dialog__title) {
    font-size: 18px;
    font-weight: 600;
    color: #0f172a;
}

.announcement-modal :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 60vh;
    overflow-y: auto;
}

.announcement-modal :deep(.el-dialog__footer) {
    background: #f8fafc;
    border-top: 1px solid #e2e8f0;
    padding: 16px 20px;
}

.announcement-content {
    max-height: 50vh;
    overflow-y: auto;
}

.empty-state {
    text-align: center;
    padding: 40px 20px;
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
    padding: 16px;
    background: #fff;
}

.announcement-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
}

.announcement-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
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

.announcement-body {
    color: #475569;
    line-height: 1.6;
    max-height: 200px;
    overflow-y: auto;
}

.announcement-body h1,
.announcement-body h2,
.announcement-body h3,
.announcement-body h4,
.announcement-body h5,
.announcement-body h6 {
    margin: 0 0 8px 0;
    font-weight: 600;
    color: #0f172a;
}

.announcement-body h1 {
    font-size: 18px;
}

.announcement-body h2 {
    font-size: 16px;
}

.announcement-body h3 {
    font-size: 15px;
}

.announcement-body h4 {
    font-size: 14px;
}

.announcement-body h5 {
    font-size: 13px;
}

.announcement-body h6 {
    font-size: 12px;
}

.announcement-body p {
    margin: 0 0 8px 0;
}

.announcement-body ul,
.announcement-body ol {
    margin: 0 0 8px 0;
    padding-left: 20px;
}

.announcement-body li {
    margin: 0 0 4px 0;
}

.announcement-body a {
    color: #6366f1;
    text-decoration: none;
}

.announcement-body a:hover {
    text-decoration: underline;
}

.announcement-body img {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
}

.announcement-body blockquote {
    margin: 0 0 8px 0;
    padding: 8px 12px;
    background: #f8fafc;
    border-left: 4px solid #e2e8f0;
    border-radius: 0 4px 4px 0;
}

.announcement-body code {
    background: #f1f5f9;
    padding: 2px 4px;
    border-radius: 3px;
    font-family: 'Courier New', monospace;
    font-size: 0.9em;
}

.announcement-body pre {
    background: #f1f5f9;
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
    margin: 0 0 8px 0;
}

.announcement-body pre code {
    background: none;
    padding: 0;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .announcement-modal :deep(.el-dialog) {
        width: 80% !important;
    }
}

@media (max-width: 992px) {
    .announcement-modal :deep(.el-dialog) {
        width: 85% !important;
    }

    .announcement-modal :deep(.el-dialog__body) {
        padding: 18px;
    }
}

@media (max-width: 768px) {
    .announcement-modal :deep(.el-dialog) {
        width: 95% !important;
        margin: 0 auto;
    }

    .announcement-modal :deep(.el-dialog__header) {
        padding: 12px 16px;
    }

    .announcement-modal :deep(.el-dialog__title) {
        font-size: 16px;
    }

    .announcement-modal :deep(.el-dialog__body) {
        padding: 16px;
        max-height: 50vh;
    }

    .announcement-modal :deep(.el-dialog__footer) {
        padding: 12px 16px;
    }

    .announcement-content {
        max-height: 40vh;
    }

    .announcement-header {
        flex-direction: column;
        gap: 8px;
        align-items: flex-start;
    }

    .announcement-title {
        font-size: 14px;
        flex-direction: column;
        align-items: flex-start;
        gap: 6px;
    }

    .announcement-meta {
        align-self: flex-start;
    }

    .announcement-body {
        max-height: 150px;
        font-size: 14px;
    }

    .dialog-footer {
        flex-direction: column;
        gap: 8px;
    }

    .dialog-footer .el-button {
        width: 100%;
    }

    .empty-state {
        padding: 30px 16px;
    }

    .empty-state svg {
        width: 40px;
        height: 40px;
        margin-bottom: 12px;
    }

    .empty-state p {
        font-size: 14px;
    }
}

@media (max-width: 480px) {
    .announcement-modal :deep(.el-dialog) {
        width: 98% !important;
        margin: 5px auto;
    }

    .announcement-modal :deep(.el-dialog__header) {
        padding: 10px 12px;
    }

    .announcement-modal :deep(.el-dialog__title) {
        font-size: 15px;
    }

    .announcement-modal :deep(.el-dialog__body) {
        padding: 12px;
        max-height: 45vh;
    }

    .announcement-modal :deep(.el-dialog__footer) {
        padding: 10px 12px;
    }

    .announcement-content {
        max-height: 35vh;
    }

    .announcement-item {
        padding: 12px;
    }

    .announcement-title {
        font-size: 13px;
    }

    .announcement-body {
        max-height: 120px;
        font-size: 13px;
    }

    .priority-badge {
        padding: 3px 6px;
        font-size: 11px;
    }

    .publish-time {
        font-size: 12px;
    }

    .empty-state {
        padding: 20px 12px;
    }

    .empty-state svg {
        width: 36px;
        height: 36px;
        margin-bottom: 10px;
    }

    .empty-state p {
        font-size: 13px;
    }
}

/* 超小屏幕优化 */
@media (max-width: 360px) {
    .announcement-modal :deep(.el-dialog) {
        width: 100% !important;
        margin: 0;
        border-radius: 0;
    }

    .announcement-modal :deep(.el-dialog__header) {
        padding: 8px 10px;
    }

    .announcement-modal :deep(.el-dialog__body) {
        padding: 10px;
        max-height: 40vh;
    }

    .announcement-modal :deep(.el-dialog__footer) {
        padding: 8px 10px;
    }

    .announcement-content {
        max-height: 30vh;
    }

    .announcement-item {
        padding: 10px;
    }

    .announcement-title {
        font-size: 12px;
    }

    .announcement-body {
        max-height: 100px;
        font-size: 12px;
    }
}
</style>
