import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ command, mode }) => {
	// 加载环境变量
	const env = loadEnv(mode, process.cwd(), '')

	return {
		plugins: [vue()],
		server: {
			host: true,
			port: 3000,
			// 排除原型文件目录
			fs: {
				deny: ['**/prototype/**']
			},
			proxy: {
				// 开发环境代理配置
				'/api': {
					target: env.VITE_API_BASE_URL || 'http://localhost:8080',
					changeOrigin: true,
					secure: false,
				}
			}
		},
		build: {
			// 生产环境构建配置
			outDir: 'dist',
			assetsDir: 'assets',
			sourcemap: mode === 'development',
			rollupOptions: {
				// 排除原型文件
				external: (id) => {
					return id.includes('/prototype/')
				},
				output: {
					// 分包配置
					manualChunks: {
						vendor: ['vue', 'vue-router', 'pinia'],
						element: ['element-plus']
					}
				}
			}
		},
		resolve: {
			alias: {
				'@': resolve(__dirname, 'src')
			}
		},
		define: {
			// 定义全局常量
			__APP_VERSION__: JSON.stringify(process.env.npm_package_version),
		}
	}
})
