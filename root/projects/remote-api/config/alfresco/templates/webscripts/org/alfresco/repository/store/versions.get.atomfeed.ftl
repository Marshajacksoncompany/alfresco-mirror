<#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/>
<#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/>
<#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/>
<#import "/org/alfresco/cmis/cmis.lib.atom.ftl" as cmisLib/>
<?xml version="1.0" encoding="UTF-8"?>
<feed <@nsLib.feedNS/>>
<@feedLib.generic id="urn:uuid:${node.id}-versions" title="Versions of ${node.displayPath}"/>
<#list nodes as version>
<entry>
<@entryLib.version node=version version=versions[version_index]/>
<@cmisLib.version node=version version=versions[version_index]/>
</entry>
</#list>
</feed>