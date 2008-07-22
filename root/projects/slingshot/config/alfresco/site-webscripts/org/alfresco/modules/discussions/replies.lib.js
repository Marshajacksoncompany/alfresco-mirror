<import resource="classpath:alfresco/site-webscripts/org/alfresco/callutils.js">
<import resource="classpath:alfresco/site-webscripts/org/alfresco/modules/discussions/topicjsondatautils.js">

function nodeRefToUrl(nodeRef)
{
    return nodeRef.replace(/%3A/gi, ":").replace(/%2F/gi, "/").replace(/\:\/\//,"/");
}

function getRepliesRequestUrl(nodeRef)
{
    return "/api/forum/post/node/" + nodeRefToUrl(nodeRef) + "/replies";
}

function getRepliesRequestUrlIncludingLevels(nodeRef, levels)
{
    var url = getRepliesRequestUrl(nodeRef);
    url = addParamToUrl(url, "levels", levels);
    return url;
}

function fetchReplies(nodeRef, levels)
{
    var url = getRepliesRequestUrlIncludingLevels(nodeRef, levels);
    var data = doGetCall(url);
    if (data === null)
    {
        return null;
    }
    convertRepliesJSONData(data);
    return data;
}

function fetchAndAssignReplies(nodeRef, content)
{
    var data = fetchReplies(nodeRef, levels);
    if (data === null)
    {
        return;
    }
    applyDataToModel(data);
}

function createAndAssignReply(site, container, path, parentNodeRef, content)
{
    var params = {
        site : site,
        container : container,
        path : path,
        content : content
    };
    var paramsJSON = jsonUtils.toJSONString(params);
    var url = getRepliesRequestUrl(parentNodeRef);

    // fetch and assign data from the backend
    var data = doPostCall(url, paramsJSON);
    if (data === null)
    {
        return;
    }
    convertTopicJSONData(data.item);
    applyDataToModel(data);
}
