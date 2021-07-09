package com.annaaj.store.controller;

import com.annaaj.store.enums.Role;
import com.annaaj.store.service.AuthenticationService;
import com.annaaj.store.service.CategoryService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import com.annaaj.store.utils.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.annaaj.store.common.ApiResponse;
import com.annaaj.store.model.Category;

@RestController
@RequestMapping("/category")

public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private AuthenticationService authenticationService;

	@ApiOperation(value = "get categories")
	@GetMapping("/")
    public ResponseEntity<List<Category>> getCategories() {
        List<Category> body = categoryService.listCategories();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

	@ApiOperation(value = "create category, ROLE = ADMIN")
	@PostMapping("/create")
	public ResponseEntity<ApiResponse> createCategory(@ApiParam(value = "id can be left as it is") @Valid @RequestBody Category category,
													  @ApiParam @RequestParam("token") String token) {
		authenticationService.authenticate(token, Collections.singletonList(Role.admin));
		if (Helper.notNull(categoryService.readCategory(category.getCategoryName()))) {
			return new ResponseEntity<>(new ApiResponse(false, "category already exists"), HttpStatus.CONFLICT);
		}
		categoryService.createCategory(category);
		return new ResponseEntity<>(new ApiResponse(true, "created the category"), HttpStatus.CREATED);
	}

	@ApiOperation(value = "update category, ROLE = ADMIN")
	@PostMapping("/update/{categoryID}")
	public ResponseEntity<ApiResponse> updateCategory(
			@ApiParam(value = "id of the category to be updated") @PathVariable("categoryID") Integer categoryID,
			@ApiParam(value = "modified category object") @Valid @RequestBody Category category,
			@ApiParam @RequestParam("token") String token) {
		authenticationService.authenticate(token, Collections.singletonList(Role.admin));
		// Check to see if the category exists.
		if (Helper.notNull(categoryService.readCategory(categoryID))) {
			// If the category exists then update it.
			categoryService.updateCategory(categoryID, category);
			return new ResponseEntity<>(new ApiResponse(true, "updated the category"), HttpStatus.OK);
		}

		// If the category doesn't exist then return a response of unsuccessful.
		return new ResponseEntity<>(new ApiResponse(false, "category does not exist"), HttpStatus.NOT_FOUND);
	}
}
