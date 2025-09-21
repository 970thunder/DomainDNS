<template>
    <main class="main container">
        <div class="card">
            <div class="card-header">
                <h3>GitHub任务管理</h3>
                <button class="btn primary" @click="showCreateDialog">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                    新建任务
                </button>
            </div>

            <div v-if="isLoading" class="loading-state">
                <div class="loading-spinner"></div>
                <p>加载任务中...</p>
            </div>

            <div v-else-if="tasks.length === 0" class="empty-state">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor" style="color: #9ca3af;">
                    <path
                        d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z">
                    </path>
                </svg>
                <p>暂无任务</p>
                <button class="btn primary" @click="showCreateDialog">创建第一个任务</button>
            </div>

            <div v-else class="tasks-table">
                <table>
                    <thead>
                        <tr>
                            <th>任务标题</th>
                            <th>仓库</th>
                            <th>奖励积分</th>
                            <th>状态</th>
                            <th>完成数</th>
                            <th>创建时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="task in tasks" :key="task.id">
                            <td>
                                <div class="task-title">
                                    <span class="title-text">{{ task.title }}</span>
                                    <span v-if="task.description" class="description-text">{{ task.description }}</span>
                                </div>
                            </td>
                            <td>
                                <div class="repository-info">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                                        <path
                                            d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z">
                                        </path>
                                    </svg>
                                    <a :href="task.repositoryUrl" target="_blank" class="repository-link">
                                        {{ task.repositoryOwner }}/{{ task.repositoryName }}
                                    </a>
                                </div>
                            </td>
                            <td>
                                <span class="reward-points">+{{ task.rewardPoints }}</span>
                            </td>
                            <td>
                                <span class="status-badge" :class="getStatusClass(task.status)">
                                    {{ getStatusText(task.status) }}
                                </span>
                            </td>
                            <td>
                                <span class="completion-count">{{ task.completionCount || 0 }}</span>
                            </td>
                            <td>
                                <span class="create-time">{{ formatTime(task.createdAt) }}</span>
                            </td>
                            <td>
                                <div class="action-buttons">
                                    <button class="btn-icon" @click="viewStats(task)" title="查看统计">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                            stroke="currentColor" stroke-width="2">
                                            <path d="M18 20V10"></path>
                                            <path d="M12 20V4"></path>
                                            <path d="M6 20v-6"></path>
                                        </svg>
                                    </button>
                                    <button class="btn-icon" @click="editTask(task)" title="编辑">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                            stroke="currentColor" stroke-width="2">
                                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                        </svg>
                                    </button>
                                    <button class="btn-icon danger" @click="deleteTask(task)" title="删除">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                            stroke="currentColor" stroke-width="2">
                                            <polyline points="3,6 5,6 21,6"></polyline>
                                            <path
                                                d="M19,6v14a2,2 0 0,1 -2,2H7a2,2 0 0,1 -2,-2V6m3,0V4a2,2 0 0,1 2,-2h4a2,2 0 0,1 2,2v2">
                                            </path>
                                        </svg>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- 创建/编辑任务对话框 -->
        <el-dialog :model-value="taskDialogVisible" @update:model-value="taskDialogVisible = $event"
            :title="isEditing ? '编辑任务' : '新建任务'" width="600px" :close-on-click-modal="false">
            <el-form :model="taskForm" :rules="taskRules" ref="taskFormRef" label-width="100px">
                <el-form-item label="任务标题" prop="title">
                    <el-input v-model="taskForm.title" placeholder="请输入任务标题" maxlength="200" show-word-limit />
                </el-form-item>
                <el-form-item label="任务描述" prop="description">
                    <el-input v-model="taskForm.description" type="textarea" placeholder="请输入任务描述" :rows="3" />
                </el-form-item>
                <el-form-item label="仓库链接" prop="repositoryUrl">
                    <el-input v-model="taskForm.repositoryUrl" placeholder="https://github.com/owner/repo" />
                </el-form-item>
                <el-form-item label="仓库所有者" prop="repositoryOwner">
                    <el-input v-model="taskForm.repositoryOwner" placeholder="仓库所有者用户名" />
                </el-form-item>
                <el-form-item label="仓库名称" prop="repositoryName">
                    <el-input v-model="taskForm.repositoryName" placeholder="仓库名称" />
                </el-form-item>
                <el-form-item label="奖励积分" prop="rewardPoints">
                    <el-input-number v-model="taskForm.rewardPoints" :min="1" :max="1000" placeholder="奖励积分" />
                </el-form-item>
                <el-form-item label="任务状态" prop="status">
                    <el-select v-model="taskForm.status" placeholder="选择状态">
                        <el-option label="活跃" value="ACTIVE" />
                        <el-option label="非活跃" value="INACTIVE" />
                        <el-option label="已完成" value="COMPLETED" />
                    </el-select>
                </el-form-item>
                <el-form-item label="开始时间" prop="startTime">
                    <el-date-picker v-model="taskForm.startTime" type="datetime" placeholder="选择开始时间"
                        format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
                </el-form-item>
                <el-form-item label="结束时间" prop="endTime">
                    <el-date-picker v-model="taskForm.endTime" type="datetime" placeholder="选择结束时间"
                        format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="taskDialogVisible = false" :disabled="isSaving">取消</el-button>
                    <el-button type="primary" @click="saveTask" :loading="isSaving">
                        {{ isEditing ? '更新' : '创建' }}
                    </el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 任务统计对话框 -->
        <el-dialog :model-value="statsDialogVisible" @update:model-value="statsDialogVisible = $event" title="任务统计"
            width="500px">
            <div v-if="currentTaskStats" class="stats-content">
                <div class="stats-header">
                    <h4>{{ currentTaskStats.taskTitle }}</h4>
                    <p>任务ID: {{ currentTaskStats.taskId }}</p>
                </div>
                <div class="stats-grid">
                    <div class="stat-item">
                        <div class="stat-value">{{ currentTaskStats.completionCount }}</div>
                        <div class="stat-label">完成人数</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value">{{ currentTaskStats.rewardPoints }}</div>
                        <div class="stat-label">奖励积分</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-value">{{ currentTaskStats.completionCount * currentTaskStats.rewardPoints }}
                        </div>
                        <div class="stat-label">总发放积分</div>
                    </div>
                </div>
            </div>
        </el-dialog>
    </main>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.js'
import { apiGet, apiPost, apiPut, apiDelete } from '@/utils/api.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const authStore = useAuthStore()

// 响应式数据
const isLoading = ref(false)
const isSaving = ref(false)
const tasks = ref([])
const taskDialogVisible = ref(false)
const statsDialogVisible = ref(false)
const isEditing = ref(false)
const currentTaskId = ref(null)
const currentTaskStats = ref(null)

const taskFormRef = ref(null)

// 任务表单
const taskForm = reactive({
    title: '',
    description: '',
    repositoryUrl: '',
    repositoryOwner: '',
    repositoryName: '',
    rewardPoints: 10,
    status: 'ACTIVE',
    startTime: null,
    endTime: null
})

// 验证规则
const taskRules = {
    title: [
        { required: true, message: '请输入任务标题', trigger: 'blur' },
        { min: 1, max: 200, message: '标题长度应在1-200个字符之间', trigger: 'blur' }
    ],
    repositoryUrl: [
        { required: true, message: '请输入仓库链接', trigger: 'blur' },
        { pattern: /^https:\/\/github\.com\/[\w\-\.]+\/[\w\-\.]+/, message: '请输入有效的GitHub仓库链接', trigger: 'blur' }
    ],
    repositoryOwner: [
        { required: true, message: '请输入仓库所有者', trigger: 'blur' }
    ],
    repositoryName: [
        { required: true, message: '请输入仓库名称', trigger: 'blur' }
    ],
    rewardPoints: [
        { required: true, message: '请输入奖励积分', trigger: 'blur' },
        { type: 'number', min: 1, max: 1000, message: '奖励积分应在1-1000之间', trigger: 'blur' }
    ],
    status: [
        { required: true, message: '请选择任务状态', trigger: 'change' }
    ]
}

// 加载任务列表
const loadTasks = async () => {
    isLoading.value = true
    try {
        if (!authStore.adminToken) {
            ElMessage.error('请先登录')
            return
        }

        const response = await apiGet('/api/admin/github-tasks', { token: authStore.adminToken })
        if (response.data) {
            // 处理后端返回的Map数据，确保字段名正确
            tasks.value = response.data.map(task => ({
                id: task.id,
                title: task.title,
                description: task.description,
                repositoryUrl: task.repository_url,
                repositoryOwner: task.repository_owner,
                repositoryName: task.repository_name,
                rewardPoints: task.reward_points,
                status: task.status,
                startTime: task.start_time,
                endTime: task.end_time,
                createdAt: task.created_at,
                completionCount: task.completionCount || 0
            }))
        }
    } catch (error) {
        console.error('加载任务失败:', error)
        ElMessage.error('加载任务失败')
    } finally {
        isLoading.value = false
    }
}

// 显示创建对话框
const showCreateDialog = () => {
    isEditing.value = false
    currentTaskId.value = null
    resetForm()
    taskDialogVisible.value = true
}

// 编辑任务
const editTask = (task) => {
    isEditing.value = true
    currentTaskId.value = task.id
    taskForm.title = task.title
    taskForm.description = task.description || ''
    taskForm.repositoryUrl = task.repositoryUrl
    taskForm.repositoryOwner = task.repositoryOwner
    taskForm.repositoryName = task.repositoryName
    taskForm.rewardPoints = task.rewardPoints
    taskForm.status = task.status
    taskForm.startTime = task.startTime
    taskForm.endTime = task.endTime
    taskDialogVisible.value = true
}

// 保存任务
const saveTask = async () => {
    try {
        await taskFormRef.value.validate()
        isSaving.value = true

        const url = isEditing.value
            ? `/api/admin/github-tasks/${currentTaskId.value}`
            : '/api/admin/github-tasks'

        const method = isEditing.value ? apiPut : apiPost

        await method(url, taskForm, { token: authStore.adminToken })

        ElMessage.success(isEditing.value ? '任务更新成功' : '任务创建成功')
        taskDialogVisible.value = false
        await loadTasks()
    } catch (error) {
        console.error('保存任务失败:', error)
        ElMessage.error(error.message || '保存任务失败')
    } finally {
        isSaving.value = false
    }
}

// 删除任务
const deleteTask = async (task) => {
    try {
        await ElMessageBox.confirm(
            `确定要删除任务"${task.title}"吗？此操作不可撤销。`,
            '确认删除',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )

        await apiDelete(`/api/admin/github-tasks/${task.id}`, { token: authStore.adminToken })
        ElMessage.success('任务删除成功')
        await loadTasks()
    } catch (error) {
        if (error !== 'cancel') {
            console.error('删除任务失败:', error)
            ElMessage.error('删除任务失败')
        }
    }
}

// 查看统计
const viewStats = async (task) => {
    try {
        const response = await apiGet(`/api/admin/github-tasks/${task.id}/stats`, { token: authStore.adminToken })
        if (response.data) {
            currentTaskStats.value = response.data
            statsDialogVisible.value = true
        }
    } catch (error) {
        console.error('获取任务统计失败:', error)
        ElMessage.error('获取任务统计失败')
    }
}

// 重置表单
const resetForm = () => {
    taskForm.title = ''
    taskForm.description = ''
    taskForm.repositoryUrl = ''
    taskForm.repositoryOwner = ''
    taskForm.repositoryName = ''
    taskForm.rewardPoints = 10
    taskForm.status = 'ACTIVE'
    taskForm.startTime = null
    taskForm.endTime = null
}

// 格式化时间
const formatTime = (timeStr) => {
    if (!timeStr) return '未知'
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN')
}

// 获取状态样式类
const getStatusClass = (status) => {
    switch (status) {
        case 'ACTIVE': return 'status-active'
        case 'INACTIVE': return 'status-inactive'
        case 'COMPLETED': return 'status-completed'
        default: return ''
    }
}

// 获取状态文本
const getStatusText = (status) => {
    switch (status) {
        case 'ACTIVE': return '活跃'
        case 'INACTIVE': return '非活跃'
        case 'COMPLETED': return '已完成'
        default: return '未知'
    }
}

onMounted(() => {
    loadTasks()
})
</script>

<style scoped>
.tasks-table {
    overflow-x: auto;
}

.tasks-table table {
    width: 100%;
    border-collapse: collapse;
}

.tasks-table th,
.tasks-table td {
    padding: 12px;
    text-align: left;
    border-bottom: 1px solid #e2e8f0;
}

.tasks-table th {
    background-color: #f8fafc;
    font-weight: 600;
    color: #374151;
}

.task-title {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.title-text {
    font-weight: 500;
    color: #0f172a;
}

.description-text {
    font-size: 12px;
    color: #64748b;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 200px;
}

.repository-info {
    display: flex;
    align-items: center;
    gap: 6px;
}

.repository-link {
    color: #6366f1;
    text-decoration: none;
    font-size: 14px;
}

.repository-link:hover {
    text-decoration: underline;
}

.reward-points {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    color: white;
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
}

.status-badge {
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
}

.status-active {
    background-color: #dcfce7;
    color: #166534;
}

.status-inactive {
    background-color: #f1f5f9;
    color: #64748b;
}

.status-completed {
    background-color: #dbeafe;
    color: #1e40af;
}

.completion-count {
    font-weight: 500;
    color: #0f172a;
}

.create-time {
    font-size: 14px;
    color: #64748b;
}

.action-buttons {
    display: flex;
    gap: 4px;
}

.btn-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    background: white;
    color: #64748b;
    cursor: pointer;
    transition: all 0.2s ease;
}

.btn-icon:hover {
    background: #f8fafc;
    color: #0f172a;
    border-color: #cbd5e1;
}

.btn-icon.danger:hover {
    background: #fee2e2;
    color: #dc2626;
    border-color: #fecaca;
}

.loading-state,
.empty-state {
    text-align: center;
    padding: 40px 20px;
    color: #64748b;
}

.loading-spinner {
    width: 32px;
    height: 32px;
    border: 3px solid #e2e8f0;
    border-top: 3px solid #6366f1;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 16px;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

.empty-state svg {
    margin-bottom: 16px;
}

.empty-state p {
    margin: 0 0 16px 0;
    font-size: 16px;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.stats-content {
    padding: 20px 0;
}

.stats-header {
    margin-bottom: 24px;
    text-align: center;
}

.stats-header h4 {
    margin: 0 0 8px 0;
    color: #0f172a;
}

.stats-header p {
    margin: 0;
    color: #64748b;
    font-size: 14px;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
}

.stat-item {
    text-align: center;
    padding: 20px;
    background: #f8fafc;
    border-radius: 8px;
}

.stat-value {
    font-size: 24px;
    font-weight: 700;
    color: #0f172a;
    margin-bottom: 8px;
}

.stat-label {
    font-size: 14px;
    color: #64748b;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .tasks-table {
        font-size: 14px;
    }

    .tasks-table th,
    .tasks-table td {
        padding: 8px;
    }

    .title-text {
        font-size: 14px;
    }

    .description-text {
        font-size: 11px;
        max-width: 150px;
    }

    .repository-link {
        font-size: 12px;
    }

    .action-buttons {
        flex-direction: column;
        gap: 2px;
    }

    .btn-icon {
        width: 28px;
        height: 28px;
    }

    .stats-grid {
        grid-template-columns: 1fr;
        gap: 16px;
    }

    .stat-item {
        padding: 16px;
    }

    .stat-value {
        font-size: 20px;
    }
}

@media (max-width: 480px) {

    .tasks-table th,
    .tasks-table td {
        padding: 6px;
        font-size: 12px;
    }

    .title-text {
        font-size: 13px;
    }

    .description-text {
        font-size: 10px;
        max-width: 120px;
    }

    .repository-link {
        font-size: 11px;
    }

    .btn-icon {
        width: 24px;
        height: 24px;
    }

    .btn-icon svg {
        width: 12px;
        height: 12px;
    }
}
</style>
