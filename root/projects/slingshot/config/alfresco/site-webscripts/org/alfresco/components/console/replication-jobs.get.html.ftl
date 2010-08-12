<!--[if IE]>
<iframe id="yui-history-iframe" src="${url.context}/yui/history/assets/blank.html"></iframe> 
<![endif]-->
<input id="yui-history-field" type="hidden" />

<script type="text/javascript">//<![CDATA[
   new Alfresco.ConsoleReplicationJobs("${args.htmlid}").setMessages(${messages});
//]]></script>

<#assign id = args.htmlid>
<div id="${id}-body" class="replication">

   <!-- Main panel -->
   <div id="${id}-replication" class="hidden">

      <#-- Summary -->
      <div id="${id}-summary" style="display: none;">
         <h2>${msg("header.summary")}</h2>
         <div style="border: 1px solid black;" class="job-summary">
            <div id="${id}-jobCount"><strong>12 Jobs</strong></div>
            <ul id="${id}-jobSummary">
               <li class="successful">9 jobs successful</li>
               <li class="failed">1 job failed</li>
               <li class="running">1 job running</li>
               <li class="cancelled">No cancelled jobs</li>
               <li class="none">1 job has no status</li>
            </ul>
         </div>
      </div>
      
      <#-- Jobs -->
      <div id="${id}-jobs">
         <h2>${msg("header.jobs")}</h2>
         <div class="yui-gd">
            
            <div class="yui-u first jobs-list-container">
               <div class="container-panel">
                  <div class="flat-button bottom-border">
                     <span id="${id}-create" class="yui-button yui-push-button">
                        <span class="first-child">
                           <a tabindex="0" href="${url.context}/page/console/replication-job">${msg("button.create-job")}</a>
                        </span>
                     </span>
                     <input type="button" id="${id}-sortBy" value="${msg("button.sort-by", msg("label.sort-by.status"))}" />
                     <select id="${id}-sortBy-menu">
                         <option value="status">${msg("label.sort-by.status")}</option>
                         <option value="name">${msg("label.sort-by.name")}</option>
                         <option value="lastRun">${msg("label.sort-by.last-run-date")}</option>
                     </select>                  
                  </div>
                  <div id="${id}-jobsList" class="jobs-list"></div>
               </div>
            </div>
            
            <div class="yui-u job-detail-container">
               <div class="container-panel">
                  <div class="flat-button job-buttons" style="float: right;">
                     <span id="${id}-run" class="yui-button yui-push-button">
                        <span class="first-child">
                           <button type="button" tabindex="0">${msg("button.run-job")}</button>
                        </span>
                     </span>
                     <span id="${id}-cancel" class="yui-button yui-push-button">
                        <span class="first-child">
                           <button type="button" tabindex="0">${msg("button.cancel-job")}</button>
                        </span>
                     </span>
                     <span id="${id}-edit" class="yui-button yui-push-button">
                        <span class="first-child">
                           <button type="button" tabindex="0">${msg("button.edit-job")}</button>
                        </span>
                     </span>
                     <span id="${id}-delete" class="yui-button yui-push-button">
                        <span class="first-child">
                           <button type="button" tabindex="0">${msg("button.delete-job")}</button>
                        </span>
                     </span>
                  </div>
                  <div id="${id}-jobDetail" class="job-detail">
                     <div class="message">${msg("label.no-job-selected")}</div>
                  </div>
               </div>
            </div>
         </div>
      </div>

   </div>
   
   <div id="${id}-jobTemplate" style="display:none;">
      <h2>{name}</h2>
      <div>{description}</div>
      <div class="{enabledClass}">{enabledText}</div>
      <hr />
      <div class="flat-button" style="float: right;">
         <span id="${id}-refresh" class="yui-button yui-push-button">
            <span class="first-child">
               <button type="button" tabindex="0">${msg("button.refresh")}</button>
            </span>
         </span>
         <span id="${id}-viewReport" class="yui-button yui-button-disabled yui-link-button">
            <span class="first-child">
               <a tabindex="0" href="{viewReportLink}">${msg("button.view-report")}</a>
            </span>
         </span>
      </div>
      <div>
         <h3>${msg("label.status")}</h3>
         <div class="job-status">{statusText}</div>
      </div>
      <hr />
      <h3>${msg("label.payload")}</h3>
      <div class="payload">
         {payloadHTML}
      </div>
   </div>

</div>