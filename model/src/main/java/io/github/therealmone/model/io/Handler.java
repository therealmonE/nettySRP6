package io.github.therealmone.model.io;

import io.netty.channel.ChannelInboundHandler;

public interface Handler extends ChannelInboundHandler {
    boolean isActive();
    boolean containsMessages();
    void write(final Object message) throws Exception;
    <T> T read(final Class<T> clazz) throws Exception;
}
