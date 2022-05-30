//agarg2
//Arpit Garg
//agarg2@andrew.cmu.edu
package edu.cmu.akshar;

import android.net.Uri;
import android.app.Activity;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class AksharModel {
    AksharActivity ip = null;   // for callback
    Uri searchTerm = null;       // search webservice for this word
    String response = null;          // returned from webservice

    public void search(Uri searchTerm, Activity activity, AksharActivity ip) {
        this.ip = ip;
        this.searchTerm = searchTerm;
        new BackgroundTask(activity).execute();
    }

    private class BackgroundTask {

        private Activity activity; // The UI thread

        public BackgroundTask(Activity activity) {
            this.activity = activity;
        }

        private void startBackground() {
            new Thread(new Runnable() {
                public void run() {

                    doInBackground();

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            onPostExecute();
                        }
                    });
                }
            }).start();
        }

        private void execute(){
            // There could be more setup here, which is why
            //    startBackground is not called directly
            startBackground();
        }

        // doInBackground( ) implements whatever you need to do on
        //    the background thread.
        // Implement this method to suit your needs
        private void doInBackground() {
            response = search(searchTerm);
        }

        // onPostExecute( ) will run on the UI thread after the background
        //    thread completes.
        // Implement this method to suit your needs
        public void onPostExecute() {
            ip.textReady(response);
        }

        /*
         * Search webservice for the conversion of audio
         */
        private String search(Uri searchTerm) {
            if(searchTerm == null)
                return null;

            String audioURL = null;
            String fileName = searchTerm.getPath().split("/")[searchTerm.getPath().split("/").length - 1];

            System.out.println("\n\nInside search:" + fileName + "\n\n");
//            File file = new File("/data/media/0/Android/" + searchTerm.getPath().substring(searchTerm.getPath().lastIndexOf("/")));

            File file = new File("/storage/emulated/0/Download/" + fileName);

            Path path = file.toPath();
//            System.out.println("\n\nPath 1" + path);
            byte[] bytearray = null;
            try {
                bytearray = Files.readAllBytes(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bytearray == null || bytearray.length == 0) {
                return null; // no pictures found
            } else {

                audioURL = "https://salty-bayou-39676.herokuapp.com/audio";
//                audioURL = "http://10.0.2.2:8080/Project4Task1-1.0-SNAPSHOT/audio";
            }
            // At this point, we have the URL of the audio that resulted from the search.
            try {
                URL u = new URL(audioURL);
                String response = convertAudio(u, bytearray, fileName);

                return  response;
            } catch (Exception e) {
                e.printStackTrace();
                return null; // so compiler does not complain
            }

        }

        /*
         * Given a URL referring to the audio, return a text of that audio
         */
        @RequiresApi(api = Build.VERSION_CODES.P)
        private String convertAudio(final URL url, byte[] bytearray, String fileName) {
            String response = "";
            Long start = System.currentTimeMillis();

            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                String body = "{ " + "\"action\" : \"" + "convertAudio" +
                        "\",\n\"audioFile\" : \"" + Base64.getEncoder().encodeToString(bytearray) +
                        "\",\n\"audioFileName\" : \"" + fileName + "\" }";

                connection.setRequestProperty("Content-Type", "application/ecmascript");
                connection.setRequestMethod("POST");

                connection.setDoOutput(true);
                try(OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String str;
                // Read each line of "in" until done, adding each to "response"
                while ((str = in.readLine()) != null) {
                    // str is one line of text readLine() strips newline characters
                    response += str;
                }
                in.close();
            } catch (
                    IOException e) {
                System.out.println(e.getMessage());
                response = "{ \"Text\" : " + "\"Sorry, unable to connect to the server. Please try again later.\"";
                response += ",\"ApiTime\" : " + "\"0\"" + " }";
                // Do something reasonable.  This is left for students to do.
            }
            System.out.println(response);

            JsonObject responseJson = new Gson().fromJson(response, JsonObject.class);
            response = responseJson.get("Text") + ":"
                    + responseJson.get("ApiTime").toString().substring(1, responseJson.get("ApiTime").toString().length()-1);

            System.out.println("\nResponse: " + response);
            Long responseTime = System.currentTimeMillis() - start;
            if(response.length() > 0 && !response.split(":")[1].equalsIgnoreCase("0"))
                sendMetrics(url, responseTime, response.split(":")[1], response.split(":")[0]);

            return response.split(":")[0];
        }

        /*
         * Given a URL referring to the metrics
         */
        @RequiresApi(api = Build.VERSION_CODES.P)
        private void sendMetrics(final URL url, long responseTime, String apiTime, String text){
            String response = "";

            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                String body = "{ " + "\"action\" : \"" + "sendMetrics" +
                        "\",\n\"responseTime\" : \"" + responseTime +
                        "\",\n\"apiTime\" : \"" + apiTime +
                        "\",\n\"text\" : " + text +
                        ",\n\"phoneModel\" : \"" + Build.MODEL +
                        "\",\n\"phoneBrand\" : \"" + Build.BRAND +
                        "\",\n\"phoneZone\" : \"" + ZonedDateTime.now(ZoneId.systemDefault()).getZone() +
                        "\",\n\"phoneDate\" : \"" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + "\" }";

                connection.setRequestProperty("Content-Type", "application/ecmascript");
                connection.setRequestMethod("POST");

                connection.setDoOutput(true);
                try(OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String str;
                // Read each line of "in" until done, adding each to "response"
                while ((str = in.readLine()) != null) {
                    // str is one line of text readLine() strips newline characters
                    response += str;
                }
                in.close();
            } catch (
                    IOException e) {
                System.out.println(e.getMessage());
                // Do something reasonable.  This is left for students to do.
            }
            System.out.println(response);
        }
    }
}
