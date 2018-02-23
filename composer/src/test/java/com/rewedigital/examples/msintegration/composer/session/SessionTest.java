package com.rewedigital.examples.msintegration.composer.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.spotify.apollo.Request;
import com.spotify.apollo.Response;
import com.spotify.apollo.Status;

public class SessionTest {

    @Test
    public void readsSessionDataFromResponse() {
        final Response<Object> response = Response.forStatus(Status.OK).withHeader("x-rd-some-key", "value")
            .withHeader("ignored-key", "ignored-value");

        final SessionFragment session = SessionFragment.of(response);

        assertThat(session.get("some-key")).contains("value");
        assertThat(session.get("ignored-key")).isNotPresent();
    }

    @Test
    public void ignoresCaseWhileSearchingForSessionKey() {
        final Response<Object> response = Response.forStatus(Status.OK).withHeader("X-RD-SOME-KEY", "value");

        final SessionFragment session = SessionFragment.of(response);

        assertThat(session.get("some-key")).contains("value");
    }

    @Test
    public void mergesTwoSessionsByMergingAllData() {
        final SessionFragment firstSession =
            SessionFragment.of(Response.forStatus(Status.OK).withHeader("x-rd-first-key", "first-value"));
        final SessionFragment secondSession =
            SessionFragment.of(Response.forStatus(Status.OK).withHeader("x-rd-second-key", "second-value"));

        final SessionFragment result = firstSession.withValuesMergedFrom(secondSession);

        assertThat(result.get("first-key")).contains("first-value");
        assertThat(result.get("second-key")).contains("second-value");
    }

    @Test
    public void enrichesRequestWithSessionHeaders() {
        final SessionRoot session = SessionRoot.of(data("x-rd-some-key", "value")).withValuesMergedFrom(
            SessionFragment.of(Response.forStatus(Status.OK).withHeader("x-rd-other-key", "other-value")));
        final Request request = session.enrich(Request.forUri("/some/path"));

        assertThat(request.header("x-rd-some-key")).contains("value");
        assertThat(request.header("x-rd-other-key")).contains("other-value");
    }

    @Test
    public void isDirtyIfNewAttributesAreMerged() {
        final SessionRoot firstSession = SessionRoot.of(data("x-rd-first-key", "first-value"));
        final SessionFragment secondSession =
            SessionFragment.of(Response.forStatus(Status.OK).withHeader("x-rd-second-key", "second-value"));

        final SessionRoot result = firstSession.withValuesMergedFrom(secondSession);
        assertThat(result.isDirty()).isTrue();
    }

    @Test
    public void isNotDirtyIfSameAttributeAndValueIsMerged() {
        final SessionRoot sessionRoot = SessionRoot.of(data("x-rd-first-key", "first-value"), false);
        final SessionFragment secondSession =
            SessionFragment.of(Response.forStatus(Status.OK).withHeader("x-rd-first-key", "first-value"));

        final SessionRoot result = sessionRoot.withValuesMergedFrom(secondSession);
        assertThat(result.isDirty()).isFalse();
    }

    @Test
    public void isDirtyAfterChangingAttributeValue() {
        final SessionRoot firstSession = SessionRoot.of(data("x-rd-first-key", "first-value"));
        final SessionFragment secondSession =
            SessionFragment.of(Response.forStatus(Status.OK).withHeader("x-rd-first-key", "second-value"));

        final SessionRoot result = firstSession.withValuesMergedFrom(secondSession);
        assertThat(result.isDirty()).isTrue();
        assertThat(result.get("first-key")).contains("second-value");
    }

    @Test
    public void doesNotOverwriteSessionId() {
        final SessionRoot initialSession = SessionRoot.of(Collections.emptyMap()).withId("initialSessionId");
        final SessionFragment sessionWithOtherId =
            SessionFragment.of(Response.forStatus(Status.OK).withHeader("x-rd-session-id", "otherSessionId"));

        final SessionRoot mergedSession = initialSession.withValuesMergedFrom(sessionWithOtherId);
        assertThat(mergedSession.getId()).contains("initialSessionId");
    }

    @Test
    public void allowsRemovalOfSessionAttributes() {
        final SessionRoot firstSession = SessionRoot.of(data("x-rd-key", "value"));
        final SessionFragment secondSession = SessionFragment.of(
            Response.forStatus(Status.OK)
                .withHeader("x-rd-key", ""));

        final SessionRoot mergedSession = firstSession.withValuesMergedFrom(secondSession);
        assertThat(mergedSession.get("first-key")).isEmpty();
        assertThat(mergedSession.get("second-key")).isEmpty();
    }

    private Map<String, String> data(final String key, final String value) {
        final Map<String, String> data = new HashMap<>();
        data.put(key, value);
        return data;
    }
}
