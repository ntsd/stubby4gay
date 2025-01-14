package io.github.ntsd.stubby4gay.stubs;

import io.github.ntsd.stubby4gay.annotations.CoberturaIgnore;
import io.github.ntsd.stubby4gay.annotations.VisibleForTesting;
import io.github.ntsd.stubby4gay.utils.FileUtils;
import io.github.ntsd.stubby4gay.utils.StringUtils;
import org.eclipse.jetty.http.HttpStatus.Code;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static io.github.azagniotov.generics.TypeSafeConverter.asCheckedLinkedHashMap;
import static io.github.ntsd.stubby4gay.utils.FileUtils.fileToBytes;
import static io.github.ntsd.stubby4gay.utils.FileUtils.isFilePathContainTemplateTokens;
import static io.github.ntsd.stubby4gay.utils.ObjectUtils.isNull;
import static io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty.BODY;
import static io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty.FILE;
import static io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty.HEADERS;
import static io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty.LATENCY;
import static io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty.STATUS;
import static java.lang.Integer.parseInt;
import static org.eclipse.jetty.http.HttpStatus.getCode;


public class StubResponse implements ReflectableStub {

    public static final String STUBBY_RESOURCE_ID_HEADER = "x-stubby-resource-id";

    private final Code httpStatusCode;
    private final String body;
    private final File file;
    private final byte[] fileBytes;
    private final String latency;
    private final Map<String, String> headers;

    private StubResponse(final Code httpStatusCode,
                         final String body,
                         final File file,
                         final String latency,
                         final Map<String, String> headers) {
        this.httpStatusCode = httpStatusCode;
        this.body = body;
        this.file = file;
        this.fileBytes = isNull(file) ? new byte[]{} : getFileBytes();
        this.latency = latency;
        this.headers = isNull(headers) ? new LinkedHashMap<>() : headers;
    }

    public static StubResponse okResponse() {
        return new StubResponse.Builder().build();
    }

    public static StubResponse notFoundResponse() {
        return new StubResponse.Builder().withHttpStatusCode(Code.NOT_FOUND).build();
    }

    public static StubResponse unauthorizedResponse() {
        return new StubResponse.Builder().withHttpStatusCode(Code.UNAUTHORIZED).build();
    }

    public static StubResponse redirectResponse(final Optional<StubResponse> stubResponseOptional) {
        if (!stubResponseOptional.isPresent()) {
            return new StubResponse.Builder().withHttpStatusCode(Code.MOVED_PERMANENTLY).build();
        }
        final StubResponse foundStubResponse = stubResponseOptional.get();
        return new StubResponse(
                foundStubResponse.getHttpStatusCode(),
                foundStubResponse.getBody(),
                foundStubResponse.getRawFile(),
                foundStubResponse.getLatency(),
                foundStubResponse.getHeaders());
    }

    public Code getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getBody() {
        return (StringUtils.isSet(body) ? body : "");
    }

    public boolean isRecordingRequired() {
        final String body = getBody();
        return StringUtils.toLower(body).startsWith("http");
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getLatency() {
        return latency;
    }

    /**
     * Used by reflection when populating stubby admin page with stubbed information
     */
    public byte[] getFile() {
        return fileBytes;
    }

    public File getRawFile() {
        return file;
    }

    public String getRawFileAbsolutePath() {
        return file.getAbsolutePath();
    }

    public byte[] getResponseBodyAsBytes() {

        if (fileBytes.length == 0) {
            return StringUtils.getBytesUtf8(getBody());
        }
        return fileBytes;
    }

    public boolean isBodyContainsTemplateTokens() {
        final boolean isFileTemplate = fileBytes.length != 0 && isTemplateFile();
        return isFileTemplate || StringUtils.isTokenized(getBody());
    }

    public boolean isFilePathContainsTemplateTokens() {
        try {
            return isFilePathContainTemplateTokens(file);
        } catch (Exception e) {
            return false;
        }
    }

    @CoberturaIgnore
    private boolean isTemplateFile() {
        try {
            return FileUtils.isTemplateFile(file);
        } catch (Exception e) {
            return false;
        }
    }

    @CoberturaIgnore
    private byte[] getFileBytes() {
        try {
            return fileToBytes(file);
        } catch (Exception e) {
            return new byte[]{};
        }
    }

    public boolean hasHeaderLocation() {
        return getHeaders().containsKey("location");
    }

    void addResourceIDHeader(final int resourceIndex) {
        getHeaders().put(STUBBY_RESOURCE_ID_HEADER, String.valueOf(resourceIndex));
    }

    String getResourceIDHeader() {
        return getHeaders().get(StubResponse.STUBBY_RESOURCE_ID_HEADER);
    }

    public static final class Builder extends AbstractBuilder<StubResponse> {

        private String status;
        private String body;
        private File file;
        private String latency;
        private Map<String, String> headers;

        public Builder() {
            super();
            this.status = null;
            this.body = null;
            this.file = null;
            this.latency = null;
            this.headers = new LinkedHashMap<>();
        }

        public Builder emptyWithBody(final String body) {
            this.status = String.valueOf(Code.OK.getCode());
            this.body = body;

            return this;
        }

        public Builder withHttpStatusCode(final Code httpStatusCode) {
            this.status = String.valueOf(httpStatusCode.getCode());

            return this;
        }

        public Builder withBody(final String body) {
            this.body = body;

            return this;
        }

        public Builder withFile(final File file) {
            this.file = file;

            return this;
        }

        @Override
        public StubResponse build() {
            this.status = getStaged(String.class, STATUS, status);
            this.body = getStaged(String.class, BODY, body);
            this.file = getStaged(File.class, FILE, file);
            this.latency = getStaged(String.class, LATENCY, latency);
            this.headers = asCheckedLinkedHashMap(getStaged(Map.class, HEADERS, headers), String.class, String.class);

            final StubResponse stubResponse = new StubResponse(getHttpStatusCode(), body, file, latency, headers);

            this.status = null;
            this.body = null;
            this.file = null;
            this.latency = null;
            this.headers = new LinkedHashMap<>();
            this.fieldNameAndValues.clear();

            return stubResponse;
        }

        @VisibleForTesting
        Code getHttpStatusCode() {
            return isNull(this.status) ? Code.OK : getCode(parseInt(this.status));
        }
    }
}
