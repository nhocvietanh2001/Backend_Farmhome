package com.ute.farmhome.service.implement;

import com.ute.farmhome.dto.FileUpload;
import com.ute.farmhome.dto.PaginationDTO;
import com.ute.farmhome.entity.FruitImage;
import com.ute.farmhome.entity.News;
import com.ute.farmhome.exception.ResourceNotFound;
import com.ute.farmhome.repository.NewsRepository;
import com.ute.farmhome.service.NewsService;
import com.ute.farmhome.utility.UpdateFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class NewsServiceImplement implements NewsService {
    @Autowired
    NewsRepository newsRepository;
    @Autowired
    UpdateFile updateFile;
    @Override
    public News createNews(News news, MultipartFile imageBanner, MultipartFile imageContent) {
        news.setDate(LocalDate.now());
        FileUpload fileUpload = new FileUpload();
        fileUpload.setFile(imageBanner);
        updateFile.update(fileUpload);
        news.setImageBanner(fileUpload.getOutput());
        fileUpload.setFile(imageContent);
        updateFile.update(fileUpload);
        news.setImageContent(fileUpload.getOutput());
        return newsRepository.save(news);
    }

    @Override
    public News getNewsById(int id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("News", "id", String.valueOf(id)));
    }

    @Override
    public PaginationDTO getAllNews(int no, int limit) {
        Pageable pageable = PageRequest.of(no, limit);
        List<News> listNews= newsRepository.findAllPaging(pageable).stream().toList();
        Page<News> page = newsRepository.findAllPaging(pageable);
        return new PaginationDTO(listNews, page.isFirst(), page.isLast(), page.getTotalPages(), page.getTotalElements(), page.getSize(), page.getNumber(),"true", "");
    }

    @Override
    public News updateNews(News news) {
        News oldNews = newsRepository.findById(news.getId())
                .orElseThrow(() -> new ResourceNotFound("News", "id", String.valueOf(news.getId())));
        if (news.getAuthor() != null)
            oldNews.setAuthor(news.getAuthor());
        if (news.getContent() != null)
            oldNews.setContent(news.getContent());
        if (news.getTitle() != null)
            oldNews.setTitle(news.getTitle());
        if (news.getImageBanner() != null)
            oldNews.setImageBanner(news.getImageBanner());
        if (news.getImageContent() != null)
            oldNews.setImageContent(news.getImageContent());
        if (news.getCategory() != null)
            oldNews.setCategory(news.getCategory());
        oldNews.setDate(LocalDate.now());
        return newsRepository.save(oldNews);
    }

    @Override
    public void deleteNews(int id) {
        newsRepository.deleteById(id);
    }
}
