<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>

<tiles:insertDefinition name="main" flush="true">
    <tiles:putAttribute name="content">
        <h1 style="margin-left:5px">Redcap to ODM</h1>
        <form style="width: 50%; float: left; margin-left: 10px;" enctype="multipart/form-data" method="POST" action="redcaptoodm.html">
            <div id="odmFiles" class="form-group" style="width: 100%; float:left;">
                <label for="redcapFile">Redcap File:</label>
                <input id="redcapFile" class="textfield" size="54" type="file" name="redcapFile"/>
            </div>
            <div class="form-group"style="width: 100%; float:left;">
                <form:checkbox path="selectedOptions.selectedAnswersMetaData" label="Create StudyEventDef" cssClass="checkbox-inline" onclick="showStudyEventInput()" id="studyCheckbox" cssStyle="margin: 0px 5px 0px 0px;" value="studyEvent"/>
                <br>
                <label for="studyEventInput">Name:</label>
                <form:input path="selectedOptions.studyEventName" id="studyEventInput" cssClass="textfield form-control" size="54" type="text" name="studyEventInput" disabled="false"/>
                <span id="error-span" style="color: red; display: none;" hidden>Only uncheck this, if a StudyEventDef already exists. Otherwise there might be errors when converting clinical data.</span>
            </div>
            <div class="form-group"style="width: 100%; float:left;">
                <label for="multi-answer-item-select" id="multi-answer-item-select-label">How to convert Multiansweritems?</label>
                <form:select path="selectedOptions.multiAnswerStyle" id="multi-answer-item-select" cssClass="form-control" cssStyle="margin-bottom: 15px">
                <form:options items="${multiAnswerOptions}"/>
                </form:select>
                <label for="formal-expression-item-select" id="formal-expression-item-select-label">How to convert FormalExpressions?</label>
                <form:select path="selectedOptions.expressionConversionStyle" id="formal-expression-item-select" cssClass="form-control" cssStyle="margin-bottom: 15px">
                    <form:options items="${formalExpressionOptions}"/>
                </form:select>
                <form:checkbox path="selectedOptions.selectedAnswersMetaData" label="Add Language Information to TranslatedTexts" cssClass="checkbox-inline" onclick="showLanguageSelect()" id="languageCheckbox" cssStyle="margin: 0px 5px 0px 0px;" value="languageTransform"/>
                <br>
                <label for="languageSelect" id="language-label" style="display: none;">Language of Redcap File:</label>
                <form:select path="selectedOptions.language" items="${languages}" id="languageSelect" cssClass="form-control" cssStyle="display: none; margin-bottom: 15px"/>
                <form:checkboxes items="${selectedOptions.optionsMetaData}" path="selectedOptions.selectedAnswersMetaData" delimiter="<br/>" cssClass="checkbox-inline" cssStyle="margin: 0px 5px 0px 0px;"/>
            </div class="form-group"style="width: 100%; float:left;">
            <div>
                <form:checkbox path="selectedOptions.convertClinicalData" label="Convert clinical data" cssClass="checkbox-inline" id="clinicalDataCheckbox" cssStyle="margin: 0px 5px 0px 0px;" onclick="showClinicalDataOptions()"/>
                <br>
                <form:checkboxes items="${selectedOptions.optionsClinicalData}" path="selectedOptions.selectedAnswersClinicalData" delimiter="<br/>" cssClass="checkbox-inline" cssStyle="margin: 0px 5px 0px 0px;"/>
               <%-- <br>
                <form:checkbox path="selectedOptions.repairODM" label="Repair ODM" cssClass="checkbox-inline" id="repaidODM" cssStyle="margin: 0px 5px 0px 0px;" onclick="showClinicalDataOptions()"/>--%>
            </div>
            <button class="btn btn-primary" type="submit" value="redcaptoodm" id="convertButton" name="action" onclick="clearOutput()"><b>Convert to ODM</b></button>
            <div id="response"></div>
            <c:if test="${base64String != null}">
                <a id='downloadLink' download='crf.zip' style="display:none;"/>
                <p id='base64Zip' style="display:none;"><c:out value="${base64String}"/></p>
                <p id='filename' style="display:none;">${filename}</p>
                <a href="#" onclick="downloadZIP();" title='crf.zip' style="display:none;">Download the CRFs</a>
                <script>
                    <%-- Download the file from a base64String.--%>
                    if (window.navigator && window.navigator.msSaveOrOpenBlob) {
                        var base64String = document.getElementById('base64Zip').innerHTML;
                        base64String = base64String.substring(base64String.indexOf(',', 0) + 1, base64String.length);
                        var blob = base64toBlob(base64String, 'application/octet-stream');
                        var filename = document.getElementById('filename').innerHTML;
                        window.navigator.msSaveOrOpenBlob(blob, filename);
                    } else {
                        // Download the file from the base64 url on all other browsers
                        var filename = document.getElementById('filename').innerHTML;
                        var zip = document.getElementById('base64Zip').innerHTML;
                        var downloadLink = document.getElementById('downloadLink');
                        downloadLink.href = zip;
                        downloadLink.download = filename;
                        downloadLink.click();
                    }
                </script>
            </c:if>

            <div id="conversion_information">
                <c:if test="${conversionNotes != null && conversionNotes.isEmpty() == false}">
                    <b>The following ${conversionNotes.size()} notes were generated:</b>
                    <c:forEach items="${conversionNotes}" var="note">
                        <%-- Display all conversion notes. --%>
                        <p>CONVERSION NOTE: ${note}</p>
                    </c:forEach>
                </c:if>
                <c:if test="${errors != null && errors.isEmpty() == false}">
                    <div id="Errors" style="width:100%;float:left">
                        <b>The following ${errors.size()} Errors occurred:</b>
                        <c:forEach items="${errors}" var="error">
                            <%-- Display all errors from the list of errors. --%>
                            <c:if test="${error.getMessage() == null}">
                                <p>ERROR: ${error.toString()}</p>
                            </c:if>
                            <c:if test="${error.getMessage() != null}">
                                <p>ERROR ${error.getMessage()}</p>
                            </c:if>
                        </c:forEach>
                    </div>
                </c:if>
            </div>
        </form>
        <script>
            $("document").ready(function () {
                // Init file upload files
                $(`#redcapFile`).fileinput({'showRemove': true, 'showUpload': false, 'showPreview': false});
                connect();
            });
            let stompClient = null;

            function connect() {
                let socket = new SockJS("/status");
                stompClient = Stomp.over(socket);
                stompClient.connect({}, function(frame) {
                    console.log('Connected: ' + frame);
                    stompClient.subscribe('/update/messages', function(messageOutput) {
                        showMessageOutput(JSON.parse(messageOutput.body));
                    });
                });
                //sendMessage();
            }

            function disconnect() {
                if(stompClient != null) {
                    stompClient.disconnect();
                }
                console.log("Disconnected");
            }

            function sendMessage() {
                let status = "test message"; //document.getElementById('text').value;
                stompClient.send("/app/status", {},
                    JSON.stringify({'status':status}));
            }

            function showMessageOutput(messageOutput) {
                let response = $('#response')[0]; //document.getElementById('response'); //$('#response');
                let p = document.createElement('p');
                p.style.wordWrap = 'break-word';
                messageOutput.content.forEach(m => {
                    p.appendChild(document.createTextNode(m));
                    p.append(document.createElement("br"));
                });
                response.appendChild(p);
            }

            function showStudyEventInput() {
                const cb = document.querySelector('#studyCheckbox');
                let studyEventInput = $('#studyEventInput')[0];
                let errorSpan = $('#error-span')[0];
                if (cb.checked) {
                    studyEventInput.disabled = false;
                    errorSpan.style.display = 'none';
                } else {
                    studyEventInput.disabled = true;
                    errorSpan.style.display = 'inline-block';
                }
            }

            function showLanguageSelect() {
                const cb = document.querySelector('#languageCheckbox');
                let languageSelect = $('#languageSelect')[0];
                let languageLabel = $('#language-label')[0];
                if (cb.checked) {
                    languageSelect.style.display = 'inline-block';
                    languageLabel.style.display = 'inline-block';
                } else {
                    languageSelect.style.display = 'none';
                    languageLabel.style.display = 'none';
                }
            }

            function showClinicalDataOptions() {
                const cb = document.querySelector('#clinicalDataCheckbox');
                let clinicalCheckboxes = $("[name=selectedAnswersClinicalData]");
                if (cb.checked) {
                    clinicalCheckboxes.each((i, obj) => obj.disabled = false);
                } else {
                    clinicalCheckboxes.each((i, obj) => obj.disabled = true);
                }
            }

            function clearOutput() {
                document.getElementById('response').innerText = "";
                document.getElementById('conversion_information').innerHTML = "";
            }
        </script>
    </tiles:putAttribute>
</tiles:insertDefinition>
