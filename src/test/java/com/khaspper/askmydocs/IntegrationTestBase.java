package com.khaspper.askmydocs;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
abstract class IntegrationTestBase {

    // The throwaway database
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg17")
                    .asCompatibleSubstituteFor("postgres"))
            .withInitScript("db/enable-pgvector.sql");

    // Stand ins
    @MockitoBean
    Embedder embedder;

    @MockitoBean
    Answerer answerer;

    @Autowired
    MockMvc http;

    @BeforeEach
    void makeTheModelsAnswerWithoutOllama() {
        float[] madeUpNumbers = new float[768];
        Arrays.fill(madeUpNumbers, 0.1f);

        given(embedder.embedDocument(anyString())).willReturn(madeUpNumbers);
        given(embedder.embedQuery(anyString())).willReturn(madeUpNumbers);
    }
}
