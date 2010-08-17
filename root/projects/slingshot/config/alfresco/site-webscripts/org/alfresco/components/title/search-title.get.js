/**
 * Search Title component GET method
 */

function main()
{
   if (page.url.templateArgs.site != null)
   {
      // look for request scoped cached site title
      var siteTitle = context.properties["site-title"];
      if (siteTitle == null)
      {
         // Call the repository for the site profile
         var json = remote.call("/api/sites/" + page.url.templateArgs.site);
         if (json.status == 200)
         {
            // Create javascript objects from the repo response
            var obj = eval('(' + json + ')');
            if (obj)
            {
               siteTitle = (obj.title.length != 0) ? obj.title : obj.shortName;
            }
         }
      }
      
      // Prepare the model
      model.siteTitle = (siteTitle != null ? siteTitle : "");
   }
   
   // Build the Advanced Search link - construct with args to return here
   var args = page.url.args;
   if (args["t"] != null || args["tag"] != null || args["q"] != null)
   {
      var query = "st=" + (args["t"] != null ? encodeURIComponent(args["t"]) : "") +
                  "&stag=" + (args["tag"] != null ? encodeURIComponent(args["tag"]) : "") +
                  "&ss=" + (args["s"] != null ? encodeURIComponent(args["s"]) : "") +
                  "&sa=" + (args["a"] != null ? encodeURIComponent(args["a"]) : "") +
                  "&sq=" + (args["q"] != null ? encodeURIComponent(args["q"]) : "");
      model.advsearchlink = query;
   }
}

main();