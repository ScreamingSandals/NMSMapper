package org.screamingsandals.nms.generator;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ClassGeneratorExtension {
    private final List<RequiredClass> neededClasses = new ArrayList<>();

    private String sourceSet = "src/main/java";
    private String basePackage = "com.example.nms";
    private boolean cleanOnRebuild = false;

    public RequiredClass reqClass(String clazz) {
        var cl = new RequiredClass(clazz);
        neededClasses.add(cl);
        return cl;
    }

    @Data
    public static class RequiredClass {
        private final String clazz;
        private final List<String> fields = new ArrayList<>();
        private final List<String> enumFields = new ArrayList<>();
        private final List<Map.Entry<String, String[]>> methods = new ArrayList<>();
        private final List<String[]> constructors = new ArrayList<>();

        public RequiredClass reqField(String field) {
            fields.add(field);
            return this;
        }

        public RequiredClass reqEnumField(String field) {
            enumFields.add(field);
            return this;
        }

        public RequiredClass reqConstructor(Object... params) {
            var converted = new String[params.length];
            for (var i = 0; i < params.length; i++) {
                if (params[i] instanceof RequiredClass) {
                    converted[i] = "&" + ((RequiredClass) params[i]).getClazz();
                } else if (params[i] instanceof Class) {
                    converted[i] = ((Class<?>) params[i]).getName();
                } else {
                    converted[i] = params[i].toString();
                }
            }
            constructors.add(converted);
            return this;
        }

        public RequiredClass reqMethod(String method, Object... params) {
            var converted = new String[params.length];
            for (var i = 0; i < params.length; i++) {
                if (params[i] instanceof RequiredClass) {
                    converted[i] = "&" + ((RequiredClass) params[i]).getClazz();
                } else if (params[i] instanceof Class) {
                    converted[i] = ((Class<?>) params[i]).getName();
                } else {
                    converted[i] = params[i].toString();
                }
            }
            methods.add(Map.entry(method, converted));
            return this;
        }

    }
}
