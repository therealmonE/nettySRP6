package io.github.therealmone.application;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.therealmone.client.Client;
import io.github.therealmone.client.ClientImpl;
import io.github.therealmone.model.functions.*;
import io.github.therealmone.model.io.Handler;
import io.github.therealmone.model.io.HandlerImpl;
import io.github.therealmone.model.rsa.RSA;
import io.github.therealmone.server.Server;
import io.github.therealmone.server.ServerImpl;

import java.math.BigInteger;
import java.util.UUID;

public class Module extends AbstractModule {
    private final Config config = ConfigFactory.parseResources("config.conf").resolve();

    @Override
    protected void configure() {
        bind(Runnable.class).annotatedWith(Names.named("ClientRunner")).to(ClientRunner.class);
        bind(Client.class).to(ClientImpl.class);

        bind(Runnable.class).annotatedWith(Names.named("ServerRunner")).to(ServerRunner.class);
        bind(Server.class).to(ServerImpl.class);

        bind(Config.class).annotatedWith(Names.named("ClientConfig")).toInstance(config.getConfig("client"));
        bind(Config.class).annotatedWith(Names.named("ServerConfig")).toInstance(config.getConfig("server"));
        bind(Config.class).annotatedWith(Names.named("SecFieldConfig")).toInstance(config.getConfig("secField"));
    }

    @Provides
    Handler provideHandler() {
        return new HandlerImpl();
    }

    @Provides
    RandomString provideRandomString() {
        return () -> UUID.randomUUID().toString();
    }

    @Provides
    BiHashStrings provideBiHashStrings() {
        return (s, s2) -> {
            int sum = 0;
            if(s.length() > 0) {
                sum += s.codePointCount(0, s.length() - 1);
            }

            if(s2.length() > 0) {
                sum += s2.codePointCount(0, s2.length() - 1);
            }
            return BigInteger.valueOf(sum);
        };
    }

    @Provides
    BiHash provideBiHash() {
        return BigInteger::add;
    }

    @Provides
    MonoHash provideMonoHash() {
        return bigInteger -> bigInteger;
    }

    @Provides
    RSA provideRSA() {
        return new RSA(config.getConfig("rsa"));
    }

    @Provides
    MHash provideMHash() {
        return bigIntegers -> {
            BigInteger sum = BigInteger.valueOf(0);
            for (final Object o: bigIntegers) {
                if(o instanceof BigInteger) {
                    sum = sum.add((BigInteger) o);
                } else if(o instanceof String) {
                    final String s = (String) o;
                    sum = sum.add(BigInteger.valueOf(s.codePointCount(0, s.length() - 1)));
                }
            }
            return sum;
        };
    }

    @Provides
    RHash provideRHash() {
        return bigIntegers -> {
            BigInteger sum = BigInteger.valueOf(0);
            for (final BigInteger bigInteger : bigIntegers) {
                sum = sum.add(bigInteger);
            }
            return sum;
        };
    }
}
