package com.couragegang.knowledge.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class KnowledgeModels {

    private KnowledgeModels() {}

    @Serdeable
    public record ConnectorView(String connectorKey, String displayName, String ingestMode) {}

    @Serdeable
    public record ConnectorListResponse(List<ConnectorView> items) {}

    @Serdeable
    public record SourceCreateRequest(
            @NotBlank String connectorKey,
            @NotBlank String displayName,
            @Nullable UUID mcpInstallationId) {}

    @Serdeable
    public record SourceView(
            UUID id,
            UUID orgId,
            UUID workspaceId,
            String connectorKey,
            String displayName,
            String status,
            @Nullable UUID mcpInstallationId,
            @Nullable Instant lastIndexedAt) {}

    @Serdeable
    public record SourceListResponse(List<SourceView> items) {}

    @Serdeable
    public record SearchRequest(
            @NotNull UUID orgId,
            @NotNull UUID workspaceId,
            @NotBlank String query,
            @Nullable List<String> connectorKeys,
            @Nullable List<UUID> sourceIds) {}

    @Serdeable
    public record SearchHit(
            UUID documentId,
            UUID sourceId,
            String connectorKey,
            String title,
            String snippet,
            double score) {}

    @Serdeable
    public record SearchResponse(List<SearchHit> items) {}

    @Serdeable
    public record ReindexResponse(UUID sourceId, int documentsIndexed, String status) {}
}
