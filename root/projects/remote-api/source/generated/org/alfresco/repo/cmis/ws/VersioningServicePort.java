package org.alfresco.repo.cmis.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.1.2
 * Fri Jul 24 09:58:27 EEST 2009
 * Generated source version: 2.1.2
 * 
 */
 
@WebService(targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200901", name = "VersioningServicePort")
@XmlSeeAlso({ObjectFactory.class})
public interface VersioningServicePort {

    @ResponseWrapper(localName = "checkInResponse", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", className = "org.alfresco.repo.cmis.ws.CheckInResponse")
    @RequestWrapper(localName = "checkIn", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", className = "org.alfresco.repo.cmis.ws.CheckIn")
    @WebMethod
    public void checkIn(
        @WebParam(name = "repositoryId", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.String repositoryId,
        @WebParam(mode = WebParam.Mode.INOUT, name = "documentId", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        javax.xml.ws.Holder<java.lang.String> documentId,
        @WebParam(name = "major", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.Boolean major,
        @WebParam(name = "properties", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        org.alfresco.repo.cmis.ws.CmisPropertiesType properties,
        @WebParam(name = "contentStream", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        org.alfresco.repo.cmis.ws.CmisContentStreamType contentStream,
        @WebParam(name = "checkinComment", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.String checkinComment,
        @WebParam(name = "applyPolicies", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.util.List<java.lang.String> applyPolicies,
        @WebParam(name = "addACEs", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        org.alfresco.repo.cmis.ws.CmisAccessControlListType addACEs,
        @WebParam(name = "removeACEs", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        org.alfresco.repo.cmis.ws.CmisAccessControlListType removeACEs
    ) throws CmisException;

    @ResponseWrapper(localName = "cancelCheckOutResponse", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", className = "org.alfresco.repo.cmis.ws.CancelCheckOutResponse")
    @RequestWrapper(localName = "cancelCheckOut", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", className = "org.alfresco.repo.cmis.ws.CancelCheckOut")
    @WebMethod
    public void cancelCheckOut(
        @WebParam(name = "repositoryId", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.String repositoryId,
        @WebParam(name = "documentId", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.String documentId
    ) throws CmisException;

    @ResponseWrapper(localName = "getAllVersionsResponse", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", className = "org.alfresco.repo.cmis.ws.GetAllVersionsResponse")
    @RequestWrapper(localName = "getAllVersions", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", className = "org.alfresco.repo.cmis.ws.GetAllVersions")
    @WebResult(name = "object", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
    @WebMethod
    public java.util.List<org.alfresco.repo.cmis.ws.CmisObjectType> getAllVersions(
        @WebParam(name = "repositoryId", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.String repositoryId,
        @WebParam(name = "versionSeriesId", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.String versionSeriesId,
        @WebParam(name = "filter", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.String filter,
        @WebParam(name = "includeAllowableActions", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.Boolean includeAllowableActions,
        @WebParam(name = "includeRelationships", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        org.alfresco.repo.cmis.ws.EnumIncludeRelationships includeRelationships
    ) throws CmisException;

    @ResponseWrapper(localName = "getPropertiesOfLatestVersionResponse", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", className = "org.alfresco.repo.cmis.ws.GetPropertiesOfLatestVersionResponse")
    @RequestWrapper(localName = "getPropertiesOfLatestVersion", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", className = "org.alfresco.repo.cmis.ws.GetPropertiesOfLatestVersion")
    @WebResult(name = "object", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
    @WebMethod
    public org.alfresco.repo.cmis.ws.CmisObjectType getPropertiesOfLatestVersion(
        @WebParam(name = "repositoryId", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.String repositoryId,
        @WebParam(name = "versionSeriesId", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.String versionSeriesId,
        @WebParam(name = "major", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        boolean major,
        @WebParam(name = "filter", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.String filter,
        @WebParam(name = "includeACL", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.Boolean includeACL
    ) throws CmisException;

    @ResponseWrapper(localName = "checkOutResponse", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", className = "org.alfresco.repo.cmis.ws.CheckOutResponse")
    @RequestWrapper(localName = "checkOut", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901", className = "org.alfresco.repo.cmis.ws.CheckOut")
    @WebMethod
    public void checkOut(
        @WebParam(name = "repositoryId", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        java.lang.String repositoryId,
        @WebParam(mode = WebParam.Mode.INOUT, name = "documentId", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        javax.xml.ws.Holder<java.lang.String> documentId,
        @WebParam(mode = WebParam.Mode.OUT, name = "contentCopied", targetNamespace = "http://docs.oasis-open.org/ns/cmis/messaging/200901")
        javax.xml.ws.Holder<java.lang.Boolean> contentCopied
    ) throws CmisException;
}
