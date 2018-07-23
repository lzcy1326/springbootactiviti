package com.lzcy.springbootactiviti.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import groovy.util.logging.Slf4j;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/service")
public class ModelerController {

    @Autowired
    protected RepositoryService repositoryService;
    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/model/querylist")
    public ModelAndView queryModels() {
        List<Model> models = repositoryService.createModelQuery().orderByCreateTime().desc().list();
        ModelAndView mv = new ModelAndView("querymodels");
        mv.getModel().put("flows", models);
        return mv;
    }


    @RequestMapping(value = "/model/create", method = RequestMethod.POST)
    public ModelAndView createModel(@RequestParam Map<String, String> map) throws UnsupportedEncodingException {
        String key = map.get("key");
        String name = map.get("name");
        String description = map.get("description");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.set("stencilset", stencilSetNode);

        ObjectNode modelObjectNode = objectMapper.createObjectNode();
        modelObjectNode.put("name", name);
        modelObjectNode.put("revision", 1);
        modelObjectNode.put("description", description);
        modelObjectNode.put("process_id", key); // 唯一标识符
        editorNode.set("properties", modelObjectNode);

        Model modelData = repositoryService.newModel();
        modelData.setKey(key);
        modelData.setName(name);
        modelData.setMetaInfo(modelObjectNode.toString());

        repositoryService.saveModel(modelData);
        repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));

        return queryModels();
    }

    @RequestMapping(value = "/model/{modelId}/json", method = RequestMethod.GET, produces = "application/json")
    public ObjectNode getEditorJson(@PathVariable String modelId) {
        ObjectNode modelNode = null;
        Model model = repositoryService.getModel(modelId);
        if (model != null) {
            try {
                if (!StringUtils.isEmpty(model.getMetaInfo())) {
                    JsonNode jsonNode = objectMapper.readTree(model.getMetaInfo());
                    modelNode = (ObjectNode) jsonNode;
                } else {
                    modelNode = objectMapper.createObjectNode();
                    modelNode.put("name", model.getName());
                    modelNode.put("revision", model.getVersion());
                }
                modelNode.put("modelId", model.getId());
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
                modelNode.set("model", editorJsonNode);
            } catch (Exception e) {
                System.err.println("Error creating model JSON");
                throw new ActivitiException("Error creating model JSON", e);
            }
        }
        return modelNode;
    }

    @RequestMapping(value = "/editor/stencilset", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    public String getStencilset() {
        String json = "";
        try (InputStream stencilsetStream = this.getClass().getClassLoader().getResourceAsStream("stencilset/stencilset-zh_CN.json")){
            json = IOUtils.toString(stencilsetStream, "utf-8");
        } catch (Exception e) {
            throw new ActivitiException("Error while loading stencil set", e);
        }
        return json;
    }

    @RequestMapping(value = "/model/{modelId}/save", method = RequestMethod.PUT)
    public Map<String, Object> saveModel(@PathVariable("modelId") String modelId, @RequestParam("name") String name,
                                         @RequestParam("json_xml") String jsonXml, @RequestParam("svg_xml") String svgXml,
                                         @RequestParam("description") String description) {
        ModelAndView mv = new ModelAndView();
        try{
            Model model = repositoryService.getModel(modelId);
            if (!StringUtils.isEmpty(model.getDeploymentId())) {
                Model modelData = repositoryService.newModel();
                modelData.setKey(model.getKey());
                modelData.setName(model.getName());
                modelData.setMetaInfo(model.getMetaInfo());
                modelData.setVersion(model.getVersion() + 1);

                repositoryService.saveModel(modelData);
                repositoryService.addModelEditorSource(modelData.getId(), repositoryService.getModelEditorSource(modelId));
                repositoryService.addModelEditorSourceExtra(modelData.getId(), repositoryService.getModelEditorSourceExtra(modelId));

                mv.getModel().put("lastUpdated", false);
                return mv.getModel();
            }
            if (svgXml.indexOf("url(\"#") > 0) { // IE绘制，火狐没有这种情况
                svgXml = svgXml.replace("url(\"#", "url(#").replace("\")\"", ")\"");
                System.err.println(" IE  diagram. 生成 svg_xml 有误，需要转换");
            }
            model.setName(name);
            System.out.println(description);
            System.out.println(jsonXml);
            repositoryService.saveModel(model);
            repositoryService.addModelEditorSource(model.getId(), jsonXml.getBytes("utf-8"));
            repositoryService.addModelEditorSourceExtra(model.getId(), svgXml.getBytes("utf-8"));

            mv.getModel().put("lastUpdated", true);
        } catch (IOException e){
            e.printStackTrace();
        }
        return mv.getModel();
    }

    @RequestMapping(value = "/model/{modelId}/delete", method = RequestMethod.POST)
    public String deleteModel(@PathVariable("modelId") String modelId) {
        repositoryService.deleteModel(modelId);
        return "/service/model/querylist";
    }
}
