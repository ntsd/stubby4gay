package io.github.ntsd.stubby4gay.stubs;

import com.google.api.client.http.HttpMethods;
import io.github.ntsd.stubby4gay.common.Common;
import io.github.ntsd.stubby4gay.stubs.StubHttpLifecycle;
import io.github.ntsd.stubby4gay.stubs.StubRepository;
import io.github.ntsd.stubby4gay.stubs.StubRequest;
import io.github.ntsd.stubby4gay.stubs.StubResponse;
import io.github.ntsd.stubby4gay.stubs.StubSearchResult;
import io.github.ntsd.stubby4gay.utils.FileUtils;
import io.github.ntsd.stubby4gay.yaml.YamlBuilder;
import io.github.ntsd.stubby4gay.yaml.YamlParseResultSet;
import io.github.ntsd.stubby4gay.yaml.YamlParser;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.google.common.truth.Truth.assertThat;
import static io.github.ntsd.stubby4gay.stubs.StubbableAuthorizationType.BASIC;
import static io.github.ntsd.stubby4gay.stubs.StubbableAuthorizationType.BEARER;
import static io.github.ntsd.stubby4gay.stubs.StubbableAuthorizationType.CUSTOM;
import static io.github.ntsd.stubby4gay.utils.ReflectionUtils.injectObjectFields;
import static io.github.ntsd.stubby4gay.utils.StringUtils.inputStreamToString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings("serial")
@RunWith(MockitoJUnitRunner.class)
public class StubRepositoryTest {

    private static final YamlBuilder YAML_BUILDER = new YamlBuilder();

    private static final File CONFIG_FILE = new File(".");
    private static final CompletableFuture<YamlParseResultSet> YAML_PARSE_RESULT_SET_FUTURE =
            CompletableFuture.completedFuture(new YamlParseResultSet(new LinkedList<>(), new HashMap<>()));

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Spy
    private StubRepository spyStubRepository = new StubRepository(CONFIG_FILE, YAML_PARSE_RESULT_SET_FUTURE);

    private StubRequest.Builder requestBuilder;

    @Before
    public void beforeEach() throws Exception {
        requestBuilder = new StubRequest.Builder();
    }

    @Test
    public void shouldReturnMatchingStubbedSequenceResponse_WhenSequenceHasOneResponse() throws Exception {

        final String url = "/some/redirecting/uri";
        final String sequenceResponseHeaderKey = "content-type";
        final String sequenceResponseHeaderValue = Common.HEADER_APPLICATION_JSON;
        final String sequenceResponseStatus = "200";
        final String sequenceResponseBody = "OK";

        final String yaml = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withSequenceResponseStatus(sequenceResponseStatus)
                .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
                .withSequenceResponseLiteralBody(sequenceResponseBody)
                .build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet().build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.OK).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo(sequenceResponseBody);
        assertThat(foundStubResponse.getHeaders()).containsEntry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);
    }

    private StubResponse setUpStubSearchBehavior(final StubRequest assertingRequest) throws IOException {
        doReturn(assertingRequest).when(spyStubRepository).toStubRequest(any(HttpServletRequest.class));
        final StubSearchResult stubSearchResult = spyStubRepository.search(mockHttpServletRequest);
        return stubSearchResult.getMatch();
    }

    @Test
    public void shouldReturnMatchingStubbedSequenceResponse_WhenSequenceHasManyResponses() throws Exception {

        final String url = "/some/uri";

        final String sequenceResponseHeaderKey = "content-type";
        final String sequenceResponseHeaderValue = "application/xml";
        final String sequenceResponseStatus = "500";
        final String sequenceResponseBody = "OMFG";

        final String yaml = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseLiteralBody("OK")
                .withLineBreak()
                .withSequenceResponseStatus(sequenceResponseStatus)
                .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
                .withSequenceResponseFoldedBody(sequenceResponseBody)
                .build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet().build();


        final StubResponse irrelevantFirstSequenceResponse = setUpStubSearchBehavior(assertingRequest);
        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.INTERNAL_SERVER_ERROR).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo(sequenceResponseBody);
        assertThat(foundStubResponse.getHeaders()).containsEntry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);
    }

    @Test
    public void shouldReturnFirstSequenceResponse_WhenAllSequenceResponsesHaveBeenConsumedInTheList() throws Exception {

        final String url = "/some/uri";

        final String sequenceResponseHeaderKey = "content-type";
        final String sequenceResponseHeaderValue = "application/xml";
        final String sequenceResponseStatus = "200";
        final String sequenceResponseBody = "OK";

        final String yaml = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withSequenceResponseStatus(sequenceResponseStatus)
                .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
                .withSequenceResponseLiteralBody(sequenceResponseBody)
                .withLineBreak()
                .withSequenceResponseStatus("500")
                .withSequenceResponseHeaders("content-type", Common.HEADER_APPLICATION_JSON)
                .withSequenceResponseFoldedBody("OMFG")
                .build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet().build();

        final StubResponse irrelevantFirstSequenceResponse = setUpStubSearchBehavior(assertingRequest);
        final StubResponse irrelevantLastSequenceResponse = setUpStubSearchBehavior(assertingRequest);
        final StubResponse firstSequenceResponseRestarted = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.OK).isSameAs(firstSequenceResponseRestarted.getHttpStatusCode());
        assertThat(firstSequenceResponseRestarted.getBody()).isEqualTo(sequenceResponseBody);
        assertThat(firstSequenceResponseRestarted.getHeaders()).containsEntry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);
    }

    @Test
    public void shouldReturnMatchingRedirectResponse_WhenLocationHeaderSet() throws Exception {

        final String url = "/some/redirecting/uri";
        final String expectedStatus = "301";
        final String expectedBody = "oink";
        final String expectedHeaderKey = "location";
        final String expectedHeaderValue = "/invoice/123";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withHeaders(expectedHeaderKey, expectedHeaderValue)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet().build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.MOVED_PERMANENTLY).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
        assertThat(foundStubResponse.getHeaders()).containsEntry(expectedHeaderKey, expectedHeaderValue);
    }


    @Test
    public void shouldReturnMatchingStubbedResponse_WhenValidGetRequestMade() throws Exception {

        final String url = "/invoice/123";
        final String expectedStatus = "200";
        final String expectedBody = "This is a response for 123";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet().build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.OK).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }


    @Test
    public void shouldReturnMatchingStubbedResponse_WhenValidAuthorizationBasicHeaderSubmitted() throws Exception {

        final String url = "/invoice/555";

        final String expectedStatus = "200";
        final String expectedBody = "This is a response for 555";
        final String expectedHeaderValue = "'bob:secret'";
        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(BASIC.asYAMLProp(), expectedHeaderValue)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet()
                        .withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, "Basic Ym9iOnNlY3JldA==").build();  //bob:secret

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.OK).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void shouldReturnMatchingStubbedResponse_WhenValidAuthorizationBearerHeaderSubmitted() throws Exception {

        final String url = "/invoice/555";

        final String expectedStatus = "200";
        final String expectedBody = "This is a response for 555";
        final String expectedHeaderValue = "Ym9iOnNlY3JldA==";
        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(BEARER.asYAMLProp(), expectedHeaderValue)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet()
                        .withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, "Bearer Ym9iOnNlY3JldA==").build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.OK).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void shouldReturnMatchingStubbedResponse_WhenValidAuthorizationCustomHeaderSubmitted() throws Exception {

        final String url = "/invoice/555";

        final String expectedStatus = "200";
        final String expectedBody = "This is a response for 555";
        final String expectedHeaderValue = "CustomAuthorizationName Ym9iOnNlY3JldA==";
        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(CUSTOM.asYAMLProp(), expectedHeaderValue)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet()
                        .withHeader(StubRequest.HTTP_HEADER_AUTHORIZATION, "CustomAuthorizationName Ym9iOnNlY3JldA==").build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.OK).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void shouldReturnMatchingUnauthorizedStubResponse_WhenAuthorizationHeaderNotSubmitted() throws Exception {

        final String url = "/invoice/555";
        final String expectedStatus = "200";
        final String expectedBody = "This is a response for 555";
        final String expectedHeaderValue = "'bob:secret'";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(BASIC.asYAMLProp(), expectedHeaderValue)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet().build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.UNAUTHORIZED).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }


    @Test
    public void shouldReturnMatchingUnauthorizedStubResponse_WhenAuthorizationHeaderSubmittedWithBadCredentials() throws Exception {

        final String url = "/invoice/555";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(BASIC.asYAMLProp(), "'bob:secret'")
                .newStubbedResponse()
                .withStatus("200")
                .withLiteralBody("This is a response for 555").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet()
                        .withHeader(BASIC.asYAMLProp(), "Basic BadCredentials").build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.UNAUTHORIZED).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }

    @Test
    public void shouldReturnMatchingUnauthorizedStubResponse_WhenAuthorizationHeaderSubmittedWithNull() throws Exception {

        final String url = "/invoice/555";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withHeaders(BASIC.asYAMLProp(), "'bob:secret'")
                .newStubbedResponse()
                .withStatus("200")
                .withLiteralBody("This is a response for 555").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet()
                        .withHeader(BASIC.asYAMLProp(), null).build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.UNAUTHORIZED).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }

    @Test
    public void shouldReturnNotFoundStubResponse_WhenAssertingRequestWasNotMatched() throws Exception {

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl("/invoice/125")
                .newStubbedResponse()
                .withStatus("200")
                .withLiteralBody("This is a response for 125").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl("/invoice/300")
                        .withMethodGet().build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.NOT_FOUND).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }

    @Test
    public void shouldReturnMatchingStubbedResponse_WhenValidPostRequestMade() throws Exception {

        final String url = "/invoice/567";
        final String postData = "This is a post data";
        final String expectedStatus = "503";
        final String expectedBody = "This is a response for 567";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodPost()
                .withLiteralPost("This is a post data")
                .withUrl(url)
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withLiteralBody(expectedBody).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodPost()
                        .withPost(postData).build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.SERVICE_UNAVAILABLE).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void shouldReturnNotFoundStubResponse_WhenPostBodyMissing() throws Exception {

        final String url = "/invoice/567";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodPost()
                .withLiteralPost("This is a post data")
                .withUrl(url)
                .newStubbedResponse()
                .withStatus("503")
                .withLiteralBody("This is a response for 567").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodPost().build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.NOT_FOUND).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }

    @Test
    public void shouldReturnNotFoundStubResponse_WhenHittingCorrectUrlButWrongMethod() throws Exception {

        final String url = "/invoice/567";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .newStubbedResponse()
                .withStatus("503")
                .withLiteralBody("This is a response for 567").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodPost().build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.NOT_FOUND).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }

    @Test
    public void shouldReturnNotFoundStubResponse_WhenPostRequestMadeToIncorrectUrl() throws Exception {

        final String url = "/invoice/non-existent-url";
        final String postData = "This is a post data";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodPost()
                .withLiteralPost("This is a post data")
                .withUrl("/invoice/567")
                .newStubbedResponse()
                .withStatus("503")
                .withLiteralBody("This is a response for 567").build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodPost()
                        .withPost(postData).build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.NOT_FOUND).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }

    @Test
    public void shouldReturnMatchingStubbedResponse_WhenQueryParamIsArray() throws Exception {

        final String url = "/entity.find";
        final String expectedStatus = "200";
        final String expectedBody = "{\"status\": \"hello world\"}";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withQuery("type_name", "user")
                .withQuery("client_id", "id")
                .withQuery("client_secret", "secret")
                .withQuery("attributes", "'[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]'")
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withFoldedBody(expectedBody)
                .withHeaders("content-type", Common.HEADER_APPLICATION_JSON).build();

        loadYamlToDataStore(yaml);

        final StubRequest assertingRequest =
                requestBuilder
                        .withUrl(url)
                        .withMethodGet()
                        .withQuery("type_name", "user")
                        .withQuery("client_id", "id")
                        .withQuery("client_secret", "secret")
                        .withQuery("attributes", "[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]")
                        .build();

        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.OK).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void shouldReturnMatchingStubbedResponse_WhenQueryParamArrayHasElementsWithinUrlEncodedQuotes() throws Exception {

        final String url = "/entity.find";

        final String expectedStatus = "200";
        final String expectedBody = "{\"status\": \"hello world\"}";
        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withQuery("type_name", "user")
                .withQuery("client_id", "id")
                .withQuery("client_secret", "secret")
                .withQuery("attributes", "'[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]'")
                .newStubbedResponse()
                .withStatus(expectedStatus)
                .withFoldedBody(expectedBody)
                .withHeaders("content-type", Common.HEADER_APPLICATION_JSON).build();

        loadYamlToDataStore(yaml);

        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString())
                .thenReturn(
                        "type_name=user&client_id=id&client_secret=secret&attributes=[%22id%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22]"
                );

        final StubRequest assertingRequest = spyStubRepository.toStubRequest(mockHttpServletRequest);
        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.OK).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo(expectedBody);
    }

    @Test
    public void shouldReturnNotFoundStubResponse_WhenQueryParamArrayHasNonMatchedElementsWithinUrlEncodedQuotes() throws Exception {
        final String url = "/entity.find";

        final String yaml = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl(url)
                .withQuery("type_name", "user")
                .withQuery("client_id", "id")
                .withQuery("client_secret", "secret")
                .withQuery("attributes", "'[\"id\",\"uuid\",\"created\",\"lastUpdated\",\"displayName\",\"email\",\"givenName\",\"familyName\"]'")
                .newStubbedResponse()
                .withStatus("200")
                .withFoldedBody("{\"status\": \"hello world\"}")
                .withHeaders("content-type", Common.HEADER_APPLICATION_JSON).build();

        loadYamlToDataStore(yaml);

        when(mockHttpServletRequest.getPathInfo()).thenReturn(url);
        when(mockHttpServletRequest.getMethod()).thenReturn(HttpMethods.GET);
        when(mockHttpServletRequest.getQueryString())
                .thenReturn(
                        "type_name=user&client_id=id&client_secret=secret&attributes=[%22NOMATCH%22,%22uuid%22,%22created%22,%22lastUpdated%22,%22displayName%22,%22email%22,%22givenName%22,%22familyName%22]"
                );

        final StubRequest assertingRequest = spyStubRepository.toStubRequest(mockHttpServletRequest);
        final StubResponse foundStubResponse = setUpStubSearchBehavior(assertingRequest);

        assertThat(Code.NOT_FOUND).isSameAs(foundStubResponse.getHttpStatusCode());
        assertThat(foundStubResponse.getBody()).isEqualTo("");
    }

    @Test
    public void shouldReturnRequestAndResponseExternalFiles() throws Exception {
        final File expectedRequestFile = FileUtils.uriToFile(StubRepositoryTest.class.getResource("/json/request.external.file.json").getFile());
        final File expectedResponseFile = FileUtils.uriToFile(StubRepositoryTest.class.getResource("/json/response.1.external.file.json").getFile());

        resetStubHttpLifecyclesFromYamlResource("/yaml/two.external.files.yaml");
        final Map<File, Long> externalFiles = spyStubRepository.getExternalFiles();

        assertThat(externalFiles.size()).isEqualTo(2);
        assertThat(externalFiles.containsValue(expectedRequestFile.lastModified())).isTrue();
        assertThat(externalFiles.containsValue(expectedResponseFile.lastModified())).isTrue();

        final Set<String> filenames = new HashSet<>();
        for (final Map.Entry<File, Long> entry : externalFiles.entrySet()) {
            filenames.add(entry.getKey().getName());
        }

        assertThat(filenames.size()).isEqualTo(externalFiles.size());
        assertThat(filenames.contains(expectedRequestFile.getName())).isTrue();
        assertThat(filenames.contains(expectedResponseFile.getName())).isTrue();
    }

    @Test
    public void shouldReturnOnlyResponseExternalFile() throws Exception {
        final File expectedRequestFile = FileUtils.uriToFile(StubRepositoryTest.class.getResource("/json/request.external.file.json").getFile());
        final File expectedResponseFile = FileUtils.uriToFile(StubRepositoryTest.class.getResource("/json/response.1.external.file.json").getFile());

        resetStubHttpLifecyclesFromYamlResource("/yaml/one.external.files.yaml");
        final Map<File, Long> externalFiles = spyStubRepository.getExternalFiles();

        assertThat(externalFiles.size()).isEqualTo(1);
        assertThat(externalFiles.containsValue(expectedResponseFile.lastModified())).isTrue();

        final Set<String> filenames = new HashSet<>();
        for (final Map.Entry<File, Long> entry : externalFiles.entrySet()) {
            filenames.add(entry.getKey().getName());
        }

        assertThat(filenames.size()).isEqualTo(externalFiles.size());
        assertThat(filenames.contains(expectedRequestFile.getName())).isFalse();
        assertThat(filenames.contains(expectedResponseFile.getName())).isTrue();
    }

    @Test
    public void shouldReturnDedupedExternalFile() throws Exception {
        final File expectedRequestFile = FileUtils.uriToFile(StubRepositoryTest.class.getResource("/json/request.external.file.json").getFile());
        final File expectedResponseFile = FileUtils.uriToFile(StubRepositoryTest.class.getResource("/json/response.1.external.file.json").getFile());

        resetStubHttpLifecyclesFromYamlResource("/yaml/same.external.files.yaml");
        final Map<File, Long> externalFiles = spyStubRepository.getExternalFiles();

        assertThat(externalFiles.size()).isEqualTo(1);
        assertThat(externalFiles.containsValue(expectedRequestFile.lastModified())).isTrue();

        final Set<String> filenames = new HashSet<>();
        for (final Map.Entry<File, Long> entry : externalFiles.entrySet()) {
            filenames.add(entry.getKey().getName());
        }

        assertThat(filenames.size()).isEqualTo(externalFiles.size());
        assertThat(filenames.contains(expectedRequestFile.getName())).isTrue();
        assertThat(filenames.contains(expectedResponseFile.getName())).isFalse();
    }

    @Test
    public void shouldReturnOnlyResponseExternalFileWhenRequestFileFailedToLoad() throws Exception {
        final File expectedRequestFile = FileUtils.uriToFile(StubRepositoryTest.class.getResource("/json/request.external.file.json").getFile());
        final File expectedResponseFile = FileUtils.uriToFile(StubRepositoryTest.class.getResource("/json/response.1.external.file.json").getFile());

        resetStubHttpLifecyclesFromYamlResource("/yaml/request.null.external.files.yaml");
        final Map<File, Long> externalFiles = spyStubRepository.getExternalFiles();

        assertThat(externalFiles.size()).isEqualTo(1);
        assertThat(externalFiles.containsValue(expectedResponseFile.lastModified())).isTrue();

        final Set<String> filenames = new HashSet<>();
        for (final Map.Entry<File, Long> entry : externalFiles.entrySet()) {
            filenames.add(entry.getKey().getName());
        }

        assertThat(filenames.size()).isEqualTo(externalFiles.size());
        assertThat(filenames.contains(expectedRequestFile.getName())).isFalse();
        assertThat(filenames.contains(expectedResponseFile.getName())).isTrue();
    }

    @Test
    public void shouldReturnOnlyRequestExternalFileWhenResponseFileFailedToLoad() throws Exception {
        final File expectedRequestFile = FileUtils.uriToFile(StubRepositoryTest.class.getResource("/json/request.external.file.json").getFile());
        final File expectedResponseFile = FileUtils.uriToFile(StubRepositoryTest.class.getResource("/json/response.1.external.file.json").getFile());

        resetStubHttpLifecyclesFromYamlResource("/yaml/response.null.external.files.yaml");
        final Map<File, Long> externalFiles = spyStubRepository.getExternalFiles();

        assertThat(externalFiles.size()).isEqualTo(1);
        assertThat(externalFiles.containsValue(expectedRequestFile.lastModified())).isTrue();

        final Set<String> filenames = new HashSet<>();
        for (final Map.Entry<File, Long> entry : externalFiles.entrySet()) {
            filenames.add(entry.getKey().getName());
        }

        assertThat(filenames.size()).isEqualTo(externalFiles.size());
        assertThat(filenames.contains(expectedRequestFile.getName())).isTrue();
        assertThat(filenames.contains(expectedResponseFile.getName())).isFalse();
    }

    @Test
    public void shouldVerifyGetAllResponsesInvokation_WhenInvokingGetExternalFiles() throws Exception {
        final URL yamlUrl = StubRepositoryTest.class.getResource("/yaml/two.cycles.with.multiple.responses.yaml");
        final InputStream stubsDatanputStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();
        final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(parentDirectory, inputStreamToString(stubsDatanputStream)).getStubs();
        assertThat(stubHttpLifecycles.size()).isEqualTo(2);
        assertThat(stubHttpLifecycles.get(0).getResponses().size()).isEqualTo(2);
        assertThat(stubHttpLifecycles.get(1).getResponses().size()).isEqualTo(2);

        final List<StubHttpLifecycle> spyStubHttpLifecycles = new LinkedList<>();
        final StubHttpLifecycle spyCycleOne = spy(stubHttpLifecycles.get(0));
        final StubHttpLifecycle spyCycleTwo = spy(stubHttpLifecycles.get(1));
        spyStubHttpLifecycles.add(spyCycleOne);
        spyStubHttpLifecycles.add(spyCycleTwo);

        spyStubRepository.resetStubsCache(new YamlParseResultSet(spyStubHttpLifecycles, new HashMap<>()));   // 1st time call to getResponses
        spyStubRepository.getExternalFiles();                               // 2nd time call to getResponses

        verify(spyCycleOne, times(2)).getResponses();
        verify(spyCycleTwo, times(2)).getResponses();
    }

    @Test
    public void shouldVerifyGetRawFileInvocation_WhenInvokingGetExternalFiles() throws Exception {
        final URL yamlUrl = StubRepositoryTest.class.getResource("/yaml/two.cycles.with.multiple.responses.yaml");
        final InputStream stubsConfigStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();
        final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(parentDirectory, inputStreamToString(stubsConfigStream)).getStubs();
        assertThat(stubHttpLifecycles.size()).isEqualTo(2);
        assertThat(stubHttpLifecycles.get(0).getResponses().size()).isEqualTo(2);
        assertThat(stubHttpLifecycles.get(1).getResponses().size()).isEqualTo(2);

        // Turn existing StubResponse objects into Mockito.spy
        final LinkedList<StubResponse> stubResponsesOne = new LinkedList<StubResponse>() {{
            add(spy(stubHttpLifecycles.get(0).getResponses().get(0)));
            add(spy(stubHttpLifecycles.get(0).getResponses().get(1)));
        }};
        injectObjectFields(stubHttpLifecycles.get(0), "response", stubResponsesOne);

        // Turn existing StubResponse objects into Mockito.spy
        final LinkedList<StubResponse> stubResponsesTwo = new LinkedList<StubResponse>() {{
            add(spy(stubHttpLifecycles.get(1).getResponses().get(0)));
            add(spy(stubHttpLifecycles.get(1).getResponses().get(1)));
        }};
        injectObjectFields(stubHttpLifecycles.get(1), "response", stubResponsesTwo);

        spyStubRepository.resetStubsCache(new YamlParseResultSet(stubHttpLifecycles, new HashMap<>()));
        spyStubRepository.getExternalFiles();

        verify(stubHttpLifecycles.get(0).getResponses().get(0)).getRawFile();
        verify(stubHttpLifecycles.get(0).getResponses().get(1)).getRawFile();
        verify(stubHttpLifecycles.get(1).getResponses().get(0)).getRawFile();
        verify(stubHttpLifecycles.get(1).getResponses().get(1)).getRawFile();
    }

    @Test
    public void shouldAdjustResourceIDHeadersAccordingly_WhenSomeHttpCycleWasDeleted() throws Exception {
        final String cycleOne = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/1")
                .withQuery("paramName1", "paramValue1")
                .newStubbedResponse()
                .withStatus("200")
                .build();

        final String cycleTwo = YAML_BUILDER
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerOne", "valueOne")
                .withSequenceResponseLiteralBody("BodyContent")
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerTwo", "valueTwo")
                .withSequenceResponseLiteralBody("BodyContentTwo")
                .build();

        final String cycleThree = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/2")
                .withQuery("paramName2", "paramValue2")
                .newStubbedResponse()
                .withStatus("201")
                .build();

        loadYamlToDataStore(String.format("%s%s%s%s%s", cycleOne, FileUtils.BR, cycleTwo, FileUtils.BR, cycleThree));

        List<StubHttpLifecycle> beforeDeletionLoadedHttpCycles = spyStubRepository.getStubs();
        assertThat(beforeDeletionLoadedHttpCycles.size()).isEqualTo(3);

        for (int resourceId = 0; resourceId < beforeDeletionLoadedHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = beforeDeletionLoadedHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }

        spyStubRepository.deleteStubByIndex(1);

        List<StubHttpLifecycle> afterDeletionLoadedHttpCycles = spyStubRepository.getStubs();
        assertThat(afterDeletionLoadedHttpCycles.size()).isEqualTo(2);

        for (int resourceId = 0; resourceId < afterDeletionLoadedHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = afterDeletionLoadedHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }
    }

    @Test
    public void shouldAdjustResourceIDHeadersAccordingly_WhenHttpCyclesWereReset() throws Exception {
        final String cycleOne = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/1")
                .withQuery("paramName1", "paramValue1")
                .newStubbedResponse()
                .withStatus("200")
                .build();

        loadYamlToDataStore(cycleOne);

        List<StubHttpLifecycle> beforeResetHttpCycles = spyStubRepository.getStubs();
        assertThat(beforeResetHttpCycles.size()).isEqualTo(1);

        for (int resourceId = 0; resourceId < beforeResetHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = beforeResetHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }

        final String cycleTwo = YAML_BUILDER
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerOne", "valueOne")
                .withSequenceResponseLiteralBody("BodyContent")
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerTwo", "valueTwo")
                .withSequenceResponseLiteralBody("BodyContentTwo")
                .build();

        final String cycleThree = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/2")
                .withQuery("paramName2", "paramValue2")
                .newStubbedResponse()
                .withStatus("201")
                .build();

        final YamlParseResultSet yamlParseResultSet = new YamlParser().parse(".", String.format("%s%s%s", cycleTwo, FileUtils.BR, cycleThree));
        spyStubRepository.resetStubsCache(yamlParseResultSet);

        List<StubHttpLifecycle> afterResetHttpCycles = spyStubRepository.getStubs();
        assertThat(afterResetHttpCycles.size()).isEqualTo(2);

        for (int resourceId = 0; resourceId < afterResetHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = afterResetHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }
    }

    @Test
    public void shouldAdjustResourceIDHeadersAccordingly_WhenSomeHttpCycleWasUpdated() throws Exception {

        final String cycleTwo = YAML_BUILDER
                .newStubbedRequest()
                .withMethodPut()
                .withUrl("/invoice")
                .newStubbedResponse()
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerOne", "valueOne")
                .withSequenceResponseLiteralBody("BodyContent")
                .withSequenceResponseStatus("200")
                .withSequenceResponseHeaders("headerTwo", "valueTwo")
                .withSequenceResponseLiteralBody("BodyContentTwo")
                .build();

        final String cycleThree = YAML_BUILDER.newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/2")
                .withQuery("paramName2", "paramValue2")
                .newStubbedResponse()
                .withStatus("201")
                .build();

        loadYamlToDataStore(String.format("%s%s%s", cycleTwo, FileUtils.BR, cycleThree));

        List<StubHttpLifecycle> beforeUpdateHttpCycles = spyStubRepository.getStubs();
        assertThat(beforeUpdateHttpCycles.size()).isEqualTo(2);

        for (int resourceId = 0; resourceId < beforeUpdateHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = beforeUpdateHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }

        final String cycleOne = YAML_BUILDER
                .newStubbedRequest()
                .withMethodGet()
                .withUrl("/some/uri/updating/cycle")
                .withQuery("paramName1", "paramValue1")
                .newStubbedResponse()
                .withStatus("200")
                .build();

        final List<StubHttpLifecycle> stubHttpLifecycles = new YamlParser().parse(".", cycleOne).getStubs();
        final StubHttpLifecycle updatingStubHttpLifecycle = stubHttpLifecycles.get(0);

        spyStubRepository.updateStubByIndex(0, updatingStubHttpLifecycle);
        final List<StubHttpLifecycle> afterUpdateHttpCycles = spyStubRepository.getStubs();

        assertThat(afterUpdateHttpCycles.size()).isEqualTo(2);
        final String firstCycleUrl = afterUpdateHttpCycles.get(0).getUrl();
        assertThat(firstCycleUrl).isEqualTo("/some/uri/updating/cycle?paramName1=paramValue1");

        for (int resourceId = 0; resourceId < afterUpdateHttpCycles.size(); resourceId++) {
            final StubHttpLifecycle cycle = afterUpdateHttpCycles.get(resourceId);
            final List<StubResponse> allResponses = cycle.getResponses();

            for (int sequence = 0; sequence < allResponses.size(); sequence++) {
                final StubResponse sequenceStubResponse = allResponses.get(sequence);
                assertThat(sequenceStubResponse.getHeaders()).containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER);
                assertThat(sequenceStubResponse.getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo(String.valueOf(resourceId));
            }
        }
    }

    private void loadYamlToDataStore(final String yaml) throws Exception {
        spyStubRepository.resetStubsCache(new YamlParser().parse(".", yaml));
    }

    private void resetStubHttpLifecyclesFromYamlResource(final String resourcePath) throws Exception {
        final URL yamlUrl = StubRepositoryTest.class.getResource(resourcePath);
        final InputStream stubsDatanputStream = yamlUrl.openStream();
        final String parentDirectory = new File(yamlUrl.getPath()).getParent();
        spyStubRepository.resetStubsCache(new YamlParser().parse(parentDirectory, inputStreamToString(stubsDatanputStream)));
    }
}