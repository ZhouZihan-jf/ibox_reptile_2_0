package reptileFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import models.Content;
import models.Trade;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import storage.MongoOperation;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TradeReptile {
    //根url
    private static final String Content_DOMAIN = "https://api-h5.ibox.art/nft-mall-web/v1.2/nft/product/";
    //配置请求头
    public static Map<String, String> getWxHeaderMap(Map<String,Object> config) {
        Map<String, String> map = new HashMap<>(new LinkedHashMap<>());
        map.put("Cookie", config.get("Cookie").toString());
        map.put("Accept", "application/json, text/plain, */*");
        map.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        map.put("Accept-Encoding", "gzip, deflate, br");
        map.put("Host", "api-h5.ibox.art");
        map.put("Origin", "https://www.ibox.art");
        map.put("Referer", "https://www.ibox.art");
        map.put("User-Agent", config.get("User-Agent").toString());
        return map;
    }

    public static List<Trade> getTradeList(Map<String,Object> config, List<Content> contents) throws IOException, InterruptedException {
        //构建容器
        List<Trade> trades = new ArrayList<Trade>();
        String json2 = "";
        //开始遍历爬取
        for (Content content : contents){
            //拼接url
            String url2 = Content_DOMAIN+"getTransactionList?"+
                    "page=1&pageSize=50"+
                    "&albumId="+config.get("id")+
                    "&gNum="+content.getName().split("#")[1];

            //暂停1秒
            TimeUnit.SECONDS.sleep(2);

            //解析交易页面
            Document doc2 = null;
            try{
                doc2 = Jsoup.connect(url2)
                        .timeout(180000)
                        .headers(getWxHeaderMap(config))
                        .ignoreContentType(true)
                        .get();
            }catch (Exception e){
                System.out.println("this trades fail");
                continue;
            }
            json2 = doc2.text();

            //解析json
            JSONObject jso2 = null;
            try {
                jso2 = JSONObject.parseObject(json2).getJSONObject("data");
            }catch (Exception e){
                System.out.println("this trades parse fail");
                continue;
            }
            if (jso2 != null && jso2.getJSONArray("list") != null){
                JSONArray jsa = jso2.getJSONArray("list");
                //将json列表映射成对象列表
                List<Trade> tradeList = jsa.toJavaList(Trade.class);
                //注入主键
                for (Trade trade : tradeList){
                    trade.setGId(content.getGId());
                }
                if(tradeList != null){
                    System.out.println("item"+content.getGId()+"The consignment list has been obtained!");
                    //该content的trade列表存入数据库
                    MongoOperation.storageTradesByMongo(tradeList,config);
                    trades.addAll(tradeList);
                }else {
                    System.out.println("item"+content.getGId()+"The consignment list did not get!");
                }
            }
        }
        return trades;
    }

    public static int getTrade(Map<String,Object> config,Content content) throws InterruptedException, IOException {
        int count = 0;
        //拼接url
        String url2 = Content_DOMAIN+"getTransactionList?"+
                "page=1&pageSize=50"+
                "&albumId="+config.get("id")+
                "&gNum="+content.getName().split("#")[1];

        //暂停1秒
        TimeUnit.SECONDS.sleep(1);

        //解析交易页面
        Document doc2 = Jsoup.connect(url2)
                .timeout(50000)
                .headers(getWxHeaderMap(config))
                .ignoreContentType(true)
                .get();
        String json2 = doc2.text();

        //解析json
        JSONObject jso2 = JSONObject.parseObject(json2).getJSONObject("data");
        if (jso2 != null && jso2.getJSONArray("list") != null){
            JSONArray jsa = jso2.getJSONArray("list");
            //将json列表映射成对象列表
            List<Trade> tradeList = jsa.toJavaList(Trade.class);
            //计数
            count = tradeList.size();
            //注入主键
            for (Trade trade : tradeList){
                trade.setGId(content.getGId());
            }
            if(tradeList != null){
                //该content的trade列表存入数据库
                MongoOperation.storageTradesByMongo(tradeList,config);
                System.out.println("item"+content.getGId()+"The consignment list has been obtained! and count="+count);
            }else {
                System.out.println("item"+content.getGId()+"The consignment list did not get!");
            }
        }
        return count;
    }

}
