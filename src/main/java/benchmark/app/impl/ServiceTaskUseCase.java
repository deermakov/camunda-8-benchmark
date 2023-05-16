package benchmark.app.impl;

import org.springframework.stereotype.Component;
import benchmark.app.api.ServiceTaskInbound;

import java.text.MessageFormat;

@Component
public class ServiceTaskUseCase implements ServiceTaskInbound {
    @Override
    public String execute(String startParam, String inputData) {
        return MessageFormat.format("{0} {1}", startParam, inputData);
    }
}
