package org.screamingsandals.nms.mapper.joined;

import lombok.Data;
import org.screamingsandals.nms.mapper.single.ClassDefinition;
import org.screamingsandals.nms.mapper.single.MappingType;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class JoinedClassDefinition {
    private final Map<Map.Entry<String, MappingType>, String> mapping = new HashMap<>();
    private final List<JoinedField> fields = new ArrayList<>();
    private final List<JoinedConstructor> constructors = new ArrayList<>();
    private final List<JoinedMethod> methods = new ArrayList<>();

    private transient final Map<String, String> pathKeys = new HashMap<>();

    @Data
    public static class JoinedField {
        private final ClassDefinition.Link type;
        private final Map<Map.Entry<String, MappingType>, String> mapping = new HashMap<>();
    }

    @Data
    public static class JoinedConstructor {
        private final List<ClassDefinition.Link> parameters = new ArrayList<>();
        private final List<String> supportedVersions = new ArrayList<>();
    }

    @Data
    public static class JoinedMethod {
        private final ClassDefinition.Link returnType;
        private final Map<Map.Entry<String, MappingType>, String> mapping = new HashMap<>();
        private final List<ClassDefinition.Link> parameters = new ArrayList<>();
    }

    // TODO: figure out how to do custom serializer (configurate is refusing to do anything for some reason)
    public ConfigurationNode asNode(ConfigurationNode node) throws SerializationException {
        mapping.forEach((entry, s) -> {
            try {
                node.node(entry.getValue().name(), entry.getKey()).set(s);
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        });
        var constructorsN = node.node("constructors");
        constructors.forEach(joinedConstructor -> {
            try {
                var constructorN = constructorsN.appendListNode();
                constructorN.node("versions").set(joinedConstructor.getSupportedVersions());
                var parametersN = constructorN.node("parameters");
                joinedConstructor.getParameters().forEach(link -> {
                    try {
                        parametersN.appendListNode().set(link.joined());
                    } catch (SerializationException serializationException) {
                        serializationException.printStackTrace();
                    }
                });
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        });
        var fieldsN = node.node("fields");
        fields.forEach(joinedField -> {
            try {
                var f = fieldsN.appendListNode();
                f.node("type").set(joinedField.getType().joined());
                joinedField.mapping.forEach((entry, s) -> {
                    try {
                        f.node(entry.getValue().name(), entry.getKey()).set(s);
                    } catch (SerializationException e) {
                        e.printStackTrace();
                    }
                });
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        });
        var methodsN = node.node("methods");
        methods.forEach(joinedMethod -> {
            try {
                var m = methodsN.appendListNode();
                m.node("returnType").set(joinedMethod.getReturnType().joined());
                joinedMethod.mapping.forEach((entry, s) -> {
                    try {
                        m.node(entry.getValue().name(), entry.getKey()).set(s);
                    } catch (SerializationException e) {
                        e.printStackTrace();
                    }
                });
                var parametersN = m.node("parameters");
                joinedMethod.getParameters().forEach(link -> {
                    try {
                        parametersN.appendListNode().set(link.joined());
                    } catch (SerializationException serializationException) {
                        serializationException.printStackTrace();
                    }
                });
            } catch (SerializationException e) {
                e.printStackTrace();
            }
        });
        return node;
    }
}
