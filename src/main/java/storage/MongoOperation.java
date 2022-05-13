package storage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import models.Content;
import models.Profile;
import models.Trade;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Sorts.descending;

public class MongoOperation {
    //创建连接并打开数据库
    private static MongoClientOptions options = MongoClientOptions.builder()
                .socketTimeout(60000)
                .connectTimeout(60000)
                .maxConnectionIdleTime(3600000)
                .build();
    private static MongoClient  mongoClient = null;
    private static MongoDatabase db = null;

    //开启连接
    private static void getConnection(){
        mongoClient = new MongoClient("120.77.66.52:37016",options);
        db = mongoClient.getDatabase("reptile_test");
    }

    //关闭连接
    private static void closeConnection(){
        mongoClient.close();
    }

    //存储简介列表
    public static void storageProfilesByMongo(List<Profile> profiles, Map<String,Object> config){
        //获得连接
        getConnection();
        //打开集合
        MongoCollection profileCollection = db.getCollection(config.get("profileCollection").toString());

        List<Document> documents = new ArrayList<>();
        for (Profile profile : profiles){
            //转成一条文档
            Document document = Document.parse(JSONObject.toJSONString(profile));
            //插入document列表
            documents.add(document);
        }
        //插入数据库
        profileCollection.insertMany(documents);
        //关闭连接
        closeConnection();
    }

    //存储详情列表
    public static  void storageContentsByMongo(List<Content> contents,Map<String,Object> config){
        //获得连接
        getConnection();
        //打开集合
        MongoCollection contentCollection = db.getCollection(config.get("contentCollection").toString());

        List<Document> documents = new ArrayList<>();
        for (Content content : contents){
            //转成一条文档
            Document document = Document.parse(JSONObject.toJSONString(content));
            //插入document列表
            documents.add(document);
        }
        //插入数据库
        contentCollection.insertMany(documents);
        //关闭连接
        closeConnection();
    }

    //存储详情
    public static  void storageContentByMongo(Content content,Map<String,Object> config){
        //获得连接
        getConnection();
        //打开集合
        MongoCollection contentCollection = db.getCollection(config.get("contentCollection").toString());

        //转成一条文档
        Document document = Document.parse(JSONObject.toJSONString(content));
        //插入数据库
        contentCollection.insertOne(document);
        //关闭连接
        closeConnection();

    }

    //存储详情，按content存，一次存存一个列表
    public static  void storageTradesByMongo(List<Trade> tradeList, Map<String,Object> config){
        //获取连接
        getConnection();
        //打开集合
        MongoCollection tradeCollection = db.getCollection(config.get("tradeCollection").toString());

        //tradelist有可能为空
        if(tradeList.isEmpty()){
            System.out.println("this tradeList is empty");
            return;
        }

        List<Document> documents = new ArrayList<>();
        for (Trade trade : tradeList){
            //转成一条文档
            Document document = Document.parse(JSONObject.toJSONString(trade));
            //插入document列表
            documents.add(document);
        }
        //插入数据库
        tradeCollection.insertMany(documents);
        //关闭连接
        closeConnection();
    }

    //存储cookie
    public static  void storageCookieByMongo(String cookie, Map<String,Object> config){
        //获得连接
        getConnection();
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("start storage cookie");
        System.out.println("---------------------------------------------------------------------------------");
        //打开集合
        MongoCollection<Document> cookieCollection = db.getCollection(config.get("cookieCollection").toString());

        //生成一条映射数据
        Map<String,Object> cookieMap = new HashMap<>();
        cookieMap.put("cookie",cookie);
        //转成一条文档
        Document document = new Document(cookieMap);
        //插入数据库
        cookieCollection.insertOne(document);
        //关闭连接
        closeConnection();
    }

    //提取简介
    public static List<Profile> getProfilesByMongo(Map<String,Object> config){
        //获得连接
        getConnection();
        //声明
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("start get profiles");
        System.out.println("---------------------------------------------------------------------------------");

        List<Profile> profiles = new ArrayList<Profile>();
        //打开集合
        MongoCollection profileCollection = db.getCollection(config.get("profileCollection").toString());
        //创建迭代容器
        MongoCursor<Document> doc = profileCollection.find().iterator();
        //开始迭代
        while (doc.hasNext()){
            //获取一条记录，转成json对象
            JSONObject jsonObject = JSON.parseObject(doc.next().toJson());
            //创建对象
            Profile profile = new Profile();
            profile.setGId(jsonObject.getString("gId"));
            profile.setGName(jsonObject.getString("gName"));
            profile.setPriceCny(jsonObject.getString("priceCny"));
            profile.setThumbPic(jsonObject.getString("thumbPic"));

            profiles.add(profile);
        }

        //关闭连接
        closeConnection();

        return profiles;
    }

    //提取正文
    public static List<Content> getContentsByMongo(Map<String,Object> config){
        //获得连接
        getConnection();
        //声明
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("start get contents");
        System.out.println("---------------------------------------------------------------------------------");

        List<Content> contents = new ArrayList<Content>();
        //打开集合
        MongoCollection contentCollection = db.getCollection(config.get("contentCollection").toString());
        //创建迭代容器
        MongoCursor<Document> doc = contentCollection.find().iterator();
        //开始迭代
        while (doc.hasNext()){
            //获取一条记录，转成json对象
            JSONObject jsonObject = JSON.parseObject(doc.next().toJson());
            //创建对象
            Content content = new Content();
            content.setName(jsonObject.getString("name"));
            content.setGId(jsonObject.getString("gId"));

            contents.add(content);
        }
        //关闭连接
        closeConnection();

        return contents;
    }

    //从数据库中获取cookie
    public static String getCookie(Map<String,Object> config){
        //获得连接
        getConnection();
        //打开集合
        MongoCollection<Document> cookieCollection = db.getCollection(config.get("cookieCollection").toString());
        //获取最新一条记录
        Document doc = cookieCollection.find().sort(descending("_id")).first();
        JSONObject jsonObject = JSON.parseObject(doc.toJson());
        //注入值
        String cookie = jsonObject.getString("cookie");
        //关闭连接
        closeConnection();

        return cookie;
    }

}
