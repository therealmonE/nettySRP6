package io.github.therealmone.client;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.typesafe.config.Config;
import io.github.therealmone.model.*;
import io.github.therealmone.model.functions.*;
import io.github.therealmone.model.io.Handler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.Random;

public class ClientImpl implements Client {
    private final static Logger logger = LogManager.getLogger(ClientImpl.class);
    private final String host;
    private final int port;
    private final Handler clientHandler;
    private final Random random = new Random();

    private ChannelFuture future;
    private EventLoopGroup workGroup;

    private final BiHashStrings biHashStrings;
    private final RandomString randomString;
    private final BiHash biHash;
    private final MonoHash monoHash;
    private final MHash mHash;
    private final RHash rHash;
    private final N n;
    private final G g;
    private final K k;

    @Inject
    public ClientImpl(
            @Named("ClientConfig") final Config config,
            @Named("SecFieldConfig") final Config secFieldConfig,
            final Handler clientHandler,
            final BiHashStrings biHashStrings,
            final RandomString randomString,
            final BiHash biHash,
            final MonoHash monoHash,
            final MHash mHash,
            final RHash rHash) {
        this.host = config.getString("host");
        this.port = config.getInt("port");
        this.clientHandler = clientHandler;
        this.biHashStrings = biHashStrings;
        this.randomString = randomString;
        this.biHash = biHash;
        this.monoHash = monoHash;
        this.mHash = mHash;
        this.rHash = rHash;
        this.n = N.getInstance(BigInteger.valueOf(secFieldConfig.getInt("Q") * 2 + 1));
        this.g = G.getInstance(BigInteger.valueOf(secFieldConfig.getInt("G")));
        this.k = K.getInstance(BigInteger.valueOf(secFieldConfig.getInt("K")));
    }

    @Override
    public void run() throws Exception {
        workGroup = new NioEventLoopGroup();
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(clientHandler);
                    }
                });

        future = bootstrap.connect(host, port).sync();
    }

    @Override
    public void shutDown() throws Exception {
        logger.info("Shutting down client...");
        if(future != null) {
            future.channel().disconnect();
            future = null;
        }

        if(workGroup != null) {
            workGroup.shutdownGracefully();
            workGroup = null;
        }
    }

    @Override
    public boolean connected() {
        return clientHandler.isActive();
    }

    @Override
    public void register(final String username, final String password) throws Exception {
        final S s = S.getInstance(randomString.get());
        final X x = X.getInstance(biHashStrings.apply(s.value(), password));
        final V v = V.getInstance(g.value().pow(x.value().intValue()).mod(n.value()));
        write(I.getInstance(username));
        write(s);
        write(v);
    }

    @Override
    public void write(Object message) throws Exception {
        validate();
        clientHandler.write(message);
        Thread.sleep(1000);
    }

    @Override
    public <T> T read(Class<T> clazz) throws Exception {
        validate();
        logger.info("Waiting for message {}", clazz.getName());
        boolean gotMessage = clientHandler.containsMessages();
        while(!gotMessage) {
            Thread.sleep(1000);
            gotMessage = clientHandler.containsMessages();
        }
        return clientHandler.read(clazz);
    }

    @Override
    public void login(final String username, final String password) throws Exception {
        final I _i = I.getInstance(username);
        final A _a = A.getInstance(BigInteger.valueOf(random.nextInt(1_000_000 - 10_000) + 10_000));
        final A _A = A.getInstance(g.value().pow(_a.value().intValue()).mod(n.value()));
        write(_i);
        write(_A);
        final S _s = read(S.class);
        final B _B = read(B.class);
        assert _B.value().compareTo(BigInteger.valueOf(0)) != 0;
        final U _u = U.getInstance(biHash.apply(_A.value(), _B.value()));
        assert _u.value().compareTo(BigInteger.valueOf(0)) != 0;
        final X _x = X.getInstance(biHashStrings.apply(_s.value(), password));
        final BigInteger sessionKey =
                monoHash.apply(
                        _B.value().subtract(k.value().multiply(g.value().pow(_x.value().intValue()).mod(n.value()))).pow(
                                _a.value().add(_u.value().multiply(_x.value())).intValue()
                        ).mod(n.value())
                );
        logger.info("Client session key: {}", sessionKey);
        final M _m = M.getInstance(mHash.apply(
                new Object[] {
                        monoHash.apply(n.value()).xor(monoHash.apply(g.value())),
                        biHashStrings.apply(_i.value(), ""),
                        _s.value(),
                        _A.value(),
                        _B.value(),
                        sessionKey
                }));
        write(_m);
        final R server_R = read(R.class);
        final R _R = R.getInstance(rHash.apply(new BigInteger[] {_A.value(), _m.value(), sessionKey}));
        if(server_R.value().compareTo(_R.value()) != 0) {
            logger.warn("Invalid server session key");
            shutDown();
        } else {
            logger.info("Correct server session key");
        }
    }

    private void validate() {
        if(future == null) {
            throw new RuntimeException("Future is null");
        }

        if(workGroup == null) {
            throw new RuntimeException("Work group is null");
        }
    }
}
