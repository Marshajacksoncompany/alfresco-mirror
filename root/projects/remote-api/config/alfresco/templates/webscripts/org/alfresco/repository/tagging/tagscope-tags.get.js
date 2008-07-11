<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">

function findTargetNode()
{
   if (url.templateArgs.site != undefined)
   {
      var siteId = url.templateArgs.site;
      var containerId = url.templateArgs.container;
      var path = url.templateArgs.path;

      // fetch site
      var site = siteService.getSite(siteId);
      if (site === null)
      {
         status.setCode(status.STATUS_NOT_FOUND, "Site " + siteId + " does not exist");
         return null;
      }
      else if (containerId == undefined)
      {
         return site;
      }
      
      // fetch container
      node = site.getContainer(containerId);
      if (node === null)
      {
         status.setCode(status.STATUS_NOT_FOUND, "Unable to fetch container '" + containerId + "' of site '" + siteId + "'. (No write permission?)");
         return null;
      }
      else if (path == undefined)
      {
         return node;
      }
      
      node = node.childByNamePath(path);
      return node;
   }
   else
   {
      return findFromReference();
   }
}

function main()
{
   var node = findTargetNode();

   // fetch the nearest available tagscope
   var scope = node.tagScope;
   if (scope == null)
   {
      //status.setCode(status.STATUS_BAD_REQUEST, "No tag scope could be found for the given resource");
      //return null;
      model.noscopefound=true;
   }
   else
   {
      model.tags = scope.tags;
   }
}

main();
