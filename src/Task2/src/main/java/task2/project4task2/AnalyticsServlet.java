//agarg2
//Arpit Garg
//agarg2@andrew.cmu.edu
package task2.project4task2;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "Analytics", value = "/aksharAnalytics")
public class AnalyticsServlet extends HttpServlet {
    private AnalyticsModel am;
    public void init() {
        am = new AnalyticsModel();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Inside GET METHOD.");

        request.setAttribute("doctype", determineView(request));

        // https://www.baeldung.com/servlet-json-response
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String result = am.doAnalyticsSearch();
        System.out.println(result);

        am.setAnalyticsData(result);

        am.getLogsData(result);
        String analytics = am.performAnalytics();

        request.setAttribute("responseTime", analytics.split(";")[0]);
        request.setAttribute("apiTime", analytics.split(";")[1]);
        request.setAttribute("phoneBrand", analytics.split(";")[2]);
        request.setAttribute("phoneZone", analytics.split(";")[3]);
        request.setAttribute("text", analytics.split(";")[4]);

        request.setAttribute("logs", am.logs);

        // Transfer control over the the correct "view"
        RequestDispatcher view = request.getRequestDispatcher("index.jsp");
        try {
            view.forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        }

//        out.print(result);
//        out.flush();
    }

    private String determineView(HttpServletRequest request){
        // determine what type of device our user is
        String ua = request.getHeader("User-Agent");

        // prepare the appropriate DOCTYPE for the view pages
        if (ua != null && ((ua.indexOf("Android") != -1) || (ua.indexOf("iPhone") != -1))) {
            /*
             * This is the latest XHTML Mobile doctype. To see the difference it
             * makes, comment it out so that a default desktop doctype is used
             * and view on an Android or iPhone.
             */
            return "<!DOCTYPE html PUBLIC \"-//WAPFORUM//DTD XHTML Mobile 1.2//EN\" \"http://www.openmobilealliance.org/tech/DTD/xhtml-mobile12.dtd\">";
        } else {
            return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
        }
    }
}