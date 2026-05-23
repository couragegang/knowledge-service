package com.couragegang.knowledge.service;

import com.couragegang.knowledge.api.dto.KnowledgeModels.ConnectorListResponse;
import com.couragegang.knowledge.api.dto.KnowledgeModels.ConnectorView;
import com.couragegang.knowledge.api.dto.KnowledgeModels.ReindexResponse;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SearchHit;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SearchRequest;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SearchResponse;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SourceCreateRequest;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SourceListResponse;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SourceView;
import com.couragegang.knowledge.repo.KnowledgeRepository;
import jakarta.inject.Singleton;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

@Singleton
public final class KnowledgeService {

    private final KnowledgeRepository repo;

    public KnowledgeService(KnowledgeRepository repo) {
        this.repo = repo;
    }

    public ConnectorListResponse listConnectors() {
        try {
            var items = new ArrayList<ConnectorView>();
            for (var row : repo.listConnectors()) {
                items.add(new ConnectorView(row.connectorKey(), row.displayName(), row.ingestMode()));
            }
            return new ConnectorListResponse(items);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public SourceView createSource(UUID orgId, UUID workspaceId, SourceCreateRequest req) {
        try {
            var id = repo.insertSource(orgId, workspaceId, req.connectorKey(), req.displayName(), req.mcpInstallationId());
            repo.upsertSampleDocument(id, req.connectorKey(), req.displayName(), "Indexed sample content for " + req.displayName());
            repo.touchSourceIndexed(id);
            return listSources(orgId, workspaceId).items().stream()
                    .filter(s -> s.id().equals(id))
                    .findFirst()
                    .orElseThrow();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public SourceListResponse listSources(UUID orgId, UUID workspaceId) {
        try {
            var items = new ArrayList<SourceView>();
            for (var row : repo.listSources(orgId, workspaceId)) {
                items.add(
                        new SourceView(
                                row.id(),
                                row.orgId(),
                                row.workspaceId(),
                                row.connectorKey(),
                                row.displayName(),
                                row.status(),
                                row.mcpInstallationId(),
                                row.lastIndexedAt()));
            }
            return new SourceListResponse(items);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public SearchResponse search(SearchRequest req) {
        try {
            var rows = repo.search(req.orgId(), req.workspaceId(), req.query(), 20);
            var items = new ArrayList<SearchHit>();
            for (var row : rows) {
                items.add(
                        new SearchHit(
                                row.documentId(),
                                row.sourceId(),
                                row.connectorKey(),
                                row.title(),
                                row.title(),
                                row.score()));
            }
            return new SearchResponse(items);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public ReindexResponse reindex(UUID sourceId) {
        try {
            var s = repo.findSource(sourceId).orElseThrow(() -> new IllegalArgumentException("not found"));
            var n =
                    repo.upsertSampleDocument(
                            sourceId, s.connectorKey(), s.displayName(), "Reindexed at " + java.time.Instant.now());
            repo.touchSourceIndexed(sourceId);
            return new ReindexResponse(sourceId, n, "ok");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
