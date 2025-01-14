package io.github.ntsd.stubby4gay.handlers.strategy;

import io.github.ntsd.stubby4gay.handlers.strategy.stubs.DefaultResponseHandlingStrategy;
import io.github.ntsd.stubby4gay.handlers.strategy.stubs.NotFoundResponseHandlingStrategy;
import io.github.ntsd.stubby4gay.handlers.strategy.stubs.RedirectResponseHandlingStrategy;
import io.github.ntsd.stubby4gay.handlers.strategy.stubs.StubResponseHandlingStrategy;
import io.github.ntsd.stubby4gay.handlers.strategy.stubs.StubsResponseHandlingStrategyFactory;
import io.github.ntsd.stubby4gay.handlers.strategy.stubs.UnauthorizedResponseHandlingStrategy;
import io.github.ntsd.stubby4gay.stubs.StubResponse;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;


public class HandlingStrategyFactoryTest {

    @Test
    public void shouldIdentifyResponseStrategyForDefaultResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.okResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(DefaultResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForNotFoundResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.notFoundResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(NotFoundResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForUnauthorizedResponse() throws Exception {
        final StubResponse stubResponse = StubResponse.unauthorizedResponse();

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(UnauthorizedResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWhenFoundStubResponseNull() throws Exception {
        final StubResponse stubResponse = StubResponse.redirectResponse(Optional.empty());

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode301() throws Exception {
        Optional<StubResponse> stubResponseOptional = Optional.of(
                new StubResponse.Builder()
                        .withHttpStatusCode(Code.MOVED_PERMANENTLY)
                        .build());
        final StubResponse stubResponse = StubResponse.redirectResponse(stubResponseOptional);

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302() throws Exception {
        Optional<StubResponse> stubResponseOptional = Optional.of(
                new StubResponse.Builder()
                        .withHttpStatusCode(Code.MOVED_TEMPORARILY)
                        .build());
        final StubResponse stubResponse = StubResponse.redirectResponse(stubResponseOptional);

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode302_Found() throws Exception {
        Optional<StubResponse> stubResponseOptional = Optional.of(
                new StubResponse.Builder()
                        .withHttpStatusCode(Code.FOUND)
                        .build());
        final StubResponse stubResponse = StubResponse.redirectResponse(stubResponseOptional);

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode303() throws Exception {
        Optional<StubResponse> stubResponseOptional = Optional.of(
                new StubResponse.Builder()
                        .withHttpStatusCode(Code.SEE_OTHER)
                        .build());
        final StubResponse stubResponse = StubResponse.redirectResponse(stubResponseOptional);

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode307() throws Exception {
        Optional<StubResponse> stubResponseOptional = Optional.of(
                new StubResponse.Builder()
                        .withHttpStatusCode(Code.TEMPORARY_REDIRECT)
                        .build());
        final StubResponse stubResponse = StubResponse.redirectResponse(stubResponseOptional);

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }

    @Test
    public void shouldIdentifyResponseStrategyForRedirectResponseWithStubResponseCode308() throws Exception {
        Optional<StubResponse> stubResponseOptional = Optional.of(
                new StubResponse.Builder()
                        .withHttpStatusCode(Code.PERMANET_REDIRECT)
                        .build());
        final StubResponse stubResponse = StubResponse.redirectResponse(stubResponseOptional);

        final StubResponseHandlingStrategy stubResponseHandlingStrategy = StubsResponseHandlingStrategyFactory.getStrategy(stubResponse);
        assertThat(stubResponseHandlingStrategy).isInstanceOf(RedirectResponseHandlingStrategy.class);
    }
}
