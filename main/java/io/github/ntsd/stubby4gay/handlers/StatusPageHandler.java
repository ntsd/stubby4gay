/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.ntsd.stubby4gay.handlers;

import io.github.ntsd.stubby4gay.cli.CommandLineInterpreter;
import io.github.ntsd.stubby4gay.server.JettyContext;
import io.github.ntsd.stubby4gay.stubs.StubHttpLifecycle;
import io.github.ntsd.stubby4gay.stubs.StubRepository;
import io.github.ntsd.stubby4gay.stubs.StubResponse;
import io.github.ntsd.stubby4gay.utils.ConsoleUtils;
import io.github.ntsd.stubby4gay.utils.DateTimeUtils;
import io.github.ntsd.stubby4gay.utils.HandlerUtils;
import io.github.ntsd.stubby4gay.utils.JarUtils;
import io.github.ntsd.stubby4gay.utils.ObjectUtils;
import io.github.ntsd.stubby4gay.utils.ReflectionUtils;
import io.github.ntsd.stubby4gay.utils.StringUtils;
import io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;

import static io.github.ntsd.stubby4gay.utils.HandlerUtils.getHtmlResourceByName;
import static io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty.BODY;
import static io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty.FILE;
import static io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty.POST;
import static io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty.REQUEST;
import static io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty.RESPONSE;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public final class StatusPageHandler extends AbstractHandler {

    private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
    private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();
    private static final List<ConfigurableYAMLProperty> FIELDS_FOR_AJAX_LINKS = unmodifiableList(asList(FILE, BODY, POST));

    private static final String TEMPLATE_LOADED_FILE_METADATA_PAIR = "<span style='color: #8B0000'>%s</span>=<span style='color: green'>%s</span>";
    private static final String TEMPLATE_AJAX_TO_RESOURCE_HYPERLINK = "<strong><a class='ajax-resource' href='/ajax/resource/%s/%s/%s'>[view]</a></strong>";
    private static final String TEMPLATE_AJAX_TO_STATS_HYPERLINK = "<strong><a class='ajax-stats' href='/ajax/stats'>[view]</a></strong>";
    private static final String TEMPLATE_HTML_TABLE_ROW = "<tr><td width='250px' valign='top' align='left'>%s</td><td align='left'>%s</td></tr>";
    private static final String NEXT_IN_THE_QUEUE = " NEXT IN THE QUEUE";

    private final StubRepository stubRepository;
    private final JettyContext jettyContext;

    public StatusPageHandler(final JettyContext newContext, final StubRepository newStubRepository) {
        this.jettyContext = newContext;
        this.stubRepository = newStubRepository;
    }

    @Override
    public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        ConsoleUtils.logIncomingRequest(request);
        if (response.isCommitted() || baseRequest.isHandled()) {
            ConsoleUtils.logIncomingRequestError(request, "status", "HTTP response was committed or base request was handled, aborting..");
            return;
        }
        baseRequest.setHandled(true);
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpStatus.OK_200);
        response.setHeader(HttpHeader.SERVER.asString().toLowerCase(), HandlerUtils.constructHeaderServerName());

        try {
            response.getWriter().println(buildStatusPageHtml());
        } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
        }

        ConsoleUtils.logOutgoingResponse(request.getRequestURI(), response);
    }

    private String buildStatusPageHtml() throws Exception {
        final StringBuilder builder = new StringBuilder();

        final String templateHtmlTable = getHtmlResourceByName("_table");

        builder.append(buildJvmParametersHtmlTable(templateHtmlTable));
        builder.append(buildJettyParametersHtmlTable(templateHtmlTable));
        builder.append(buildStubbyParametersHtmlTable(templateHtmlTable));
        builder.append(buildEndpointStatsHtmlTable(templateHtmlTable));

        final List<StubHttpLifecycle> stubHttpLifecycles = stubRepository.getStubs();
        for (int cycleIndex = 0; cycleIndex < stubHttpLifecycles.size(); cycleIndex++) {
            final StubHttpLifecycle stubHttpLifecycle = stubHttpLifecycles.get(cycleIndex);
            builder.append(buildStubRequestHtmlTable(stubHttpLifecycle, templateHtmlTable));
            builder.append(buildStubResponseHtmlTable(stubHttpLifecycle, templateHtmlTable));
            builder.append("<br /><br />");
        }

        final long timestamp = System.currentTimeMillis();
        return HandlerUtils.populateHtmlTemplate("status", timestamp, timestamp, builder.toString());
    }

    private String buildStubRequestHtmlTable(final StubHttpLifecycle stubHttpLifecycle, final String templateHtmlTable) throws Exception {
        final String resourceId = stubHttpLifecycle.getResourceId();
        final String ajaxLinkToRequestAsYaml = String.format(TEMPLATE_AJAX_TO_RESOURCE_HYPERLINK, resourceId, ConfigurableYAMLProperty.HTTPLIFECYCLE, "requestAsYAML");
        final StringBuilder requestTableBuilder = buildStubHtmlTableBody(resourceId, REQUEST.toString(), ReflectionUtils.getProperties(stubHttpLifecycle.getRequest()));
        requestTableBuilder.append(interpolateHtmlTableRowTemplate("RAW YAML", ajaxLinkToRequestAsYaml));

        return String.format(templateHtmlTable, REQUEST, requestTableBuilder.toString());
    }

    private String buildStubResponseHtmlTable(final StubHttpLifecycle stubHttpLifecycle, final String templateHtmlTable) throws Exception {
        final String resourceId = stubHttpLifecycle.getResourceId();
        final StringBuilder responseTableBuilder = new StringBuilder();
        final List<StubResponse> allResponses = stubHttpLifecycle.getResponses();
        for (int sequenceId = 0; sequenceId < allResponses.size(); sequenceId++) {

            final boolean isResponsesSequenced = allResponses.size() != 1;
            final int nextSequencedResponseId = stubHttpLifecycle.getNextSequencedResponseId();
            final String nextResponseLabel = (isResponsesSequenced && nextSequencedResponseId == sequenceId ? NEXT_IN_THE_QUEUE : "");
            final String responseTableTitle = (isResponsesSequenced ? String.format("%s/%s%s", RESPONSE, sequenceId, nextResponseLabel) : RESPONSE.toString());
            final StubResponse stubResponse = allResponses.get(sequenceId);
            final Map<String, String> stubResponseProperties = ReflectionUtils.getProperties(stubResponse);
            final StringBuilder sequencedResponseBuilder = buildStubHtmlTableBody(resourceId, responseTableTitle, stubResponseProperties);
            final String ajaxLinkToResponseAsYaml = String.format(TEMPLATE_AJAX_TO_RESOURCE_HYPERLINK, resourceId, ConfigurableYAMLProperty.HTTPLIFECYCLE, "responseAsYAML");
            sequencedResponseBuilder.append(interpolateHtmlTableRowTemplate("RAW YAML", ajaxLinkToResponseAsYaml));

            responseTableBuilder.append(String.format(templateHtmlTable, responseTableTitle, sequencedResponseBuilder.toString()));
        }

        return responseTableBuilder.toString();
    }

    private String buildJvmParametersHtmlTable(final String templateHtmlTable) throws Exception {

        final StringBuilder builder = new StringBuilder();
        if (!RUNTIME_MX_BEAN.getInputArguments().isEmpty()) {
            builder.append(interpolateHtmlTableRowTemplate("INPUT ARGS", RUNTIME_MX_BEAN.getInputArguments()));
        }
        builder.append(interpolateHtmlTableRowTemplate("HEAP MEMORY USAGE", MEMORY_MX_BEAN.getHeapMemoryUsage()));
        builder.append(interpolateHtmlTableRowTemplate("NON-HEAP MEMORY USAGE", MEMORY_MX_BEAN.getNonHeapMemoryUsage()));

        return String.format(templateHtmlTable, "jvm", builder.toString());
    }

    private String buildJettyParametersHtmlTable(final String templateHtmlTable) throws Exception {

        final StringBuilder builder = new StringBuilder();
        final String host = jettyContext.getHost();
        final int adminPort = jettyContext.getAdminPort();
        builder.append(interpolateHtmlTableRowTemplate("HOST", host));
        builder.append(interpolateHtmlTableRowTemplate("ADMIN PORT", adminPort));
        builder.append(interpolateHtmlTableRowTemplate("STUBS PORT", jettyContext.getStubsPort()));
        builder.append(interpolateHtmlTableRowTemplate("STUBS TLS PORT", jettyContext.getStubsTlsPort()));
        final String endpointRegistration = HandlerUtils.linkifyRequestUrl(HttpScheme.HTTP.asString(), AdminPortalHandler.ADMIN_ROOT, host, adminPort);
        builder.append(interpolateHtmlTableRowTemplate("NEW STUB DATA POST URI", endpointRegistration));

        return String.format(templateHtmlTable, "jetty parameters", builder.toString());
    }

    private String buildStubbyParametersHtmlTable(final String templateHtmlTable) throws Exception {

        final StringBuilder builder = new StringBuilder();
        builder.append(interpolateHtmlTableRowTemplate("VERSION", JarUtils.readManifestImplementationVersion()));
        builder.append(interpolateHtmlTableRowTemplate("RUNTIME CLASSPATH", RUNTIME_MX_BEAN.getClassPath()));
        builder.append(interpolateHtmlTableRowTemplate("LOCAL BUILT DATE", JarUtils.readManifestBuiltDate()));
        builder.append(interpolateHtmlTableRowTemplate("UPTIME", HandlerUtils.calculateStubbyUpTime(RUNTIME_MX_BEAN.getUptime())));
        builder.append(interpolateHtmlTableRowTemplate("INPUT ARGS", CommandLineInterpreter.PROVIDED_OPTIONS));
        builder.append(interpolateHtmlTableRowTemplate("STUBBED ENDPOINTS", stubRepository.getStubs().size()));
        builder.append(interpolateHtmlTableRowTemplate("LOADED YAML", buildLoadedFileMetadata(stubRepository.getYamlConfig())));

        if (!stubRepository.getExternalFiles().isEmpty()) {
            final StringBuilder externalFilesMetadata = new StringBuilder();
            for (Map.Entry<File, Long> entry : stubRepository.getExternalFiles().entrySet()) {
                final File externalFile = entry.getKey();
                externalFilesMetadata.append(buildLoadedFileMetadata(externalFile));
            }
            builder.append(interpolateHtmlTableRowTemplate("LOADED EXTERNAL FILES", externalFilesMetadata.toString()));
        }

        return String.format(templateHtmlTable, "stubby4gay parameters", builder.toString());
    }

    private String buildEndpointStatsHtmlTable(final String templateHtmlTable) throws Exception {

        final StringBuilder builder = new StringBuilder();
        if (stubRepository.getResourceStats().isEmpty()) {
            builder.append(interpolateHtmlTableRowTemplate("ENDPOINT HITS", "No requests were made to stubby yet"));
        } else {
            builder.append(interpolateHtmlTableRowTemplate("ENDPOINT HITS", TEMPLATE_AJAX_TO_STATS_HYPERLINK));
        }

        return String.format(templateHtmlTable, "stubby stats", builder.toString());
    }

    private String buildLoadedFileMetadata(final File file) throws IOException {
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format(TEMPLATE_LOADED_FILE_METADATA_PAIR, "parentDir", determineParentDir(file))).append("<br />");
        builder.append(String.format(TEMPLATE_LOADED_FILE_METADATA_PAIR, "name", file.getName())).append("<br />");
        builder.append(String.format(TEMPLATE_LOADED_FILE_METADATA_PAIR, "size", String.format("%1$,.2f", ((double) file.length() / 1024)) + "kb")).append("<br />");
        builder.append(String.format(TEMPLATE_LOADED_FILE_METADATA_PAIR, "lastModified", DateTimeUtils.systemDefault(file.lastModified()))).append("<br />");

        return "<div style='margin-top: 5px; padding: 3px 7px 3px 7px; background-color: #fefefe'>" + builder.toString() + "</div>";
    }

    private String determineParentDir(final File file) throws IOException {
        return (ObjectUtils.isNull(file.getParentFile()) ? file.getCanonicalPath().replaceAll(file.getName(), "") : file.getParentFile().getCanonicalPath() + "/");
    }

    private StringBuilder buildStubHtmlTableBody(final String resourceId, final String stubTypeName, final Map<String, String> stubObjectProperties) throws Exception {
        final StringBuilder builder = new StringBuilder();

        for (final Map.Entry<String, String> keyValue : stubObjectProperties.entrySet()) {
            final String value = keyValue.getValue();
            final String key = keyValue.getKey();

            if (!StringUtils.isSet(value)) {
                continue;
            }

            builder.append(buildHtmlTableSingleRow(resourceId, stubTypeName, key, value));
        }
        return builder;
    }

    private String buildHtmlTableSingleRow(final String resourceId, final String stubTypeName, final String fieldName, final String value) {

        if (FIELDS_FOR_AJAX_LINKS.contains(fieldName)) {
            final String cleansedStubTypeName = stubTypeName.replaceAll(NEXT_IN_THE_QUEUE, "");   //Only when there are sequenced responses
            final String ajaxHyperlink = String.format(TEMPLATE_AJAX_TO_RESOURCE_HYPERLINK, resourceId, cleansedStubTypeName, fieldName);
            return interpolateHtmlTableRowTemplate(StringUtils.toUpper(fieldName), ajaxHyperlink);
        }

        final String escapedValue = StringUtils.escapeHtmlEntities(value);
        if (fieldName.equals(ConfigurableYAMLProperty.URL)) {
            final String urlAsHyperlink = HandlerUtils.linkifyRequestUrl(HttpScheme.HTTP.asString(), escapedValue, jettyContext.getHost(), jettyContext.getStubsPort());
            final String tlsUrlAsHyperlink = HandlerUtils.linkifyRequestUrl(HttpScheme.HTTPS.asString(), escapedValue, jettyContext.getHost(), jettyContext.getStubsTlsPort());

            final String tableRowWithUrl = interpolateHtmlTableRowTemplate(StringUtils.toUpper(fieldName), urlAsHyperlink);
            final String tableRowWithTlsUrl = interpolateHtmlTableRowTemplate("TLS " + StringUtils.toUpper(fieldName), tlsUrlAsHyperlink);

            return String.format("%s%s", tableRowWithUrl, tableRowWithTlsUrl);
        }

        return interpolateHtmlTableRowTemplate(StringUtils.toUpper(fieldName), escapedValue);
    }

    private String interpolateHtmlTableRowTemplate(final Object... tokens) {
        return String.format(TEMPLATE_HTML_TABLE_ROW, tokens);
    }
}
