package com.example.mates;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.enums.WallFilter;
import com.vk.api.sdk.objects.groups.Fields;
import com.vk.api.sdk.objects.groups.responses.GetByIdLegacyResponse;
import com.vk.api.sdk.objects.wall.GetFilter;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Job {

    // Поля для создания класса
    public String id,
            ownerId,
            groupPhotoUrl,
            groupName,
            date,
            text;
    public ArrayList<String> mediaUrlClean = new ArrayList<String>();
    public ArrayList<String> videosClean = new ArrayList<String>();
    public String link = "NO";

    // Поле для записи новостей
    JSONArray array = new JSONArray();

    ArrayList<NewsStencil> newsJsonMakerParser(VkApiClient vk, UserActor actor, int ownerId, int count) throws ClientException, ApiException, JSONException {

        // Строка, которую возвращаем
        JSONObject json;

        //
        NewsStencil stencil;
        ArrayList<NewsStencil> stencilArrayList = new ArrayList<>();
        //

        // Получили новости в формате GetResponse
        GetResponse getResponse = vk.wall().get(actor) // Пользуясь введёнными vk и actor
                .ownerId(ownerId) // По ownerId - введённому номеру сообщества
                .count(count) // count - количество новостей
                .filter(GetFilter.ALL) // Получаем все новости
                .execute();

        // Преобразовываем в строку
        String str = getResponse.toString();

        // Получаем обьект JSON для парсинга
        JSONObject jsonObject = new JSONObject(str);

        // Требуется только то, что лежит в items
        // itemsArr - массив содержащий новости
        JSONArray itemsArr = (JSONArray) jsonObject.get("items");

        // Получение имени и фото сообщества
        // Считываем информацию о сообществе вместе со вторым именем сообощества
        int groupId = ownerId * (-1);
        List<GetByIdLegacyResponse> responseTwo = vk.groups().getByIdLegacy(actor) // Пользуясь введёнными vk и actor
                .groupIds(String.valueOf(groupId)) // По ownerId - введённому номеру сообщества
                .fields(Fields.SECONDARY_SECTION)
                .execute();
        //System.out.println(responseTwo);

        // Преобразовываем в строку полученную информацию
        String storage = new String(String.valueOf(responseTwo));
        storage = replaceToJsonType(storage);

        // Получаем обьект JSON для парсинга
        JSONObject jsonObjectStorage = new JSONObject(storage);

        // Считываем название и адресс картинки сообщества
        String name = String.valueOf(jsonObjectStorage.get("name")), photo200 = String.valueOf(jsonObjectStorage.get("photo_200"));

        // Считываем короткое имя и идентификатор сообщества, подготавливаем ссылку
        link = "https://vk.com/" + jsonObjectStorage.get("screen_name") + "?w=wall" + "-" + String.valueOf(groupId);

        // Обходим все новости
        for (int i = 0; i < itemsArr.length(); i++) {

            // Получаем конкретную новость в jsonObject
            JSONObject jsonObjectWood = itemsArr.getJSONObject(i);

            // Отделяем прямые новости от переправленных
            if (jsonObjectWood.has("copyright")) {
                json = new JSONObject(helperNewsParserVarTwo(jsonObjectWood));;
                array.put(json);
            }
            else if(jsonObjectWood.has("copy_history")){
                JSONArray jsonArray = (JSONArray) jsonObjectWood.get("copy_history");
                JSONObject aid = (JSONObject) jsonArray.get(0);
                json = new JSONObject(helperNewsParserVarOne(vk, actor, aid, name, photo200, link));
                array.put(json);
            }
            else{
                //json = new JSONObject(helperNewsParserVarOne(vk, actor, jsonObjectWood, name, photo200, link));
                //
                stencil = helperNewsParserVarOne(vk, actor, jsonObjectWood, name, photo200, link);
                stencilArrayList.add(stencil);
                //array.put(json);
            }
        }
        return /*array*/ stencilArrayList;
    }

    // Функция для считывания прямых новостей
    NewsStencil helperNewsParserVarOne(VkApiClient vk, UserActor actor, JSONObject jsonObjectWood, String name, String photo200, String linking) throws JSONException, ClientException, ApiException {

        // Строка, которую возвращаем
        String json = new String();

        // Записываем первые значения
        id = String.valueOf(jsonObjectWood.get("id"));
        ownerId = String.valueOf(jsonObjectWood.get("owner_id"));
        groupPhotoUrl = photo200;
        groupName = name;
        date = String.valueOf(jsonObjectWood.get("date"));
        text = String.valueOf(jsonObjectWood.get("text"));
        linking = link + "_" + id;

        // Получить список адрессов изображений новости
        // Массив элементов ключа attachments
        JSONArray jsonArray = (JSONArray) jsonObjectWood.get("attachments");

        // Обходим все приложенные данные о медиа
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObjectAttachments = jsonArray.getJSONObject(i);
            if (jsonObjectAttachments.has("photo")){
                mediaUrlClean.add(getPhotoInfo(jsonObjectAttachments));
            }
            if (jsonObjectAttachments.has("video")){
                videosClean.add(getVideoInfo(vk, actor, jsonObjectAttachments));
            }
        }

        // Создаём новый обьект новости и конвертируем в json
        NewsStencil newsStencil = new NewsStencil(id, ownerId, groupPhotoUrl, groupName, date, text, mediaUrlClean, videosClean, linking);

        /*Gson gson = new Gson();
        json = gson.toJson(newsStencil);
*/
        // Очищаем использованные массивы и строки
        mediaUrlClean = new ArrayList<>();
        videosClean = new ArrayList<>();

        return newsStencil;
    }

    // Функция для считывания переправленных новостей
    String helperNewsParserVarTwo(JSONObject jsonObjectWood) throws JSONException {

        // Строка, которую возвращаем
        String json = new String();

        return "";
    }

    // Функция для получения адрессов конкретного видео
    String getVideoInfo(VkApiClient vk, UserActor actor, JSONObject jsonObjectAttachments) throws JSONException, ClientException, ApiException {

        // Получаем список параметров видео
        JSONObject jsonObjectVideo = (JSONObject) jsonObjectAttachments.get("video");

        // Возврат информации о видеозаписи в формате GetResponse
        com.vk.api.sdk.objects.video.responses.GetResponse response = vk.videos().get(actor)
                .videos(jsonObjectVideo.get("owner_id") + "_" + jsonObjectVideo.get("id") + "_" + jsonObjectVideo.get("access_key"))
                .count(1)
                .execute();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Преобразовываем в строку
        String str = response.toString();

        // Получаем обьект JSON для парсинга
        JSONObject jsonObjectVideoInfo = new JSONObject(str);

        // Требуется только то, что лежит в items в ключе player
        // itemsArr - массив содержащий информацию о видео, в player находиться ссылка на плеер
        JSONArray itemsArr = (JSONArray) jsonObjectVideoInfo.get("items");
        JSONObject jsonObjectPlayer = itemsArr.getJSONObject(0);


        // Передаём в функцию для преобразования в вид IfFrame
        return videoIfFrame(String.valueOf(jsonObjectPlayer.get("player")), String.valueOf(jsonObjectVideo.get("owner_id")), String.valueOf(jsonObjectVideo.get("id")));
    }
    // Функция для вычленения хеша и сборки кода вставки
    String videoIfFrame(String hrefPlayer, String wallId, String videoId){

        // Переменная для обхода
        char str = 0;

        // Первое вхождение хеша в строку
        int indexStart = searchStringInString(hrefPlayer, "&hash=") + "&hash=".length() - 1;

        // Вычисления окончания хеша
        int i = indexStart;
        while (str != '&'){
            str = hrefPlayer.charAt(i);
            i++;
        }

        // Вычленение хеша в строку
        String strongHash = hrefPlayer.substring(indexStart, i);
        return "<iframe src=\"https://vk.com/video_ext.php?oid=" +  wallId + "&id" + videoId + "&hash=" + strongHash + "&hd=2\"";
    }

    // Функция для получения адресса конкретной картинки
    String getPhotoInfo(JSONObject jsonObjectAttachments) throws JSONException {

        // Обьект ключа photo и массив элементов ключа sizes
        JSONObject jsonObjectPhoto = (JSONObject) jsonObjectAttachments.get("photo");
        JSONArray jsonObjectSizes = (JSONArray) jsonObjectPhoto.get("sizes");

        // Получаем адресс картинки с лучшим качеством
        JSONObject jsonObjectLength = (JSONObject) jsonObjectSizes.get(jsonObjectSizes.length()-1);

        return ""+jsonObjectLength.get("url")+"";
    }

    // Функция для придания строке вида JSON
    String replaceToJsonType(String storage){

        // Далее используем служебные переменные для придания строке разметки JSONObject
        final char dm = '[', dn = ']';
        String lm = String.valueOf(dm), ln = String.valueOf(dn);
        storage = storage.replace(lm, "").replace(ln,""); // Убирает [ и ]
        return storage;
    }

    // Функция для пойска начала и конца определённой строки в строке
    int searchStringInString(String main, String child){
        int t = -1;
        t = main.indexOf(child);
        return t;
    }
}