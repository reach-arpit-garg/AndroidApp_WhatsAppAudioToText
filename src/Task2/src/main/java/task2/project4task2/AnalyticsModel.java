//agarg2
//Arpit Garg
//agarg2@andrew.cmu.edu
package task2.project4task2;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;

public class AnalyticsModel {
    String[] responseTime;
    String[] apiTime;
    String[] text;
    String[] phoneBrand;
    String[] phoneZone;
    String[] logs;

    protected String doAnalyticsSearch(){
        String response = "";

        // Create a URL for the page to get JSON response from
        String detailsURL = "https://infinite-everglades-25018.herokuapp.com//audio";

        // Fetch the response
        response = fetch(detailsURL);

        return response;
    }

    private String fetch(String urlString) {
        String response = "";
        try {
            URL url = new URL(urlString);
            /*
             * Create an HttpURLConnection.  This is useful for setting headers
             * and for getting the path of the resource that is returned (which
             * may be different than the URL above if redirected).
             * HttpsURLConnection (with an "s") can be used if required by the site.
             */
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String str;
            // Read each line of "in" until done, adding each to "response"
            while ((str = in.readLine()) != null) {
                // str is one line of text readLine() strips newline characters
                response += str;
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            // Do something reasonable.  This is left for students to do.
        }
        return response;
    }

    protected void setAnalyticsData(String response){
        System.out.println("\nSetting analytics data.");

        // Convert it to JsonObject
        JsonObject responseJson = new Gson().fromJson(response, JsonObject.class);

        //Split it to get an array of results
        JsonArray msgArr = responseJson.getAsJsonArray("message");

        responseTime = new String[msgArr.size()];
        apiTime = new String[msgArr.size()];
        text = new String[msgArr.size()];
        phoneBrand = new String[msgArr.size()];
        phoneZone = new String[msgArr.size()];

        for(int i=0; i<msgArr.size(); i++){
            responseTime[i] = msgArr.get(i).getAsJsonObject().get("responseTime").toString();
            responseTime[i] = responseTime[i].substring(1, responseTime[i].length()-1);

            apiTime[i] = msgArr.get(i).getAsJsonObject().get("apiTime").toString();
            apiTime[i] = apiTime[i].substring(1, apiTime[i].length()-1);

            text[i] = msgArr.get(i).getAsJsonObject().get("text").toString();
            text[i] = text[i].substring(1, text[i].length()-1);

            phoneBrand[i] = msgArr.get(i).getAsJsonObject().get("phoneBrand").toString();
            phoneBrand[i] = phoneBrand[i].substring(1, phoneBrand[i].length()-1);

            phoneZone[i] = msgArr.get(i).getAsJsonObject().get("phoneZone").toString();
            phoneZone[i] = phoneZone[i].substring(1, phoneZone[i].length()-1);

            System.out.println(responseTime[i] + "\t" + apiTime[i] + "\t" + text[i] + "\t" + phoneBrand[i] + "\t" + phoneZone[i]);
        }
    }

    protected void getLogsData(String response){
       System.out.println("\nGetting logs data.");

        // Convert it to JsonObject
        JsonObject responseJson = new Gson().fromJson(response, JsonObject.class);

        //Split it to get an array of results
        JsonArray msgArr = responseJson.getAsJsonArray("logs");

        logs = new String[msgArr.size()];

        for(int i=0; i<msgArr.size(); i++){
            String time = msgArr.get(i).getAsJsonObject().get("time").toString();
            time = time.substring(1, time.length()-1);

            String log = msgArr.get(i).getAsJsonObject().get("log").toString();
            log = log.substring(1, log.length()-1);

            logs[i] = time + "\n" + log + "\n";
        }


    }

    protected String performAnalytics(){
        System.out.println("\nPerforming analytics");

        String result = "";


        long time = 0l;
        for(int i=0; i<responseTime.length; i++)
            time += Long.parseLong(responseTime[i]);
        result += "Average response time = " + (time/responseTime.length) + " ms;";

        time = 0l;
        for(int i=0; i<apiTime.length; i++)
            time += Long.parseLong(apiTime[i]);
        result += "Average third party api time = " + (time/apiTime.length) + " ms;";


        String maxBrand = "";
        int maxHits = 0;
        HashMap<String, Integer> brandHashMap = new HashMap<>();
        for(int i=0; i<phoneBrand.length; i++) {
            int val = 0;

            if(brandHashMap.containsKey(phoneBrand[i]))
                val = brandHashMap.get(phoneBrand[i]);

            val++;
            brandHashMap.put(phoneBrand[i], val);

            if(val > maxHits){
                maxHits = val;
                maxBrand = phoneBrand[i];
            }
        }
        result += "Highest phone brand users = " + maxBrand + ";";


        String maxZone = "";
        maxHits = 0;
        HashMap<String, Integer> zoneHashMap = new HashMap<>();
        for(int i=0; i<phoneZone.length; i++) {
            int val = 0;

            if(zoneHashMap.containsKey(phoneZone[i]))
                val = zoneHashMap.get(phoneZone[i]);

            val++;
            zoneHashMap.put(phoneZone[i], val);

            if(val > maxHits){
                maxHits = val;
                maxZone = phoneZone[i];
            }
        }
        result += "Highest phone zone users = " + maxZone + ";";

        int wordLength = 0;
        int requestNum = 0;
        for(int i=0; i<text.length; i++) {
            if(Integer.parseInt(apiTime[i]) != 0){
                wordLength += text[i].trim().split(" ").length;
                requestNum++;
            }
        }
        result += "Average number of words in an audio = " + (wordLength/requestNum) + ";";

        return result;
    }
}
