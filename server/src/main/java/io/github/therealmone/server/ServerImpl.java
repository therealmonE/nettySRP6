package io.github.therealmone.server;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.github.therealmone.model.*;
import io.github.therealmone.model.functions.*;
import io.github.therealmone.model.io.Handler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ServerImpl implements Server {
    private final static Logger logger = LogManager.getLogger(ServerImpl.class);
    private final int port;
    private final Handler serverHandler;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture future;
    private final Random random = new Random();

    private Map<String, UserContext> cache = new HashMap<>();

    private final BiHash biHash;
    private final MonoHash monoHash;
    private final BiHashStrings biHashStrings;
    private final MHash mHash;
    private final RHash rHash;
    private final G g;
    private final N n;
    private final K k;

    @Inject
    public ServerImpl(
            @Named("ServerConfig") final Config config,
            @Named("SecFieldConfig") final Config secFieldConfig,
            final BiHash biHash,
            final MonoHash monoHash,
            final Handler serverHandler,
            final BiHashStrings biHashStrings,
            final MHash mHash,
            final RHash rHash) {
        this.port = config.getInt("port");
        this.serverHandler = serverHandler;
        this.n = N.getInstance(BigInteger.valueOf(secFieldConfig.getInt("Q") * 2 + 1));
        this.g = G.getInstance(BigInteger.valueOf(secFieldConfig.getInt("G")));
        this.k = K.getInstance(BigInteger.valueOf(secFieldConfig.getInt("K")));
        this.biHash = biHash;
        this.monoHash = monoHash;
        this.biHashStrings = biHashStrings;
        this.mHash = mHash;
        this.rHash = rHash;
    }

    @Override
    public void run() throws Exception {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        final ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(serverHandler);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        future = bootstrap.bind(port).sync();
    }

    @Override
    public void write(final Object message) throws Exception {
        validate();
        serverHandler.write(message);
        Thread.sleep(100);
    }

    @Override
    public <T> T read(final Class<T> clazz) throws Exception {
        validate();
        logger.info("Waiting for message {}", clazz.getName());
        boolean gotMessage = serverHandler.containsMessages();
        while(!gotMessage) {
            Thread.sleep(1000);
            gotMessage = serverHandler.containsMessages();
        }
        return serverHandler.read(clazz);
    }

    private void validate() {
        if(bossGroup == null) {
            throw new RuntimeException("boss group is null");
        }

        if(workerGroup == null) {
            throw new RuntimeException("worker group is null");
        }

        if(future == null) {
            throw new RuntimeException("future is null");
        }
    }

    @Override
    public void shutDown() throws Exception {
        logger.info("Shutting down server...");
        if(future != null) {
            future.channel().disconnect();
            future = null;
        }

        if(workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }

        if(bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
    }

    @Override
    public boolean connected() {
        return serverHandler.isActive();
    }

    @Override
    public void registerNewUser() throws Exception {
        final I username = read(I.class);
        final S salt = read(S.class);
        final V passwordVerificator = read(V.class);
        if(!cache.containsKey(username.value())) {
            cache.put(username.value(), new UserContext(username.value(), salt.value(), passwordVerificator.value()));
        }
        write("Successfully registered new user");
    }

    @Override
    public void authenticate() throws Exception {
        final I _i = read(I.class);
        final A _A = read(A.class);
        assert _A.value().compareTo(BigInteger.valueOf(0)) != 0;
        final S _s = S.getInstance(cache.get(_i.value()).getSalt());
        final V _v = V.getInstance(cache.get(_i.value()).getPasswordVerifier());
        final B _b = B.getInstance(BigInteger.valueOf(random.nextInt(1_000_000 - 10_000) + 10_000));
        final B _B = B.getInstance(k.value().multiply(_v.value()).add(g.value().modPow(_b.value(), n.value())).mod(n.value()));
        write(_s);
        write(_B);
        final U _u = U.getInstance(biHash.apply(_A.value(), _B.value()));
        assert _u.value().compareTo(BigInteger.valueOf(0)) != 0;
        final BigInteger sessionKey = monoHash.apply(
                _A.value().multiply(_v.value().modPow(_u.value(), n.value())).pow(_b.value().intValue()).mod(n.value())
        );
        logger.info("Server session key for {} : {}", _i.value(), sessionKey);
        final M client_M = read(M.class);
        final M _m = M.getInstance(mHash.apply(
                new Object[] {
                        monoHash.apply(n.value()).xor(monoHash.apply(g.value())),
                        biHashStrings.apply(_i.value(), ""),
                        _s.value(),
                        _A.value(),
                        _B.value(),
                        sessionKey
                }));
        if(client_M.value().compareTo(_m.value()) != 0) {
            logger.warn("Invalid client session key");
            shutDown();
        } else {
            logger.info("Correct client session key");
            final R _R = R.getInstance(rHash.apply(new BigInteger[] {_A.value(), _m.value(), sessionKey}));
            write(_R);
        }
    }
}
