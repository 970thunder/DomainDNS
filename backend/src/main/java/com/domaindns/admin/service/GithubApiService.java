package com.domaindns.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

/**
 * GitHub API服务
 */
@Service
public class GithubApiService {

    private static final Logger logger = LoggerFactory.getLogger(GithubApiService.class);
    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String GITHUB_API_VERSION = "application/vnd.github.v3+json";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${github.token:}")
    private String githubToken;

    public GithubApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 检查用户是否Star了指定仓库
     * 
     * @param username GitHub用户名
     * @param owner    仓库所有者
     * @param repo     仓库名称
     * @return true如果已Star，false如果未Star或检查失败
     */
    public boolean checkUserStarredRepository(String username, String owner, String repo) {
        // 检查是否有GitHub Token
        if (githubToken == null || githubToken.trim().isEmpty()) {
            logger.error("GitHub Token未配置，无法检查Star状态");
            return false;
        }

        try {
            // 使用正确的GitHub API端点：检查认证用户是否star了指定仓库
            String url = String.format("%s/user/starred/%s/%s", GITHUB_API_BASE, owner, repo);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            // GitHub API返回204表示已Star，200表示其他状态
            if (response.getStatusCode() == HttpStatus.NO_CONTENT || response.getStatusCode() == HttpStatus.OK) {
                logger.info("用户已Star仓库 {}/{}", owner, repo);
                return true;
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // 404表示用户未Star该仓库
                logger.info("用户未Star仓库 {}/{}", owner, repo);
                return false;
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.error("GitHub API认证失败，请检查Token是否有效: {}", e.getMessage());
                return false;
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.error("GitHub API访问被禁止，可能是Token权限不足: {}", e.getMessage());
                return false;
            }
            logger.error("检查GitHub Star状态失败: {} - {}", e.getStatusCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("检查GitHub Star状态时发生异常: {}", e.getMessage());
            return false;
        }

        return false;
    }

    /**
     * 获取仓库信息
     * 
     * @param owner 仓库所有者
     * @param repo  仓库名称
     * @return 仓库信息JSON
     */
    public JsonNode getRepositoryInfo(String owner, String repo) {
        try {
            String url = String.format("%s/repos/%s/%s", GITHUB_API_BASE, owner, repo);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readTree(response.getBody());
            }

        } catch (Exception e) {
            logger.error("获取GitHub仓库信息失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 验证GitHub用户名是否存在
     * 
     * @param username GitHub用户名
     * @return true如果用户存在，false如果不存在
     */
    public boolean validateGithubUsername(String username) {
        try {
            String url = String.format("%s/users/%s", GITHUB_API_BASE, username);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            return response.getStatusCode() == HttpStatus.OK;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            logger.error("验证GitHub用户名失败: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("验证GitHub用户名时发生异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户的公开仓库列表
     * 
     * @param username GitHub用户名
     * @return 仓库列表JSON
     */
    public JsonNode getUserRepositories(String username) {
        try {
            String url = String.format("%s/users/%s/repos?type=public&sort=updated&per_page=10",
                    GITHUB_API_BASE, username);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readTree(response.getBody());
            }

        } catch (Exception e) {
            logger.error("获取用户GitHub仓库列表失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 创建HTTP请求头
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", GITHUB_API_VERSION);
        headers.set("User-Agent", "DomainDNS-App/1.0");

        // 如果有GitHub Token，添加到请求头中
        if (githubToken != null && !githubToken.trim().isEmpty()) {
            String auth = "token " + githubToken;
            headers.set("Authorization", auth);
        }

        return headers;
    }

    /**
     * 解析GitHub URL获取owner和repo
     * 
     * @param repositoryUrl GitHub仓库URL
     * @return 包含owner和repo的数组，如果解析失败返回null
     */
    public String[] parseRepositoryUrl(String repositoryUrl) {
        try {
            // 支持多种GitHub URL格式
            // https://github.com/owner/repo
            // https://github.com/owner/repo.git
            // git@github.com:owner/repo.git

            String cleanUrl = repositoryUrl.trim();

            // 处理git@github.com:owner/repo.git格式
            if (cleanUrl.startsWith("git@github.com:")) {
                cleanUrl = cleanUrl.replace("git@github.com:", "https://github.com/");
            }

            // 移除.git后缀
            if (cleanUrl.endsWith(".git")) {
                cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 4);
            }

            // 解析URL
            if (cleanUrl.contains("github.com/")) {
                String[] parts = cleanUrl.split("github.com/");
                if (parts.length == 2) {
                    String[] pathParts = parts[1].split("/");
                    if (pathParts.length >= 2) {
                        return new String[] { pathParts[0], pathParts[1] };
                    }
                }
            }

        } catch (Exception e) {
            logger.error("解析GitHub URL失败: {}", e.getMessage());
        }

        return null;
    }
}
