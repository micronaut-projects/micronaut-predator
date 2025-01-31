package io.micronaut.validation.tck;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterExtension implements ExecutionCondition {
    private static final ConditionEvaluationResult DISABLED = ConditionEvaluationResult.disabled("DISABLED");

    private final Set<String> disabledTests = Files.readAllLines(
        Path.of(getClass().getResource("/exclusions.txt").toURI())
    ).stream().filter(l -> {
        String line = l.trim();
        return !line.startsWith("#") && !line.isEmpty();
    }).collect(Collectors.toSet());

    public FilterExtension() throws Exception {
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Class<?> testClass = context.getTestClass().orElse(null);
        String testMethodName = context.getTestMethod().map(Method::getName).orElse("");
//        if (testClass == null) {
//            return ConditionEvaluationResult.enabled("No test class or method");
//        }
//        if (testClass != ee.jakarta.tck.ws.rs.ee.rs.core.uriinfo.JAXRSClientIT.class) {
//            return DISABLED;
//        }

//        if (testClass != ee.jakarta.tck.ws.rs.jaxrs21.ee.priority.JAXRSClientIT.class) {
//            return DISABLED;
//        }
//        String id = testClass.getName() + context.getTestMethod().map(method -> "#" + method.getName()).orElse("");
//        if (disabledTests.contains(id)) {
//            return DISABLED;
//        }
//        if ("baseUriTest".equals(testMethodName) || testMethodName.isEmpty()) {
//            return ConditionEvaluationResult.enabled(null);
//        } else if (true) {
//            return DISABLED;
//        }
        return ConditionEvaluationResult.enabled(null);
    }

}
