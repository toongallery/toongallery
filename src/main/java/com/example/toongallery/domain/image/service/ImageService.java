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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final FileUpLoader fileUpLoader;

    public String uploadEpisodeThumbnail(Long webtoonId, int episodeNumber, MultipartFile file) {
        String path = ImageType.EPISODE_THUMBNAIL.getPath(
                webtoonId.toString(),
                String.valueOf(episodeNumber)
        );

        return fileUpLoader.upload(file, path, "thumbnail.png");
    }

    public List<Image> uploadEpisodeImages(Long webtoonId, int episodeNumber, List<MultipartFile> files, Episode episode) {
        List<Image> images = new ArrayList<>();
        List<String> uploadedFilePaths = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String ext = FileUtils.getExtension(file.getOriginalFilename());
            String filename = FileName.forEpisode(i).getValue() + "." + ext;
            String path = ImageType.EPISODE_MAIN.getPath(webtoonId.toString(), String.valueOf(episodeNumber));
            String key = path.endsWith("/") ? path + filename : path + "/" + filename;

            try {
                String url = fileUpLoader.upload(file, path, filename);
                Image image = Image.of(filename, url, i, episode);
                images.add(image);
                uploadedFilePaths.add(key);
            } catch (BaseException uploadEx) {
                // 실패한 경우, 이전에 업로드한 이미지들 S3에서 삭제
//                for (String uploadedKey : uploadedFilePaths) {
//                    try {
//                        fileUpLoader.delete(uploadedKey);
//                    } catch (BaseException deleteEx) {
//                        System.err.println("업로드 롤백 중 이미지 삭제 실패: " + uploadedKey);
//                    }
//                }
// 여기서 상태 값 변경, 체크드익셉션, 언체크드 익셉션을 찾아보고 롤백이 안되는 법을 확인..
                throw new BaseException(ErrorCode.SERVER_NOT_WORK, "에피소드 이미지 업로드 중 실패");
            }
        }

        return imageRepository.saveAll(images);
    }


}
