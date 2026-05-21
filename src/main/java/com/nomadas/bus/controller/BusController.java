package com.nomadas.bus.controller;

import com.nomadas.bus.dto.BusCreateRequest;
import com.nomadas.bus.dto.BusResponse;
import com.nomadas.bus.dto.BusUpdateRequest;
import com.nomadas.bus.service.BusService;
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
@RequestMapping("/api/buses")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @GetMapping
    public List<BusResponse> getAll() {
        return busService.getAll();
    }

    @GetMapping("/{id}")
    public BusResponse getById(@PathVariable Long id) {
        return busService.getById(id);
    }

    @PostMapping
    public ResponseEntity<BusResponse> create(@Valid @RequestBody BusCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(busService.create(request));
    }

    @PutMapping("/{id}")
    public BusResponse update(@PathVariable Long id, @Valid @RequestBody BusUpdateRequest request) {
        return busService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        busService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
