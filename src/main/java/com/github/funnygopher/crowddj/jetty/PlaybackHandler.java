package com.github.funnygopher.crowddj.jetty;

import com.github.funnygopher.crowddj.CrowdDJ;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PlaybackHandler extends AbstractHandler {

    private CrowdDJ crowdDJ;

    public PlaybackHandler(CrowdDJ crowdDJ) {
        this.crowdDJ = crowdDJ;
    }

    public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        httpServletResponse.setContentType("text/html; charset=utf-8");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);

        switch(s) {
            case "/play":
                crowdDJ.getVLC().getController().play();
                break;

            case "/pause":
                crowdDJ.getVLC().getController().pause();
                break;

            case "/stop":
                crowdDJ.getVLC().getController().stop();
                break;

            case "/status":
                break;

            case "/playlist":
                break;
        }

        httpServletResponse.getWriter().println(s);
        request.setHandled(true);
    }
}