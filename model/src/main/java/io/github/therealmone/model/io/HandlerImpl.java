package io.github.therealmone.model.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class HandlerImpl extends ChannelInboundHandlerAdapter implements Handler {
    private ChannelHandlerContext context;
    private final ArrayList<Object> messages = new ArrayList<>();
    private boolean active = false;

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        context = ctx;
        active = true;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final ByteBuf buffer = (ByteBuf) msg;
        buffer.resetReaderIndex();
        final int messageLength = buffer.readInt();
        final byte[] bytes = new byte[messageLength];
        buffer.readBytes(bytes, 0, messageLength);
        try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            messages.add(objectInputStream.readObject());
        }
    }

    @Override
    public void write(final Object message) throws Exception {
        validate();
        final ByteBuf buffer = context.alloc().buffer();
        buffer.resetWriterIndex();
        try(final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(message);
            buffer.writeInt(byteArrayOutputStream.size());
            buffer.writeBytes(byteArrayOutputStream.toByteArray());
        }
        context.writeAndFlush(buffer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T read(final Class<T> clazz) throws Exception {
        if(messages.isEmpty()) {
            throw new RuntimeException("No messages");
        }
        final T message = clazz.cast(messages.get(0));
        messages.remove(0);
        return message;
    }

    private void validate() {
        if(context == null) {
            throw new RuntimeException("Context is null");
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean containsMessages() {
        return messages.size() > 0;
    }
}
