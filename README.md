# [WIP] NMSMapper

This library will be used to easily get any information about nms classes and should speed up updates of our plugins when new Minecraft version is released!

## Features and planned features
- [X] Reading Mojang obfuscation maps
- [X] Reading Spigot mappings
- [ ] Reading MCP mappings
- [ ] Reading Yarn mappings
- [X] Exporting result for each version to separated json file
- [ ] Support for pre-obfuscation map versions
- [ ] Generating website where you can easily browse through mapping (just like javadocs)
- [ ] Getting extra information about classes (modifiers, superclass, implemented interfaces) - server jar needed
- [X] Getting extra information about fields (type)
- [ ] Getting extra information about fields (modifiers) - server jar needed
- [X] Getting extra information about methods (return type)
- [ ] Getting extra information about methods (modifiers) - server jar needed
- [ ] Comparing between versions, saving result to file (file will be then used by slib to generate reflection)