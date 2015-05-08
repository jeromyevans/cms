package com.blueskyminds.cms;

import com.blueskyminds.cms.template.TemplateFactory;
import com.blueskyminds.cms.template.UrlTemplateFactory;
import com.blueskyminds.cms.definitions.PageType;
import com.blueskyminds.cms.definitions.LocationSpec;
import com.blueskyminds.web.components.InfoPanelFactory;
import com.blueskyminds.framework.persistence.PersistenceSession;
import com.blueskyminds.framework.persistence.PersistenceService;
import com.blueskyminds.framework.persistence.PersistenceServiceException;
import com.blueskyminds.framework.persistence.PersistenceServiceFactory;
import com.blueskyminds.framework.tools.PropertiesContext;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.*;

/**
 * The main servlet for the Cms
 *
 * Date Started: 23/07/2006
 *
 * History:
 *
 * ---[ Blue Sky Minds Pty Ltd ]------------------------------------------------------------------------------
 */
public class CmsServlet extends HttpServlet {

    private CmsController cmsController;
    private PersistenceService persistenceService;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        initialiseTestSite();

        PropertiesContext properties = new PropertiesContext();
        properties.setProperty(PersistenceServiceFactory.PERSISTENCE_SERVICE, "com.blueskyminds.framework.persistence.jpa.JpaPersistenceService");
        try {
            persistenceService = PersistenceServiceFactory.createPersistenceService(properties);
        } catch (PersistenceServiceException e) {
            throw new ServletException(e);
        }

        cmsController = new CmsController();
        cmsController.loadTemplates();
        cmsController.loadLocations();
    }

    private void initialiseTestSite() {
        TemplateFactory template1 = new UrlTemplateFactory("main", "mainTemplate.html");
        TemplateFactory template2 = new UrlTemplateFactory("infoPanel", "infopanel.html");

        PageType type = new PageType("main");
        type.addBinding("content", InfoPanelFactory.class.getName(), "infoPanel");

        LocationSpec location = new LocationSpec("index.html", type);
        location.setProperty("infopanel.title", "Information");
        location.setProperty("infopanel.content", "<p>Sorry, this site is currently for use by clients only.</p><p>Please contact Jeromy Evans for information.</p>");

        LocationSpec notFound = new LocationSpec("notFound.html", type);
        notFound.setProperty("infopanel.title", "Error");
        notFound.setProperty("infopanel.content", "<p>Sorry, the requested document could not be found.</p>");

        PersistenceSession session;

        try {            
            persistenceService.save(template1);
            persistenceService.save(template2);
            persistenceService.save(type);
            persistenceService.save(location);
            persistenceService.save(notFound);

            //TestTools.printAll(ForumGroup.class);

        } catch (PersistenceServiceException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------------------------------------

    /** Write from the input stream tothe output stream */
    private void stream(OutputStream outputStream, InputStream inputStream) throws IOException {
        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            String line;

            while ((line = reader.readLine()) != null) {
                writer.write(line+"\n");
                writer.flush();
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        stream(response.getOutputStream(), cmsController.get(request.getRequestURI()));
        //response.getWriter().print("That's it, finally.");
    }

    // ------------------------------------------------------------------------------------------------------
}
