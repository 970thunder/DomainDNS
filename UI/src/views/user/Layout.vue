<template>
	<div>
		<header class="app-header">
			<div class="app-header-inner container">
				<div class="brand">HyperNym</div>
				<div class="spacer"></div>
				<button class="mobile-menu-toggle" @click="toggleMobileMenu" v-if="isMobile">
					<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<line x1="3" y1="6" x2="21" y2="6"></line>
						<line x1="3" y1="12" x2="21" y2="12"></line>
						<line x1="3" y1="18" x2="21" y2="18"></line>
					</svg>
				</button>
			</div>
		</header>

		<!-- Mobile Sidebar Overlay -->
		<div class="mobile-overlay" :class="{ active: mobileMenuOpen }" @click="closeMobileMenu"></div>

		<!-- Mobile Sidebar -->
		<div class="mobile-sidebar" :class="{ active: mobileMenuOpen }">
			<div class="mobile-sidebar-header">
				<div class="brand">HyperNym</div>
				<button class="close-btn" @click="closeMobileMenu">
					<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<line x1="18" y1="6" x2="6" y2="18"></line>
						<line x1="6" y1="6" x2="18" y2="18"></line>
					</svg>
				</button>
			</div>
			<nav class="mobile-nav">
				<router-link to="/user/dashboard" @click="closeMobileMenu" class="nav-link">
					<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<rect x="3" y="3" width="7" height="7"></rect>
						<rect x="14" y="3" width="7" height="7"></rect>
						<rect x="14" y="14" width="7" height="7"></rect>
						<rect x="3" y="14" width="7" height="7"></rect>
					</svg>
					主页
				</router-link>
				<router-link to="/user/apply" @click="closeMobileMenu" class="nav-link">
					<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<path
							d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z">
						</path>
						<polyline points="3.27,6.96 12,12.01 20.73,6.96"></polyline>
						<line x1="12" y1="22.08" x2="12" y2="12"></line>
					</svg>
					申请域名
				</router-link>
				<router-link to="/user/domains" @click="closeMobileMenu" class="nav-link">
					<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<circle cx="12" cy="12" r="10"></circle>
						<path d="M8 14s1.5 2 4 2 4-2 4-2"></path>
						<line x1="9" y1="9" x2="9.01" y2="9"></line>
						<line x1="15" y1="9" x2="15.01" y2="9"></line>
					</svg>
					我的域名
				</router-link>
				<router-link to="/user/invite" @click="closeMobileMenu" class="nav-link">
					<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
						<circle cx="8.5" cy="7" r="4"></circle>
						<line x1="20" y1="8" x2="20" y2="14"></line>
						<line x1="23" y1="11" x2="17" y2="11"></line>
					</svg>
					邀请
				</router-link>
				<router-link to="/user/recharge" @click="closeMobileMenu" class="nav-link">
					<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<line x1="12" y1="1" x2="12" y2="23"></line>
						<path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
					</svg>
					充值
				</router-link>
				<button @click="logout" class="nav-link logout-btn">
					<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
						<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
						<polyline points="16,17 21,12 16,7"></polyline>
						<line x1="21" y1="12" x2="9" y2="12"></line>
					</svg>
					退出登录
				</button>
			</nav>
			<div class="mobile-sidebar-footer">
			</div>
		</div>

		<div class="container" style="padding-top:16px;">
			<!-- Desktop Navigation -->
			<nav class="desktop-nav" v-if="!isMobile">
				<div class="nav-links">
					<router-link to="/user/dashboard" class="nav-link"
						:class="{ active: $route.path === '/user/dashboard' }">
						<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
							stroke-width="2">
							<rect x="3" y="3" width="7" height="7"></rect>
							<rect x="14" y="3" width="7" height="7"></rect>
							<rect x="14" y="14" width="7" height="7"></rect>
							<rect x="3" y="14" width="7" height="7"></rect>
						</svg>
						主页
					</router-link>
					<router-link to="/user/apply" class="nav-link" :class="{ active: $route.path === '/user/apply' }">
						<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
							stroke-width="2">
							<path
								d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z">
							</path>
							<polyline points="3.27,6.96 12,12.01 20.73,6.96"></polyline>
							<line x1="12" y1="22.08" x2="12" y2="12"></line>
						</svg>
						申请域名
					</router-link>
					<router-link to="/user/domains" class="nav-link"
						:class="{ active: $route.path === '/user/domains' }">
						<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
							stroke-width="2">
							<circle cx="12" cy="12" r="10"></circle>
							<path d="M8 14s1.5 2 4 2 4-2 4-2"></path>
							<line x1="9" y1="9" x2="9.01" y2="9"></line>
							<line x1="15" y1="9" x2="15.01" y2="9"></line>
						</svg>
						我的域名
					</router-link>
					<router-link to="/user/invite" class="nav-link" :class="{ active: $route.path === '/user/invite' }">
						<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
							stroke-width="2">
							<path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
							<circle cx="8.5" cy="7" r="4"></circle>
							<line x1="20" y1="8" x2="20" y2="14"></line>
							<line x1="23" y1="11" x2="17" y2="11"></line>
						</svg>
						邀请
					</router-link>
					<router-link to="/user/recharge" class="nav-link"
						:class="{ active: $route.path === '/user/recharge' }">
						<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
							stroke-width="2">
							<line x1="12" y1="1" x2="12" y2="23"></line>
							<path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
						</svg>
						充值
					</router-link>
				</div>
				<div class="nav-actions">
					<button @click="logout" class="nav-link logout-btn">
						<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
							stroke-width="2">
							<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
							<polyline points="16,17 21,12 16,7"></polyline>
							<line x1="21" y1="12" x2="9" y2="12"></line>
						</svg>
						退出登录
					</button>
				</div>
			</nav>

			<div class="page">
				<router-view />
			</div>
		</div>
		<footer class="footer container">
			© 2025 HyperNym-DomainDNS
		</footer>
	</div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const isMobile = ref(false)
const mobileMenuOpen = ref(false)

const checkMobile = () => {
	isMobile.value = window.innerWidth <= 768
}

const toggleMobileMenu = () => {
	mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
	mobileMenuOpen.value = false
}

// 退出登录
const logout = async () => {
	try {
		await ElMessageBox.confirm(
			'确定要退出登录吗？',
			'确认退出',
			{
				confirmButtonText: '确定',
				cancelButtonText: '取消',
				type: 'warning'
			}
		)

		// 调用用户登出
		authStore.logout()
		ElMessage.success('已退出登录')

		// 关闭移动端菜单
		closeMobileMenu()

		// 跳转到登录页
		router.push('/user/login')
	} catch (error) {
		// 用户取消退出
		if (error !== 'cancel') {
			console.error('退出登录失败:', error)
		}
	}
}

onMounted(() => {
	checkMobile()
	window.addEventListener('resize', checkMobile)

	// 监听token过期事件
	const handleTokenExpired = () => {
		console.log('用户界面收到token过期事件')
		// 显示提示信息
		ElMessage.warning('登录已过期，请重新登录')
		// 跳转到登录页
		router.push('/user/login')
	}

	window.addEventListener('token-expired', handleTokenExpired)

	// 保存事件监听器的引用，以便在组件卸载时移除
	window._tokenExpiredHandler = handleTokenExpired
})

onUnmounted(() => {
	window.removeEventListener('resize', checkMobile)
	// 移除token过期事件监听器
	if (window._tokenExpiredHandler) {
		window.removeEventListener('token-expired', window._tokenExpiredHandler)
		delete window._tokenExpiredHandler
	}
})
</script>

<style scoped>
.footer {
	padding: 16px;
	color: #64748b;
	font-size: 14px;
	text-align: center;
	border-top: 1px solid #e2e8f0;
}

.container {
	max-width: 1200px;
	margin: 0 auto;
	padding: 0 16px;
	transition: all 0.3s ease;
}

/* 响应式容器 */
@media (min-width: 1400px) {
	.container {
		max-width: 1400px;
		padding: 0 24px;
	}
}

@media (min-width: 1200px) and (max-width: 1399px) {
	.container {
		max-width: 1200px;
		padding: 0 20px;
	}
}

@media (min-width: 992px) and (max-width: 1199px) {
	.container {
		max-width: 1000px;
		padding: 0 16px;
	}
}

@media (max-width: 768px) {
	.container {
		padding: 0 12px;
	}
}

@media (max-width: 480px) {
	.container {
		padding: 0 8px;
	}
}

/* Mobile Menu Toggle */
.mobile-menu-toggle {
	background: none;
	border: none;
	color: #fff;
	cursor: pointer;
	padding: 8px;
	border-radius: 6px;
	display: none;
	transition: background-color 0.15s ease;
	will-change: background-color;
}

.mobile-menu-toggle:hover {
	background: rgba(255, 255, 255, 0.1);
}

/* Desktop Navigation */
.desktop-nav {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20px;
	padding: 12px 16px;
	background: rgba(255, 255, 255, 0.6);
	backdrop-filter: blur(10px);
	-webkit-backdrop-filter: blur(10px);
	border: 1px solid rgba(255, 255, 255, 0.3);
	border-radius: 16px;
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
	will-change: transform;
}

.nav-links {
	display: flex;
	gap: 4px;
}

.nav-actions {
	display: flex;
	gap: 8px;
}

.desktop-nav .nav-link {
	display: flex;
	align-items: center;
	gap: 8px;
	padding: 10px 16px;
	border-radius: 12px;
	text-decoration: none;
	color: #64748b;
	font-weight: 500;
	transition: transform 0.2s ease, background-color 0.2s ease, color 0.2s ease;
	background: rgba(255, 255, 255, 0.4);
	border: 1px solid rgba(255, 255, 255, 0.3);
	backdrop-filter: blur(8px);
	-webkit-backdrop-filter: blur(8px);
	will-change: transform;
}

.desktop-nav .nav-link:hover {
	background: rgba(255, 255, 255, 0.6);
	color: #0f172a;
	transform: translateY(-1px);
}

.desktop-nav .nav-link.active {
	background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
	color: #fff;
	border-color: rgba(255, 255, 255, 0.3);
	box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

/* Mobile Sidebar */
.mobile-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	z-index: 998;
	opacity: 0;
	visibility: hidden;
	transition: opacity 0.2s ease, visibility 0.2s ease;
	will-change: opacity;
}

.mobile-overlay.active {
	opacity: 1;
	visibility: visible;
}

.mobile-sidebar {
	position: fixed;
	top: 0;
	left: 0;
	width: 280px;
	height: 100vh;
	background: #fff;
	z-index: 999;
	transform: translateX(-100%);
	transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
	display: flex;
	flex-direction: column;
	box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
	will-change: transform;
}

.mobile-sidebar.active {
	transform: translateX(0);
}

.mobile-sidebar-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 16px 20px;
	border-bottom: 1px solid #e2e8f0;
	background: #0f172a;
	color: #fff;
}

.mobile-sidebar-header .brand {
	font-weight: 700;
	font-size: 18px;
}

.close-btn {
	background: none;
	border: none;
	color: #fff;
	cursor: pointer;
	padding: 4px;
	border-radius: 4px;
	transition: background-color 0.15s ease;
	will-change: background-color;
}

.close-btn:hover {
	background: rgba(255, 255, 255, 0.1);
}

.mobile-nav {
	flex: 1;
	padding: 20px 0;
	overflow-y: auto;
	-webkit-overflow-scrolling: touch;
	scroll-behavior: smooth;
	contain: layout style paint;
}

.mobile-nav .nav-link {
	display: flex;
	align-items: center;
	gap: 12px;
	padding: 12px 20px;
	color: #64748b;
	text-decoration: none;
	font-weight: 500;
	transition: background-color 0.15s ease, color 0.15s ease, border-left-color 0.15s ease;
	border-left: 3px solid transparent;
	will-change: background-color;
}

.mobile-nav .nav-link:hover,
.mobile-nav .nav-link.router-link-active {
	background: #f8fafc;
	color: #0f172a;
	border-left-color: #6366f1;
}

.mobile-nav .nav-link svg {
	flex-shrink: 0;
}

.logout-btn {
	background: none;
	border: none;
	width: 100%;
	text-align: left;
	cursor: pointer;
}

.logout-btn:hover {
	background: #f8fafc;
	color: #0f172a;
	border-left-color: #ef4444;
}

.mobile-sidebar-footer {
	padding: 20px;
	border-top: 1px solid #e2e8f0;
}

/* Responsive Design */
@media (max-width: 768px) {
	.mobile-menu-toggle {
		display: block;
	}

	.desktop-nav {
		display: none;
	}

	.container {
		padding: 0 12px;
	}

	.page {
		padding: 12px 0;
	}
}

@media (max-width: 480px) {
	.mobile-sidebar {
		width: 100%;
	}

	.container {
		padding: 0 8px;
	}
}

/* Performance optimizations for reduced motion */
@media (prefers-reduced-motion: reduce) {

	.mobile-sidebar,
	.mobile-overlay,
	.desktop-nav .nav-link,
	.mobile-nav .nav-link,
	.mobile-menu-toggle,
	.close-btn {
		transition: none !important;
		animation: none !important;
	}

	.mobile-sidebar {
		transform: translateX(-100%);
	}

	.mobile-sidebar.active {
		transform: translateX(0);
	}
}
</style>
