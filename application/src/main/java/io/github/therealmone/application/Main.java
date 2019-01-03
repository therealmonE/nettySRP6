package io.github.therealmone.application;

import com.google.inject.Guice;

public class Main {
    public static void main(String[] args) {
        final Application application = Guice.createInjector(new Module()).getInstance(Application.class);
        application.run();
    }
}
