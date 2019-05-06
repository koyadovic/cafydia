package org.cafydia.android.bedcasearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.cafydia.android.core.Food;
import org.cafydia.android.util.C;
import org.cafydia.android.util.MyFoodArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by usuario on 30/03/14.
 */
public class BedcaSearch {
    private Context mContext;

    public static final String SHARED_PREFERENCES_INITIALIZED = "bedca_local_database_initialized";

    private boolean mInitialized = false;

    private BedcaLocalDatabase mBedcaLocalDatabase;

    private void logMsg(String s){
        //Log.d("BedcaSearch", s);
    }

    public BedcaSearch(Context c){
        mContext = c;
        mBedcaLocalDatabase = new BedcaLocalDatabase(c);

        logMsg("instanciando la busqueda");

        SharedPreferences sp = mContext.getSharedPreferences(SHARED_PREFERENCES_INITIALIZED, Context.MODE_PRIVATE);
        mInitialized = sp.getBoolean("initialized", false);

        if(!mInitialized) {
            logMsg("db local de bedca no inicializada");
            new InitializeBedcaLocalDatabase().execute();
        }
    }

    // busca en segundo plano alimentos en la red.
    private class InitializeBedcaLocalDatabase extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<BedcaFood> foods;

        protected Boolean doInBackground(Void... params) {

            logMsg("Inicializando db local de bedca");

            foods = getAllBedcaDatabase();

            boolean result = foods.size() > 0;

            if(result){
                mBedcaLocalDatabase.emptyBedcaFoodTable();

                for(BedcaFood f : foods) {
                    mBedcaLocalDatabase.updateOrInsertBedcaFood(f);
                }
            }

            return result;

        }

        protected void onPostExecute(Boolean result) {
            if(result){
                mInitialized = true;
                logMsg("Marcamos como inicializada");
                SharedPreferences sp = mContext.getSharedPreferences(SHARED_PREFERENCES_INITIALIZED, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();

                editor.putBoolean("initialized", mInitialized);
                editor.apply();
            }
        }
    }


    private boolean isBedcaOnline() {
        int timeout = 3000;
        String url = "http://www.bedca.net/";
        logMsg("testeando si bedca está online");
        try{
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            logMsg("está online");
            return true;
        } catch (Exception e) {
            logMsg("no está online");
            return false;
        }
    }

    // do the search
    public MyFoodArrayList workAndGetResults(String query, Boolean searchLocallyOnError){
        MyFoodArrayList results = new MyFoodArrayList();

        logMsg("Iniciando una búsqueda bedca");
        logMsg("query: " + query);
        logMsg("Buscar en local si existe un error: " + searchLocallyOnError.toString());
        //
        // First of all it's needed to check if bedca is online
        //
        if(isBedcaOnline()) {

            DefaultHttpClient httpClient;
            HttpPost httpPost;
            HttpResponse response;

            String xmlQuery = getXMLQuery(query);

            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost("http://www.bedca.net/bdpub/procquery.php");
            httpPost.addHeader("Content-Type", "application/xml");

            try {
                StringEntity entity = new StringEntity(xmlQuery, "UTF-8");
                entity.setContentType("application/xml");
                httpPost.setEntity(entity);
                HttpHost targetHost = new HttpHost("www.bedca.net", 80, "http");
                response = httpClient.execute(targetHost, httpPost);
                HttpEntity entityResponse = response.getEntity();
                if (entityResponse != null) {

                    // the response
                    String rawXmlResponse = EntityUtils.toString(entityResponse, "UTF-8");

                    DocumentBuilderFactory dbf =
                            DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();

                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(rawXmlResponse));

                    Document doc = db.parse(is);
                    Element root = doc.getDocumentElement();
                    NodeList nodes = root.getElementsByTagName("food");

                    if (nodes != null) {
                        for (int i = 0; i < nodes.getLength(); i++) {

                            Node foodNode = nodes.item(i);
                            NodeList foodParams = foodNode.getChildNodes();

                            int bedcaId = 0;
                            String nameEn = "";
                            String nameEs = "";
                            String name = "";
                            Float carbohydrates = 0.0f;

                            for (int j = 0; j < foodParams.getLength(); j++) {
                                Node foodParam = foodParams.item(j);

                                if (foodParam != null) {
                                    String paramName = foodParam.getNodeName();

                                    if (paramName.equals("f_ori_name")) {
                                        nameEs = foodParam.getFirstChild().getNodeValue();
                                    }
                                    else if (paramName.equals("f_eng_name")) {
                                        nameEn = foodParam.getFirstChild().getNodeValue();
                                    }
                                    else if (paramName.equals("best_location") && foodParam.getFirstChild() != null) {
                                        String carb = foodParam.getFirstChild().getNodeValue();
                                        carbohydrates = Float.parseFloat(carb);
                                    }
                                    else if (paramName.equals("f_id")) {
                                        bedcaId = Integer.parseInt(foodParam.getFirstChild().getNodeValue());
                                    }
                                }
                            }

                            if(Locale.getDefault().getISO3Language().equals("spa")){
                                name = nameEs;
                            } else {
                                name = nameEn;
                            }

                            if (!name.equals("")) {

                                BedcaFood bFood = new BedcaFood(bedcaId, nameEn, nameEs, carbohydrates);
                                mBedcaLocalDatabase.updateOrInsertBedcaFood(bFood);

                                Food food = new Food(0, name, C.FOOD_TYPE_SIMPLE, C.FOOD_FAVORITE_NO, carbohydrates);
                                results.addOrUpdateFood(food);
                            }
                        }

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                // aquí petó la conexión a Bedca, a pesar de que estaba online
                if(searchLocallyOnError)
                    results = workAndGetResultsLocally(query);
            }
        } else {
            // bedca no está online
            if(searchLocallyOnError)
                results = workAndGetResultsLocally(query);
        }

        return results;
    }


    public ArrayList<BedcaFood> getAllBedcaDatabase(){
        ArrayList<BedcaFood> results = new ArrayList<>();

        //
        // First of all it's needed to check if bedca is online
        //
        if(isBedcaOnline()) {

            DefaultHttpClient httpClient;
            HttpPost httpPost;
            HttpResponse response;

            String xmlQuery = getXMLQuery("");

            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost("http://www.bedca.net/bdpub/procquery.php");
            httpPost.addHeader("Content-Type", "application/xml");

            try {
                StringEntity entity = new StringEntity(xmlQuery, "UTF-8");
                entity.setContentType("application/xml");
                httpPost.setEntity(entity);
                HttpHost targetHost = new HttpHost("www.bedca.net", 80, "http");
                response = httpClient.execute(targetHost, httpPost);
                HttpEntity entityResponse = response.getEntity();
                if (entityResponse != null) {

                    // the response
                    String rawXmlResponse = EntityUtils.toString(entityResponse, "UTF-8");

                    DocumentBuilderFactory dbf =
                            DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();

                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(rawXmlResponse));

                    Document doc = db.parse(is);
                    Element root = doc.getDocumentElement();
                    NodeList nodes = root.getElementsByTagName("food");

                    if (nodes != null) {
                        for (int i = 0; i < nodes.getLength(); i++) {

                            Node foodNode = nodes.item(i);
                            NodeList foodParams = foodNode.getChildNodes();

                            int bedcaId = 0;
                            String nameEn = "";
                            String nameEs = "";
                            String name = "";
                            Float carbohydrates = 0.0f;

                            for (int j = 0; j < foodParams.getLength(); j++) {
                                Node foodParam = foodParams.item(j);

                                if (foodParam != null) {
                                    String paramName = foodParam.getNodeName();

                                    if (paramName.equals("f_ori_name")) {
                                        nameEs = foodParam.getFirstChild().getNodeValue();
                                    }
                                    else if (paramName.equals("f_eng_name")) {
                                        nameEn = foodParam.getFirstChild().getNodeValue();
                                    }
                                    else if (paramName.equals("best_location") && foodParam.getFirstChild() != null) {
                                        String carb = foodParam.getFirstChild().getNodeValue();
                                        carbohydrates = Float.parseFloat(carb);
                                    }
                                    else if (paramName.equals("f_id")) {
                                        bedcaId = Integer.parseInt(foodParam.getFirstChild().getNodeValue());
                                    }
                                }
                            }

                            if(Locale.getDefault().getISO3Language().equals("spa")){
                                name = nameEs;
                            } else {
                                name = nameEn;
                            }

                            if (!name.equals("")) {

                                BedcaFood bFood = new BedcaFood(bedcaId, nameEn, nameEs, carbohydrates);
                                results.add(bFood);

                            }
                        }

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return results;
    }


    private String getXMLQuery(String query){

        String xmlQuery = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<foodquery><type level=\"1a\"/>" +
                "<selection>" +
                "<atribute name=\"f_id\"/><atribute name=\"f_ori_name\"/><atribute name=\"f_eng_name\"/><atribute name=\"best_location\"/>" +
                "</selection>" +
                "<condition><cond1><atribute1 name=\"c_id\"/></cond1><relation type=\"EQUAL\"/><cond3>53</cond3></condition>" +
                "<condition><cond1><atribute1 name=\"f_origen\"/></cond1><relation type=\"EQUAL\"/><cond3>BEDCA</cond3></condition>";


        if (Locale.getDefault().getISO3Language().equals("spa")) {
            xmlQuery += "<condition><cond1><atribute1 name=\"f_ori_name\"/></cond1><relation type=\"LIKE\"/><cond3>" +
                    query +
                    "</cond3></condition>" +
                    "<order ordtype=\"ASC\"><atribute3 name=\"f_ori_name\"/></order></foodquery>";

        } else {
            xmlQuery += "<condition><cond1><atribute1 name=\"f_eng_name\"/></cond1><relation type=\"LIKE\"/><cond3>" +
                    query +
                    "</cond3></condition>" +
                    "<order ordtype=\"ASC\"><atribute3 name=\"f_eng_name\"/></order></foodquery>";
        }

        return xmlQuery;
    }

    private MyFoodArrayList workAndGetResultsLocally(String query){
        return mBedcaLocalDatabase.searchFood(query, Locale.getDefault().getISO3Language());
    }
}
