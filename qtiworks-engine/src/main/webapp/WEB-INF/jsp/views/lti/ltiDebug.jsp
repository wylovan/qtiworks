<%--

Copyright (c) 2012-2013, The University of Edinburgh.
All Rights Reserved

LTI diagnostic

Model:

ltiLaunchResult

--%>
<%@ include file="/WEB-INF/jsp/includes/pageheader.jspf" %>
<page:page title="LTI Diagnostics">

  <h2>LTI Diagnostic information</h2>

  <pre>${utils:dumpObject(ltiLaunchResult)}</pre>

</page:page>

