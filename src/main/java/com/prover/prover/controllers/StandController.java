package com.prover.prover.controllers;

import com.prover.prover.models.Images;
import com.prover.prover.models.Pattern;
import com.prover.prover.models.Stand;
import com.prover.prover.services.ImageService;
import com.prover.prover.services.PatternService;
import com.prover.prover.services.StandService;
import com.prover.prover.utils.Constants;
import com.prover.prover.utils.helpers.FileHelper;
import com.prover.prover.utils.helpers.UserHelper;
import com.prover.prover.wrappers.StandListWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Created by Admin on 21.05.2017.
 */
@Controller
@RequestMapping("/")
public class StandController {

    private final StandService standService;
    private final PatternService patternService;
    private final ImageService imageService;

    @RequestMapping()
    public String testhur(Model model, Authentication authentication) {
        model.addAttribute("current_user", SecurityContextHolder.getContext().getAuthentication().getName());
        if (SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
            model.addAttribute("logged",false);
        }
        return "template";
    }

    @RequestMapping("/content")
    public String testhus() {
        return "content";
    }


    @RequestMapping("/stand")
    public String testhuk(Model model) {
        return "stand";
    }


    @RequestMapping(value = "/secret/patterns", method = RequestMethod.POST)
    public String secretPatt(String name) {
        Pattern pattern = new Pattern();
        pattern.setName(name);
        patternService.save(pattern);
        return "patternsForm";
    }
    @RequestMapping(value = "/secret/patterns", method = RequestMethod.GET)
    public String secretPatt() {
        return "patternsForm";
    }
    @RequestMapping(value = "/secret/image", method = RequestMethod.GET)
    public String uploadForm() {
        return "uploadForm";
    }

    @RequestMapping(value = "/secret/image", method = RequestMethod.POST)
    public String uploadForm(String mops, String type) {
        imageService.saveImage("/"+mops+type);
        return "uploadForm";
    }
    @Autowired
    public StandController(StandService standService, PatternService patternService, ImageService imageService) {
        this.standService = standService;
        this.patternService = patternService;
        this.imageService = imageService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/stands")
    @ResponseBody
    public StandListWrapper viewAll(@RequestParam(required = false, name = "patterns[]") List<Long> patternIds,
                                    @RequestParam(required = false, defaultValue = "1") Integer page) {
        List<Stand> stands;

        if (patternIds != null && !patternIds.isEmpty()) {
            stands = standService.findByPatterns(patternIds, page - 1);
        } else {
            stands = standService.findAll(page - 1);
        }

        StandListWrapper standListWrapper =new StandListWrapper();
        standListWrapper.setStandList(stands);
        standListWrapper.setSize((long) Math.floor(standService.sizeOfStands(patternIds)/ Constants.STANDS_LIMIT));

        return standListWrapper;
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> upload(@RequestParam(name = "file") MultipartFile uploadFile) {
        System.out.println(uploadFile.getContentType());
        if (uploadFile.isEmpty()) {
            return new ResponseEntity<>("file is empty!", HttpStatus.BAD_REQUEST);
        }
        if (!uploadFile.getContentType().equals(MimeTypeUtils.IMAGE_GIF_VALUE) ||
                !uploadFile.getContentType().equals(MimeTypeUtils.IMAGE_JPEG_VALUE) ||
                !uploadFile.getContentType().equals(MimeTypeUtils.IMAGE_PNG_VALUE)) {
            return new ResponseEntity<>("invalidType", HttpStatus.BAD_REQUEST);
        }
        Images images = null;
        try {

            String fileName = FileHelper.saveUploadedFile(uploadFile);
            images = imageService.saveImage(fileName);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(images, HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String createStand(@RequestParam String text,
                              @RequestParam String title,
                              @RequestParam(name = "patterns[]") List<Long> pattenIds,
                              Model model) {
        List<Pattern> patterns = patternService.getByIds(pattenIds);
        Stand stand = standService.save(text, title, patterns);
        model.addAttribute("stand", stand);
        return "redirect:/";
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String createStand() {
        return "createLesson";
    }

    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Stand viewStand(@PathVariable Long id, Model model) {
        return standService.getOne(id);
    }

    @RequestMapping(value = "/{id}/delete", method = RequestMethod.GET)
    public String deleteStand(@PathVariable Long id, Model model) {
        Stand stand = standService.getOne(id);
        if (stand != null && UserHelper.currentUser().equals(stand.getUser())) {
            standService.delete(stand);
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/{id}/update", method = RequestMethod.POST)
    public String patchStand(@PathVariable Long id,
                             @RequestParam String text,
                             @RequestParam String title,
                             @RequestParam(name = "patterns[]") List<Long> pattenIds,
                             Model model) {
        Stand stand = standService.getOne(id);
        List<Pattern> patterns = patternService.getByIds(pattenIds);
        if (stand != null && UserHelper.currentUser().equals(stand.getUser())) {
            stand.setBody(text);
            stand.setTitle(title);
            stand.getPatterns().clear();
            stand.getPatterns().addAll(patterns);
            standService.update(stand);
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/{id}/update", method = RequestMethod.GET)
    @ResponseBody
    public Stand viewBeforeUpdateStand(@PathVariable Long id, Model model) {
        Stand stand = standService.getOne(id);

        model.addAttribute("stand", stand);
        return stand;
    }

    @RequestMapping(value = "/canManageStand/{id}", method = RequestMethod.GET)
    @ResponseBody
    public boolean canManageStand(@PathVariable Long id){
        Stand stand = standService.getOne(id);
        return stand.getUser().equals(UserHelper.currentUser());
    }

    @RequestMapping(value = "/patterns", method = RequestMethod.GET)
    @ResponseBody
    public List<Pattern> getPatterns(){
        return patternService.findAll();
    }
}
