package com.rewedigital.examples.msintegration.composer;

import com.rewedigital.examples.msintegration.composer.client.ClientDecoratingModule;
import com.rewedigital.examples.msintegration.composer.client.ErrorClientDecorator;
import com.rewedigital.examples.msintegration.composer.proxy.ComposerFactory;
import com.rewedigital.examples.msintegration.composer.proxy.ComposingRequestHandler;
import com.rewedigital.examples.msintegration.composer.proxy.TemplateClient;
import com.rewedigital.examples.msintegration.composer.routing.BackendRouting;
import com.rewedigital.examples.msintegration.composer.routing.StaticBackendRoutes;
import com.spotify.apollo.Environment;
import com.spotify.apollo.core.Service;
import com.spotify.apollo.http.client.HttpClientModule;
import com.spotify.apollo.httpservice.HttpService;
import com.spotify.apollo.httpservice.LoadingException;
import com.spotify.apollo.route.Route;

public class ComposerApplication {

    public static void main(final String[] args) throws LoadingException {

        final Service service =
            HttpService
                .usingAppInit(Initializer::init, "composer")
                .withModule(HttpClientModule.create())
                .withModule(new ClientDecoratingModule(new ErrorClientDecorator()))
                .build();

        HttpService.boot(service, args);
    }

    private static class Initializer {

        private static void init(final Environment environment) {
            final ComposingRequestHandler handler =
                new ComposingRequestHandler(
                    new BackendRouting(StaticBackendRoutes.routes()),
                    new TemplateClient(),
                    new ComposerFactory());

            environment.routingEngine()
                .registerAutoRoute(Route.async("GET", "/", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("GET", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("HEAD", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("POST", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("PUT", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("DELETE", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("TRACE", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("OPTIONS", "/<path:path>", rc -> handler.execute(rc)))
                .registerAutoRoute(Route.async("PATCH", "/<path:path>", rc -> handler.execute(rc)));
        }

    }
}
