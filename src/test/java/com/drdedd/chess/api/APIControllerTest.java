package com.drdedd.chess.api;

import com.drdedd.chess.api.data.EvaluationData;
import com.drdedd.chess.game.data.FENs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class APIControllerTest {

    private APIController controller;

    @BeforeEach
    void setUp() {
        controller = new APIController();
    }

    @Test
    void about() {
        ResponseEntity<Object> response = controller.about();
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String about = response.getBody().toString();
        assertNotNull(about);
        assertTrue(about.startsWith(APIController.ABOUT));
    }

    @Test
    void validate() {
        ResponseEntity<Object> validate = controller.validate();
        assertEquals(HttpStatus.OK, validate.getStatusCode());
        Object body = validate.getBody();
        assertInstanceOf(String.class, body);
        String decoded = new String(Base64.getDecoder().decode(body.toString()));
        assertEquals("chess", decoded);
    }

    @Test
    void evaluation() {
        ResponseEntity<Object> evaluation = controller.evaluation(FENs.defaultPosition, 15, 1);
        Object body = evaluation.getBody();
        if (body != null) System.out.println(body);
        assertInstanceOf(EvaluationData.class, body);

    }
}