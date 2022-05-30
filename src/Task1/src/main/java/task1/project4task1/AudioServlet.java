//agarg2
//Arpit Garg
//agarg2@andrew.cmu.edu
package task1.project4task1;
/*
 * @author Arpit Garg
 *
 * The welcome-file in the deployment descriptor (web.xml) points
 * to this servlet.  So it is also the starting place for the web
 * application.
 *
 * The servlet is acting as the controller.
 * It receives POST requests from Android App
 * one for request to convert audio
 * another for request to send metrics
 * It also receives GET requests from web application
 * The response is in JSON format
 */

import java.io.*;
import java.util.Base64;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/*
 * The following WebServlet annotation gives instructions to the web container.
 * It states that when the user browses to the URL path /audio
 * then the servlet with the name AudioServlet should be used.
 */
@WebServlet(name = "audioToText", value = "/audio")
public class AudioServlet extends HttpServlet {
    private AudioModel am; // "business model" for the servlet
    private AudioView av; // "view" for the servlet

    // Initiating model
    public void init() {
        am = new AudioModel();
        av = new AudioView();
    }

    // This servlet will reply to HTTP POST requests via this doPost method
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String result = "";
        String log = request.getMethod()+"  "+request.getRequestURL();

        // https://stackoverflow.com/questions/8100634/get-the-post-request-body-from-httpservletrequest
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        System.out.println("Entered web service." + "\n" + requestBody);

        // Convert it to JsonObject
        JsonObject requestJson = new Gson().fromJson(requestBody, JsonObject.class);
        System.out.println("Json: \n" + requestJson);

        // Check if the request is to convert audio to text
        if(requestJson.get("action").toString().equals("\"convertAudio\"")) {

            // Get the file name
            String requestFileName = requestJson.get("audioFileName").toString();
            log += ", Convert to audio.";

            System.out.println("File name: " + requestFileName.substring(1, requestFileName.length() - 1));

            // Check format using extension
            if (!am.checkAudioFormat(requestFileName.substring(1, requestFileName.length() - 1))) {
                result = "{ \"Text\" : " + "\"Please select a valid .opus WhatsApp audio format\"";
                log += ", Response: " + "Internal server error HTTP/500 " +
                        "Please select a valid .opus WhatsApp audio format";
                result += ",\"ApiTime\" : " + "\"0\"" + " }";

            } else {
                // get the file to be converted
                String requestFile = requestJson.get("audioFile").toString();
                System.out.println("Body " + requestFile.substring(1, requestFile.length() - 1));

                // https://mkyong.com/java/how-do-convert-byte-array-to-string-in-java/
                byte[] byteAudio = null;
                byteAudio = Base64.getDecoder().decode(requestFile.substring(1, requestFile.length() - 1));

                /*
                 * Check if the byteAudio is present.
                 */
                if (byteAudio != null) {
                    // use model to add the values
                    long start = System.currentTimeMillis();

                    // get result in text format
                    result = am.getText(byteAudio);
                    String status = result.split(";")[0];
                    log += ", Response: " + status;

                    long apiTime = System.currentTimeMillis() - start;

//                    // Convert it to JsonObject
//                    JsonObject responseJson = new Gson().fromJson(result.split(";")[1].trim(), JsonObject.class);
//
//                    String text = responseJson.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray("alternatives").get(0).getAsJsonObject().get("transcript").toString();
//                    result = "{ \"Text\" : " + text;
//                    result += ",\"ApiTime\" : \"" + apiTime + "\" }";

                    result = av.toJSON(result, apiTime);

                    System.out.println(result);
                }
            }
        }

        // check if the request is to send metrics
        else if(requestJson.get("action").toString().equals("\"sendMetrics\"")) {
            log += ", Send metrics.";

            System.out.println("Entered metrics function.");

            // extract fields from json

            String responseTime = requestJson.get("responseTime").toString();
            responseTime = responseTime.substring(1, responseTime.length() - 1);

            String apiTime = requestJson.get("apiTime").toString();
            apiTime = apiTime.substring(1, apiTime.length() - 1);

            String text = requestJson.get("text").toString();
            text = text.substring(1, text.length() - 1);

            String phoneModel = requestJson.get("phoneModel").toString();
            phoneModel = phoneModel.substring(1, phoneModel.length() - 1);

            String phoneBrand = requestJson.get("phoneBrand").toString();
            phoneBrand = phoneBrand.substring(1, phoneBrand.length() - 1);

            String phoneZone = requestJson.get("phoneZone").toString();
            phoneZone = phoneZone.substring(1, phoneZone.length() - 1);

            String phoneDate = requestJson.get("phoneDate").toString();
            phoneDate = phoneDate.substring(1, phoneDate.length() - 1);

            //Add to DB
            result = am.addToDB(responseTime + ":" + apiTime + ":" +
                    text + ":" + phoneModel + ":" + phoneBrand + ":" +
                    phoneZone + ":" + phoneDate);

            log += ", Response: " + result;
            System.out.println(result);
        }

        am.addLogsToDB(log);

        JsonObject responseJson = new Gson().fromJson(result, JsonObject.class);
        // https://www.baeldung.com/servlet-json-response
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(responseJson);
        out.flush();
    }

    // This servlet will reply to HTTP GET requests via this doGet method
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("Inside GET METHOD.");
        String result = "";

        result = am.getAnalyticsData();

        // https://www.baeldung.com/servlet-json-response
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(result);
        out.flush();
    }
}