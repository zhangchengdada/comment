package com.netease.comment.utils;


import com.netease.cloud.services.nos.model.PutObjectResult;
import com.netease.comment.dto.NosProxy;
import com.netease.comment.dto.PictureData;
import com.netease.comment.enums.ImageEnum;
import com.netease.comment.utils.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Component
public class NosPicUtils {



    private static final String LOCALPATH = "."+File.separatorChar +"picfile";
    private static final String IMAGEINFO = "?imageInfo";
    private static final String HEIGHT = "Height";
    private static final String SIZE = "Height";
    private static final String WIDTH = "Height";
    private static final String TYPE = "Type";


    @Autowired
    HttpUtil httpUtil;



    //nos  图片下载处理
    public String processPictureUrl(String url) {

        // 首先下载到本地
        String path = downLoadResourceLocal(url);
        if (path == null) {
            return null;
        }
        String fileName = path.substring(path.lastIndexOf(File.separatorChar) + 1, path.length());
        // 上传到nos
        NosProxy nosProxy = new NosProxy();
        nosProxy.init();
        PutObjectResult result = nosProxy.uploadObject(path, fileName);
        // 删除本地下载的文件
        removeLocalPicset(path);
        return nosProxy.getUploadAddress(result.getObjectName());
    }


    //nos  图片下载处理
    public PictureData processPictureSingle(String url) {

        // 首先下载到本地
        String path = downLoadResourceLocal(url);
        if (path == null) {
            return null;
        }
        String fileName = path.substring(path.lastIndexOf(File.separatorChar) + 1, path.length());
        // 上传到nos
        NosProxy nosProxy = new NosProxy();
        nosProxy.init();
        PutObjectResult result = nosProxy.uploadObject(path, fileName);
        // 删除本地下载的文件
        removeLocalPicset(path);
        PictureData pictureData =  getPicData( nosProxy.getUploadAddress(result.getObjectName()));
        return pictureData;
    }


    /**
     * 下载图片到本地
     *
     * @param url
     * @return
     */
    private String downLoadResourceLocal(String url) {
        String path = null;
        OutputStream os = null;
        try {
            List<Header> headers = new ArrayList<>();
            Header header = new BasicHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) CriOS/56.0.2924.75 Mobile/14E5239e Safari/602.1");
            headers.add(header);
            if (!(url.startsWith("http") || url.startsWith("https"))) {
                if (url.startsWith("//")) {
                    url = "http:".concat(url);
                } else {
                    url = "http://".concat(url);
                }
            }
            HttpResponse res = httpUtil.downloadPictureByGet(url, headers);
            byte[] picset = EntityUtils.toByteArray(res.getEntity());
            String type = res.getEntity().getContentType().getValue();
            if (picset == null) {
                log.error("下载图片到本地失败,图片地址{}", url);
                return null;
            }
            String subFolder=LOCALPATH;
            File folder=new File(subFolder);
            if(!folder.exists()){
                folder.mkdirs();
            }
            path = LOCALPATH + File.separatorChar + System.currentTimeMillis() + "_" + UUID.randomUUID() + ImageEnum.getExtendName(type);
            os = new BufferedOutputStream(new FileOutputStream(new File(path)));
            os.write(picset);
            os.flush();
        } catch (IOException e) {
            log.error("下载图片到本地失败,图片地址{}", url, e);
            return null;
        } finally {
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    log.error("关闭流失败", e);
                }
            }
        }
        Random random = new Random(System.currentTimeMillis());
        try {
            Thread.sleep(Math.abs(random.nextLong() % 2000));
        } catch (InterruptedException e) {
            return path;
        }
        return path;
    }


    /**
     * 删除本地图片
     *
     * @param path
     * @return
     */
    private boolean removeLocalPicset(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     *  获取图片的长宽高封装成对象
     * @param nosUrl
     * @return
     */
    public PictureData getPicData(String nosUrl) {
        try {
            String res = httpUtil.getMethod(nosUrl.concat(IMAGEINFO));
            Document document = DocumentHelper.parseText(res);
            Element root = document.getRootElement();
            String height = root.element(HEIGHT).getText();
            String size = root.element(SIZE).getText();
            String width = root.element(WIDTH).getText();
            String type = root.element(TYPE).getText();
            return new PictureData(Long.valueOf(height), Long.valueOf(size), type, nosUrl, Long.valueOf(width));
        } catch (Exception e) {
            log.error("载入图片dom时出错", e);
            return null;
        }
    }


}
