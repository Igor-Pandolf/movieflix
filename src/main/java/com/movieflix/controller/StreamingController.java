package com.movieflix.controller;

import com.movieflix.controller.request.StreamingRequest;
import com.movieflix.controller.response.StreamingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Streaming", description = "Recurso responsável pelo gerenciamento das plataformas de streaming.")
@SecurityRequirement(name = "bearerAuth")
public interface StreamingController {

    @Operation(summary = "Listar todos os streamings", description = "Retorna uma lista com todas as plataformas de streaming cadastradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StreamingResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<List<StreamingResponse>> getAllStreamings();

    @Operation(summary = "Cadastrar streaming", description = "Cadastra uma nova plataforma de streaming.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Streaming criado com sucesso",
                    content = @Content(schema = @Schema(implementation = StreamingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<StreamingResponse> saveStreaming(@Valid @RequestBody StreamingRequest request);

    @Operation(summary = "Buscar streaming por ID", description = "Retorna os dados de uma plataforma de streaming pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Streaming encontrado",
                    content = @Content(schema = @Schema(implementation = StreamingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Streaming não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<StreamingResponse> getByStreamingId(
            @Parameter(description = "ID do streaming", required = true) @PathVariable Long id);

    @Operation(summary = "Deletar streaming", description = "Remove uma plataforma de streaming pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Streaming deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Streaming não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<Void> deleteByStreamingId(
            @Parameter(description = "ID do streaming", required = true) @PathVariable Long id);
}
