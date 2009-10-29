[#ftl]
[#import "/org/alfresco/cmis/ns.lib.atom.ftl" as nsLib/]
[#import "/org/alfresco/cmis/atomfeed.lib.atom.ftl" as feedLib/]
[#import "/org/alfresco/cmis/atomentry.lib.atom.ftl" as entryLib/]
[#compress]

<?xml version="1.0" encoding="UTF-8"?>
<feed [@nsLib.feedNS/]>

[@feedLib.node node "descendants"/]

[#if depth &gt; 0 || depth == -1]
[#list cmischildren(node, typesFilter) as child]
  [#if child.isDocument]
    [@entryLib.document node=child propfilter=propFilter includeallowableactions=includeAllowableActions includerelationships=false/]
  [#else]
    [@entryLib.folder node=child propfilter=propFilter typesfilter=typeFilter includeallowableactions=includeAllowableActions includerelationships=false depth=1 maxdepth=depth/]
  [/#if]
[/#list]
[/#if]

</feed>

[/#compress]
