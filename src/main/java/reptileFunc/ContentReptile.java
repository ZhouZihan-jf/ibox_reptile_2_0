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
    //��url
    private static final String Content_DOMAIN = "https://api-h5.ibox.art/nft-mall-web/v1.2/nft/product/";
    //��������ͷ
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

    //��ʼ����profiles��ȡ��ϸ����
    public static List<Content> getContentList(Map<String,Object> config, List<Profile> profiles, SimpleBloomFilter cbf) throws InterruptedException, IOException {
        //��������
        List<Content> contents = new ArrayList<Content>();
        String json1 = "";
        //����flag
        boolean ok = false;
        //��ʼ������ȡ
        for (Profile profile : profiles){
            if(cbf.contains(profile.getGId())){
                System.out.println("This content has already got the details!");
                continue;
            }
            Content content = new Content();

            //�Ȱ�һ�µ�ע��
            content.setGId(profile.getGId());
            content.setPriceCny(profile.getPriceCny());
            content.setThumbPic(profile.getThumbPic());//��ǰ��ͷ���Ѿ������ˣ�����Ͳ��ü���

            //��ʼƴ��url1
            String url1 = Content_DOMAIN+"getProductDetail?"+
                    "albumId="+config.get("id")+
                    "&gId="+profile.getGId()+
                    "&uid="+config.get("uid");

            //��ͣ2s
            TimeUnit.SECONDS.sleep(2);

            //��������ҳ��
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

            //����json
            try {
                JSONObject jso1 = JSONObject.parseObject(json1).getJSONObject("data");
                content.setName(jso1.getString("gName")+"#"+jso1.getString("gNum"));//�������
                content.setIssue(jso1.getString("sellLimit"));//��ȡ����
                content.setCirculation(jso1.getString("soldNum"));//��ȡ��ͨ
                content.setChain(jso1.getString("tokenId"));//��ȡ���ϱ�ʶ
                content.setContract(config.get("Contract").toString());//��ȡ��Լ��ַ
                content.setCreater(jso1.getString("authorWalletInfo"));//��ȡ������id
                content.setCreaterName(jso1.getString("authorName"));//��ȡ����������
                content.setOwner(jso1.getString("ownerWalletInfo"));//��ȡӵ����id
                content.setOwnerName(jso1.getString("ownerName"));//��ȡӵ��������
                content.setIntroduction(jso1.getString("introduction"));//��ȡ���ֲ�Ʒ����
                content.setGDesc(jso1.getString("gDesc"));//��ȡ��Ʒ����
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

        //���ж���û������
        if(bf.contains(profile.getGId())){
            System.out.println("This profile has already got the details!");
            return content;
        }

        //�Ȱ�һ�µ�ע��
        content.setGId(profile.getGId());
        content.setPriceCny(profile.getPriceCny());
        content.setThumbPic(profile.getThumbPic());//��ǰ��ͷ���Ѿ������ˣ�����Ͳ��ü���

        //��ʼƴ��url1
        String url1 = Content_DOMAIN+"getProductDetail?"+
                "albumId="+config.get("id")+
                "&gId="+profile.getGId()+
                "&uid="+config.get("uid");

        //��ͣ2s
        TimeUnit.SECONDS.sleep(2);

        //��������ҳ��
        Document doc1 = Jsoup.connect(url1)
                .timeout(50000)
                .headers(getWxHeaderMap(config))
                .ignoreContentType(true)
                .get();
        String json1 = doc1.text();

        //����json
        JSONObject jso1 = JSONObject.parseObject(json1).getJSONObject("data");

        content.setName(jso1.getString("gName")+"#"+jso1.getString("gNum"));//�������
        content.setIssue(jso1.getString("sellLimit"));//��ȡ����
        content.setCirculation(jso1.getString("soldNum"));//��ȡ��ͨ
        content.setChain(jso1.getString("tokenId"));//��ȡ���ϱ�ʶ
        content.setContract(config.get("Contract").toString());//��ȡ��Լ��ַ
        content.setCreater(jso1.getString("authorWalletInfo"));//��ȡ������id
        content.setCreaterName(jso1.getString("authorName"));//��ȡ����������
        content.setOwner(jso1.getString("ownerWalletInfo"));//��ȡӵ����id
        content.setOwnerName(jso1.getString("ownerName"));//��ȡӵ��������
        content.setIntroduction(jso1.getString("introduction"));//��ȡ���ֲ�Ʒ����
        content.setGDesc(jso1.getString("gDesc"));//��ȡ��Ʒ����
        //��content�������ݿ�
        MongoOperation.storageContentByMongo(content,config);

        System.out.println("Get one content successfully!");
        return content;
    }

}
