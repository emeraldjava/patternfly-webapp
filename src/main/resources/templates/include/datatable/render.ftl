<#macro showDataTable dataTable type='static'>

<#local id=dataTable.id!"def" />

<#local resultsPerPage=dataTable.resultsPerPage!10 />
<#local showFilters = dataTable.isShowFilters()!true />

<!-- Un espace pour un problème de DIV -->
&nbsp;
<#--
<span style="float: right; font-style: italic; font-size: 0.8em"><a href="javascript: showDataTableVersion('flexigrid')">Afficher la version dynamique</a></span>
-->

    <div class="dataTable_wrapper form-inline">
    
    <form method="get" action="" name="formRecherche_${id}" id="formRecherche_${id}">
    <input type="hidden" value="${dataTable.modeTri}" name="modeTri_${id}" id="modeTri_${id}">
    <input type="hidden" value="${dataTable.sensTri}" name="sensTri_${id}" id="sensTri_${id}">
    <input type="hidden" value="${dataTable.pageIndex}" name="pageIndex_${id}" id="pageIndex_${id}"/>
    <input type="hidden" value="${dataTable.modeExport!}" name="modeExport_${id}" id="modeExport_${id}">
    <input type="hidden" value="${showFilters?string("1", "0")}" name="showFilters_${id}" id="showFilters_${id}">

    <#if (dataTable.params?size > 0)>
        <#list dataTable.params?keys as paramName>
            <input type="hidden" value="${dataTable.params[paramName]!}" name="${paramName}" id="${paramName}">
        </#list>
    </#if>

    <#-- Affichage des filtres -->
    <#local hasFilter = dataTable.hasVisibleFilter()>

    <#if hasFilter || (dataTable.results?size = 0)>
    <div id="dataTableSearchBarTitle_${id}" style="display: visible" class="row">

        <div class="col-md-6">
            <#if hasFilter>
                <span style="font-weight: bold"><a href="javascript: switchFiltrage_${id}()"><@spring.message "datatable.filter.block" /> <span id="dataTableSearchBarTitleIcon_${id}" class="fa fa-angle-<#if showFilters>up<#else>down</#if>"></span></a></span>
            </#if>
        </div>
        <div class="col-md-6" style="min-height: 27px">
            <#if dataTable.availableExporters??>
                <#if (dataTable.results?size > 0)>
                    <#if ((","+dataTable.availableExporters+",")?index_of(",csv,") >= 0) >
                        <a href="javascript:doExport_${id}('csv')" class="btn btn-default btn-xs pull-right" style="margin-top: 0px; margin-bottom: 5px"><span class="fa fa-file-excel-o"></span> Exporter</a>
                    </#if>
                </#if>
            </#if>
            <div id="dataTableSearchBarSearchButton" style="display: <#if showFilters>none<#else>visible</#if>">
                <a href="javascript:doSubmit_${id}()" class="btn btn-default btn-xs pull-right" style="margin-top: 0px; margin-bottom: 5px; margin-right: 5px"><@spring.message "datatable.filter.button" /></a>
            </div>
        </div>
    </div>
    </#if>

    <#if hasFilter>
        <#-- Comptage nb de lignes et nb de colonnes -->
        <#assign filterRowCount = 1>
        <#assign filterColumnCount = 0>
        <#assign filterTmpColumnCount = 0>
        <#list dataTable.filters as filter>
            <#if !filter.hidden>
                <#if filter.newLine>
                    <#assign filterRowCount = filterRowCount + 1>
                    <#assign filterTmpColumnCount = 0>
                </#if>
                <#assign filterTmpColumnCount = filterTmpColumnCount + filter.colSpan>
                <#if (filterTmpColumnCount > filterColumnCount)>
                    <#assign filterColumnCount = filterTmpColumnCount>
                </#if>
            </#if>
        </#list>
        <#if filterColumnCount = 0>
            <#assign filterColumnCount = dataTable.filters?size>
        </#if>

        <div class="dataTables_header">

        <div id="dataTableSearchBarExtended_${id}" style="display: <#if showFilters>visible<#else>none</#if>">

        <table cellspacing="0" cellpadding="0" border="0" style="width: 100%; margin: 0; margin-bottom: 0px">
            <tbody>
                <tr>
                    <#local filterSubmitWritten = false>
                    <#local filterTmpRowCount = 0>
                    <#local filterTmpColumnCount = 0>
                    <#local btnSearchWritten = false>
                    <#list dataTable.filters as filter>
                        <#if !filter.hidden>
                            <#local filterTmpColumnCount = filterTmpColumnCount + filter.colSpan>
            
                            <td <#if filter.width??>style="width:${filter.width}px"</#if>>
                                ${filter.label}&nbsp;:
                            </td>
                            <td colspan="${1 + 2 * (filter.colSpan - 1)}" style="text-align: left">
                            	<#if filter.acceptedOperators??>
                           			<select id="filter_${filter_index + 1}_op_${id}" name="filter_${filter_index + 1}_op_${id}" class="selectpicker" data-width="60px">
                            		<#list filter.acceptedOperators as filterAcceptedOperator>
                            			<option value="${(filterAcceptedOperator.operator.id)!0}" <#if filter.operator.id == (filterAcceptedOperator.operator.id)!0>selected="selected"</#if>>${filterAcceptedOperator.label!}</option>
                            		</#list>
                           			</select>
                            	</#if>
                                <#if !filter.acceptedValues??>
                                    <label><input type="text" id="filter_${filter_index + 1}_${id}" name="filter_${filter_index + 1}_${id}" value="${filter.value!?html}" <#if filter.size??>size="${filter.size}"</#if> class="form-control"></label>
                                <#else>
                                    <select id="filter_${filter_index + 1}_${id}" name="filter_${filter_index + 1}_${id}" style="font-size: 11px; <#if filter.size??>width: ${filter.size}px</#if>" class="selectpicker">
                                        <option value="">&nbsp;</option>
                                        <#list filter.acceptedValues as filterAcceptedValue>
                                            <option <#if filter.value??><#if filterAcceptedValue.value = filter.value>selected</#if></#if> value="${filterAcceptedValue.value?html}">${filterAcceptedValue.label?html}</option>
                                        </#list>
                                    </select>
                                </#if>
                            </td>
            
                            <#if filterTmpColumnCount = filterColumnCount || !filter_has_next || dataTable.filters[filter_index + 1].newLine>
                                <#-- ecriture TD restants -->
                                <#if filterTmpColumnCount < filterColumnCount>
                                    <td colspan="${(filterColumnCount - filterTmpColumnCount) * 2}">&nbsp;</td>
                                </#if>
                                <#if ((filterTmpColumnCount = filterColumnCount && filterTmpRowCount = 0) || filter_index = 0) && !btnSearchWritten>
                                    <#local btnSearchWritten = true>
                                    <td style="vertical-align: middle; text-align: center" colspan="2" rowspan="${filterRowCount}">
                                        <input id="btnSearch_${id}" type="submit" onClick="return false;" value="<@spring.message "datatable.filter.button" />" class="btn btn-default btn-sm" style="width: 80px">
                                    </td>
                                </#if>
                                </tr>
                                <#if filter_has_next>
                                    <tr>
                                </#if>
                                <#local filterTmpColumnCount = 0>
                                <#local filterTmpRowCount = filterTmpRowCount + 1>
                            </#if>
                        </#if>
                    </#list>
            </tbody>
        </table>
        </div>

        </div>
    </#if>

    <#assign resultColCount=0 />

    <table cellpadding="0" cellspacing="0" border="0" class="datatable datable-manual dataTable_result table table-condensed table-striped table-bordered" id="dataTableResult_${id}">
        <thead>
            <tr role="row">
                <#if dataTable.viewLink??>
                    <#assign resultColCount = resultColCount + 1 />
                    <th style="width:20px">&nbsp;</th>
                </#if>
                <#if dataTable.modifyLink??>
                    <#assign resultColCount = resultColCount + 1 />
                    <th style="width:20px">&nbsp;</th>
                </#if>
                <#if dataTable.changeStatusLink??>
                    <#assign resultColCount = resultColCount + 1 />
                    <th style="width:20px">&nbsp;</th>
                </#if>
                <#list dataTable.columns as column>
                    <#if !column.hidden>
                        <#local sensTri = -1>
                        <#if column.sortable == true>
                            <#local sensTri = 0>
                            <#if dataTable.modeTri == column_index + 1>
                                <#local sensTri = dataTable.sensTri>
                            </#if>
                        </#if>
                        <#assign resultColCount = resultColCount + 1 />
                        <th dt_sens_tri='${sensTri}' dt_mode_tri='${column_index + 1}' class="<#switch sensTri><#case 0>sorting<#break><#case 1>sorting_asc<#break><#case 2>sorting_desc<#break></#switch>"
                            style="<#if ((column.width!0) > 0)>width: ${column.width!0}px;</#if> vertical-align: top">
                            ${column.label}
                            <#if column.headerComment??>
                                <span class="glyphicon glyphicon-info-sign" id="tip-info-dt${dataTable.id}-${column_index}" title="${column.headerComment}"></span>
                            </#if>
                        </th>
                    </#if>
                </#list>
                <#if dataTable.deleteLink??>
                    <#assign resultColCount = resultColCount + 1 />
                    <th style="width:20px">&nbsp;</th>
                </#if>
                <#--
                <th style="width: 0px">Actions</th>
                -->
            </tr>
        </thead>
        <tbody>
            <#if dataTable.results?size == 0>
                <tr>
                    <td colspan="${resultColCount}">
                        <#if dataTable.firstView!false>
                            &nbsp;
                        <#else>
                        	<@spring.message "datatable.no_result" />
                        </#if>
                    </td>
                </tr>
            </#if>
            <#list dataTable.results as result>
                <tr>
                    <#if dataTable.viewLink??>
                        <td style="text-align: center">
                            <#if dataTable.viewLink?? && (!result.canBeViewed?? || result.canBeViewed == true)>
                                <a href="${replace(dataTable.viewLink, result)}" title="visualiser"><img src="../rsrc-bo/images/preview.gif"></a>
                            </#if>
                        </td>
                    </#if>
                    <#if dataTable.modifyLink??>
                        <td style="text-align: center">
                            <#if dataTable.modifyLink?? && (!result.canBeModified?? || result.canBeModified == true)>
                                <a href="${replace(dataTable.modifyLink, result)}" title="modifier"><img src="../rsrc-bo/images/edit2.png"></a>
                            </#if>
                        </td>
                    </#if>
                    <#if dataTable.changeStatusLink??>
                        <td style="text-align: center">
                            <#if result.rawCodeStatus??>
                                <#switch (result.rawCodeStatus?string)>
                                    <#case "0">
                                        <a href="${replaceCodeStatus(dataTable.changeStatusLink, result)}" title="Activer"><img src="../rsrc-bo/images/up2.gif"></a>
                                        <#break>
                                    <#case "1">
                                        <a href="${replaceCodeStatus(dataTable.changeStatusLink, result)}" title="Désactiver"><img src="../rsrc-bo/images/down2.gif"></a>
                                        <#break>
                                </#switch>
                            </#if>
                        </td>
                    </#if>
                    <#list dataTable.columns as column>
                        <#if !column.hidden>
                            <td class='dtr${dataTable.id}-${(column.getId()!"")?replace(".", "_")}'>
                                ${getResultValueByColumn(result, column)}
                            </td>
                        </#if>
                    </#list>
                    <#if dataTable.deleteLink??>
                        <td style="text-align: center">
                            <#if dataTable.deleteLink?? && (!result.canBeDeleted?? || result.canBeDeleted == true)>
                                <a href="${replace(dataTable.deleteLink, result)}" title="supprimer"><img src="../rsrc-bo/images/trash.gif"></a>
                            </#if>
                        </td>
                    </#if>
                </tr>
            </#list>
        </tbody>
    </table>

	<#-- pager -->
	<div class="dataTables_footer" style="">
		<#assign viewCount1 = 1 + (dataTable.pageIndex -1) * resultsPerPage />
	
	    <#if (dataTable.pageIndex * resultsPerPage > dataTable.resultCount!0)>
	        <#assign viewCount2 = dataTable.resultCount!0 />
	    <#else>
	        <#assign viewCount2 = (dataTable.pageIndex) * resultsPerPage />
	    </#if>
	
	    <#assign viewCount3 = dataTable.resultCount!0 />

		<#if dataTable.results?size &gt; 0>	
		&nbsp; <@message "datatable.results.view.label" viewCount1 viewCount2 viewCount3 />
		</#if>
	
	    <#if dataTable.resultRealCount ?? && (dataTable.resultRealCount > dataTable.resultCount)><img
	        src="../rsrc-bo/images/error2.png" style="margin-top: -4px" data-placement="right" data-toggle="tooltip"
	        title="Trop de résultats (${dataTable.resultRealCount}). Veuillez affiner le filtrage."
	        id="img-too-many-results-${id}">
	        <script language="javascript"> $(document).ready(function() { $("#img-too-many-results-${dataTable.id}").tooltip(); }); </script></#if>
	
	    <div class="dataTables_paginate paging_bootstrap_input" id="DataTables_Table_${id}_paginate">
	        <ul class="pagination">
	            <#if (dataTable.pageIndex == 1)>
	                <li class="first disabled">
	                    <span class="i fa fa-angle-double-left"></span>
	                </li>
	                <li class="prev disabled">
	                    <span class="i fa fa-angle-left"></span>
	                </li>
	            <#else>
	                <#assign pagLink = pageLink(dataTable) />
	                <li class="first">
	                    <span class="i fa fa-angle-double-left"></span>
	                </li>
	                <li class="prev">
	                    <span class="i fa fa-angle-left"></span>
	                </li>
	            </#if>
	        </ul>
	
	        <div class="pagination-input">
	        	<#-- 
	            <input type="text" class="paginate_input"><span class="paginate_of_no_js"><@spring.message "of" /> <b>${dataTable.pageCount}</b></span>
	            -->
	            <input type="text" class="paginate_input"><span class="paginate_of_no_js"><@spring.message "of" /> <b>${dataTable.pageCount}</b></span>
	        </div>
	        
	        <ul class="pagination">
	            <li class="next <#if (dataTable.pageIndex >= dataTable.pageCount)>disabled</#if>"><span class="i fa fa-angle-right"></span></li>
	            <li class="last <#if (dataTable.pageIndex >= dataTable.pageCount)>disabled</#if>"><span class="i fa fa-angle-double-right"></span></li>
	        </ul>
	    </div>
	</div>
    
    </form>
    </div>

    <@writeJavaScript dataTable type />

</#macro>
