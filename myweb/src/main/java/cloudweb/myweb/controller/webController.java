package cloudweb.myweb.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class webController {
    private final String API_ENDPOINT = "https://d9qgbj7p80.execute-api.ap-northeast-2.amazonaws.com/loc/locations?action=getLocations";

    @GetMapping("/locations")
    public String getLocations(Model model) {

        RestTemplate restTemplate = new RestTemplate();

        // API 호출
        List<Map<String, Object>> locations = restTemplate.getForObject(API_ENDPOINT, List.class);

        // 데이터 확인
        log.info("Fetched Locations: {}" , locations);

        // 데이터를 모델에 추가하여 Thymeleaf로 전달
        model.addAttribute("locations", locations);

        return "locations"; // Thymeleaf 템플릿 이름 (locations.html)

    }
}
