package com.movieflix.controller;

import com.movieflix.controller.request.CategoryRequest;
import com.movieflix.controller.response.CategoryResponse;
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

@Tag(name = "Category", description = "Recurso responsável pelo gerenciamento das categorias de filmes.")
@SecurityRequirement(name = "bearerAuth")
public interface CategoryController {

    @Operation(summary = "Listar todas as categorias", description = "Retorna uma lista com todas as categorias cadastradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<List<CategoryResponse>> getAllCategories();

    @Operation(summary = "Cadastrar categoria", description = "Cria uma nova categoria de filmes.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<CategoryResponse> saveCategory(@Valid @RequestBody CategoryRequest request);

    @Operation(summary = "Buscar categoria por ID", description = "Retorna os dados de uma categoria a partir do seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<CategoryResponse> getByCategoryId(
            @Parameter(description = "ID da categoria", required = true) @PathVariable Long id);

    @Operation(summary = "Deletar categoria", description = "Remove uma categoria pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria deletada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<Void> deleteByCategoryId(
            @Parameter(description = "ID da categoria", required = true) @PathVariable Long id);
}
