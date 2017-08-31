package com.rewedigital.examples.msintegration.composer.routing;

import static com.rewedigital.examples.msintegration.composer.routing.StaticBackendRoutes.RouteType.PROXY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.rewedigital.examples.msintegration.composer.routing.BackendRouting.RouteMatch;
import com.rewedigital.examples.msintegration.composer.routing.StaticBackendRoutes.Match;
import com.spotify.apollo.Request;
import com.spotify.apollo.route.Rule;
import com.spotify.apollo.route.RuleRouter;

public class BackendRoutingTest {

    @Test
    public void findsRouteForSimpleRule() {
        final Rule<Match> simpleRule = Rule.fromUri("/", "GET", Match.of("http://test.com/", PROXY));
        final RuleRouter<Match> ruleRouter = RuleRouter.of(ImmutableList.of(simpleRule));
        final BackendRouting backendRouting = new BackendRouting(ruleRouter);

        Optional<RouteMatch> matchResult = backendRouting.matches(requestFor("GET", "/"));
        assertThat(matchResult).isPresent();
        assertThat(matchResult.get().backend()).isEqualTo("http://test.com/");
        assertThat(matchResult.get().shouldProxy()).isTrue();
    }

    @Test
    public void findsRouteWithPathArguments() {
        final Rule<Match> ruleWithPath =
            Rule.fromUri("/<someValue>", "GET", Match.of("http://test.com/{someValue}", PROXY));
        final RuleRouter<Match> ruleRouter = RuleRouter.of(ImmutableList.of(ruleWithPath));
        final BackendRouting backendRouting = new BackendRouting(ruleRouter);

        Optional<RouteMatch> matchResult = backendRouting.matches(requestFor("GET", "/123"));
        assertThat(matchResult).isPresent();
        assertThat(matchResult.get().backend()).isEqualTo("http://test.com/{someValue}");
        assertThat(matchResult.get().parsedPathArguments()).containsEntry("someValue", "123");
    }

    @Test
    public void findsNoRouteThatIsNotConfigured() {
        final Rule<Match> simpleRule = Rule.fromUri("/", "GET", Match.of("http://test.com/", PROXY));
        final RuleRouter<Match> ruleRouter = RuleRouter.of(ImmutableList.of(simpleRule));
        final BackendRouting backendRouting = new BackendRouting(ruleRouter);

        Optional<RouteMatch> matchResult = backendRouting.matches(requestFor("PUT", "/"));
        assertThat(matchResult).isNotPresent();
    }

    private Request requestFor(final String method, final String uri) {
        final Request request = mock(Request.class);

        when(request.method()).thenReturn(method);
        when(request.uri()).thenReturn(uri);

        return request;
    }

}
