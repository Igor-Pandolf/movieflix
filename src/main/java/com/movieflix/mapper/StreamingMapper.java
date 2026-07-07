package com.movieflix.mapper;

import com.movieflix.controller.request.StreamingRequest;
import com.movieflix.controller.response.StreamingResponse;
import com.movieflix.entity.Streaming;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StreamingMapper {

    public Streaming toStreaming(StreamingRequest streamingRequest){
        return Streaming
                .builder()
                .name(streamingRequest.name())
                .build();
    }

    public StreamingResponse toStreamingResponse(Streaming streaming) {
        return StreamingResponse
                .builder()
                .id(streaming.getId())
                .name(streaming.getName())
                .build();
    }
}
