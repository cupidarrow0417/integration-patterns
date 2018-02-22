package com.rewedigital.examples.msintegration.composer.session;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.spotify.apollo.Request;
import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;

public abstract class SessionHandler implements Session.Serializer {

    public interface Interceptor {
        Session afterCreation(Session session, RequestContext context);
    }

    private static final SessionHandler NOOP_HANDLER = new SessionHandler(Collections.emptyList()) {

        @Override
        public <T> Response<T> writeTo(final Response<T> response, final Map<String, String> sessionData,
            final boolean dirty) {
            return response;
        }

        @Override
        protected Session obtainSession(final Request request) {
            return Session.empty();
        }

        @Override
        public Session initialize(final RequestContext context) {
            return obtainSession(context.request());
        }
    };


    public static SessionHandler noSession() {
        return NOOP_HANDLER;
    }

    private final List<Interceptor> interceptors;

    protected SessionHandler(final List<Interceptor> interceptors) {
        this.interceptors = new LinkedList<>(interceptors);
    }

    public Session initialize(final RequestContext context) {
        final Session session = obtainSession(context.request());
        return runInterceptors(session, context);
    }

    public <T> Response<T> store(final ResponseWithSession<T> response) {
        return response.writeSessionToResponse(this);
    }

    protected abstract Session obtainSession(Request request);


    private Session runInterceptors(final Session session, final RequestContext context) {
        Session result = session;
        for (final Interceptor interceptor : interceptors) {
            result = interceptor.afterCreation(result, context);
        }
        return result;
    }
}
