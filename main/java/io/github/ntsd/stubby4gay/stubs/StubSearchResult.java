package io.github.ntsd.stubby4gay.stubs;


public class StubSearchResult {

    private final StubRequest invariant;
    private final StubResponse match;

    StubSearchResult(final StubRequest invariant, final StubResponse match) {
        this.invariant = invariant;
        this.match = match;
    }

    public StubRequest getInvariant() {
        return invariant;
    }

    public StubResponse getMatch() {
        return match;
    }
}
