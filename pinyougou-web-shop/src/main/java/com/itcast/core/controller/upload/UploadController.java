package com.itcast.core.controller.upload;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.entity.Result;
import com.itcast.core.utils.fdfs.FastDFSClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    @RequestMapping("/uploadFile.do")
    public Result uploadFile(MultipartFile file){

        try {
            String conf="classpath:fastDFS/fdfs_client.conf";
            FastDFSClient fastDFSClient=new FastDFSClient(conf);
            String filename=file.getOriginalFilename();
            String extName= FilenameUtils.getExtension(filename);
            String path = fastDFSClient.uploadFile(file.getBytes(), extName, null);
            String url=FILE_SERVER_URL+path;
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
