package funpun.org;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Network {


    String FUNPUN_API_URL = "http://kvmnl01-15724.fornex.org/%s?%s";
//    String FUNPUN_API_URL = "http://192.168.0.103:8090/%s?%s";

    private synchronized String performRequest(String uri, Map<String, String> params) {

        try {

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
                sb.append("&");
            }

            URL url = new URL(String.format(FUNPUN_API_URL, uri, sb.toString()));
            final HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");


//            byte[] postData = sb.toString().getBytes();
//            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//            wr.write(postData);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder(1024);
            String tmp;
            while ((tmp = reader.readLine()) != null)
                response.append(tmp).append("\n");
            reader.close();


            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("NetworkImpl issue not 200");
            }

            return response.toString();

        } catch (Exception e) {
            throw new RuntimeException("NetworkImpl issue:" + e.getMessage());
        }
    }


    protected JSONArray getJSONArray(String uri, Map<String, String> params) {
        try {

            JSONArray data = new JSONArray(performRequest(uri, params));

            return data;
        } catch (JSONException e) {
            throw new RuntimeException("JSONArray issue");
        }
    }


    List<FunImage> getList(int page) {

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        JSONArray jsonArray = getJSONArray("fun-images", params);

        List<FunImage> ret = new ArrayList<>();

        if (jsonArray != null) {

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject;
                try {
                    jsonObject = (JSONObject) jsonArray.get(i);

                    FunImage funImage = new FunImage();

                    try {
                        funImage.id = Integer.valueOf(jsonObject.getString("id"));
                        funImage.liked = Integer.valueOf(jsonObject.getString("liked"));
                    } catch (JSONException e) { // TODO delete
                        e.printStackTrace();
                    }

                    funImage.url = jsonObject.getString("path");

                    ret.add(funImage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }


    void like(Integer id) {

        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(id));

        performRequest("like", params);
    }

    void add(String url) {
        Map<String, String> params = new HashMap<>();
        params.put("url", url);

        performRequest("add-url", params);
    }

}
