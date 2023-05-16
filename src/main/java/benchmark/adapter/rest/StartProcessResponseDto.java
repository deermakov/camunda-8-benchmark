package benchmark.adapter.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartProcessResponseDto {
    private String processId;
}
