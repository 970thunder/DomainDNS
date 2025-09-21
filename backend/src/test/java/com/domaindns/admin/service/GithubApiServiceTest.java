package com.domaindns.admin.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * GitHub API服务测试
 */
@SpringBootTest
@TestPropertySource(properties = {
        "github.token=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"
})
public class GithubApiServiceTest {

    @Autowired
    private GithubApiService githubApiService;

    @Test
    public void testCheckUserStarredRepository() {
        // 测试检查用户是否Star了指定仓库
        String owner = "Tsundeer";
        String repo = "MeowField_AutoPiano";

        boolean isStarred = githubApiService.checkUserStarredRepository("test", owner, repo);

        System.out.println("用户是否Star了仓库 " + owner + "/" + repo + ": " + isStarred);
    }
}
