package com.couragegang.knowledge.api;

import com.couragegang.knowledge.api.dto.KnowledgeModels.ConnectorListResponse;
import com.couragegang.knowledge.api.dto.KnowledgeModels.ReindexResponse;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SearchRequest;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SearchResponse;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SourceCreateRequest;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SourceListResponse;
import com.couragegang.knowledge.api.dto.KnowledgeModels.SourceView;
import com.couragegang.knowledge.service.KnowledgeService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import jakarta.validation.Valid;
import java.util.UUID;

@Controller
public class KnowledgeController {

    private final KnowledgeService knowledge;

    public KnowledgeController(KnowledgeService knowledge) {
        this.knowledge = knowledge;
    }

    @Get("/connectors")
    public ConnectorListResponse connectors() {
        return knowledge.listConnectors();
    }

    @Get("/workspaces/{workspaceId}/sources")
    public SourceListResponse listSources(
            @PathVariable UUID workspaceId, @io.micronaut.http.annotation.QueryValue("org_id") UUID orgId) {
        return knowledge.listSources(orgId, workspaceId);
    }

    @Post("/workspaces/{workspaceId}/sources")
    public HttpResponse<SourceView> createSource(
            @PathVariable UUID workspaceId,
            @io.micronaut.http.annotation.QueryValue("org_id") UUID orgId,
            @Body @Valid SourceCreateRequest body) {
        return HttpResponse.created(knowledge.createSource(orgId, workspaceId, body));
    }

    @Post("/sources/{sourceId}/reindex")
    public ReindexResponse reindex(@PathVariable UUID sourceId) {
        return knowledge.reindex(sourceId);
    }

    @Post("/search")
    public SearchResponse search(@Body @Valid SearchRequest body) {
        return knowledge.search(body);
    }
}
