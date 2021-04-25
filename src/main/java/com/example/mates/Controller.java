package com.example.mates;

import com.google.gson.Gson;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import net.minidev.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;

@RestController
public class Controller {

    @GetMapping("getNews")
    public String response (@RequestParam(value = "groups", defaultValue = "site:   11234423423   1241345   435345   vk:   27770435   144626470   120099959   96626248")  String arr){
        return newsMade(arr);
    }

    private String newsMade (String groups){
        JSONObject jsonObject = new JSONObject();

        // Подключаемся к vk и создаём actor при помощи accessToken
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);
        UserActor actor = new UserActor(7748712, "e66dd95bdeeba652fe74e72af86e7c1429ddac16c98af47d9eee88cafd9aebbb332ebfd5e3f8647011baf");

        // Массив Json для считывания результата работы функции
        JSONArray jsonArray = new JSONArray();
        ArrayList<NewsStencil> stencilArrayList = new ArrayList<>();
        ArrayList<NewsStencil> list1 = new ArrayList<>();
        // Создаём обьект класса - парсера
        Job job = new Job();

        // Попытка выполнить функцию
        try {
            // Подготовка к парсингу
            String[] news = groups.split(" {3}");
            String arg = "";
            try {
                for (int i = 0; i < news.length; i++) {
                    switch (arg){
                        case "vk:":
                            stencilArrayList = job.newsJsonMakerParser(vk, actor, Integer.parseInt(news[i]) * (-1), 10);

                            for (int j = 0; j < stencilArrayList.size(); j++) {
                                list1.add((NewsStencil) stencilArrayList.get(j));
                            }

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "site:":
                            break;
                        case "telegram":
                            System.out.println("s");
                            break;
                    }

                    if (news[i].equals("vk:")){
                        arg = news[i];
                    }
                }
            } catch (ApiException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Перестановка в порядке возрастания
        Collections.sort(list1);

        Gson gson = new Gson();
        String json = gson.toJson(list1);

/*
        try {
            JSONArray jsObject = new JSONArray(json);
            return jsObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        return json;
    }
}
