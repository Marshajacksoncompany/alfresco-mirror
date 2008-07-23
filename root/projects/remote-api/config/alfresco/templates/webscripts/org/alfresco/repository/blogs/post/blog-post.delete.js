<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">

/**
 * Deletes a blog post node.
 */
function deleteBlogPost(postNode)
{
   // delete the node
   var nodeRef = postNode.nodeRef;
   var isDeleted = postNode.remove();
   if (! isDeleted)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unable to delete node: " + nodeRef);
      return;
   }
   model.message = "Node " + nodeRef + " deleted";
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

   var item = getBlogPostData(node);

   var title = node.properties.title;
   var tags = node.properties.tags;
          
   deleteBlogPost(node);
   
   if (args["site"] != undefined && args["container"] != undefined && ! item.isDraft)
   {
      var data = {
          postTitle: title
      }
      activities.postActivity("org.alfresco.blog.post-deleted", args["site"], args["container"], jsonUtils.toJSONString(data));
   }
}

main();
