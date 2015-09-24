package com.stoko.mappenger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class HttpClient {
    private String _url = null;
    private String _access_token = null;

    public HttpClient(String URL){
        _url = URL;
    }

    public HttpClient(String URL, String access_token){
        _url = URL;
        _access_token = access_token;
    }

    public String Get(String endPoint) {
        try {
            URL url = new URL(_url + endPoint);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            Object content = urlConnection.getContent();
            StringBuilder sb = new StringBuilder();
            while(in.available()>0) {
                sb.append((char)in.read());
            }
            return sb.toString();

        } catch(Exception ex){
            Object vv = ex;
        }
        return "";
    }

    public String sendGet(String endPoint) throws Exception {
        URL obj = new URL(_url+endPoint);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + _url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
        return response.toString();
    }

    public LoginResult sendPostLogin(String endPoint, String userName, String password) throws Exception {
        URL obj = new URL(_url + endPoint);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "grant_type=password&username="+userName+"&password="+password;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + _url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        if(endPoint == "Token" && responseCode == 400) {
            LoginResult lerror = new LoginResult();
            lerror.error = true;
            lerror.errorMessage = "Invalid login.";
            return lerror;
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
        Gson gson = new Gson();
        LoginResult lr = gson.fromJson(response.toString(), LoginResult.class);
        return lr;
    }

    public List<UserMessage> sendGetMessages(String endPoint, String lat, String lon) throws Exception {
        URL obj = new URL(_url + endPoint + "?latitude="+lat+"&longitude="+lon);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + _access_token);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + _url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ").create();

        Type listOfMessages = new TypeToken<List<UserMessage>>(){}.getType();
        List<UserMessage> messages = gson.fromJson(response.toString(), listOfMessages);

        return messages;
    }

    public UserMessage sendGetMessageAtPoint(String endPoint, String lat, String lon) throws Exception {
        URL obj = new URL(_url + endPoint + "?latitude="+lat+"&longitude="+lon);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + _access_token);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + _url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ").create();

        UserMessage message = gson.fromJson(response.toString(), UserMessage.class);

        return message;
    }

    public UserMessage sendGetMessageByPKRK(String endPoint, String PK, String RK) throws Exception {
        URL obj = new URL(_url + endPoint + "?PK="+PK+"&RK="+RK);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + _access_token);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + _url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ").create();

        UserMessage message = gson.fromJson(response.toString(), UserMessage.class);

        return message;
    }

    public List<FoundMessage> sendGetFoundMessages(String endPoint) throws Exception {
        URL obj = new URL(_url + endPoint);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + _access_token);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + _url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ").create();

        Type listOfMessages = new TypeToken<List<FoundMessage>>(){}.getType();
        List<FoundMessage> messages = gson.fromJson(response.toString(), listOfMessages);

        return messages;
    }

    public void sendPostFoundMessage(String endPoint, UserMessage message) throws Exception {
        URL obj = new URL(_url + endPoint);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Authorization", "Bearer " + _access_token);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ").create();;
        String data = gson.toJson(message);

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(data);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + _url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return;
    }

    public void sendPostThrowMessage(String endPoint, String lat, String lon, String message, String speed, String iconType) throws Exception {
        URL obj = new URL(_url + endPoint);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Authorization", "Bearer " + _access_token);

        String data = "Latitude="+lat+"&Longitude="+lon+"&MessageText="+message+"&Speed="+speed+"&IconType="+iconType;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(data);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + _url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return;
    }

    public SignUpResponse sendPostSignUp(String endPoint, String email, String password, String confirmPassword) throws Exception {
        URL obj = new URL(_url + endPoint);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String data = "Email="+email+"&Password="+password+"&ConfirmPassword="+confirmPassword;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(data);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + _url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Gson gson = new Gson();
        SignUpResponse sur = gson.fromJson(response.toString(), SignUpResponse.class);
        return sur;
    }
}