package io.github.ntsd.stubby4gay.http;

import io.github.ntsd.stubby4gay.cli.ANSITerminal;
import io.github.ntsd.stubby4gay.client.StubbyResponse;
import io.github.ntsd.stubby4gay.stubs.StubRequest;
import io.github.ntsd.stubby4gay.utils.ConsoleUtils;
import io.github.ntsd.stubby4gay.utils.StringUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.ntsd.stubby4gay.common.Common.POSTING_METHODS;
import static io.github.ntsd.stubby4gay.utils.StringUtils.charsetUTF8;
import static io.github.ntsd.stubby4gay.utils.StringUtils.inputStreamToString;
import static java.lang.String.valueOf;
import static java.util.Map.Entry;
import static org.eclipse.jetty.http.HttpHeader.CONTENT_ENCODING;
import static org.eclipse.jetty.http.HttpHeader.CONTENT_LANGUAGE;
import static org.eclipse.jetty.http.HttpHeader.CONTENT_LENGTH;
import static org.eclipse.jetty.http.HttpHeader.CONTENT_TYPE;

/**
 * @author Alexander Zagniotov
 * @since 11/4/12, 11:03 AM
 */
public class StubbyHttpTransport {
    private static final Logger LOGGER = LoggerFactory.getLogger(StubbyHttpTransport.class);

    private static final Set<String> SUPPORTED_METHODS = new HashSet<String>() {{
        add(HttpMethod.GET.asString());
        add(HttpMethod.HEAD.asString());
        add(HttpMethod.TRACE.asString());
        add(HttpMethod.OPTIONS.asString());
        add(HttpMethod.DELETE.asString());
        add(HttpMethod.POST.asString());
        add(HttpMethod.PUT.asString());
    }};

    public StubbyHttpTransport() {

    }

    public StubbyResponse fetchRecordableHTTPResponse(final StubRequest request, final String recordingSource) throws IOException {
        final String method = request.getMethod().get(0);
        if (!ANSITerminal.isMute()) {
            final String logMessage = String.format("[%s] -> Recording HTTP response using %s [%s]", ConsoleUtils.getTime(), method, recordingSource);
            ANSITerminal.incoming(logMessage);
        }
        LOGGER.debug("Recording HTTP response using {} [{}].", method, recordingSource);
        return getResponse(method,
                recordingSource,
                request.getPostBody(),
                request.getHeaders(),
                StringUtils.calculateStringLength(request.getPostBody()));
    }

    public StubbyResponse getResponse(final String method,
                                      final String fullUrl,
                                      final String post,
                                      final Map<String, String> headers,
                                      final int postLength) throws IOException {

        if (!SUPPORTED_METHODS.contains(method)) {
            throw new UnsupportedOperationException(String.format("HTTP method '%s' not supported when contacting stubby4gay", method));
        }

        final URL url = new URL(fullUrl);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(false);
        setRequestHeaders(connection, headers, postLength);

        if (POSTING_METHODS.contains(method)) {
            writePost(connection, post);
        }

        return buildStubbyResponse(connection);
    }

    private StubbyResponse buildStubbyResponse(final HttpURLConnection connection) throws IOException {
        try {
            connection.connect();
            final int responseCode = connection.getResponseCode();
            if (responseCode == HttpStatus.OK_200 || responseCode == HttpStatus.CREATED_201) {
                try (final InputStream inputStream = connection.getInputStream()) {
                    final String responseContent = inputStreamToString(inputStream);

                    return new StubbyResponse(responseCode, responseContent);
                }
            }
            return new StubbyResponse(responseCode, connection.getResponseMessage());
        } finally {
            connection.disconnect();
        }
    }

    private void setRequestHeaders(final HttpURLConnection connection, final Map<String, String> headers, final int postLength) {
        connection.setRequestProperty("User-Agent", StringUtils.constructUserAgentName());
        final String requestMethod = connection.getRequestMethod();
        if (POSTING_METHODS.contains(StringUtils.toUpper(requestMethod))) {
            connection.setDoOutput(true);
            connection.setRequestProperty(CONTENT_TYPE.asString(), "application/x-www-form-urlencoded");
            connection.setRequestProperty(CONTENT_LANGUAGE.asString(), "en-US");
            connection.setRequestProperty(CONTENT_ENCODING.asString(), StringUtils.UTF_8);

            connection.setRequestProperty(CONTENT_LENGTH.asString(), valueOf(postLength));
            if (postLength > 0) {
                connection.setFixedLengthStreamingMode(postLength);
            } else {
                connection.setChunkedStreamingMode(0);
            }
        }

        for (final Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private void writePost(final HttpURLConnection connection, final String post) throws IOException {
        try (final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream(), charsetUTF8());
             final BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
            bufferedWriter.write(post);
        }
    }
}
