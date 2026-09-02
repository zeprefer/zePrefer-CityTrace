package com.citytrace.controller;

import cn.hutool.core.util.StrUtil;
import com.citytrace.dto.Result;
import com.citytrace.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("upload")
public class UploadController {

    @PostMapping("blog")
    public Result uploadImage(@RequestParam("file") MultipartFile image) {
        try {
            // 获取原始文件名称
            String originalFilename = image.getOriginalFilename();
            // 生成新文件名
            String fileName = createNewFileName(originalFilename);
            // 保存文件
            File targetFile = resolveImageFile(fileName);
            Files.createDirectories(targetFile.toPath().getParent());
            image.transferTo(targetFile);
            // 返回结果
            log.debug("文件上传成功，{}", fileName);
            return Result.ok(fileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @GetMapping("/blog/delete")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        File file;
        try {
            file = resolveImageFile(filename);
        } catch (IllegalArgumentException e) {
            return Result.fail("错误的文件名称");
        }
        if (file.isDirectory()) {
            return Result.fail("错误的文件名称");
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("文件删除失败", e);
        }
        return Result.ok();
    }

    private String createNewFileName(String originalFilename) {
        // 获取后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // 生成目录
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        // 生成文件名
        return StrUtil.format("/blogs/{}/{}/{}.{}", d1, d2, name, suffix);
    }

    private File resolveImageFile(String publicPath) {
        String relativePath = StrUtil.removePrefix(publicPath, "/");
        Path uploadRoot = Paths.get(SystemConstants.IMAGE_UPLOAD_DIR).toAbsolutePath().normalize();
        Path targetPath = uploadRoot.resolve(relativePath).normalize();
        if (!targetPath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("文件路径超出上传目录");
        }
        return targetPath.toFile();
    }
}
