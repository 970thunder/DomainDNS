/**
 * API 请求工具
 * 统一处理所有 API 请求，包括环境配置、错误处理、认证等
 */

import { API_CONFIG, ERROR_CODES, ERROR_MESSAGES, DEBUG_CONFIG, STORAGE_CONFIG, APP_CONFIG } from '@/config/env.js'

// Token过期事件防抖
let tokenExpiredEventTriggered = false
const TOKEN_EXPIRED_DEBOUNCE_TIME = 5000 // 5秒内不重复触发

/**
 * 创建完整的 API URL
 * @param {string} endpoint - API 端点
 * @returns {string} 完整的 API URL
 */
const createApiUrl = (endpoint) => {
    // 确保 endpoint 以 / 开头
    const normalizedEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`

    // 避免双斜杠问题
    const baseUrl = API_CONFIG.BASE_URL.endsWith('/') ? API_CONFIG.BASE_URL.slice(0, -1) : API_CONFIG.BASE_URL
    const endpointPath = normalizedEndpoint.startsWith('/') ? normalizedEndpoint : `/${normalizedEndpoint}`

    const finalUrl = `${baseUrl}${endpointPath}`

    // 开发环境调试日志
    if (APP_CONFIG.IS_DEV) {
        console.log('API URL构建:', {
            baseUrl,
            endpoint,
            normalizedEndpoint,
            endpointPath,
            finalUrl
        })
    }

    return finalUrl
}

/**
 * 获取认证头
 * @param {string} token - JWT token
 * @returns {Object} 认证头对象
 */
const getAuthHeaders = (token) => {
    const headers = { ...API_CONFIG.HEADERS }
    if (token) {
        headers['Authorization'] = `Bearer ${token}`
    }
    return headers
}

/**
 * 处理 API 响应
 * @param {Response} response - Fetch 响应对象
 * @returns {Promise<Object>} 处理后的响应数据
 */
const handleResponse = async (response) => {
    try {
        const data = await response.json()

        // 开发环境日志
        if (DEBUG_CONFIG.SHOW_API_DETAILS) {
            console.log('API Response:', {
                url: response.url,
                status: response.status,
                data: data
            })
        }

        // 检查JWT token过期 - 只检查特定的token过期消息
        // 排除登录相关的API调用，避免在登录过程中误触发
        const isLoginRelated = response.url.includes('/auth/login') ||
            response.url.includes('/auth/register') ||
            response.url.includes('/auth/send-code')

        if (!isLoginRelated && data.message && (
            data.message.includes('JWT expired at') ||
            data.message.includes('Token无效或已过期') ||
            (data.message.includes('Token已过期') && !data.message.includes('验证码'))
        )) {
            console.warn('检测到token过期:', data.message)

            // 防抖处理，避免短时间内多次触发
            if (!tokenExpiredEventTriggered) {
                tokenExpiredEventTriggered = true
                // 触发token过期事件
                window.dispatchEvent(new CustomEvent('token-expired'))

                // 5秒后重置防抖标志
                setTimeout(() => {
                    tokenExpiredEventTriggered = false
                }, TOKEN_EXPIRED_DEBOUNCE_TIME)
            }

            throw new Error('Token已过期，请重新登录')
        }

        return data
    } catch (error) {
        console.error('解析响应数据失败:', error)
        throw new Error(ERROR_MESSAGES.NETWORK_ERROR)
    }
}

/**
 * 处理 API 错误
 * @param {Object} errorData - 错误数据
 * @returns {string} 错误消息
 */
const handleError = (errorData) => {
    const { code, message } = errorData

    // 开发环境日志
    if (DEBUG_CONFIG.ENABLE_LOG) {
        console.error('API Error:', errorData)
    }

    // 根据错误码返回对应消息
    if (ERROR_MESSAGES[code]) {
        return ERROR_MESSAGES[code]
    }

    return message || ERROR_MESSAGES.UNKNOWN_ERROR
}

/**
 * 基础 API 请求方法
 * @param {string} endpoint - API 端点
 * @param {Object} options - 请求选项
 * @returns {Promise<Object>} API 响应数据
 */
const apiRequest = async (endpoint, options = {}) => {
    const {
        method = 'GET',
        data = null,
        token = null,
        timeout = API_CONFIG.TIMEOUT,
        params = null,
        ...fetchOptions
    } = options

    let url = createApiUrl(endpoint)

    // 处理查询参数
    if (params && method === 'GET') {
        const searchParams = new URLSearchParams()
        Object.keys(params).forEach(key => {
            if (params[key] !== null && params[key] !== undefined) {
                searchParams.append(key, params[key])
            }
        })
        const queryString = searchParams.toString()
        if (queryString) {
            url += `?${queryString}`
        }
    }

    const headers = getAuthHeaders(token)

    // 请求配置
    const requestConfig = {
        method,
        headers,
        ...fetchOptions
    }

    // 添加请求体
    if (data && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
        requestConfig.body = JSON.stringify(data)
    }

    // 开发环境日志
    if (DEBUG_CONFIG.SHOW_API_DETAILS) {
        console.log('API Request:', {
            url,
            method,
            headers,
            data
        })
    }

    try {
        // 创建超时控制器
        const controller = new AbortController()
        const timeoutId = setTimeout(() => controller.abort(), timeout)

        const response = await fetch(url, {
            ...requestConfig,
            signal: controller.signal
        })

        clearTimeout(timeoutId)

        const result = await handleResponse(response)

        // 检查业务错误码
        if (result.code !== ERROR_CODES.SUCCESS) {
            throw new Error(handleError(result))
        }

        return result
    } catch (error) {
        if (error.name === 'AbortError') {
            throw new Error('请求超时，请稍后重试')
        }

        if (error.message) {
            throw error
        }

        throw new Error(ERROR_MESSAGES.NETWORK_ERROR)
    }
}

/**
 * GET 请求
 * @param {string} endpoint - API 端点
 * @param {Object} options - 请求选项
 * @returns {Promise<Object>} API 响应数据
 */
export const apiGet = (endpoint, options = {}) => {
    return apiRequest(endpoint, { ...options, method: 'GET' })
}

/**
 * POST 请求
 * @param {string} endpoint - API 端点
 * @param {Object} data - 请求数据
 * @param {Object} options - 请求选项
 * @returns {Promise<Object>} API 响应数据
 */
export const apiPost = (endpoint, data, options = {}) => {
    return apiRequest(endpoint, { ...options, method: 'POST', data })
}

/**
 * PUT 请求
 * @param {string} endpoint - API 端点
 * @param {Object} data - 请求数据
 * @param {Object} options - 请求选项
 * @returns {Promise<Object>} API 响应数据
 */
export const apiPut = (endpoint, data, options = {}) => {
    return apiRequest(endpoint, { ...options, method: 'PUT', data })
}

/**
 * DELETE 请求
 * @param {string} endpoint - API 端点
 * @param {Object} options - 请求选项
 * @returns {Promise<Object>} API 响应数据
 */
export const apiDelete = (endpoint, options = {}) => {
    return apiRequest(endpoint, { ...options, method: 'DELETE' })
}

/**
 * 带认证的请求方法
 * @param {string} token - JWT token
 * @returns {Object} 带认证的请求方法对象
 */
export const createAuthenticatedApi = (token) => {
    return {
        get: (endpoint, options = {}) => apiGet(endpoint, { ...options, token }),
        post: (endpoint, data, options = {}) => apiPost(endpoint, data, { ...options, token }),
        put: (endpoint, data, options = {}) => apiPut(endpoint, data, { ...options, token }),
        delete: (endpoint, options = {}) => apiDelete(endpoint, { ...options, token }),
    }
}

// 导出配置常量
export { ERROR_CODES, STORAGE_CONFIG, API_CONFIG, DEBUG_CONFIG }

// 导出默认 API 方法
export default {
    get: apiGet,
    post: apiPost,
    put: apiPut,
    delete: apiDelete,
    createAuthenticatedApi,
}
