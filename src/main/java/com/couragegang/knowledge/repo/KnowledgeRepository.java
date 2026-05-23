package com.couragegang.knowledge.repo;

import jakarta.inject.Singleton;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;

@Singleton
public final class KnowledgeRepository {

    private final DataSource dataSource;

    public KnowledgeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ConnectorRow> listConnectors() throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        "SELECT connector_key, display_name, ingest_mode FROM connector_catalog ORDER BY connector_key");
                var rs = ps.executeQuery()) {
            var rows = new ArrayList<ConnectorRow>();
            while (rs.next()) {
                rows.add(
                        new ConnectorRow(
                                rs.getString("connector_key"),
                                rs.getString("display_name"),
                                rs.getString("ingest_mode")));
            }
            return rows;
        }
    }

    public UUID insertSource(
            UUID orgId,
            UUID workspaceId,
            String connectorKey,
            String displayName,
            UUID mcpInstallationId)
            throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        INSERT INTO knowledge_sources
                          (org_id, workspace_id, connector_key, display_name, mcp_installation_id)
                        VALUES (?, ?, ?, ?, ?)
                        RETURNING id
                        """)) {
            ps.setObject(1, orgId);
            ps.setObject(2, workspaceId);
            ps.setString(3, connectorKey);
            ps.setString(4, displayName);
            if (mcpInstallationId == null) {
                ps.setNull(5, Types.OTHER);
            } else {
                ps.setObject(5, mcpInstallationId);
            }
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getObject(1, UUID.class);
            }
        }
    }

    public Optional<SourceRow> findSource(UUID sourceId) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        SELECT id, org_id, workspace_id, connector_key, display_name, status,
                               mcp_installation_id, last_indexed_at
                        FROM knowledge_sources WHERE id = ?
                        """)) {
            ps.setObject(1, sourceId);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapSource(rs));
            }
        }
    }

    public List<SourceRow> listSources(UUID orgId, UUID workspaceId) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        SELECT id, org_id, workspace_id, connector_key, display_name, status,
                               mcp_installation_id, last_indexed_at
                        FROM knowledge_sources
                        WHERE org_id = ? AND workspace_id = ?
                        ORDER BY created_at DESC
                        """)) {
            ps.setObject(1, orgId);
            ps.setObject(2, workspaceId);
            var rows = new ArrayList<SourceRow>();
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapSource(rs));
                }
            }
            return rows;
        }
    }

    public int upsertSampleDocument(UUID sourceId, String connectorKey, String title, String body)
            throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        INSERT INTO knowledge_documents (source_id, connector_key, external_id, title, body)
                        VALUES (?, ?, ?, ?, ?)
                        ON CONFLICT (source_id, external_id) DO UPDATE
                          SET title = EXCLUDED.title, body = EXCLUDED.body, indexed_at = now()
                        """)) {
            ps.setObject(1, sourceId);
            ps.setString(2, connectorKey);
            ps.setString(3, "sample-1");
            ps.setString(4, title);
            ps.setString(5, body);
            return ps.executeUpdate();
        }
    }

    public void touchSourceIndexed(UUID sourceId) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        "UPDATE knowledge_sources SET last_indexed_at = now(), status = 'active' WHERE id = ?")) {
            ps.setObject(1, sourceId);
            ps.executeUpdate();
        }
    }

    public List<SearchRow> search(UUID orgId, UUID workspaceId, String query, int limit) throws SQLException {
        try (var c = dataSource.getConnection();
                var ps = c.prepareStatement(
                        """
                        SELECT d.id, d.source_id, d.connector_key, d.title,
                               ts_rank(
                                 to_tsvector('simple', coalesce(d.title, '') || ' ' || coalesce(d.body, '')),
                                 plainto_tsquery('simple', ?)
                               ) AS score
                        FROM knowledge_documents d
                        JOIN knowledge_sources s ON s.id = d.source_id
                        WHERE s.org_id = ? AND s.workspace_id = ?
                          AND to_tsvector('simple', coalesce(d.title, '') || ' ' || coalesce(d.body, ''))
                              @@ plainto_tsquery('simple', ?)
                        ORDER BY score DESC
                        LIMIT ?
                        """)) {
            ps.setString(1, query);
            ps.setObject(2, orgId);
            ps.setObject(3, workspaceId);
            ps.setString(4, query);
            ps.setInt(5, limit);
            var rows = new ArrayList<SearchRow>();
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(
                            new SearchRow(
                                    rs.getObject("id", UUID.class),
                                    rs.getObject("source_id", UUID.class),
                                    rs.getString("connector_key"),
                                    rs.getString("title"),
                                    rs.getDouble("score")));
                }
            }
            return rows;
        }
    }

    private SourceRow mapSource(java.sql.ResultSet rs) throws SQLException {
        var indexed = rs.getTimestamp("last_indexed_at");
        return new SourceRow(
                rs.getObject("id", UUID.class),
                rs.getObject("org_id", UUID.class),
                rs.getObject("workspace_id", UUID.class),
                rs.getString("connector_key"),
                rs.getString("display_name"),
                rs.getString("status"),
                rs.getObject("mcp_installation_id", UUID.class),
                indexed != null ? indexed.toInstant() : null);
    }

    public record ConnectorRow(String connectorKey, String displayName, String ingestMode) {}

    public record SourceRow(
            UUID id,
            UUID orgId,
            UUID workspaceId,
            String connectorKey,
            String displayName,
            String status,
            UUID mcpInstallationId,
            Instant lastIndexedAt) {}

    public record SearchRow(UUID documentId, UUID sourceId, String connectorKey, String title, double score) {}
}
