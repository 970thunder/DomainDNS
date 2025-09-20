/**
 * API 请求工具
 * 统一处理所有 API 请求，包括环境配置、错误处理、认证等
 */

import { API_CONFIG, ERROR_CODES, ERROR_MESSAGES, DEBUG_CONFIG, STORAGE_CONFIG } from '@/config/env.js'

/**
 * 创建完整的 API URL
 * @param {string} endpoint - API 端点
 * @returns {string} 完整的 API URL
 */
const createApiUrl = (endpoint) => {
    // 确保 endpoint 以 / 开头
    const normalizedEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`
    return `${API_CONFIG.BASE_URL}${normalizedEndpoint}`
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
        ...fetchOptions
    } = options

    const url = createApiUrl(endpoint)
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
