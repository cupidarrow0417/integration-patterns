package com.rewedigital.examples.msintegration.composer.session;

import java.util.Objects;

import com.spotify.apollo.Response;

public class ResponseWithSession<T> {

    private final Response<T> response;
    private final Session session;

    public ResponseWithSession(final Response<T> response, final Session session) {
        this.response = Objects.requireNonNull(response);
        this.session = Objects.requireNonNull(session);
    }

    public Response<T> response() {
        return response;
    }

    public Session session() {
        return session;
    }

    public Response<T> writeSessionToResponse(final Session.Serializer serializer) {
        return session.writeTo(response, serializer);
    }
}
