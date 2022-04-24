package com.example.excel;

import com.example.excel.controller.BuildController;
import com.example.excel.service.BuildApplication;
import org.assertj.core.api.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
public class BuildAppControllerTests {

    @MockBean
    BuildApplication buildAppService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;


    @Spy
    @InjectMocks
    private BuildController controller = new BuildController();


    private InputStream is;

    @BeforeEach
    public void setup() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
        this.mockMvc = builder.build();
    }

    @Test
    void fileTypeNotSupported() throws Exception {
        File file = new File(String.valueOf(is));

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "helloworld.json", MediaType.MULTIPART_FORM_DATA_VALUE, this.getClass().getResourceAsStream("C:\\Users\\Dev\\Documents\\excel\\helloworld.zip"));


        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/buildApp").file(mockMultipartFile).contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
              .andDo(document("{methodName}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()))) .andExpect(MockMvcResultMatchers.status().is(501)).andReturn();


        MockHttpServletResponse response = result.getResponse();
        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), response.getStatus());

    }

    @Test
    void fileTypeSupported() throws Exception {

        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\Dev\\Documents\\excel\\helloworld.zip");
        final byte[] bytes = Files.readAllBytes(Paths.get("C:\\Users\\Dev\\Documents\\excel\\helloworld.zip"));
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "helloworld.zip", "application/zip", bytes);


      // ResponseEntity<String> path = controller.buildVue(mockMultipartFile);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/buildApp")
                        .file("file",bytes).contentType("application/zip"))
                        .andDo(document("{methodName}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()))).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();


        MockHttpServletResponse response = result.getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

    }

    @Test
    public void testController() throws Exception {
        ResultMatcher ok = MockMvcResultMatchers.status().isOk();
        final byte[] bytes = Files.readAllBytes(Paths.get("C:\\Users\\Dev\\helloworld.zip"));
        String fileName = "helloworld.zip";
        File file = new File("C:\\Users\\Dev\\Documents\\excel\\helloworld.zip");

        //delete if exits
        file.delete();

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file",fileName,
                "application/zip", bytes);

        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/buildApp")
                        .file(mockMultipartFile).contentType("application/zip");
        this.mockMvc.perform(builder).andExpect(ok)
                .andDo(MockMvcResultHandlers.print());;
       assertFalse(!file.exists());
    }


}
