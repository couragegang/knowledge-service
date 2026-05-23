package com.couragegang.knowledge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.couragegang.knowledge.api.dto.KnowledgeModels.SearchRequest;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SourceCreateRequest;
import com.couragegang.knowledge.repo.KnowledgeRepository;
import com.couragegang.knowledge.repo.KnowledgeRepository.ConnectorRow;
import com.couragegang.knowledge.repo.KnowledgeRepository.SearchRow;
import com.couragegang.knowledge.repo.KnowledgeRepository.SourceRow;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KnowledgeServiceTest {

    @Mock
    KnowledgeRepository repo;

    KnowledgeService svc;
    UUID orgId = UUID.randomUUID();
    UUID wsId = UUID.randomUUID();
    UUID sourceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        svc = new KnowledgeService(repo);
    }

    @Test
    void listConnectors() throws Exception {
        when(repo.listConnectors()).thenReturn(List.of(new ConnectorRow("notion", "Notion", "mcp")));

        var res = svc.listConnectors();

        assertThat(res.items()).hasSize(1);
        assertThat(res.items().getFirst().connectorKey()).isEqualTo("notion");
    }

    @Test
    void createSourceIndexesSample() throws Exception {
        when(repo.insertSource(orgId, wsId, "notion", "Docs", null)).thenReturn(sourceId);
        when(repo.listSources(orgId, wsId)).thenReturn(List.of(sourceRow()));

        var view = svc.createSource(orgId, wsId, new SourceCreateRequest("notion", "Docs", null));

        assertThat(view.id()).isEqualTo(sourceId);
        verify(repo).upsertSampleDocument(eq(sourceId), eq("notion"), eq("Docs"), any());
        verify(repo).touchSourceIndexed(sourceId);
    }

    @Test
    void searchReturnsHits() throws Exception {
        when(repo.search(orgId, wsId, "hello", 20))
                .thenReturn(List.of(new SearchRow(UUID.randomUUID(), sourceId, "notion", "Title", 0.9)));

        var res = svc.search(new SearchRequest(orgId, wsId, "hello", null, null));

        assertThat(res.items()).hasSize(1);
    }

    @Test
    void reindexNotFound() throws Exception {
        when(repo.findSource(sourceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> svc.reindex(sourceId)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reindexOk() throws Exception {
        when(repo.findSource(sourceId)).thenReturn(Optional.of(sourceRow()));
        when(repo.upsertSampleDocument(any(), any(), any(), any())).thenReturn(1);

        var res = svc.reindex(sourceId);

        assertThat(res.status()).isEqualTo("ok");
        verify(repo).touchSourceIndexed(sourceId);
    }

    private SourceRow sourceRow() {
        return new SourceRow(sourceId, orgId, wsId, "notion", "Docs", "active", null, Instant.now());
    }
}
