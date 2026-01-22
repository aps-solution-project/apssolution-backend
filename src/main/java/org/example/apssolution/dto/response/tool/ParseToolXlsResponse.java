package org.example.apssolution.dto.response.tool;

import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.Tool;

import java.util.List;

@Getter
@Setter
public class ParseToolXlsResponse {
    List<Tool> tools;
}
