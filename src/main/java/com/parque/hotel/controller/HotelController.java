package com.parque.hotel.controller;

import com.parque.hotel.dto.HotelCreateRequest;
import com.parque.hotel.dto.HotelResponse;
import com.parque.hotel.dto.HotelUpdateRequest;
import com.parque.hotel.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@Tag(name = "Hotels", description = "Gestión de hoteles de la agencia")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los hoteles", description = "Retorna la lista completa de hoteles disponibles")
    @ApiResponse(responseCode = "200", description = "Lista de hoteles obtenida correctamente")
    public List<HotelResponse> getAll() {
        return hotelService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener hotel por ID", description = "Retorna los detalles de un hotel específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Hotel encontrado"),
        @ApiResponse(responseCode = "404", description = "Hotel no encontrado")
    })
    public HotelResponse getById(@PathVariable Long id) {
        return hotelService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Crear nuevo hotel", description = "Crea un nuevo hotel en el sistema (requiere rol ADMIN)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Hotel creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<HotelResponse> create(@Valid @RequestBody HotelCreateRequest request) {
        HotelResponse created = hotelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar hotel", description = "Actualiza los datos de un hotel existente (requiere rol ADMIN)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Hotel actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Hotel no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public HotelResponse update(@PathVariable Long id, @Valid @RequestBody HotelUpdateRequest request) {
        return hotelService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar hotel", description = "Elimina un hotel del sistema (requiere rol ADMIN)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Hotel eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Hotel no encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

