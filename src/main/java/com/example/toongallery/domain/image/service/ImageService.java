package com.example.toongallery.domain.image.service;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.common.util.FileName;
import com.example.toongallery.domain.common.util.FileUpLoader;
import com.example.toongallery.domain.common.util.FileUtils;
import com.example.toongallery.domain.common.util.ImageType;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.image.entity.Image;
import com.example.toongallery.domain.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final FileUpLoader fileUpLoader;

    @Transactional
    public String uploadWebtoonThumbnail(Long webtoonId, MultipartFile file) {
        String path = ImageType.WEBTOON_THUMBNAIL.getPath(webtoonId.toString());
        return fileUpLoader.upload(file, path, "thumbnail.png");
    }

    @Transactional
    public String uploadEpisodeThumbnail(Long webtoonId, int episodeNumber, MultipartFile file) {
        String path = ImageType.EPISODE_THUMBNAIL.getPath(
                webtoonId.toString(),
                String.valueOf(episodeNumber)
        );

        return fileUpLoader.upload(file, path, "thumbnail.png");
    }

    @Transactional
    public void deleteEpisodeThumbnail(Long webtoonId, int episodeNumber) {
        String path = ImageType.EPISODE_THUMBNAIL.getPath(
                webtoonId.toString(),
                String.valueOf(episodeNumber)
        );
        String key = path.endsWith("/") ? path + "thumbnail.png" : path + "/thumbnail.png";
        fileUpLoader.delete(key);
    }

    @Transactional
    public List<Image> uploadEpisodeImages(Long webtoonId, int episodeNumber, List<MultipartFile> files, Episode episode) {
        List<Image> images = new ArrayList<>();
        List<String> uploadedFilePaths = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String ext = FileUtils.getExtension(file.getOriginalFilename());
            String filename = FileName.forEpisode(i).getValue() + "." + ext;
            String path = ImageType.EPISODE_MAIN.getPath(webtoonId.toString(), String.valueOf(episodeNumber));
            String key = path.endsWith("/") ? path + filename : path + "/" + filename;

            String url = fileUpLoader.upload(file, path, filename);
                Image image = Image.of(filename, url, i, episode);
                images.add(image);
                uploadedFilePaths.add(key);
        }

        return imageRepository.saveAll(images);
    }

    @Transactional
    public void deleteEpisodeImages(Long episodeId) {
        List<Image> images = imageRepository.findImagesByEpisodeId(episodeId);
        for (Image image : images) {
            // CloudFront URL에서 key 부분만 추출
            String s3Key = extractS3KeyFromUrl(image.getImageUrl());
            fileUpLoader.delete(s3Key);
        }
        imageRepository.deleteAll(images);
    }

    private String extractS3KeyFromUrl(String imageUrl) {
        URI uri = URI.create(imageUrl);
        return uri.getPath().substring(1); // 맨 앞의 '/' 제거해서 S3 key 완성
    }

}
