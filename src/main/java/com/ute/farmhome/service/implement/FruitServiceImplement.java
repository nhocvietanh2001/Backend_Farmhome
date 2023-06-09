package com.ute.farmhome.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ute.farmhome.dto.*;
import com.ute.farmhome.entity.Fruit;
import com.ute.farmhome.entity.FruitImage;
import com.ute.farmhome.exception.ResourceNotFound;
import com.ute.farmhome.mapper.FruitMapper;
import com.ute.farmhome.repository.CategoryRepository;
import com.ute.farmhome.repository.FruitImageRepository;
import com.ute.farmhome.repository.FruitRepository;
import com.ute.farmhome.repository.UserRepository;
import com.ute.farmhome.service.FruitImageService;
import com.ute.farmhome.service.FruitService;
import com.ute.farmhome.service.UserService;
import com.ute.farmhome.utility.UpdateFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FruitServiceImplement implements FruitService {
	@Autowired
	private FruitImageService fruitImageService;
	@Autowired
	private UserService userService;
	@Autowired
	private FruitRepository fruitRepository;
	@Autowired
	private FruitImageRepository fruitImageRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private UpdateFile updateFile;
	@Autowired
	private FruitMapper fruitMapper;
	@Override
	public PaginationDTO getAllFruit(int no, int limit) {
		PageRequest pageRequest = PageRequest.of(no, limit);
		List<FruitShowDTO> fruits = fruitRepository.findAllFruit(pageRequest).stream().map(item -> {
			try {
				return addSuggestPrice(fruitMapper.mapToShow(item));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).toList();
		Page<Fruit> page = fruitRepository.findAllFruit(pageRequest);
		return new PaginationDTO(fruits, page.isFirst(), page.isLast(), page.getTotalPages(), page.getTotalElements(), page.getSize(), page.getNumber(),"true", "");
	}

	@Override
	public List<TableFruitDTO> getAllFruitByTable(int no, int limit) {
		PageRequest pageRequest = PageRequest.of(no, limit);
		List<TableFruitDTO> listFruit = new ArrayList<>();
		fruitRepository.findAllFruit(pageRequest).getContent().forEach(fruit -> {
			try {
				listFruit.add(new TableFruitDTO(fruit.getName(), crawlData(fruit.getName())));
			} catch (Exception e) {
				throw new RuntimeException(e);
			};
		});
		return listFruit;
	}

	@Override
	public FruitShowDTO getFruitById(int id) throws Exception {
		return addSuggestPrice(fruitMapper.mapToShow(fruitRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFound("fruit", "id", String.valueOf(id)))));
	}
	@Override
	public FruitDTO readJson(String fruit, List<MultipartFile> images) {
		FruitDTO fruitDTO = new FruitDTO();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			fruitDTO = objectMapper.readValue(fruit, FruitDTO.class);
			if (images != null) {
				if (images.stream().count() > 0) {
					fruitDTO.setImageFiles(images);
				}
			}
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return fruitDTO;
	}
	@Override
	public FruitShowDTO createFruit(FruitDTO fruitDTO) {
		if (fruitDTO.getImageFiles().stream().count() > 0) {
			for (MultipartFile imageFile : fruitDTO.getImageFiles()) {
				FileUpload fileUpload = new FileUpload();
				fileUpload.setFile(imageFile);
				updateFile.update(fileUpload);
				FruitImage fruitImage = new FruitImage();
				fruitImage.setUrl(fileUpload.getOutput());
				fruitDTO.getImages().add(fruitImage);
			}
		}
		Fruit fruit = fruitMapper.map(fruitDTO);
		fruit.setRemainingWeight(fruit.getWeight());
		fruitDTO.getImages().forEach(fruitImage -> {fruitImage.setFruit(fruit);});
		return fruitMapper.mapToShow(fruitRepository.save(fruit));
	}

	@Override
	public PaginationDTO searchFruit(String name, int no, int limit) {
		Pageable pageable = PageRequest.of(no, limit);
		List<FruitShowDTO> listFruit = fruitRepository.searchByName(name, pageable).stream().map(item -> {
			try {
				return addSuggestPrice(fruitMapper.mapToShow(item));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).toList();
		Page<Fruit> page = fruitRepository.searchByName(name, pageable);
		return new PaginationDTO(listFruit, page.isFirst(), page.isLast(), page.getTotalPages(), page.getTotalElements(), page.getSize(), page.getNumber(),"true", "");
	}

	@Override
	public FruitShowDTO updateFruit(FruitDTO fruitDTO) {
		Fruit fruit = fruitRepository.findById(fruitDTO.getId())
				.orElseThrow(() -> new ResourceNotFound("fruit", "id", String.valueOf(fruitDTO.getId())));

		fruit.setName(fruitDTO.getName());
		fruit.setWeight(fruitDTO.getWeight());
		fruit.setRemainingWeight(fruit.getWeight());
		fruit.setUnit(fruitDTO.getUnit());
		fruit.setDate(LocalDate.parse(fruitDTO.getDate()));
		fruit.setSeason(fruitDTO.getSeason());
		fruit.setPopular(fruitDTO.getPopular());
		fruit.setDescription(fruitDTO.getDescription());
		fruit.setCategory(fruitDTO.getCategory() != null
				? categoryRepository.findByCategory(fruitDTO.getCategory())
				: categoryRepository.findById(1)
				.orElseThrow(() -> new ResourceNotFound("category", "id", String.valueOf(1))));
		if (fruitDTO.getImageFiles().stream().count() > 0) {
			List<FruitImage> fruitImages = new ArrayList<>();
			for (MultipartFile imageFile : fruitDTO.getImageFiles()) {
				FileUpload fileUpload = new FileUpload();
				fileUpload.setFile(imageFile);
				fruit.getImages().forEach(fruitImage -> {
					fruitImageService.deleteImageById(fruitImage.getId());
				});
				updateFile.update(fileUpload);
				FruitImage fruitImage = new FruitImage();
				fruitImage.setUrl(fileUpload.getOutput());
				fruitImage.setFruit(fruit);
				fruitImages.add(fruitImage);
			}
			fruit.setImages(fruitImages);
		}
		return fruitMapper.mapToShow(fruitRepository.save(fruit));
	}

	@Override
	public PaginationDTO getFruitByUserId(int id, int no, int limit) {
		Pageable pageable = PageRequest.of(no, limit);
		List<?> listFruit = fruitRepository.getFruitByUserId(id, pageable).stream().map(item -> {
			try {
				return addSuggestPrice(fruitMapper.mapToShow(item));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).toList();
		Page<?> page = fruitRepository.getFruitByUserId(id, pageable);
		return new PaginationDTO(listFruit, page.isFirst(), page.isLast(), page.getTotalPages(), page.getTotalElements(), page.getSize(), page.getNumber(),"true", "");
	}

	@Override
	public PaginationDTO filterPaging(String name, Float amount, List<String> seasonList, Boolean popular, String order, int no, int limit) {
		Pageable pageable = PageRequest.of(no, limit);
		List<?> listFruit = fruitRepository.filterFruit(name, amount, seasonList, popular, order, pageable).stream().map(item -> fruitMapper.mapToShow(item)).toList();
		Page<Fruit> page = fruitRepository.filterFruit(name, amount, seasonList, popular, order, pageable);
		return new PaginationDTO(listFruit, page.isFirst(), page.isLast(), page.getTotalPages(), page.getTotalElements(), page.getSize(), page.getNumber(),"true", "");
	}

	@Override
	public Fruit findFruitById(int id) {
		return fruitRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFound("Fruit", "id", String.valueOf(id)));
	}

	@Override
	public PaginationDTO getFruitByCategory(String category, int no, int limit) {
		Pageable pageable = PageRequest.of(no, limit);
		List<?> listFruit = fruitRepository.getFruitByCategory(category, pageable).stream().map(item -> fruitMapper.mapToShow(item)).toList();
		Page<Fruit> page = fruitRepository.getFruitByCategory(category, pageable);
		return new PaginationDTO(listFruit, page.isFirst(), page.isLast(), page.getTotalPages(), page.getTotalElements(), page.getSize(), page.getNumber(),"true", "");
	}

	@Override
	public void deleteById(int id) {
		Fruit fruit = fruitRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFound("Fruit", "id", String.valueOf(id)));
		fruit.getImages().forEach(item -> {
			fruitImageService.deleteImageById(item.getId());
		});
		fruit.setFarmer(null);
		fruitRepository.save(fruit);
	}

	@Override
	public void save(Fruit fruit) {
		fruitRepository.save(fruit);
	}

	@Override
	public String crawlData(String fruitName) throws Exception {
		URL url = new URL("https://baokinhte.net/gia-ca-thi-truong/gia-nong-san/");

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("GET");

		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		String line;
		StringBuilder response = new StringBuilder();
		while ((line = rd.readLine()) != null) {
			response.append(line);
		}
		rd.close();

		Document doc = Jsoup.parse(response.toString(), "UTF-8");

		Element element = doc.select("tr:has(td:containsOwn("+filterFruitName(fruitName)+"))").first();
		if (element != null) {
			Element price = element.select("td:containsOwn(00)").first();
			if (price!=null)
				return price.text();
		}

		return "no data";
	}

	@Override
	public List<FruitShowDTO> getListFruitByUserId(int id, int no, int limit) {
		Pageable pageable = PageRequest.of(no, limit);
		return fruitRepository.getFruitByUserId(id, pageable).stream().map(item -> fruitMapper.mapToShow(item)).toList();
	}

	private String filterFruitName(String fruitName) {
		String wordToRemove = "trái ";
		String removedString = fruitName.replaceAll("\\b" + wordToRemove + "\\b", ""); 	//remove "trái "

		wordToRemove = "Trái ";
		removedString = removedString.replaceAll("\\b" + wordToRemove + "\\b", ""); 	//remove "Trái "
		//modifiedString = modifiedString.replaceAll("\\s", ""); 							//remove all whitespace

		String[] words = removedString.split("\\s+"); //split words by space
		if (words.length > 1) {
			return words[0] + " " + words[1]; //return first word and second word
		}
		return words[0];
	}

	private FruitShowDTO addSuggestPrice(FruitShowDTO fruitShowDTO) throws Exception {
		fruitShowDTO.setSuggestPrice(crawlData(fruitShowDTO.getName()));
		return fruitShowDTO;
	}
}
