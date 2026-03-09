package com.app.projectbar.application.implementation;

import com.app.projectbar.application.exception.ErrorMessagesService;
import com.app.projectbar.application.exception.ingredient.IngredientNotFoundException;
import com.app.projectbar.application.exception.product.ProductAlreadyExistsException;
import com.app.projectbar.application.exception.product.ProductNotFoundException;
import com.app.projectbar.application.interfaces.IProductService;
import com.app.projectbar.application.mapper.ProductMapper;
import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.ProductIngredient;
import com.app.projectbar.domain.dto.product.ProductForListResponseDTO;
import com.app.projectbar.domain.dto.product.ProductRequestDTO;
import com.app.projectbar.domain.dto.product.ProductResponseDTO;
import com.app.projectbar.domain.dto.product.UpdateProductRequestDTO;
import com.app.projectbar.domain.enums.Category;
import com.app.projectbar.domain.Product;
import com.app.projectbar.infra.repositories.IProductRepository;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;
    private final IIngredientRepository ingredientRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductForListResponseDTO> findAll() {
        List<Product> productList = productRepository.findAll();
        return productMapper.toListDTOList(productList);
    }

    @Override
    public ProductResponseDTO findById(Long id) {
        return productMapper.toResponseDTO(productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(ErrorMessagesService.PRODUCT_NOT_FOUND_BY_ID.getMessage())));
    }

    @Override
    public ProductResponseDTO findByCode(String code) {
        var product = productRepository.findByCode(code)
                .orElseThrow(() -> new ProductNotFoundException(ErrorMessagesService.PRODUCT_NOT_FOUND_BY_CODE.getMessage()));
        return productMapper.toResponseDTO(product);
    }

    @Override
    public ProductResponseDTO findByNameExact(String name) {
        var product = productRepository.findOneByName(name)
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format(ErrorMessagesService.PRODUCT_NOT_FOUND_BY_NAME.getMessage(), name)));
        return productMapper.toResponseDTO(product);
    }

    @Override
    public ProductResponseDTO save(ProductRequestDTO productRequest) {
        if (productRepository.existsByNameIgnoreCase(productRequest.getName())) {
            throw new ProductAlreadyExistsException(
                    String.format(ErrorMessagesService.PRODUCT_ALREADY_EXISTS_BY_NAME.getMessage(), productRequest.getName()));
        }
        if (productRepository.existsByCode(productRequest.getCode())) {
            throw new ProductAlreadyExistsException(
                    String.format(ErrorMessagesService.PRODUCT_ALREADY_EXISTS_BY_CODE.getMessage(), productRequest.getCode()));
        }

        var product = new Product();
        return saveOrUpdate(product, productRequest);
    }

    @Override
    public ProductResponseDTO update(UpdateProductRequestDTO productRequestDTO) {
        var product = productRepository.findById(productRequestDTO.getId())
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format(ErrorMessagesService.PRODUCT_NOT_FOUND_BY_ID.getMessage(), productRequestDTO.getId())));

        if (!product.getName().equalsIgnoreCase(productRequestDTO.getName())
                && productRepository.existsByNameIgnoreCase(productRequestDTO.getName())) {
            throw new ProductAlreadyExistsException(
                    String.format(ErrorMessagesService.PRODUCT_ALREADY_EXISTS_BY_NAME.getMessage(), productRequestDTO.getName()));
        }

        if (!product.getCode().equals(productRequestDTO.getCode())
                && productRepository.existsByCode(productRequestDTO.getCode())) {
            throw new ProductAlreadyExistsException(
                    String.format(ErrorMessagesService.PRODUCT_ALREADY_EXISTS_BY_CODE.getMessage(), productRequestDTO.getCode()));
        }

        ProductRequestDTO mapped = productMapper.updateToRequest(productRequestDTO);
        return saveOrUpdate(product, mapped);
    }

    @Override
    public void delete(String code) {
        var product = productRepository.findByCode(code);
        product.ifPresentOrElse(productRepository::delete, () -> {
            throw new ProductNotFoundException(
                    String.format(ErrorMessagesService.PRODUCT_NOT_FOUND_BY_CODE.getMessage(), code));
        });
    }

    @Override
    public ProductResponseDTO toggleActive(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format(ErrorMessagesService.PRODUCT_NOT_FOUND_BY_ID.getMessage(), id)));
        product.setActive(!Boolean.TRUE.equals(product.getActive()));
        return productMapper.toResponseDTO(productRepository.save(product));
    }

    @Override
    public List<ProductForListResponseDTO> findByCategory(Category category) {
        return productMapper.toListDTOList(productRepository.findByCategory(category));
    }

    @Override
    public List<ProductForListResponseDTO> findByNameContaining(String name) {
        return productMapper.toListDTOList(productRepository.findByNameContaining(name));
    }

    public ProductResponseDTO saveOrUpdate(Product product, ProductRequestDTO productRequest) {

        product.setName(productRequest.getName());
        product.setCode(productRequest.getCode());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setPhotoId(productRequest.getPhotoId());
        product.setIsPrepared(productRequest.getIsPrepared());
        product.setCategory(productRequest.getCategory());

        List<ProductIngredient> existingIngredients = product.getProductIngredients();
        List<ProductIngredient> newIngredients = productRequest.getIngredients()
                .stream()
                .map(piRequest -> {
                    Ingredient ingredient = ingredientRepository.findById(piRequest.getIngredientId())
                            .orElseThrow(() -> new IngredientNotFoundException(
                                    String.format(ErrorMessagesService.INGREDIENT_NOT_FOUND_BY_ID.getMessage(), piRequest.getIngredientId())));
                    return ProductIngredient.builder()
                            .product(product)
                            .ingredient(ingredient)
                            .amount(piRequest.getAmount())
                            .build();
                })
                .toList();

        existingIngredients
                .removeIf(existingIngredient -> newIngredients.stream().noneMatch(newIngredient -> newIngredient
                        .getIngredient().getId().equals(existingIngredient.getIngredient().getId())));

        for (ProductIngredient newIngredient : newIngredients) {
            existingIngredients.stream()
                    .filter(existingIngredient -> existingIngredient.getIngredient().getId()
                            .equals(newIngredient.getIngredient().getId()))
                    .findFirst()
                    .ifPresentOrElse(
                            existingIngredient -> existingIngredient.setAmount(newIngredient.getAmount()),
                            () -> existingIngredients.add(newIngredient));
        }

        product.setProductIngredients(existingIngredients);
        Product savedProduct = productRepository.save(product);

        ProductResponseDTO productResponseDTO = productMapper.toResponseDTO(savedProduct);
        for (int i = 0; i < productResponseDTO.getIngredients().size(); i++) {
            productResponseDTO.getIngredients().get(i)
                    .setIngredient_id(savedProduct.getProductIngredients().get(i).getIngredient().getId());
            productResponseDTO.getIngredients().get(i).setIngredientExtend(
                    savedProduct.getProductIngredients().get(i).getIngredient().getUnitOfMeasure().toString());
        }
        return productResponseDTO;
    }

}
