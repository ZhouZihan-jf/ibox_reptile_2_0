package reptileFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import models.Content;
import models.Profile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import storage.MongoOperation;
import tools.bloom.SimpleBloomFilter;

import java.io.IOException;
import java.util.*;

public class ProfileReptile {
    //根url
    private static final String URL_DOMAIN = "https://api-h5.ibox.art/nft-mall-web/v1.2/nft/product/getResellList?";

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

    //开始爬取简介
    public static List<Profile> getProfileList(Map<String,Object> config, SimpleBloomFilter pbf) throws IOException, InterruptedException {
        //构建容器
        List<Profile> profiles = new ArrayList<Profile>();
        int tradeCount = 0;
        //构建url
        String url = URL_DOMAIN+
                "type="+config.get("type")+
                "&origin="+config.get("origin")+
                "&sort="+config.get("sort")+
                "&page="+config.get("page")+
                "&pageSize="+config.get("pageSize");

        //解析页面
        Document doc = null;
        try {
            doc = Jsoup.connect(url)
                    .timeout(60000)
                    .headers(getWxHeaderMap(config))
                    .ignoreContentType(true)
                    .get();
        }catch (Exception e){
            System.out.println("profiles crawl fail");
            return profiles;
        }
        String json = doc.text();

        //解析json,阿里巴巴做了人事
        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        try{
            jsonObject = JSONObject.parseObject(json).getJSONObject("data");
            jsonArray = jsonObject.getJSONArray("list");
        }catch (Exception e){
            System.out.println("profiles fail");
            System.out.println(e.toString());
            return profiles;
        }

        //将json列表映射成对象列表
        profiles = jsonArray.toJavaList(Profile.class);

        //使用布隆过滤器过滤,并且为图片添加头部
        for (int i = 0; i < profiles.size();i++){
            if(pbf.contains(profiles.get(i).getGId())){
                profiles.remove(i);
                i--;
                System.out.println("this index already in collection !");
            }else {
                //添加host
                profiles.get(i).setThumbPic("https://www.ibox.art"+profiles.get(i).getThumbPic());

                //Content content = ContentReptile.getContent(config,profiles.get(i),bf);//整合起来，提高成功率
                //tradeCount += TradeReptile.getTrade(config,content);

                System.out.println("one index already get !");
                //System.out.println("\n-----------------------------------------------------------\n");
            }
        }

        if(!profiles.isEmpty()){
            System.out.println("index list:"+profiles.size()+" crawl succeed! and have tradeCount:" + tradeCount);
            //profiles存入数据库
            MongoOperation.storageProfilesByMongo(profiles,config);
        }else {
            System.out.println("index list crawl fail!");
        }
        return profiles;
    }

}




