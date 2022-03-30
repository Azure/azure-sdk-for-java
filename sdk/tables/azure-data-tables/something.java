package com.data.tables.controller;

import com.data.tables.common.Constants;
import com.data.tables.entities.ExpandableWeatherObject;
import com.data.tables.entities.UpdateWeatherObject;
import com.data.tables.models.FilterResultsInputModel;
import com.data.tables.models.SampleDataInputModel;
import com.data.tables.models.WeatherDataModel;
import com.data.tables.models.WeatherInputModel;
import com.data.tables.service.ITablesService;
import com.data.tables.untils.WeatherDataUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import java.util.HashMap;
import java.util.List;

@RestController
public class TablesServiceController  {

    @Autowired
    private ITablesService tablesService;

    private static final String LAYOUT_PAGE = "layouts/_layout";

    private static final String PRIVACY_PAGE = "privacy";

    private static final String INDEX_PAGE = "index";

    private static final String FILTER_PAGE = "filter-results";

    private static final String HOME_PAGE_TITLE = "Home page";

    private static final String PRIVACY_PAGE_TITLE = "Privacy Policy";

    private static final String ATTRIBUTE_NAME_TITLE = "title";

    @GetMapping("/")
    public ModelAndView initLayout() {
        ModelAndView modelAndView = new ModelAndView(LAYOUT_PAGE);
        modelAndView.addObject(ATTRIBUTE_NAME_TITLE, HOME_PAGE_TITLE);
        return modelAndView;
    }

    @GetMapping("/initAllRows")
    public ModelAndView initAllRows() {
        return new ModelAndView(INDEX_PAGE);
    }

    @GetMapping("/initFilteredRows")
    public ModelAndView initFilteredRows() {
        ModelAndView modelAndView = new ModelAndView(FILTER_PAGE);
        modelAndView.addObject(ATTRIBUTE_NAME_TITLE, HOME_PAGE_TITLE);
        return modelAndView;
    }

    @GetMapping("/initPrivacy")
    public ModelAndView initPrivacy() {
        ModelAndView modelAndView = new ModelAndView(PRIVACY_PAGE);
        modelAndView.addObject(ATTRIBUTE_NAME_TITLE, PRIVACY_PAGE_TITLE);
        return modelAndView;
    }

    @GetMapping("/getAllRows")
    public String getAllRows() {
        List<WeatherDataModel> entitiesList = tablesService.getAllRows();
        return new JSONObject(new HashMap<String, Object>(){{
            put("entitiesList", entitiesList);
            put("listOfKeys", WeatherDataUtils.getListOfKeys(entitiesList));
            put("code", Constants.SUCCESS_CODE);
            put("msg", Constants.SUCCESS_MSG);
        }}).toString();
    }

    @PostMapping("/getFilteredRows")
    public String getFilteredRows(@RequestBody FilterResultsInputModel model) {
        List<WeatherDataModel> entitiesList = tablesService.getFilteredRows(model);
        return new JSONObject(new HashMap<String, Object>(){{
            put("entitiesList", entitiesList);
            put("listOfKeys", WeatherDataUtils.getListOfKeys(entitiesList));
            put("code", Constants.SUCCESS_CODE);
            put("msg", Constants.SUCCESS_MSG);
        }}).toString();
    }

    @PostMapping("/removeEntity")
    public String removeEntity(@RequestBody WeatherInputModel weatherInputModel) {
        tablesService.removeEntity(weatherInputModel);
        return new JSONObject(new HashMap<String, Object>(){{
            put("code", Constants.SUCCESS_CODE);
            put("msg", Constants.SUCCESS_MSG);
        }}).toString();
    }

    @PostMapping(value = "/insertTableEntity")
    public String insertTableEntity(@RequestBody WeatherInputModel weatherInputModel) {
        tablesService.insertTableEntity(weatherInputModel);
        return new JSONObject(new HashMap<String, Object>(){{
            put("code", Constants.SUCCESS_CODE);
            put("msg", Constants.SUCCESS_MSG);
        }}).toString();
    }

    @PostMapping("/upsertTableEntity")
    public String upsertTableEntity(@RequestBody WeatherInputModel weatherInputModel) {
        tablesService.upsertTableEntity(weatherInputModel);
        return new JSONObject(new HashMap<String, Object>(){{
            put("code", Constants.SUCCESS_CODE);
            put("msg", Constants.SUCCESS_MSG);
        }}).toString();
    }

    @PostMapping("/updateEntity")
    public String updateEntity(@RequestBody UpdateWeatherObject updateWeatherObject) {
        tablesService.updateEntity(updateWeatherObject);
        return new JSONObject(new HashMap<String, Object>(){{
            put("code", Constants.SUCCESS_CODE);
            put("msg", Constants.SUCCESS_MSG);
        }}).toString();
    }

    @PostMapping("/insertExpandableData")
    public String insertExpandableData(@RequestBody ExpandableWeatherObject expandableWeatherObject) {
        tablesService.insertExpandableData(expandableWeatherObject);
        return new JSONObject(new HashMap<String, Object>(){{
            put("code", Constants.SUCCESS_CODE);
            put("msg", Constants.SUCCESS_MSG);
        }}).toString();
    }

    @PostMapping("/upsertExpandableData")
    public String upsertExpandableData(@RequestBody ExpandableWeatherObject expandableWeatherObject) {
        tablesService.upsertExpandableData(expandableWeatherObject);
        return new JSONObject(new HashMap<String, Object>(){{
            put("code", Constants.SUCCESS_CODE);
            put("msg", Constants.SUCCESS_MSG);
        }}).toString();
    }

    @PostMapping("/insertSampleData")
    public String insertSampleData(@RequestBody SampleDataInputModel sampleDataInputModel) {
        tablesService.insertSampleData(sampleDataInputModel);
        return new JSONObject(new HashMap<String, Object>(){{
            put("code", Constants.SUCCESS_CODE);
            put("msg", Constants.SUCCESS_MSG);
        }}).toString();
    }

}
