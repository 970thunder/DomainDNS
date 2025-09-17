package com.domaindns.cf.service;

import com.domaindns.cf.mapper.CfAccountMapper;
import com.domaindns.cf.mapper.ZoneMapper;
import com.domaindns.cf.model.CfAccount;
import com.domaindns.cf.model.Zone;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ZoneService {
    private static final Logger log = LoggerFactory.getLogger(ZoneService.class);
    private final ZoneMapper zoneMapper;
    private final CfAccountMapper accMapper;
    private final CfClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ZoneService(ZoneMapper zoneMapper, CfAccountMapper accMapper, CfClient client) {
        this.zoneMapper = zoneMapper;
        this.accMapper = accMapper;
        this.client = client;
    }

    public int syncAll() {
        return accMapper.findAll(1).stream().mapToInt(a -> syncByAccount(a.getId())).sum();
    }

    public int syncByAccount(Long cfAccountId) {
        CfAccount a = accMapper.findById(cfAccountId);
        if (a == null)
            throw new IllegalArgumentException("Cloudflare 账户不存在: " + cfAccountId);
        if (a.getEnabled() == null || a.getEnabled() == 0) {
            log.debug("CF account {} disabled, skip sync", cfAccountId);
            return 0;
        }
        int page = 1, per = 50;
        int totalSaved = 0;
        while (true) {
            String json = client.listZones(a, page, per).block();
            try {
                JsonNode root = objectMapper.readTree(json);
                if (!root.path("success").asBoolean(true)) {
                    String err = root.path("errors").toString();
                    log.warn("CF listZones error: {}", err);
                    throw new IllegalStateException("Cloudflare API 错误: " + err);
                }
                JsonNode result = root.get("result");
                int pageSaved = 0;
                if (result != null && result.isArray()) {
                    for (JsonNode n : result) {
                        Zone z = new Zone();
                        z.setCfAccountId(cfAccountId);
                        z.setZoneId(n.path("id").asText());
                        z.setName(n.path("name").asText());
                        z.setStatus(n.path("status").asText("unknown"));
                        z.setEnabled(0);
                        z.setSyncedAt(LocalDateTime.now());
                        zoneMapper.upsert(z);
                        pageSaved++;
                    }
                }
                totalSaved += pageSaved;
                log.debug("zones sync page {} saved {}", page, pageSaved);
                int totalPages = root.path("result_info").path("total_pages").asInt(-1);
                boolean hasMore;
                if (totalPages >= 1) {
                    hasMore = page < totalPages;
                } else {
                    // 无 result_info 时，按是否满页判断
                    hasMore = pageSaved == per;
                }
                if (!hasMore || pageSaved == 0)
                    break;
                page++;
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                log.error("解析 Cloudflare zones 响应失败(page={}): {}", page, e.getMessage(), e);
                throw new IllegalStateException("解析 Cloudflare 响应失败");
            }
        }
        return totalSaved;
    }

    public List<Zone> list(Integer enabled, String name, Long cfAccountId) {
        return zoneMapper.list(enabled, name, cfAccountId);
    }

    public void setEnabled(Long id, boolean enabled) {
        zoneMapper.setEnabled(id, enabled ? 1 : 0);
    }
}
