package com.softserve.testapp.foodimageretriever.infra.gateways.foodimage;

import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.List;

public interface Base64ByteBuffersLengthNormalizer {
    static Flux<ByteBuffer> normalizeBuffersForBase64Encoding(Flux<ByteBuffer> rawBody) {
        return rawBody
                .buffer(2, 1)
                .map(buffers -> {
                    var firstBuf = buffers.get(0);
                    if (isLastBuffer(buffers)) {
                        return firstBuf;
                    }

                    var overlappingBuf = buffers.get(1);
                    if (!overlappingBuf.hasRemaining()) {
                        return firstBuf;
                    }

                    var base64MisalignmentRemainder = firstBuf.remaining() % 3;
                    var overlappingBufCapacityToConsume = Math.min(overlappingBuf.remaining(), 3 - base64MisalignmentRemainder);
                    var complementaryBuf = getComplementaryBuffer(overlappingBuf, overlappingBufCapacityToConsume);
                    var base64DivisibleWithoutRemainderBuf = ByteBuffer.allocate(firstBuf.remaining() + overlappingBufCapacityToConsume).put(firstBuf).put(complementaryBuf).rewind();
                    return base64DivisibleWithoutRemainderBuf;
                });
    }

    static ByteBuffer getComplementaryBuffer(ByteBuffer overlappingBuf, int overlappingBufCapacityToConsume) {
        var bytesFromOverlappingBuf = new byte[overlappingBufCapacityToConsume];
        overlappingBuf.get(bytesFromOverlappingBuf);
        return ByteBuffer.wrap(bytesFromOverlappingBuf);
    }

    static private boolean isLastBuffer(List<ByteBuffer> buffers) {
        return buffers.size() < 2;
    }
}
