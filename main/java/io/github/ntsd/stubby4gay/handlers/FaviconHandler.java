package io.github.ntsd.stubby4gay.handlers;

import io.github.ntsd.stubby4gay.utils.ConsoleUtils;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

import static io.github.ntsd.stubby4gay.utils.ObjectUtils.isNotNull;

public class FaviconHandler extends AbstractHandler {

    private final long faviconModified = (System.currentTimeMillis() / 1000) * 1000L;
    private byte[] faviconBytes;

    public FaviconHandler() {
        try {
            final URL fav = this.getClass().getClassLoader().getResource("ui/images/favicon.ico");
            if (isNotNull(fav)) {
                faviconBytes = IO.readBytes(Resource.newResource(fav).getInputStream());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handle(final String target,
                       final Request baseRequest,
                       final HttpServletRequest request,
                       final HttpServletResponse response) throws IOException, ServletException {
        ConsoleUtils.logIncomingRequest(request);
        if (response.isCommitted() || baseRequest.isHandled()) {
            ConsoleUtils.logIncomingRequestError(request, "favicon", "HTTP response was committed or base request was handled, aborting..");
            return;
        }
        baseRequest.setHandled(true);

        if (isNotNull(faviconBytes) && HttpMethod.GET.is(request.getMethod()) && request.getRequestURI().equals("/favicon.ico")) {
            if (request.getDateHeader(HttpHeader.IF_MODIFIED_SINCE.toString()) == faviconModified)
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("image/x-icon");
                response.setContentLength(faviconBytes.length);
                response.setDateHeader(HttpHeader.LAST_MODIFIED.toString(), faviconModified);
                response.setHeader(HttpHeader.CACHE_CONTROL.toString(), "max-age=360000,public");
                response.getOutputStream().write(faviconBytes);
            }
        }
    }
}
