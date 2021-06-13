package org.screamingsandals.nms.mapper.single;

import java.util.*;

import lombok.Data;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

@Data
public class ClassDefinition {
    private final Map<MappingType, String> mapping = new HashMap<>();
    private final Map<String, FieldDefinition> fields = new HashMap<>();
    private final List<MethodDefinition> methods = new ArrayList<>();
    private final List<ConstructorDefinition> constructors = new ArrayList<>();

    @Data
    public static class FieldDefinition {
        private final Link type;
        private final Map<MappingType, String> mapping = new HashMap<>();
    }

    @Data
    public static class MethodDefinition {
        private final Link returnType;
        private final Map<MappingType, String> mapping = new HashMap<>();
        private final List<Link> parameters = new ArrayList<>();
    }

    @Data
    public static class ConstructorDefinition {
        private final List<Link> parameters = new ArrayList<>();
    }

    @Data
    public static class Link {
        private final String type;
        private final boolean nms;

        public static Link casualLink(String type) {
            return new Link(type, false);
        }

        public static Link nmsLink(String type) {
            return new Link(type, true);
        }
    }

    // TODO: figure out how to do custom serializer (configurate is refusing to do anything for some reason)
    public ConfigurationNode asNode(ConfigurationNode node) throws SerializationException {
        node.node("mapping").set(mapping);
        var fieldsN = node.node("fields");
        fields.forEach((k,v) -> {
            try {
                var f = fieldsN.appendListNode();
                f.node("type").node("type").set(v.getType().getType());
                f.node("type").node("nms").set(v.getType().isNms());
                f.node("mapping").set(v.getMapping());
            } catch (SerializationException serializationException) {
                serializationException.printStackTrace();
            }
        });
        var constructorsN = node.node("constructors");
        constructors.forEach(v -> {
            var constructorN = constructorsN.appendListNode();
            v.getParameters().forEach(link -> {
                try {
                    var listNode = constructorN.appendListNode();
                    listNode.node("type").set(link.getType());
                    listNode.node("nms").set(link.isNms());
                } catch (SerializationException serializationException) {
                    serializationException.printStackTrace();
                }
            });
        });
        var methodsN = node.node("methods");
        methods.forEach(v -> {
            try {
                var methodN = methodsN.appendListNode();
                methodN.node("returnType").node("type").set(v.getReturnType().getType());
                methodN.node("returnType").node("nms").set(v.getReturnType().isNms());
                methodN.node("mapping").set(v.getMapping());
                v.getParameters().forEach(link -> {
                    try {
                        var listNode = methodN.node("parameters").appendListNode();
                        listNode.node("type").set(link.getType());
                        listNode.node("nms").set(link.isNms());
                    } catch (SerializationException serializationException) {
                        serializationException.printStackTrace();
                    }
                });
            } catch (SerializationException serializationException) {
                serializationException.printStackTrace();
            }
        });
        return node;
    }
}
