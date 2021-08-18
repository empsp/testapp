package com.softserve.testapp.shared.utils;

import java.nio.ByteBuffer;
import java.util.List;

public interface ByteBufferUtil {
    static ByteBuffer concatBuffers(List<ByteBuffer> buffers) {
        var output = ByteBuffer.allocate(buffers.stream().mapToInt(ByteBuffer::remaining).sum());
        buffers.forEach(output::put);
        output.rewind();
        return output;
    }
}
