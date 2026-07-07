package com.movieflix.controller;

import com.movieflix.controller.request.StreamingRequest;
import com.movieflix.controller.response.StreamingResponse;
import com.movieflix.entity.Streaming;
import com.movieflix.mapper.StreamingMapper;
import com.movieflix.service.StreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movieflix/streaming")
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingService service;

    @GetMapping
    public ResponseEntity<List<StreamingResponse>> getAllStreamings() {
        List<Streaming> streamings = service.findAll();
        List<StreamingResponse> list = streamings.stream()
                .map(StreamingMapper::toStreamingResponse)
                .toList();

        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<StreamingResponse> saveStreaming(@RequestBody StreamingRequest request) {
        Streaming streaming = service.saveStreaming(StreamingMapper.toStreaming(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(StreamingMapper.toStreamingResponse(streaming));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StreamingResponse> getByStreamingId(@PathVariable Long id) {
        return service.findStreamingById(id)
                .map(streaming -> ResponseEntity.ok(StreamingMapper.toStreamingResponse(streaming)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteByStreamingId(@PathVariable Long id) {
        service.deleteStreaming(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
