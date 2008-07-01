<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/searchutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/generic-paged-results.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/discussions/topicpost.lib.js">

const DEFAULT_NUM_DAYS = 30;

/**
 * Returns the date object for the date "numdays" ago
 */
function getTodayMinusXDays(numdays)
{
   var date = new Date();
   var dateMillis = new Date().getTime();
   dateMillis -= 1000 * 60 * 60 * 24 * numdays;
   date.setTime(dateMillis);
   return date;
}

/**
 * Fetches all posts added to the forum in the last numdays days
 */
function getTopicPostList(node, numdays, index, count)
{
   var fromDate = getTodayMinusXDays(numdays);
   
   // query information
   var luceneQuery = " +TYPE:\"{http://www.alfresco.org/model/forum/1.0}topic\"" +
                     " +PATH:\"" + node.qnamePath + "/*\" " +
                     getCreationDateRangeQuery(fromDate, null);
   var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}created";
  
   // get the data
   return getPagedResultsDataByLuceneQuery(node, luceneQuery, sortAttribute, false, index, count, getTopicPostData);
}

function main()
{
   // get requested node
   var node = getRequestNode();
   if (status.getCode() != status.STATUS_OK)
   {
      return;
   }

   // process additional parameters
   var index = args["startIndex"] != undefined ? parseInt(args["startIndex"]) : 0;
   var count = args["pageSize"] != undefined ? parseInt(args["pageSize"]) : 10;
   var numdays = args["numdays"] != undefined ? parseInt(args["numdays"]) : DEFAULT_NUM_DAYS;
   
   // fetch the data and assign it to the model
   model.data = getTopicPostList(node, numdays, index, count);
   
   model.contentFormat = (args["contentFormat"] != undefined) ? args["contentFormat"] : "full";
}

main();
