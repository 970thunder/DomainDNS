package com.domaindns.cf.service;

import com.domaindns.cf.dto.CfAccountDtos.CreateReq;
import com.domaindns.cf.dto.CfAccountDtos.ItemResp;
import com.domaindns.cf.dto.CfAccountDtos.UpdateReq;
import com.domaindns.cf.mapper.CfAccountMapper;
import com.domaindns.cf.model.CfAccount;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CfAccountService {
    private final CfAccountMapper mapper;
    private final CfClient client;

    public CfAccountService(CfAccountMapper mapper, CfClient client) {
        this.mapper = mapper;
        this.client = client;
    }

    public Long create(CreateReq req) {
        String type = req.apiType == null ? "GLOBAL_KEY" : req.apiType;
        CfAccount exists = mapper.findByEmailAndType(req.email, type);
        if (exists != null)
            return exists.getId();
        CfAccount a = new CfAccount();
        a.setName(req.name);
        a.setEmail(req.email);
        a.setApiType(type);
        a.setApiKey(req.apiKey);
        a.setEnabled(req.enabled == null || req.enabled ? 1 : 0);
        mapper.insert(a);
        return a.getId();
    }

    public void update(Long id, UpdateReq req) {
        CfAccount a = new CfAccount();
        a.setId(id);
        a.setName(req.name);
        a.setEmail(req.email);
        a.setApiType(req.apiType);
        a.setApiKey(req.apiKey);
        a.setEnabled(req.enabled == null ? null : (req.enabled ? 1 : 0));
        mapper.update(a);
    }

    public void delete(Long id) {
        mapper.delete(id);
    }

    public ItemResp get(Long id) {
        CfAccount a = mapper.findById(id);
        return toItem(a);
    }

    public List<ItemResp> list(Integer enabled) {
        return mapper.findAll(enabled).stream().map(this::toItem).collect(Collectors.toList());
    }

    public void setEnabled(Long id, boolean enabled) {
        CfAccount a = new CfAccount();
        a.setId(id);
        a.setEnabled(enabled ? 1 : 0);
        mapper.update(a);
    }

    public boolean test(Long id) {
        CfAccount a = mapper.findById(id);
        if (a == null)
            throw new IllegalArgumentException("账户不存在");
        try {
            client.listZones(a, 1, 1).block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private ItemResp toItem(CfAccount a) {
        if (a == null)
            return null;
        ItemResp i = new ItemResp();
        i.id = a.getId();
        i.name = a.getName();
        i.email = a.getEmail();
        i.apiType = a.getApiType();
        i.enabled = a.getEnabled();
        i.createdAt = a.getCreatedAt();
        return i;
    }
}
