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
        try {
            // 归一化入参
            String u = username == null ? "" : username.trim();
            String o = owner == null ? "" : owner.trim();
            String r = repo == null ? "" : repo.trim();
            // 使用公开端点：检查“指定用户名”是否 star 了仓库
            // 参考：GET /users/{username}/starred/{owner}/{repo}
            String url = String.format("%s/users/%s/starred/%s/%s", GITHUB_API_BASE, u, o, r);

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // GitHub 返回 204 表示已 Star（有时会返回 304 缓存命中）；个别代理会返回 200
            if (response.getStatusCode() == HttpStatus.NO_CONTENT
                    || response.getStatusCode() == HttpStatus.OK
                    || response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
                logger.info("{} 已 Star {}/{}", u, o, r);
                return true;
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // 404 表示该用户未 star 该仓库
                logger.info("{} 未 Star {}/{} (或用户设置了隐私星标)", username, owner, repo);
                // 无论是否配置 token，都做一次 stargazers 兜底（列表是公开信息）
                return checkViaStargazers(username, owner, repo);
            }
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.warn("GitHub API 访问受限（可能触发速率限制或权限不足）：{}", e.getMessage());
                // 发生限流时可退化为不通过，前端提示稍后再试
                return false;
            }
            logger.error("检查 GitHub Star 状态失败：{} - {}", e.getStatusCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("检查 GitHub Star 状态异常：{}", e.getMessage());
            return false;
        }

        return false;
    }

    /**
     * 兜底方案：遍历 stargazers 列表判断是否包含该用户名（公开接口，速率更宽松）。
     * 仅检查前若干页以避免过度调用。
     */
    private boolean checkViaStargazers(String username, String owner, String repo) {
        try {
            int perPage = 100;
            int maxPages = 10; // 最多检查 1000 人
            for (int page = 1; page <= maxPages; page++) {
                String url = String.format("%s/repos/%s/%s/stargazers?per_page=%d&page=%d", GITHUB_API_BASE, owner,
                        repo, perPage, page);
                HttpHeaders headers = createHeaders();
                // stargazers 列表需使用新版 Accept
                headers.set("Accept", "application/vnd.github+json");
                headers.set("X-GitHub-Api-Version", "2022-11-28");
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                if (resp.getStatusCode() != HttpStatus.OK) {
                    break;
                }
                JsonNode arr = objectMapper.readTree(resp.getBody());
                if (arr == null || !arr.isArray() || arr.size() == 0) {
                    break; // 没有更多
                }
                for (JsonNode n : arr) {
                    String login = n.path("login").asText();
                    if (username != null && username.equalsIgnoreCase(login)) {
                        logger.info("(stargazers) {} 已 Star {}/{}", username, owner, repo);
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("stargazers 兜底检查失败: {}", ex.getMessage());
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
        // 统一使用新版 Accept 与 API 版本头，避免 404/行为差异
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
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
