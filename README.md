# [WIP] NMSMapper

This library will be used to easily get any information about nms classes and should speed up updates of our plugins when new Minecraft version is released!

## Features and planned features
- [X] Reading Mojang obfuscation maps
- [X] Reading Spigot mappings
- [X] Reading MCP mappings
- [ ] Reading Yarn mappings
- [X] Exporting result for each version to separated json file
- [X] Support for pre-obfuscation map versions
- [X] Generating website where you can easily browse through mapping (just like javadocs)
- [X] Getting extra information about classes (modifiers, superclass, implemented interfaces) - server jar needed
- [X] Getting extra information about fields (type)
- [X] Getting extra information about fields (modifiers) - server jar needed
- [X] Getting extra information about methods (return type)
- [X] Getting extra information about methods (modifiers) - server jar needed
- [X] Comparing between versions, saving result to file (file will be then used by slib to generate reflection)
- [ ] Caching for json files to speed up building

## Usage
### How to add new minecraft version or update its info.json
```
$ ./gradlew generateNmsConfig -PminecraftVersion=1.17.1
```