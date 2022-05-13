import models.Content;
import models.Profile;
import models.Trade;

import org.yaml.snakeyaml.Yaml;
import reptileFunc.ContentReptile;
import reptileFunc.ProfileReptile;
import reptileFunc.TradeReptile;
import storage.MongoOperation;
import tools.bloom.SimpleBloomFilter;
import tools.cookie.GetCookieByJava;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainReptile {
    //创建布隆过滤器
    private static SimpleBloomFilter pbf = new SimpleBloomFilter();
    private static SimpleBloomFilter cbf = new SimpleBloomFilter();

    private static long startTime = System.currentTimeMillis();
    private static long endTime = 0;
    private static int COUNT = 0;
    //程序主入口
    public static void main(String[] args) throws IOException, InterruptedException {
        //获取配置
        Yaml yaml = new Yaml();//从yaml中获得部分配置
        Map<String,Object> config = yaml.load(
                new BufferedReader(new FileReader("src/main/resources/config.yaml"))
        );

        while (COUNT != 1000){
            //获取cookie
            String cookie = null;
            try {
                cookie = GetCookieByJava.getCookie();
            }catch (Exception e){
                System.out.println("get cookie by firefox fail");
            }
            if(cookie !=null){
                System.out.println("cookie in collection");
                MongoOperation.storageCookieByMongo(cookie,config);
                config.put("Cookie",cookie);
            }else {
                System.out.println("cookie is null!");
                cookie = MongoOperation.getCookie(config);
                config.put("Cookie",cookie);
            }


            //获取已爬取记录
            List<Profile> profiles = MongoOperation.getProfilesByMongo(config);
            List<Content> contents = MongoOperation.getContentsByMongo(config);

            //引入布隆过滤器并锻炼
            for (Profile profile : profiles){
                pbf.addValue(profile.getGId());
            }
            for (Content content : contents){
                cbf.addValue(content.getGId());
            }

            //爬取简介列表,并存入数据库.
            //注意：最好每爬到一个简介项就对简介解析爬取正文和寄售列表，防止出错导致之前爬取失效，提升稳定性
            profiles = ProfileReptile.getProfileList(config,pbf);

            //爬取详情,并逐条存入数据库
            contents = ContentReptile.getContentList(config,profiles,cbf);

            //补救措施 List<Content> contents = MongoOperation.getContentsByMongo(config);

            //爬取寄售信息，并按商品逐个存入数据库
            List<Trade> trades = TradeReptile.getTradeList(config,contents);

            System.out.println("End of a crawl");
            System.out.println("crawled to"+profiles.size()+" Introduction");
            System.out.println("crawled to"+contents.size()+" Article body");
            System.out.println("crawled to"+trades.size()+" consignment record");
            endTime = System.currentTimeMillis();
            System.out.println("program has been executed"+(endTime-startTime)+"ms");
            System.out.println("--------------------------------------------------------------");

            //设定20分钟一循环
            TimeUnit.SECONDS.sleep(60*20);
            COUNT++;
        }
    }
}
