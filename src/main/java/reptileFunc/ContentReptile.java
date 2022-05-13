package reptileFunc;

import com.alibaba.fastjson.JSONObject;
import models.Content;
import models.Profile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import storage.MongoOperation;
import tools.bloom.SimpleBloomFilter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ContentReptile {
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

    //开始利用profiles爬取详细内容
    public static List<Content> getContentList(Map<String,Object> config, List<Profile> profiles, SimpleBloomFilter cbf) throws InterruptedException, IOException {
        //构建容器
        List<Content> contents = new ArrayList<Content>();
        String json1 = "";
        //创立flag
        boolean ok = false;
        //开始遍历爬取
        for (Profile profile : profiles){
            if(cbf.contains(profile.getGId())){
                System.out.println("This content has already got the details!");
                continue;
            }
            Content content = new Content();

            //先把一致的注入
            content.setGId(profile.getGId());
            content.setPriceCny(profile.getPriceCny());
            content.setThumbPic(profile.getThumbPic());//在前面头部已经加上了，这里就不用加了

            //开始拼接url1
            String url1 = Content_DOMAIN+"getProductDetail?"+
                    "albumId="+config.get("id")+
                    "&gId="+profile.getGId()+
                    "&uid="+config.get("uid");

            //暂停2s
            TimeUnit.SECONDS.sleep(2);

            //解析详情页面
            Document doc1 = null;
            try {
                 doc1 = Jsoup.connect(url1).timeout(180000)
                        .headers(getWxHeaderMap(config))
                        .ignoreContentType(true)
                        .get();
            }catch (Exception e){
                System.out.println("this content crawl fail");
                continue;
            }
            json1 = doc1.text();

            //解析json
            try {
                JSONObject jso1 = JSONObject.parseObject(json1).getJSONObject("data");
                content.setName(jso1.getString("gName")+"#"+jso1.getString("gNum"));//获得名字
                content.setIssue(jso1.getString("sellLimit"));//获取发行
                content.setCirculation(jso1.getString("soldNum"));//获取流通
                content.setChain(jso1.getString("tokenId"));//获取链上标识
                content.setContract(config.get("Contract").toString());//获取合约地址
                content.setCreater(jso1.getString("authorWalletInfo"));//获取创建者id
                content.setCreaterName(jso1.getString("authorName"));//获取创建者名称
                content.setOwner(jso1.getString("ownerWalletInfo"));//获取拥有者id
                content.setOwnerName(jso1.getString("ownerName"));//获取拥有者名称
                content.setIntroduction(jso1.getString("introduction"));//获取数字藏品介绍
                content.setGDesc(jso1.getString("gDesc"));//获取商品介绍
            }catch (Exception e){
                System.out.println("this content parse fail");
                continue;
            }

            ok = contents.add(content);
            if(ok){
                System.out.println("Get one successfully!");
            }else {
                System.out.println("Failed to get this article!");
            }
        }
        if(!contents.isEmpty()){
            System.out.println("content list count="+contents.size()+"contents in mongodb");
            MongoOperation.storageContentsByMongo(contents,config);
        }
        return contents;
    }

    public static Content getContent(Map<String,Object> config, Profile profile, SimpleBloomFilter bf) throws InterruptedException, IOException {
        Content content = new Content();

        //先判断有没有爬过
        if(bf.contains(profile.getGId())){
            System.out.println("This profile has already got the details!");
            return content;
        }

        //先把一致的注入
        content.setGId(profile.getGId());
        content.setPriceCny(profile.getPriceCny());
        content.setThumbPic(profile.getThumbPic());//在前面头部已经加上了，这里就不用加了

        //开始拼接url1
        String url1 = Content_DOMAIN+"getProductDetail?"+
                "albumId="+config.get("id")+
                "&gId="+profile.getGId()+
                "&uid="+config.get("uid");

        //暂停2s
        TimeUnit.SECONDS.sleep(2);

        //解析详情页面
        Document doc1 = Jsoup.connect(url1)
                .timeout(50000)
                .headers(getWxHeaderMap(config))
                .ignoreContentType(true)
                .get();
        String json1 = doc1.text();

        //解析json
        JSONObject jso1 = JSONObject.parseObject(json1).getJSONObject("data");

        content.setName(jso1.getString("gName")+"#"+jso1.getString("gNum"));//获得名字
        content.setIssue(jso1.getString("sellLimit"));//获取发行
        content.setCirculation(jso1.getString("soldNum"));//获取流通
        content.setChain(jso1.getString("tokenId"));//获取链上标识
        content.setContract(config.get("Contract").toString());//获取合约地址
        content.setCreater(jso1.getString("authorWalletInfo"));//获取创建者id
        content.setCreaterName(jso1.getString("authorName"));//获取创建者名称
        content.setOwner(jso1.getString("ownerWalletInfo"));//获取拥有者id
        content.setOwnerName(jso1.getString("ownerName"));//获取拥有者名称
        content.setIntroduction(jso1.getString("introduction"));//获取数字藏品介绍
        content.setGDesc(jso1.getString("gDesc"));//获取商品介绍
        //把content存入数据库
        MongoOperation.storageContentByMongo(content,config);

        System.out.println("Get one content successfully!");
        return content;
    }

}
