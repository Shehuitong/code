package com.example.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.FileInputStream;
import java.io.InputStream;

// 关键：指定主类为你项目的 SpringbootApplication（包名是 springbootdemo）
@SpringBootTest(classes = SpringbootApplication.class)
@AutoConfigureMockMvc
public class AvatarUploadTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testUploadAvatar() throws Exception {
        // 1. 你的文件路径（确保真实存在）
        String filePath = "C:\\Users\\AK\\avatar-test\\test.png";

        // 2. 读取文件流
        try (InputStream inputStream = new FileInputStream(filePath)) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",          // 后端 @RequestParam("file") 对应的参数名
                    "test.png",      // 文件名
                    "image/png",     // 文件类型（如果是jpg改成image/jpeg）
                    inputStream
            );

            // 3. 发送请求：userId=1（H2中存在的用户ID）
            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/user/avatar")
                            .file(file)
                            .param("userId", "1")  // 你H2中存在的用户ID（ID=1）
                    )
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print());
        }
    }
}