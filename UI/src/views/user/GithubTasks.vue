<template>
    <main class="main container">
        <div class="card">
            <div class="card-header">
                <h3>GitHub Star 任务</h3>
                <button class="btn outline" @click="loadTasks" :disabled="isLoading">
                    {{ isLoading ? '加载中...' : '刷新' }}
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
                <p>暂无可用任务</p>
            </div>

            <div v-else class="tasks-grid">
                <div v-for="task in tasks" :key="task.id" class="task-card">
                    <div class="task-header">
                        <div class="task-title">
                            <span class="reward-badge">+{{ task.rewardPoints }} 积分</span>
                            <h4>{{ task.title }}</h4>
                        </div>
                        <div class="task-status">
                            <span v-if="task.isCompleted" class="status-badge completed">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"></path>
                                </svg>
                                已完成
                            </span>
                            <span v-else class="status-badge pending">待完成</span>
                        </div>
                    </div>

                    <div class="task-content">
                        <p class="task-description">{{ task.description }}</p>

                        <div class="task-repository">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                                <path
                                    d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z">
                                </path>
                            </svg>
                            <a :href="task.repositoryUrl" target="_blank" class="repository-link">
                                {{ task.repositoryOwner }}/{{ task.repositoryName }}
                            </a>
                        </div>

                        <div class="task-time">
                            <span v-if="task.startTime" class="time-info">
                                开始时间: {{ formatTime(task.startTime) }}
                            </span>
                            <span v-if="task.endTime" class="time-info">
                                结束时间: {{ formatTime(task.endTime) }}
                            </span>
                        </div>
                    </div>

                    <div class="task-actions">
                        <button v-if="!task.isCompleted" class="btn primary" @click="openVerifyDialog(task)">
                            验证Star状态
                        </button>
                        <button v-else class="btn success" disabled>
                            已完成
                        </button>
                        <a :href="task.repositoryUrl" target="_blank" class="btn outline">
                            前往GitHub
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <!-- 验证Star状态对话框 -->
        <el-dialog :model-value="verifyDialogVisible" @update:model-value="verifyDialogVisible = $event"
            :title="currentTask?.isCompleted ? '任务完成详情' : '验证Star状态'" :width="dialogWidth" :close-on-click-modal="false"
            class="verify-dialog">
            <div v-if="currentTask" class="verify-content">
                <div class="task-info">
                    <h4>{{ currentTask.title }}</h4>

                    <!-- 已完成任务显示 -->
                    <div v-if="currentTask.isCompleted" class="completed-info">
                        <div class="success-message">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor" style="color: #10b981;">
                                <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"></path>
                            </svg>
                            <span>恭喜！您已完成此任务</span>
                        </div>
                        <div class="completion-details">
                            <p><strong>获得积分：</strong>{{ currentTask.pointsAwarded || currentTask.rewardPoints }} 分</p>
                            <p><strong>完成时间：</strong>{{ currentTask.completedAt ? formatTime(currentTask.completedAt) :
                                '未知' }}</p>
                        </div>
                    </div>

                    <!-- 未完成任务显示 -->
                    <div v-else>
                        <p>请确保您已Star该仓库，然后输入您的GitHub用户名进行验证：</p>
                    </div>

                    <div class="repository-info">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                            <path
                                d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z">
                            </path>
                        </svg>
                        <a :href="currentTask.repositoryUrl" target="_blank" class="repository-link">
                            {{ currentTask.repositoryOwner }}/{{ currentTask.repositoryName }}
                        </a>
                    </div>
                </div>

                <!-- 只有未完成任务才显示验证表单 -->
                <div v-if="!currentTask.isCompleted">
                    <el-form :model="verifyForm" :rules="verifyRules" ref="verifyFormRef" :label-width="labelWidth"
                        class="verify-form">
                        <el-form-item label="GitHub用户名" prop="githubUsername">
                            <el-input v-model="verifyForm.githubUsername" placeholder="请输入您的GitHub用户名"
                                :disabled="isVerifying" />
                        </el-form-item>
                    </el-form>

                    <div class="verify-tips">
                        <h5>验证步骤：</h5>
                        <ol>
                            <li>点击上方链接前往GitHub仓库</li>
                            <li>点击仓库右上角的"Star"按钮</li>
                            <li>返回此页面，输入您的GitHub用户名</li>
                            <li>点击"验证"按钮完成验证</li>
                        </ol>

                        <div class="important-notice">
                            <h5>⚠️ 重要提示：</h5>
                            <ul>
                                <li>每个GitHub用户名只能用于完成一次任务</li>
                                <li>如果您的GitHub用户名已被其他用户使用，请使用其他GitHub账号</li>
                                <li>请确保您输入的GitHub用户名准确无误</li>
                                <li>验证前请确保您已经Star了目标仓库</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="verifyDialogVisible = false" :disabled="isVerifying">
                        {{ currentTask?.isCompleted ? '关闭' : '取消' }}
                    </el-button>
                    <el-button v-if="!currentTask?.isCompleted" type="primary" @click="verifyStar"
                        :loading="isVerifying">
                        验证
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </main>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth.js'
import { apiGet, apiPost } from '@/utils/api.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const authStore = useAuthStore()

// 响应式计算属性
const dialogWidth = computed(() => {
    if (window.innerWidth <= 480) return '95%'
    if (window.innerWidth <= 768) return '90%'
    return '500px'
})

const labelWidth = computed(() => {
    if (window.innerWidth <= 480) return '80px'
    if (window.innerWidth <= 768) return '90px'
    return '120px'
})

// 响应式数据
const isLoading = ref(false)
const tasks = ref([])
const verifyDialogVisible = ref(false)
const currentTask = ref(null)
const isVerifying = ref(false)

// 验证表单
const verifyForm = reactive({
    githubUsername: ''
})

const verifyFormRef = ref(null)

// 验证规则
const verifyRules = {
    githubUsername: [
        { required: true, message: '请输入GitHub用户名', trigger: 'blur' },
        { min: 1, max: 39, message: 'GitHub用户名长度应在1-39个字符之间', trigger: 'blur' }
    ]
}

// 加载任务列表
const loadTasks = async () => {
    isLoading.value = true
    try {
        if (!authStore.token) {
            ElMessage.error('请先登录')
            return
        }

        const response = await apiGet('/api/user/github-tasks', { token: authStore.token })
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
                isCompleted: task.isCompleted || false,
                pointsAwarded: task.points_awarded,
                completedAt: task.completed_at
            }))
        }
    } catch (error) {
        console.error('加载任务失败:', error)
        ElMessage.error('加载任务失败')
    } finally {
        isLoading.value = false
    }
}

// 打开验证对话框
const openVerifyDialog = (task) => {
    // 检查任务是否已完成
    if (task.isCompleted) {
        ElMessage.warning('该任务已完成，无法重复提交')
        return
    }

    currentTask.value = task
    verifyForm.githubUsername = ''
    verifyDialogVisible.value = true
}

// 验证Star状态
const verifyStar = async () => {
    try {
        await verifyFormRef.value.validate()
        isVerifying.value = true

        const response = await apiPost(`/api/user/github-tasks/${currentTask.value.id}/verify`, {
            githubUsername: verifyForm.githubUsername
        }, { token: authStore.token })

        if (response.data && response.data.success) {
            ElMessage.success(response.data.message)
            verifyDialogVisible.value = false
            // 重新加载任务列表
            await loadTasks()
        } else {
            // 根据不同的错误类型显示不同的提示
            const errorMessage = response.data?.message || '验证失败'

            if (errorMessage.includes('已被其他用户使用')) {
                ElMessageBox.alert(
                    '该GitHub用户名已被其他用户使用，每个GitHub用户名只能用于完成一次任务。请使用其他GitHub用户名或创建新的GitHub账号。',
                    '用户名已被使用',
                    {
                        confirmButtonText: '我知道了',
                        type: 'warning',
                        dangerouslyUseHTMLString: false
                    }
                )
            } else if (errorMessage.includes('您已经完成过该任务')) {
                ElMessage.warning('您已经完成过该任务，无法重复提交')
            } else if (errorMessage.includes('请先Star该仓库')) {
                ElMessageBox.alert(
                    '请先Star该仓库后再进行验证。请确保您已经点击了仓库右上角的"Star"按钮。',
                    '请先Star仓库',
                    {
                        confirmButtonText: '我知道了',
                        type: 'info'
                    }
                )
            } else if (errorMessage.includes('GitHub用户名不存在')) {
                ElMessage.error('GitHub用户名不存在或无效，请检查用户名是否正确')
            } else {
                ElMessage.error(errorMessage)
            }
        }
    } catch (error) {
        console.error('验证失败:', error)
        ElMessage.error(error.message || '验证失败')
    } finally {
        isVerifying.value = false
    }
}

// 格式化时间
const formatTime = (timeStr) => {
    if (!timeStr) return '未知'
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN')
}

onMounted(() => {
    loadTasks()
})
</script>

<style scoped>
.tasks-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 20px;
    margin-top: 20px;
}

.task-card {
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    padding: 20px;
    background: #fff;
    transition: all 0.3s ease;
}

.task-card:hover {
    border-color: #6366f1;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.task-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
}

.task-title {
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex: 1;
}

.reward-badge {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    color: white;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    align-self: flex-start;
}

.task-title h4 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #0f172a;
}

.status-badge {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
}

.status-badge.completed {
    background-color: #dcfce7;
    color: #166534;
}

.status-badge.pending {
    background-color: #fef3c7;
    color: #92400e;
}

.task-content {
    margin-bottom: 20px;
}

.task-description {
    color: #64748b;
    line-height: 1.6;
    margin-bottom: 16px;
}

.task-repository {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
}

.repository-link {
    color: #6366f1;
    text-decoration: none;
    font-weight: 500;
}

.repository-link:hover {
    text-decoration: underline;
}

.task-time {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.time-info {
    font-size: 12px;
    color: #94a3b8;
}

.task-actions {
    display: flex;
    gap: 12px;
}

.btn {
    padding: 8px 16px;
    border-radius: 6px;
    border: none;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
}

.btn.primary {
    background: #6366f1;
    color: white;
}

.btn.primary:hover {
    background: #5856eb;
}

.btn.outline {
    background: transparent;
    color: #6366f1;
    border: 1px solid #6366f1;
}

.btn.outline:hover {
    background: #6366f1;
    color: white;
}

.btn.success {
    background: #10b981;
    color: white;
    cursor: not-allowed;
}

.btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
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

.verify-content {
    padding: 20px 0;
}

.task-info h4 {
    margin: 0 0 12px 0;
    color: #0f172a;
}

.task-info p {
    color: #64748b;
    margin-bottom: 16px;
}

.repository-info {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 20px;
}

.verify-tips {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 16px;
    margin-top: 20px;
}

.verify-tips h5 {
    margin: 0 0 12px 0;
    color: #0f172a;
    font-size: 14px;
}

.verify-tips ol {
    margin: 0;
    padding-left: 20px;
    color: #64748b;
    font-size: 14px;
}

.verify-tips li {
    margin-bottom: 4px;
}

.completed-info {
    margin-bottom: 20px;
}

.success-message {
    display: flex;
    align-items: center;
    gap: 8px;
    background: #dcfce7;
    color: #166534;
    padding: 12px 16px;
    border-radius: 8px;
    margin-bottom: 16px;
    font-weight: 500;
}

.completion-details {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 16px;
}

.completion-details p {
    margin: 0 0 8px 0;
    color: #374151;
}

.completion-details p:last-child {
    margin-bottom: 0;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

/* 验证对话框响应式样式 */
.verify-dialog .el-dialog {
    margin: 0 auto;
}

.verify-form {
    padding: 0;
}

.verify-form .el-form-item {
    margin-bottom: 20px;
}

.verify-form .el-form-item__label {
    font-weight: 500;
    color: #374151;
}

.verify-form .el-input__wrapper {
    border-radius: 6px;
}

.verify-form .el-button {
    border-radius: 6px;
    font-weight: 500;
}

.verify-tips {
    margin-top: 20px;
    padding: 16px;
    background: #f8fafc;
    border-radius: 8px;
    border-left: 4px solid #6366f1;
}

.verify-tips h5 {
    margin: 0 0 12px 0;
    font-size: 14px;
    font-weight: 600;
    color: #374151;
}

.verify-tips ol {
    margin: 0 0 16px 0;
    padding-left: 20px;
}

.verify-tips li {
    margin-bottom: 6px;
    font-size: 13px;
    color: #6b7280;
    line-height: 1.5;
}

.important-notice {
    margin-top: 16px;
    padding: 12px;
    background: #fef3c7;
    border-radius: 6px;
    border-left: 4px solid #f59e0b;
}

.important-notice h5 {
    margin: 0 0 8px 0;
    font-size: 13px;
    font-weight: 600;
    color: #92400e;
}

.important-notice ul {
    margin: 0;
    padding-left: 16px;
}

.important-notice li {
    margin-bottom: 4px;
    font-size: 12px;
    color: #92400e;
    line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .tasks-grid {
        grid-template-columns: 1fr;
    }

    .task-header {
        flex-direction: column;
        gap: 12px;
        align-items: flex-start;
    }

    .task-actions {
        flex-direction: column;
    }

    .btn {
        width: 100%;
    }

    .dialog-footer {
        flex-direction: column;
        gap: 8px;
    }

    .dialog-footer .el-button {
        width: 100%;
    }

    .verify-tips {
        padding: 12px;
    }

    .verify-tips h5 {
        font-size: 13px;
    }

    .verify-tips ol {
        font-size: 13px;
    }
}

@media (max-width: 480px) {
    .verify-content {
        padding: 16px 0;
    }

    .task-info h4 {
        font-size: 16px;
    }

    .repository-info {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
    }

    .repository-link {
        font-size: 13px;
    }

    .verify-form .el-form-item {
        margin-bottom: 16px;
    }

    .verify-form .el-form-item__label {
        font-size: 14px;
    }

    .verify-form .el-input {
        font-size: 14px;
    }

    .verify-form .el-button {
        font-size: 14px;
        padding: 8px 16px;
    }

    .success-message {
        flex-direction: column;
        text-align: center;
        gap: 4px;
    }

    .completion-details {
        padding: 12px;
    }

    .completion-details p {
        font-size: 13px;
    }
}
</style>
