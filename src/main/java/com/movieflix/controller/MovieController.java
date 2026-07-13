package com.movieflix.controller;

import com.movieflix.controller.request.MovieRequest;
import com.movieflix.controller.response.MovieResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Movie", description = "Recurso responsável pelo gerenciamento do catálogo de filmes.")
@SecurityRequirement(name = "bearerAuth")
public interface MovieController {

    @Operation(summary = "Cadastrar filme", description = "Cria um novo filme no catálogo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme criado com sucesso",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<MovieResponse> save(@Valid @RequestBody MovieRequest request);

    @Operation(summary = "Listar todos os filmes", description = "Retorna uma lista com todos os filmes cadastrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MovieResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<List<MovieResponse>> findAll();

    @Operation(summary = "Buscar filme por ID", description = "Retorna os dados de um filme a partir do seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme encontrado",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<MovieResponse> findById(
            @Parameter(description = "ID do filme", required = true) @PathVariable Long id);

    @Operation(summary = "Atualizar filme", description = "Atualiza os dados de um filme existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = MovieResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição", content = @Content),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<MovieResponse> findById(
            @Parameter(description = "ID do filme", required = true) @PathVariable Long id,
            @Valid @RequestBody MovieRequest request);

    @Operation(summary = "Buscar filmes por categoria", description = "Retorna todos os filmes de uma determinada categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MovieResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<List<MovieResponse>> findByCategory(
            @Parameter(description = "ID da categoria", required = true) @RequestParam Long category);

    @Operation(summary = "Deletar filme", description = "Remove um filme do catálogo pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Filme deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "ID do filme", required = true) @PathVariable Long id);
}
