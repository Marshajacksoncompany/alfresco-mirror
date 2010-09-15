<#include "../../include/alfresco-macros.lib.ftl" />
<#assign el=args.htmlid?js_string>
<script type="text/javascript">//<![CDATA[
new Alfresco.WorkflowDetailsHeader("${el}").setOptions(
{
   taskId: <#if page.url.args.taskId??>"${page.url.args.taskId?js_string}"<#else>null</#if>
}).setMessages(
   ${messages}
);
//]]></script>
<div id="${el}-body" class="form-manager workflow-details-header">
   <#if page.url.args.taskId??>
   <div class="links">
      <#assign referrer><#if page.url.args.referrer??>&referrer=${page.url.args.referrer?js_string}</#if></#assign>
      <a href="${siteURL("task-details?taskId=" + page.url.args.taskId?js_string + referrer)}">${msg("label.taskDetails")}</a>
      <span class="separator">|</span>
      <span class="theme-color-2">${msg("label.workflowDetails")}</span>
   </div>
   </#if>
   <h1>${msg("header")}: <span></span></h1>
   <div class="clear"></div>
</div>
