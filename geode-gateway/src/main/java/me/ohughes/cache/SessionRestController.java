package me.ohughes.cache;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;

import java.util.Map;

@RestController
public class SessionRestController {

    @PostMapping(path = "/api/session", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public WebSession setAttribute(@ModelAttribute SessionAttributeForm sessionAttributeForm, WebSession session) {
        session.getAttributes().put(sessionAttributeForm.getAttributeName(), sessionAttributeForm.getAttributeValue());
        return session;
    }

    @GetMapping("/api")
    public Map<String, Object> index(WebSession webSession) {
        return webSession.getAttributes();
    }
}
