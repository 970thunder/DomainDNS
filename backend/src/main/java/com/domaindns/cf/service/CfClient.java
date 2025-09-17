package com.domaindns.cf.service;

import com.domaindns.cf.model.CfAccount;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CfClient {
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.cloudflare.com/client/v4")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    private HttpHeaders buildHeaders(CfAccount acc) {
        HttpHeaders h = new HttpHeaders();
        if ("API_TOKEN".equalsIgnoreCase(acc.getApiType())) {
            h.add(HttpHeaders.AUTHORIZATION, "Bearer " + acc.getApiKey());
        } else {
            h.add("X-Auth-Email", acc.getEmail());
            h.add("X-Auth-Key", acc.getApiKey());
        }
        return h;
    }

    public Mono<String> listZones(CfAccount acc, int page, int perPage) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/zones").queryParam("page", page).queryParam("per_page", perPage)
                        .build())
                .headers(h -> h.addAll(buildHeaders(acc)))
                .retrieve().bodyToMono(String.class);
    }

    public Mono<String> listDnsRecords(CfAccount acc, String zoneId, int page, int perPage) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/zones/" + zoneId + "/dns_records").queryParam("page", page)
                        .queryParam("per_page", perPage).build())
                .headers(h -> h.addAll(buildHeaders(acc)))
                .retrieve().bodyToMono(String.class);
    }

    public Mono<String> createDnsRecord(CfAccount acc, String zoneId, String bodyJson) {
        return webClient.post().uri("/zones/" + zoneId + "/dns_records")
                .headers(h -> h.addAll(buildHeaders(acc)))
                .bodyValue(bodyJson)
                .retrieve().bodyToMono(String.class);
    }

    public Mono<String> updateDnsRecord(CfAccount acc, String zoneId, String recordId, String bodyJson) {
        return webClient.put().uri("/zones/" + zoneId + "/dns_records/" + recordId)
                .headers(h -> h.addAll(buildHeaders(acc)))
                .bodyValue(bodyJson)
                .retrieve().bodyToMono(String.class);
    }

    public Mono<String> deleteDnsRecord(CfAccount acc, String zoneId, String recordId) {
        return webClient.delete().uri("/zones/" + zoneId + "/dns_records/" + recordId)
                .headers(h -> h.addAll(buildHeaders(acc)))
                .retrieve().bodyToMono(String.class);
    }
}
