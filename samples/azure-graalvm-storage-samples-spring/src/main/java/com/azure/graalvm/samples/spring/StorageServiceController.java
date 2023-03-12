// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.samples.spring;

import java.io.IOException;
import java.util.stream.Collectors;

import com.azure.graalvm.samples.spring.storage.StorageItem;
import com.azure.graalvm.samples.spring.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;

@Controller
public class StorageServiceController {

	private final StorageService storageService;

	@Autowired
	public StorageServiceController(final StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String listUploadedFiles(final Model model,
									final HttpServletResponse response) {
		model.addAttribute("files", storageService.listAllFiles().collect(Collectors.toList()));
		response.addHeader("Cache-Control", "no-cache");
		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	public ResponseEntity<Resource> serveFile(@PathVariable final String filename) {
		final StorageItem storageItem = storageService.getFile(filename);

		final String contentDisposition;
		switch (storageItem.getContentDisplayMode()) {
			default:
			case DOWNLOAD: {
				contentDisposition = "attachment";
				break;
			}
			case MODAL_POPUP:
			case NEW_BROWSER_TAB: {
				contentDisposition = "inline";
			}
		}

		final Resource body = new InputStreamResource(storageItem.getContent(), storageItem.getFileName());

		return ResponseEntity.ok()
		   .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition + "; filename=\"" + filename + "\"")
		   .contentType(MediaType.parseMediaType(storageItem.getContentType()))
		   .body(body);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") final MultipartFile file,
								   final RedirectAttributes redirectAttributes) {
		boolean success = false;
		try {
			storageService.store(file.getOriginalFilename(), file.getInputStream(), file.getSize());
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		redirectAttributes.addFlashAttribute("success", success);
		redirectAttributes.addFlashAttribute("message", success ?
				"You successfully uploaded " + file.getOriginalFilename() + "!" :
				"Failed to upload " + file.getOriginalFilename());

		return "redirect:/";
	}

	@GetMapping("/files/delete/{filename}")
	public String deleteFile(@PathVariable final String filename,
							 final RedirectAttributes redirectAttributes) {
		final boolean success = storageService.deleteFile(filename);

		redirectAttributes.addFlashAttribute("success", success);
		redirectAttributes.addFlashAttribute("message", success ?
			"You successfully deleted " + filename + "!" :
			"Failed to delete " + filename + ".");

		return "redirect:/";
	}
}